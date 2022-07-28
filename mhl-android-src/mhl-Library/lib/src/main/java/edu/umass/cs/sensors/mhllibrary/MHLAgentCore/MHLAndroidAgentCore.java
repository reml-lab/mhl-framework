package edu.umass.cs.sensors.mhllibrary.MHLAgentCore;


import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.umass.cs.sensors.mhllibrary.Configuration.Configuration;
import edu.umass.cs.sensors.mhllibrary.Configuration.IdentityConfig;
import edu.umass.cs.sensors.mhllibrary.Configuration.StudyConfig;
import edu.umass.cs.sensors.mhllibrary.Configuration.NetworkConfig;
import edu.umass.cs.sensors.mhllibrary.MHLAgentCore.PreferencesAccessor;

import java.util.concurrent.TimeUnit;
import java.util.UUID;

//import androidx.work.OneTimeWorkRequest;
//import androidx.work.WorkManager;

//import edu.umass.cs.sensors.mhlhome.Communicator.Communicator;
import edu.umass.cs.sensors.mhllibrary.Configuration.Configuration;
import edu.umass.cs.sensors.mhllibrary.MHLClient.MHLClient;
import edu.umass.cs.sensors.mhllibrary.MHLClient.MHLMessageReceiver;
import edu.umass.cs.sensors.mhllibrary.MHLShepherd.DummyShepherd;
import edu.umass.cs.sensors.mhllibrary.MHLShepherd.MHLShepherd;
import edu.umass.cs.sensors.mhllibrary.MHLUtilities.AndroidOutputHandler;
import edu.umass.cs.sensors.mhllibrary.MHLUtilities.MHLOutputHandler;
//import edu.umass.cs.sensors.mhlhome.R;
//import edu.umass.cs.sensors.mhlhome.Service.MHLAgentService;
//import edu.umass.cs.sensors.mhlhome.Survey.Surveys;
//import edu.umass.cs.sensors.mhlhome.View.MainActivity;

/**
 * Created by erisinger on 1/8/19.
 */
public class MHLAndroidAgentCore implements MHLMessageReceiver {

    protected final String TAG = "MHLAndroidAgent";

    Context context;
    MHLOutputHandler outputHandler;
    MHLClient client;
    //protected MHLWearOSDevice wearOSDevice;
    String badgeID;
    StateOracle oracle;
    //public ContextTracker contextTracker;
    //Configuration config;


    // TODO add user-facing notification functionality via shepherd
    protected MHLShepherd shepherd;

    // conditional singleton: getInstance() does not create an instance if it doesn't already exist
    protected static MHLAndroidAgentCore instance = null;


    public MHLAndroidAgentCore(Context context) {

        this.context = context;
        //this.config = config;
        this.badgeID = IdentityConfig.getBadgeID(context);
        //this.wearOSDevice = new MHLWearOSDevice(context, this);

        // TODO replace dummy shepherd
        this.shepherd = new DummyShepherd();

        this.client = new MHLClient(context, this, this.shepherd);

        this.outputHandler = new AndroidOutputHandler();
        //this.contextTracker = new ContextTracker(context);
        //this.oracle = StateOracle.getInstance(context);

    }

    public MHLClient getClient(){
        return this.client;
    }

    protected static synchronized MHLAndroidAgentCore createAndReturnInstance(Context context) {
        MHLOutputHandler outputHandler = new AndroidOutputHandler();
        String TAG = "MHLAndroidAgent";

        if (instance != null) {
            //instance.disconnect();

            outputHandler.printMessage(TAG,"Retunring existing agent.");
            return instance;
        }
        else {
            outputHandler.printMessage(TAG, "Starting new agent.");
            return (instance = new MHLAndroidAgentCore(context));
        }
    }

