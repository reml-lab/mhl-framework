import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import java.io.*;

import cliargs.CliArgs;
import org.json.simple.JSONObject;

/**
 * Created by erisinger on 12/22/16.
 */
public class MHLIngest {

    public static void main(String[] args) {
                       
        SSLServerSocketFactory sslserversocketfactory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
        SSLServerSocket sslserversocket = null;
        int port = 9696;
        boolean coordinated = true;

        //parse command line arguments
        CliArgs cliArgs  = new CliArgs(args);

        //kafka broker list: ABSENCE FATAL
        String brokers = null;
        if (cliArgs.switchPresent("-kafka")) {
            brokers = cliArgs.switchValue("-kafka");
        } else {
            System.out.println("aborted: no kafka brokers specified!");
            return;
        }

        //coordination server
        String cs = null;
        if (cliArgs.switchPresent("-coordination-server")) {
            cs = cliArgs.switchValue("-coordination-server");
        } else {
            coordinated = false;
            System.out.println("no coordination server specified: running uncoordinated!");
        }

        //port number
        if (cliArgs.switchPresent("-port")) {
            port = Integer.parseInt(cliArgs.switchValue("-port"));
        } else {
            System.out.println("no port specified: defaulting to " + port);
        }

        if (brokers == null) {
            System.out.println("aborting on broker list failure");
            return;
        }

        //spin up new server thread
        try {
            sslserversocket = (SSLServerSocket) sslserversocketfactory.createServerSocket(port);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {

        }

        if (sslserversocket == null) {
            System.out.println("aborted: server socket is null!");
            return;
        }

        System.out.println("listening on port " + port);

        while (true){
            try {
                SSLSocket sslsocket = (SSLSocket) sslserversocket.accept();
                sslsocket.setSoTimeout(1000);

                System.out.println("accepted connection with ip " + sslsocket.getInetAddress().getHostAddress());

                //launch runnable (coordinated or uncoordinated)
                if (coordinated) {
                    new Thread(new MHLIngestThread(sslsocket, brokers, cs, coordinated)).start();
                } else {
                    new Thread(new MHLIngestThread(sslsocket, brokers)).start();
                }

            } catch (Exception exception) {
                System.out.println("Exception in MHLIngest Thread");
                exception.printStackTrace();
            }
        }
    }

}
