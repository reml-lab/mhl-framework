import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by erisinger on 6/21/17.
 */
public class TapMain {
//    Client client;

    public static void main(String[] args) {

        if (args.length != 5) {
            System.out.println("required arguments (as CSLs): brokers, channels, ids, max index queue length, max index time");
            return;
        }

		System.out.println("Starting ESTap with configuration:");
		System.out.println("  Brokers: " + args[0]);
		System.out.println("  Channels: " + args[1]);
		System.out.println("  IDs: " + args[2]);
		System.out.println("  Max index queue length: " + args[3]);
		System.out.println("  Max index time: " + args[4]);

        ESTap elasticTap = new ESTap(args[0], args[1], args[2],args[3],args[4]);

        Thread elasticThread = new Thread(elasticTap);
        elasticThread.start();

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        try {
            while (!reader.readLine().equals("q")) {
                Thread.sleep(2000);
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("interrupted -- initiating shutdown...");
        elasticThread.interrupt();
    }

}
