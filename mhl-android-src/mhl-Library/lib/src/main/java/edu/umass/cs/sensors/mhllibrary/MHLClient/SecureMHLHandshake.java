package edu.umass.cs.sensors.mhllibrary.MHLClient;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import org.apache.http.conn.ssl.SSLSocketFactory;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManagerFactory;

import edu.umass.cs.sensors.mhllibrary.R;
import edu.umass.cs.sensors.mhllibrary.Configuration.Configuration;

import edu.umass.cs.sensors.mhllibrary.Configuration.Configuration;
import edu.umass.cs.sensors.mhllibrary.Configuration.IdentityConfig;
import edu.umass.cs.sensors.mhllibrary.Configuration.StudyConfig;
import edu.umass.cs.sensors.mhllibrary.Configuration.NetworkConfig;
import edu.umass.cs.sensors.mhllibrary.MHLAgentCore.PreferencesAccessor;

//import org.apache.http.conn.ssl.SSLSocketFactory;


/**
 * Created by erisinger on 1/5/19.
 */
public class SecureMHLHandshake extends MHLHandshake {
    //Context context;

    final String TAG = "SecureMHLHandshake";

    public SecureMHLHandshake(Context context) {
        super(context);

        Log.d(TAG, "context: " + context.toString());
    }

    public HandShakeResponse connect(){
        //Connect with two retries allowed
        return connect(2);
    }

    public HandShakeResponse connect(int retries) {
        SSLSocket sock = null;
        this.socket = null;
        HandShakeResponse response;

        if(retries==0){
            this.printMessage("MHL handshake out of retries");
            return HandShakeResponse.OUT_OF_RETRIES;
        }

        this.printMessage("Attempting secure handshake with server. Retries remaning " + retries);

        Log.w(TAG, Configuration.getConfig(AppContext));

        try {

            boolean allowAllHostname = AppContext.getResources().getString(R.string.allow_all_hostnames).equals("true");
//            boolean debug = true;
            /*
             * If debug (above) is set to false, the SSL library will verify that the hostname in the
             * server key file matches the hostname being connected to (via config).
             *
             * A new key will need to be generated that matches the production IP or the
             * connection attempt will fail.
             */

            if (allowAllHostname) {
                KeyStore ks = KeyStore.getInstance("BKS");

                InputStream keyIn = AppContext.getResources().openRawResource(R.raw.keystore);
                InputStream passStream = AppContext.getResources().openRawResource(R.raw.keystore_password);
                BufferedReader passStreamReader = new BufferedReader(new InputStreamReader(passStream));
                String password = passStreamReader.readLine();
                ks.load(keyIn, password.toCharArray());

                SSLSocketFactory socketFactory = new SSLSocketFactory(ks);

                socketFactory.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

                Log.d(TAG, "" + NetworkConfig.getServerIP(AppContext) + ", " + NetworkConfig.getSecurePort(AppContext));

                sock = (SSLSocket) socketFactory.createSocket(
                        new Socket(
                                NetworkConfig.getServerIP(AppContext),
                                NetworkConfig.getSecurePort(AppContext)),
                        NetworkConfig.getServerIP(AppContext),
                        NetworkConfig.getSecurePort(AppContext),
                        false);

                Log.w(TAG, "socketFactory is null: " + (socketFactory == null));
                Log.w(TAG, "sock is null: " + (sock == null));

            } else {

                // customize this for hostname verification
                KeyStore ks = KeyStore.getInstance("BKS");
                //#InputStream keyIn = AppContext.getResources().openRawResource(R.raw.test_server_bks);
                //ks.load(keyIn, "secure_password".toCharArray());

                InputStream keyIn = AppContext.getResources().openRawResource(R.raw.keystore);
                InputStream passStream = AppContext.getResources().openRawResource(R.raw.keystore_password);
                BufferedReader passStreamReader = new BufferedReader(new InputStreamReader(passStream));
                String password = passStreamReader.readLine();
                ks.load(keyIn, password.toCharArray());

                TrustManagerFactory tmf = TrustManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
                tmf.init(ks);

                SSLContext context = SSLContext.getInstance("TLS");
                context.init(null, tmf.getTrustManagers(), new SecureRandom());
                Socket socket = context.getSocketFactory().createSocket(NetworkConfig.getServerIP(AppContext), NetworkConfig.getSecurePort(AppContext));

                SSLSocketFactory socketFactory = new SSLSocketFactory(ks);

                sock = (SSLSocket) socketFactory.createSocket(socket, NetworkConfig.getServerIP(AppContext), NetworkConfig.getSecurePort(AppContext), false);

                Log.w(TAG, "socketFactory is null: " + (socketFactory == null));
                Log.w(TAG, "sock is null: " + (sock == null));
            }

            sock.setSoTimeout(5000);
            sock.startHandshake();

            if(sock==null){
                System.out.println("Could not establish secure connection to server");
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
        } catch (IOException
                | KeyStoreException
                | CertificateException
                | NoSuchAlgorithmException
                | UnrecoverableKeyException
                | KeyManagementException
                e) {

            e.printStackTrace();
            this.printMessage("Error attempting to make secure connection to server.");
            if(sock!=null){
                try {
                    sock.close();
                }
                catch (IOException e2){
                    this.printMessage("Error closing non-null socket.");
                }
            }
            return(HandShakeResponse.CONNECTION_ERR);

        }
        return response;
    }
}
