package edu.umass.cs.sensors.mhllibrary.MHLClient;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.Socket;

import android.content.Context;
import edu.umass.cs.sensors.mhllibrary.Configuration.Configuration;
import edu.umass.cs.sensors.mhllibrary.MHLUtilities.AndroidOutputHandler;
import edu.umass.cs.sensors.mhllibrary.Configuration.Configuration;
import edu.umass.cs.sensors.mhllibrary.Configuration.IdentityConfig;
import edu.umass.cs.sensors.mhllibrary.Configuration.StudyConfig;
import edu.umass.cs.sensors.mhllibrary.Configuration.NetworkConfig;
import edu.umass.cs.sensors.mhllibrary.MHLAgentCore.PreferencesAccessor;

/**
 * Created by erisinger on 1/5/19.
 */


public abstract class MHLHandshake {
    protected Socket socket;
    protected BufferedReader reader;
    protected BufferedWriter writer;
    protected Context AppContext;

    protected String TAG = "MHLHandshake";

    public enum HandShakeResponse {
        HANDSHAKE_ERR,
        CONNECTION_ERR,
        OK,
        RETRY,
        OUT_OF_RETRIES;
    }

    public MHLHandshake(Context context) {
        //this.config = config;
        this.AppContext = context;
        AndroidOutputHandler outputHandler = new AndroidOutputHandler();
    }

    public abstract HandShakeResponse connect();

    protected HandShakeResponse performHandshake() {
        boolean handshakeSucceeded = false;

        try {

            // get buffered reader/writer from socket
            this.reader = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
            this.writer = new BufferedWriter(new OutputStreamWriter(this.socket.getOutputStream()));

            // initiate handshake
            try {
                String handshake = reader.readLine();

                this.printMessage("Got step 1 handshake message from server: "+ handshake);

                JSONObject prompt = new JSONObject(handshake);

                prompt.put("type", "shibboleth");
                prompt.put("response-version", "2.0");
                prompt.put("process-type", "field-device");
                prompt.put("badge-id", IdentityConfig.getBadgeID(AppContext));
                prompt.put("research-token", IdentityConfig.getResearchToken(AppContext));
                prompt.put("config-version", StudyConfig.getConfigVersion(AppContext));

                // return client half of the handshake
                writer.write(prompt.toString() + "\n");
                writer.flush();

                this.printMessage("Replied to step 1 handshake message from server: "+ prompt.toString());

                // check for successful ack message from server
                String msgString = reader.readLine();

                if (msgString != null) {

                    this.printMessage("Got step 2 handshake message from server: "+ msgString);
                    JSONObject msg = new JSONObject(msgString);

                    if (msg.has("type")) {
                        if (msg.get("type").equals("ack")) {
                            this.printMessage("Configuration accepted.");
                            //handshakeSucceeded = true;
                            return HandShakeResponse.OK;
                        }
                        //Ben: adding config-type message to push new config to
                        else if(msg.get("type").equals("config")){
                            this.printMessage("Got config message. Configuring.");

                            Configuration.set_from_JSONObject(AppContext,msg);

                            return HandShakeResponse.RETRY;
                        }
                        //Ben: adding error-type message to push new config to
                        else if(msg.get("type").equals("err")){
                            this.printMessage("Got error message from server.");
                            //handshakeSucceeded = false;
                            return HandShakeResponse.HANDSHAKE_ERR;
                        }
                    }
                }
                else {
                    this.printMessage("handshake with server failed");
                    //return false;
                    return HandShakeResponse.HANDSHAKE_ERR;
                }

            }
            catch (JSONException e) {
                handleException(e);
                //return false;
                return HandShakeResponse.HANDSHAKE_ERR;
            }

        }
        catch (IOException e) {
            handleException(e);
            //return false;
            return HandShakeResponse.HANDSHAKE_ERR;
        }

        return HandShakeResponse.HANDSHAKE_ERR; //Assume if not Ok or RETRY then ERR
    }

    public Socket getSocket() {
        return this.socket;
    }

    public BufferedReader getReader() {
        return this.reader;
    }

    public BufferedWriter getWriter() {
        return this.writer;
    }

    public void close() {
        Log.d(TAG, "close() called");

        if (this.reader != null) {
            try {
                this.reader.close();
            } catch (IOException e) {
                this.handleException(e);
            }
        }

        if (this.writer != null){
            try {
                this.writer.close();
            } catch (IOException e) {
                this.handleException(e);
            }
        }

        if (this.socket != null) {
            try {
                this.socket.close();
            } catch (IOException e) {
                this.handleException(e);
            }
        }
    }

    /* utility methods */
    protected void handleException(Exception e) {
        e.printStackTrace();
    }

    protected void printMessage(String message) {
        System.out.println(message);
    }
}