    public static MHLAndroidAgentCore getInstance(Context context) {

        //PreferencesAccessor prefs = PreferencesAccessor.getInstance(context);

        //Configuration config = Configuration.getInstance(context);

        String storedBadge = IdentityConfig.getBadgeID(context); //prefs.getStoredPref("badge-id");
        String storedResearchToken = IdentityConfig.getResearchToken(context);// prefs.getStoredPref("research-token");
        int configVersion = StudyConfig.getConfigVersion(context); //prefs.getStoredPref("config-version");

        if (instance == null || instance.badgeID == "" || !instance.badgeID.equals(storedBadge) || configVersion==0) {

            // honoring the "null if not stored" contract of the PreferencesAccessor
            String ip = NetworkConfig.getServerIP(context); //prefs.getStoredPref("ip");

            if (storedResearchToken == null || storedResearchToken.equals("") || ip == null || ip.equals("")) {
                //Can't continue without research token value and server ip
                instance = null;

            } else {
                createAndReturnInstance(context);
            }
        }

        return instance;
    }



    public Boolean connect_test() {
        outputHandler.printMessage(TAG, "connect_test() called");

        Boolean connected = client.connect();
        if(connected){ client.disconnect();}

        return connected;
    }


    public void connect() {

        outputHandler.printMessage(TAG, "connect() called");

        if (!client.isConnected()) {
            client.connectAndRetry();
            //client.connect();
        }
        //contextTracker.startTracking();

    }

