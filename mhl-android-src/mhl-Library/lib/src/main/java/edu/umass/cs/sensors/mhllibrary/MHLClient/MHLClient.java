package edu.umass.cs.sensors.mhllibrary.MHLClient;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import edu.umass.cs.sensors.mhllibrary.Configuration.Configuration;
import edu.umass.cs.sensors.mhllibrary.MHLAgentCore.StateOracle;
import edu.umass.cs.sensors.mhllibrary.MHLShepherd.DummyShepherd;
import edu.umass.cs.sensors.mhllibrary.MHLShepherd.MHLShepherd;
import edu.umass.cs.sensors.mhllibrary.MHLUtilities.AndroidOutputHandler;
import edu.umass.cs.sensors.mhllibrary.MHLUtilities.MHLOutputHandler;

import edu.umass.cs.sensors.mhllibrary.Configuration.Configuration;
import edu.umass.cs.sensors.mhllibrary.Configuration.IdentityConfig;
import edu.umass.cs.sensors.mhllibrary.Configuration.StudyConfig;
import edu.umass.cs.sensors.mhllibrary.Configuration.NetworkConfig;
import edu.umass.cs.sensors.mhllibrary.MHLAgentCore.PreferencesAccessor;


/**
 * Created by erisinger on 1/5/19.
 */
public class MHLClient {
    final String TAG = "MHLClient";

    protected Context context;
    protected MHLHandshake handshake;
    protected BlockingQueue<String> outgoingMessageQueue;
    protected String ip;
    protected int securePort;
    protected int insecurePort;
    protected String badgeID;
    protected String researchToken;
    protected int configVersion;
    protected boolean enforceSSL;
    protected boolean isConnected;
    protected boolean heal;
    protected boolean disconnecting;
    protected Thread connectivityThread;
    protected Thread readThread;
    protected Thread writeThread;
    protected SocketReadRunnable readRunnable;
    protected SocketWriteRunnable writeRunnable;
    protected MHLMessageReceiver messageReceiver;
    protected MHLShepherd shepherd;
    //protected ConnectivityMonitor connectivityMonitor;
    protected MHLOutputHandler outputHandler;
    protected boolean verbose = false;
    protected long timeOfLastHeartbeat;
    //protected Configuration config;
    protected StateOracle oracle;
    protected final String lock = "";

    public MHLClient(Context context, MHLMessageReceiver receiver, MHLShepherd shepherd) {
        this.context = context;
        //this.config = config;
        this.enforceSSL = insecurePort == -1;
        this.messageReceiver = receiver;
        this.shepherd = shepherd;
        this.outputHandler = new AndroidOutputHandler();
        this.timeOfLastHeartbeat = 0L;
        this.outgoingMessageQueue = new ArrayBlockingQueue<String>(500);
        //this.oracle = StateStateOracle.getInstance(context);
        this.disconnecting = false;
    }

    public void connectAndRetry() {
        heal = true;

        connectivityThread = new Thread(new Runnable() {

            @Override
            public void run() {
                outputHandler.printMessage(TAG,"Connectivity thread checking in");

                // check for airplane mode
                if (StateOracle.airplaneModeOn(context)) {
                    isConnected = false;
                    heal = false;

                    // TODO: remind user periodically about airplane mode?

                    // airplane mode is on -- no point continuing
                    outputHandler.printMessage(TAG,"Airplane mode is on -- not continuing.");
                    return;
                }

                try {

                    // try to maintain a connection if successfully established
                    while (heal) {

                        attemptConnectionWithBackoffs();

                        synchronized (lock) {

                            // sleep until notify()-ed by disconnect()
                            while (isConnected) {
                                outputHandler.printMessage(TAG, "Connection looks good.  Healer going to sleep.");

                                lock.wait();

                                outputHandler.printMessage(TAG, "Healer notified.");
                            }
                        }

                    }

                    // we've broken out of the loop -- fall through to exit the runnable
                    outputHandler.printMessage(TAG,"Broke out of while(heal) loop");

                } catch (InterruptedException e) {
                    // thread interruption is taken to be a call to end self-healing
                    disconnect(false);

                    outputHandler.handleException(e);
                }

                outputHandler.printMessage(TAG,"Healer checking out");
            }
        });

        connectivityThread.start();
    }

    protected void attemptConnectionWithBackoffs() {
        int tries = 0;

        try {
            while (!isConnected && StateOracle.hasRetriesRemaining(context,tries)) {

                outputHandler.printMessage(TAG, "Attempting connection");

                connect();
                tries++;

                if (!isConnected) {

                    if (StateOracle.hasRetriesRemaining(context,tries)) {
                        long sleepDuration = StateOracle.getBackoffForRetry(context,tries);
                        int triesRemaining = StateOracle.retriesRemaining(context,tries);

                        outputHandler.printMessage(TAG, "Connection failed with "
                                + triesRemaining + (triesRemaining == 1 ? " try" : " tries")
                                + " left.  Retrying in "
                                + (int) (sleepDuration / 1000)
                                + (sleepDuration == 1000 ? " second" : " seconds."));

                        Thread.sleep(sleepDuration);

                    } else {
                        // not able to connect within backoff schedule -- cancel further efforts
                        heal = false;

                        outputHandler.printMessage(TAG,
                                "Not able to connect within backoff schedule.  Giving up.");
                    }
                }
            }

        } catch (InterruptedException e) {

            // interrupted during one of the backoff periods -- succeeded is false
            outputHandler.handleException(e);
        }
    }

