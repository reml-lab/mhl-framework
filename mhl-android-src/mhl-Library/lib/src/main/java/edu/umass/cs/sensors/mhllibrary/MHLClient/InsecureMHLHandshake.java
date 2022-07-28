package edu.umass.cs.sensors.mhllibrary.MHLClient;

import android.content.Context;
import java.io.IOException;
import java.net.Socket;

import edu.umass.cs.sensors.mhllibrary.Configuration.Configuration;
import edu.umass.cs.sensors.mhllibrary.Configuration.IdentityConfig;
import edu.umass.cs.sensors.mhllibrary.Configuration.StudyConfig;
import edu.umass.cs.sensors.mhllibrary.Configuration.NetworkConfig;
import edu.umass.cs.sensors.mhllibrary.MHLAgentCore.PreferencesAccessor;

/**
 * Created by erisinger on 1/5/19.
 */
public class InsecureMHLHandshake extends MHLHandshake {

    /*public InsecureMHLHandshake(String ip, int port, String badgeID, String researchToken,int configVersion) {
        super(ip, port, badgeID, researchToken, configVersion);
        this.insecure_port = port;
    }*/

    public InsecureMHLHandshake(Context context) {
        super(context);
    }

    public HandShakeResponse connect(){
        return connect(2);
    }

    public HandShakeResponse connect(int retries) {
        Socket sock = null;
        HandShakeResponse response;

        if(retries==0){
            this.printMessage("MHL handshake out of retries");
            return HandShakeResponse.OUT_OF_RETRIES;
        }

        try {
            this.printMessage("Attempting insecure handshake with server. Retries remaning " + retries);
            this.socket = null;
            sock = new Socket(NetworkConfig.getServerIP(AppContext), NetworkConfig.getInsecurePort(AppContext));

            if(sock==null){
                System.out.println("Could not establish insecure connection to server");
                return HandShakeResponse.CONNECTION_ERR;
            }

            this.socket = sock;

            // attempt MHL-specific handshake with server
            response = performHandshake();
            if (response==HandShakeResponse.OK) {
                this.printMessage("MHL handshake returned OK");
                this.socket = sock;
            }
            else if(response==HandShakeResponse.RETRY) {
                this.printMessage("MHL handshake returned RETRY");
                this.socket = null;
                response=connect(retries-1);
            }
            else if(response==HandShakeResponse.HANDSHAKE_ERR) {
                this.printMessage("MHL handshake with server returned ERR");
                this.socket = null;
            }

        } catch (IOException e) {
            this.printMessage("Error attempting to make insecure connection to server.");
            response = HandShakeResponse.CONNECTION_ERR;
            //handleException(e);
        }
        return response;
    }
}