    public void disconnect() {

        outputHandler.printMessage(TAG, "disconnect() called");

        //Stop motionsense
        //stopMotionSense();

        //Stop the context tracker
        //if(contextTracker!=null) contextTracker.stopTracking();

        //Stop the client
        if(client!=null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    client.disconnect();
                }
            }).start();
        }
    }


    public void refreshConnection() {
        outputHandler.printMessage(TAG, "Refreshing connection with backend");

        new Thread(new Runnable() {
            @Override
            public void run() {
                client.disconnect(true);
            }
        }).start();
    }

    public long timeSinceLastHeartbeat() {
        return client.timeSinceLastHeartbeat();
    }

    public boolean isConnected() {
        return client.isConnected();
    }

    public void receive(String message) {

        outputHandler.printMessage("", "received message from server: " + message);

        JSONObject obj = parseJSONString(message);

        if (obj != null) {
            try {
                JSONObject header = (JSONObject) obj.get("header");
                String messageType = (String) header.get("message-type");

                handleIncomingMessage(messageType, obj);
            } catch (JSONException e) {
                outputHandler.handleException(e);
            }
        }
    }

    public void  handleIncomingMessage(String messageType, JSONObject obj){

    }

    public boolean addMessage(String message) {
        return client.addMessage(message);
    }

    public String getBadgeID() {
        return badgeID;
    }

    public void setVerbose() {
        setVerbose(true);
    }

    public void setVerbose(boolean verbose) {
        client.setVerbose(verbose);
    }

    public void clearNotifications() {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
    }




    /*
    public void scheduleFirstSanityCheckWorker() {
        long now = System.currentTimeMillis();
        long millisUntilFirstSanityCheck = oracle.getStartTimeForDataCollectionPeriod(1) - now;
        int hoursUntilFirstSanityCheck = (int)(millisUntilFirstSanityCheck / 1000 / 60 / 60); // millis > seconds > minutes > hours

        scheduleSanityCheckWorker(hoursUntilFirstSanityCheck);
    }

    public void scheduleSanityCheckWorker(int delay) {
        OneTimeWorkRequest serviceRequest = new OneTimeWorkRequest.Builder(SanityCheckWorker.class)
                .addTag("SCW")
                .setInitialDelay(delay, TimeUnit.HOURS)
                .build();

        WorkManager.getInstance().enqueue(serviceRequest);
    }
    */

    /*
    public void scheduleDataCollection() {
        // once a schedule has been received, this method enqueues the first service worker
        // each subsequent service worker enqueues the following service worker
        OneTimeWorkRequest serviceWorker = new OneTimeWorkRequest.Builder(DataCollectionServiceWorker.class)
                .setInitialDelay(2, TimeUnit.SECONDS)
                .build();

        WorkManager.getInstance().enqueue(serviceWorker);

        outputHandler.printMessage(TAG, "Scheduled first DataCollectionServiceWorker");
    }
    */

    public void turnOffDozeMode(Context context){  //you can use with or without passing context
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent intent = new Intent();
            String packageName = context.getPackageName();
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            if (pm.isIgnoringBatteryOptimizations(packageName)) { // if you want to desable doze mode for this package
                Log.d(TAG, "already ignoring battery optimizations");
                intent.setAction(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
            }

            else { // if you want to enable doze mode
                Log.d(TAG, "need to ignore battery optimizations...");
                intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + packageName));
                context.startActivity(intent);
            }
        }
    }


    public int getTimeSinceLastReceivedHeartbeat(){
        if (client!=null) {
            return client.getTimeSinceLastReceivedHeartbeat();
        }
        else{
            return -1;
        }
    }

    public boolean startMotionSense() {
        boolean startSucceeded = true;

        ComponentName name =  new ComponentName("org.md2k.motionsense", "org.md2k.motionsense.ServiceMotionSense");

        try {

            outputHandler.printMessage(TAG, "Trying to start motionsense...");


            Intent startMotionSenseIntent = new Intent();

            //config = Configuration.getInstance(context);

            //PreferencesAccessor prefs = PreferencesAccessor.getInstance(context);
            String badgeID            = IdentityConfig.getBadgeID(context);
            String research_token     = IdentityConfig.getResearchToken(context);
            String ip                 = NetworkConfig.getServerIP(context);
            int secPort               = NetworkConfig.getSecurePort(context);
            int insecPort             = NetworkConfig.getInsecurePort(context);
            int configuration_version = StudyConfig.getConfigVersion(context);

            Log.d(TAG, "" + secPort + ", " + insecPort);

            startMotionSenseIntent.putExtra("badge-id", badgeID);
            startMotionSenseIntent.putExtra("ip", ip);
            startMotionSenseIntent.putExtra("secure-port", secPort);
            startMotionSenseIntent.putExtra("insecure-port", insecPort);
            startMotionSenseIntent.putExtra("research-token", research_token);
            startMotionSenseIntent.putExtra("configuration-version", configuration_version);


            startMotionSenseIntent.setComponent(name);

            ComponentName c;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                c = context.startForegroundService(startMotionSenseIntent);
            } else {
                c = context.startService(startMotionSenseIntent);
            }

            Log.d("DataCollectionStarter", "IP: " + ip);

            if (c == null) {
                Log.e("DataCollectionStarter", "failed to start with " + startMotionSenseIntent);
                startSucceeded = false;
            }


        } catch (Exception e) {
            e.printStackTrace();
            startSucceeded = false;
        }

        return startSucceeded;
    }

    public boolean stopMotionSense() {
        boolean stopSucceeded = true;

        ComponentName name =  new ComponentName("org.md2k.motionsense", "org.md2k.motionsense.ServiceMotionSense");


        try {

            Intent abc = new Intent();
            abc.setComponent(name);
            stopSucceeded = context.stopService(abc);

        } catch (Exception e) {
            e.printStackTrace();
            stopSucceeded = false;
        }

        return stopSucceeded;
    }

    protected JSONObject parseJSONString(String jsonString) {

        try {
            JSONObject obj = new JSONObject(jsonString);
            return obj;

        } catch (JSONException e) {
            outputHandler.handleException(e);
        }

        return null;
    }

    /*public void submitDeregistrationRequest() {
        // send deregistration message to backend
        SCHDeregistrationMessage message = new SCHDeregistrationMessage(badgeID, System.currentTimeMillis());
        addMessage(message.toJSONString());

    }*/


    protected void showToast(String message, int length) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast toast1 = Toast.makeText(context, message, length);
                toast1.show();
            }
        });
    }

    public JSONObject buildExpiredSVASMessage(String serial) {
        JSONObject svasMessage = buildSVASMessage(serial);

        try {
            JSONObject metadata = svasMessage.getJSONObject("metadata");
            metadata.put("expires-at", System.currentTimeMillis() - 1000 * 60); // expired 1 minute ago
            svasMessage.put("metadata", metadata);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return svasMessage;
    }

    public JSONObject buildSVASMessage(String serial) {
        JSONObject obj = new JSONObject();
        JSONObject header = new JSONObject();
        JSONObject metadata = new JSONObject();
        JSONArray payload = new JSONArray();

        try {
            header.put("badge-id", this.badgeID);
            header.put("channel", "outgoing-message");
            header.put("timestamps", new JSONArray());
            header.put("message-version", "1.0");
            header.put("message-type", "response-request");

            metadata.put("expires-at", Long.MAX_VALUE);
            metadata.put("expires-after", Long.MAX_VALUE);
            metadata.put("injection-rules", new JSONArray());
            metadata.put("ack-receipt", "false");
            metadata.put("request-type", "survey");
            metadata.put("serial-number", serial);
            metadata.put("serial", serial);
            metadata.put("inject-at", System.currentTimeMillis());
            metadata.put("survey-title", "Stress Rating");
            metadata.put("survey-id", "stress-rating");

            // generate SVAS question
            JSONObject question = new JSONObject();
            question.put("question-type", "continuous-scale");
            question.put("question", "Please rate your stress level (0-100).");

            // add question to list of questions (size: 1)
            JSONArray questions = new JSONArray();
            questions.put(question);

            // add questions and instructions to survey
            JSONObject survey = new JSONObject();
            survey.put("instructions", "Please answer as accurately as possible.");
            survey.put("questions", questions);

            // add survey to payload
            payload.put(survey);

            // add components to message and return
            obj.put("header", header);
            obj.put("metadata", metadata);
            obj.put("payload", payload);

        } catch (JSONException e) {
            outputHandler.handleException(e);
        }

        return obj;
    }

    public JSONObject buildSmokingCessationMessageRatingMessage(String cessationMessage, String serial) {
        JSONObject obj = new JSONObject();
        JSONObject header = new JSONObject();
        JSONObject metadata = new JSONObject();
        JSONArray payload = new JSONArray();

        try {
            header.put("badge-id", this.badgeID);
            header.put("channel", "outgoing-message");
            header.put("timestamps", new JSONArray());
            header.put("message-version", "1.0");
            header.put("message-type", "response-request");

            metadata.put("expires-at", Long.MAX_VALUE);
            metadata.put("expires-after", Long.MAX_VALUE);
            metadata.put("injection-rules", new JSONArray());
            metadata.put("ack-receipt", "false");
            metadata.put("request-type", "survey");
            metadata.put("serial-number", serial);
            metadata.put("serial", serial);
            metadata.put("inject-at", System.currentTimeMillis());
            metadata.put("survey-title", "Smoking Cessation Message Rating");
            metadata.put("survey-id", "smoking-cessation-message-rating");
            metadata.put("smoking-cessation-message", cessationMessage);

            // generate SVAS question
            JSONArray responses = new JSONArray();
            responses.put("Strongly agree");
            responses.put("Agree");
            responses.put("Neutral");
            responses.put("Disagree");
            responses.put("Strongly disagree");

            JSONObject question = new JSONObject();
            question.put("question-type", "radio-set");
            question.put("question", "This question influenced me to not smoke?");
            question.put("radio-values", responses);

            // add question to list of questions (size: 1)
            JSONArray questions = new JSONArray();
            questions.put(question);

            // add questions and instructions to survey
            JSONObject survey = new JSONObject();
            survey.put("instructions", cessationMessage);
            survey.put("questions", questions);

            // add survey to payload
            payload.put(survey);

            // add components to message and return
            obj.put("header", header);
            obj.put("metadata", metadata);
            obj.put("payload", payload);

        } catch (JSONException e) {
            outputHandler.handleException(e);
        }

        return obj;
    }

}