    public boolean connect() {

        // check network
        if(StateOracle.getNetworkAvailability(context)){

            // check config
            handshake = new SecureMHLHandshake(context);

            // network + config ok -- proceed to connect
            handshake.connect();
            isConnected = (handshake.getSocket() != null);

            if (isConnected) {

                // success -- start up read/write threads
                startThreads();
            }
        }

        return isConnected;
    }

    public long timeSinceLastHeartbeat() {
        return System.currentTimeMillis() - this.timeOfLastHeartbeat;
    }

    // convenience method for terminal disconnection
    public boolean disconnect() {
        return disconnect(false);
    }

    // method allows for the possibility of automatic reconnection (via connectAndMaintain...())
    public boolean disconnect(boolean reconnect) {
        this.disconnecting=true;
        boolean disconnectionSucceeded = false;

        // gracefully close connection and optionally cease efforts to heal lost connection
        heal = reconnect;

        if (readRunnable != null) {
            readRunnable.stop();
        } else {
            outputHandler.printMessage(TAG, "readRunnable is null...  moving on");
        }

        if (writeRunnable != null) {
            writeRunnable.stop();
        } else {
            outputHandler.printMessage(TAG, "writeRunnable is null...  moving on");
        }

        if (readThread != null) {
            readThread.interrupt();

            try {
                readThread.join();
            } catch (InterruptedException e) {

            }

        } else {
            outputHandler.printMessage(TAG, "readThread is null...  moving on");
        }

        if (writeThread != null) {
            writeThread.interrupt();
            try {
                writeThread.join();
            } catch (InterruptedException e) {

            }
        } else {
            outputHandler.printMessage(TAG, "writeThread is null...  moving on");
        }

        if (handshake != null) {
            handshake.close();
        }

        disconnectionSucceeded = true;

        isConnected = false;

        if (shepherd != null) {
            //shepherd.removeMonitor(connectivityMonitor);
        }

        // wake up the outer connection loop
        synchronized (lock) {
            lock.notify();
        }

        return disconnectionSucceeded;
    }

    public void handleLostConnection(String reporter) {

        outputHandler.printMessage(TAG, "Connection loss detected by " + reporter);

        isConnected = false;

        if (heal) {

            outputHandler.printMessage(TAG,
                    "Attempting to heal connection");

        } else {
            outputHandler.printMessage(TAG, "Not attempting to heal lost connection");
        }

        this.disconnect(heal);
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    protected void startThreads() {

        if (isConnected) {

            outputHandler.printMessage(TAG, "starting read/write threads");

            this.readRunnable = new SocketReadRunnable(handshake.getReader(), this.messageReceiver);
            this.writeRunnable = new SocketWriteRunnable(handshake.getWriter(), this.outgoingMessageQueue);

            this.readThread = new Thread(readRunnable);
            this.writeThread = new Thread(writeRunnable);

            try {
                this.readThread.start();
            } catch (IllegalThreadStateException e) {
                e.printStackTrace();
            }

            try {
                this.writeThread.start();
            } catch (IllegalThreadStateException e) {
                e.printStackTrace();
            }

        } else {
            outputHandler.printMessage(TAG, "couldn't start read/write threads");
        }
    }

    protected void stopThreads() {

    }

    /* message handling classes and methods */
    public boolean addMessage(String message) {

        boolean added = false;

        if(!this.isConnected){return false;}

        synchronized (outgoingMessageQueue) {
            added = outgoingMessageQueue.offer(message);

            if (verbose) {
                outputHandler.printMessage(TAG, "added message: " + added);
                outputHandler.printMessage(TAG, "queue size: " + outgoingMessageQueue.size());
                outputHandler.printMessage(TAG, "writeRunnable == null: " + (writeRunnable == null));
            }
        }

        return added;
    }

    protected class SocketReadRunnable implements Runnable {
        protected final String TAG = "SocketReadRunnable";

        protected BufferedReader reader;
        protected MHLMessageReceiver receiver;
        protected boolean keepAlive = true;

        public SocketReadRunnable(BufferedReader reader, MHLMessageReceiver receiver) {
            this.reader = reader;
            this.receiver = receiver;
        }

        @Override
        public void run() {
            String incomingString;

            outputHandler.printMessage(TAG, TAG + " checking in");

            try {

                while (isConnected && keepAlive && (incomingString = reader.readLine()) != null) {

                    if (incomingString.length() > 2) {
                        receiver.receive(incomingString);
                        outputHandler.printMessage(TAG, "Sending message to agent: " + incomingString);
                        MHLClient.this.timeOfLastHeartbeat = System.currentTimeMillis();

                    } else if (incomingString.length() == 1) {
                        outputHandler.printMessage(TAG, "Got heartbeat: " + incomingString);
                        MHLClient.this.timeOfLastHeartbeat = System.currentTimeMillis();
                    }
                }
            } catch (IOException e) {
                outputHandler.handleException(e);
            }

            outputHandler.printMessage(TAG, "Socket was closed");

            handleLostConnection(TAG);

            outputHandler.printMessage(TAG, TAG + " checking out");
        }

        public void stop() {
            keepAlive = false;

            try {
                reader.close();
                outputHandler.printMessage(TAG, "successfully closed buffered reader");

            } catch (IOException e) {
                outputHandler.handleException(e);
            }
        }
    }

    protected class SocketWriteRunnable implements Runnable {
        protected final String TAG = "SocketWriteRunnable";

        protected BufferedWriter writer;
        protected BlockingQueue<String> queue;
        protected boolean keepAlive = true;

        public SocketWriteRunnable(BufferedWriter writer, BlockingQueue<String> queue) {
            this.writer = writer;
            this.queue = queue;
        }

        @Override
        public void run() {
            String outputString;

            outputHandler.printMessage(TAG, "Checking in");

            try {
                try {
                    while (keepAlive) {

                                outputString = queue.take() + "\n";
                                writer.write(outputString);
                                writer.flush();

                                if (verbose) {
                                    outputHandler.printMessage(TAG, "Sent message: " + outputString);
                                }

                    }

                    outputHandler.printMessage(TAG, "keepAlive: " + keepAlive);

                } catch (InterruptedException e) {
                    outputHandler.printMessage(TAG, "Interrupted: reconnecting");
                }

            } catch (IOException e) {
                // handle lost connection
                //handleLostConnection(TAG);
            }

            outputHandler.printMessage(TAG, "Checking out");
        }

        public void stop() {
            keepAlive = false;

            try {
                writer.close();
                outputHandler.printMessage(TAG, "Successfully closed");

            } catch (IOException e) {
                outputHandler.handleException(e);
            }
        }
    }

    public int getTimeSinceLastReceivedHeartbeat(){
        return (int) ((System.currentTimeMillis() -  this.timeOfLastHeartbeat)/(1000L));
    }

    // convenience method for synthetic data test
    public boolean syntheticDataTest() {
        return syntheticDataTest(60);
    }

    public boolean syntheticDataTest(long testDuration) {
        boolean succeededWithoutIssues = true;

        outputHandler.printMessage(TAG, "SYNTHETIC DATA TEST: CHECK STREAMING VISUALIZATION");

        if(!this.isConnected) {
            connect();
        }

        if (isConnected) {
            // generate synthetic data for testDuration seconds
            long startTime = System.currentTimeMillis();

            try {
                while (System.currentTimeMillis() < startTime + 1000 * testDuration) {
                    JSONObject obj = generateSyntheticDataMessage();
                    addMessage(obj.toString());

                    Thread.sleep(10);
                }
            } catch (InterruptedException e) {
                succeededWithoutIssues = false;
                outputHandler.handleException(e);
            }

        } else {
            succeededWithoutIssues = false;
        }

        disconnect();

        sleep(2000);

        outputHandler.printMessage(TAG, "readThread: " + readThread.getState());
        outputHandler.printMessage(TAG, "writeThread: " + writeThread.getState());

        // check for leaked threads
        succeededWithoutIssues =
                succeededWithoutIssues
                        && readThread.getState() == Thread.State.TERMINATED
                        && writeThread.getState() == Thread.State.TERMINATED;

        return succeededWithoutIssues;
    }

    public JSONObject generateSyntheticDataMessage() {
        long currentMillis = System.currentTimeMillis();

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        String dateString = dateFormat.format(new Date(currentMillis));

        JSONObject obj = new JSONObject();
        JSONObject header = new JSONObject();
        JSONObject metadata = new JSONObject();
        JSONObject payload = new JSONObject();

        try {
            header.put("badge-id", IdentityConfig.getBadgeID(context));
            header.put("channel", "data-message");
            header.put("timestamps", new JSONArray());
            header.put("message-type", "sensor-message");

            metadata.put("sensor-type", "synthetic");


            JSONObject dataPoint = new JSONObject();
            dataPoint.put("val", Math.sin(currentMillis));

            JSONArray vals = new JSONArray();
            vals.put(dataPoint);

            payload.put("d",dateString);
            payload.put("t", currentMillis);
            payload.put("vals", vals);

            obj.put("header", header);
            obj.put("metadata", metadata);
            obj.put("payload", payload);

        } catch (JSONException e) {
            outputHandler.handleException(e);
        }

        return obj;
    }

    public void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            outputHandler.handleException(e);
        }
    }

}
