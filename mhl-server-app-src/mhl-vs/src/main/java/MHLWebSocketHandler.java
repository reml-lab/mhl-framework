/**
 * Created by erikrisinger on 4/16/17.
 */
import java.io.IOException;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.eclipse.jetty.websocket.api.RemoteEndpoint;
import org.eclipse.jetty.websocket.api.Session;

import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@WebSocket
public class MHLWebSocketHandler {
    private Session session;
    private KafkaConsumer<String, String> consumer;
    private String id = "empty";
    private boolean socketOpen = false;
    Thread streamThread;

    @OnWebSocketClose
    public void onClose(int statusCode, String reason) {
        socketOpen = false;
        streamThread.interrupt();
        System.out.print("websocket closed...");
//        System.out.println("Close: statusCode=" + statusCode + ", reason=" + reason);
    }

    @OnWebSocketError
    public void onError(Throwable t) {
//        System.out.println("Error: " + t.getMessage());
    }

    @OnWebSocketConnect
    public void onConnect(Session session) {
        socketOpen = true;
        this.session = session;
        System.out.print("connected to " + session.getRemoteAddress().getAddress());
    }

    @OnWebSocketMessage
    public void onMessage(String message) {
        if (message == null) return;

        Properties props = new Properties();

        // future work:
//        String[] split = message.split(";");
//        if (split.length == 4){
//            String userString = split[0];
//            String badgeString = split[1];
//            String hostnameString = split[2];
//            String secretKeyString = split[3];
//
//            String user;
//            String badge;
//            String hostname;
//            String secretKey;
//
//            if (userString.split(",")[0].equals("user")) {
//                user = userString.split(",")[1];
//            } else {
//                System.out.println("aborting on failed handshake " + message);
//                return;
//            }
//
//            if (badgeString.split(",")[0].equals("badge")) {
//                badge = badgeString.split(",")[1];
//                id = badge;
//            } else {
//                System.out.println("aborting on failed handshake " + message);
//                return;
//            }
//
//            if (hostnameString.split(",")[0].equals("hostname")) {
//                hostname = hostnameString.split(",")[1];
//            } else {
//                System.out.println("aborting on failed handshake " + message);
//                return;
//            }
//
//            if (secretKeyString.split(",")[0].equals("secret-key")) {
//                secretKey = secretKeyString.split(",")[1];
//            } else {
//                System.out.println("aborting on failed handshake " + message);
//                return;
//            }
        String[] split = message.split(",");

        if (split.length == 2 && split[0].equals("ID")) {
            id = split[1];
            //kafka consumer code
//            props.put("bootstrap.servers", "localhost:9092,localhost:9093,localhost:9094");
//            props.put("bootstrap.servers", "none.cs.umass.edu:9092,none.cs.umass.edu:9093,none.cs.umass.edu:9094");
            
			HandyFile bootstrapServersFile = new HandyFile("./bootstrap.servers");
            props.put("bootstrap.servers", bootstrapServersFile.getLine(0));
            props.put("group.id", "VS" + System.currentTimeMillis());
            props.put("enable.auto.commit", "true");
            props.put("auto.commit.interval.ms", "30000");
            props.put("session.timeout.ms", "30000");
            props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
            props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
            consumer = new KafkaConsumer<>(props);
            consumer.subscribe(Arrays.asList("data-message", "analytics-message"));

            System.out.println("...streaming data for ID " + id);

            streamThread = new Thread(new DataStreamerRunnable(consumer, this.session.getRemote(), id));
            streamThread.start();

        } else {
            System.out.println("aborting on failed handshake " + message);
        }
    }

    private class DataStreamerRunnable implements Runnable {
        KafkaConsumer<String, String> cons;
        RemoteEndpoint endpoint;
        String id;
        JSONParser parser = new JSONParser();
        Thread streamer;
        BlockingQueue<String> messages = new LinkedBlockingQueue<>();

        public DataStreamerRunnable(KafkaConsumer<String, String> c, RemoteEndpoint e, String id) {
            this.cons = c;
            this.endpoint = e;
            this.id = id;
        }

        @Override
        public void run() {

            JSONObject obj;
            JSONObject header;
            String recordID;

            streamer = new Thread(new StreamerRunnable());
            streamer.start();

            while (!Thread.currentThread().isInterrupted()) {

                if (Thread.currentThread().isInterrupted()) break;

                ConsumerRecords<String, String> records = consumer.poll(2000);
                for (ConsumerRecord<String, String> record : records) {

                    try {
                        obj = (JSONObject)parser.parse(record.value());
                        header = (JSONObject)obj.get("header");
                        recordID = (String)header.get("badge-id");

                        if (recordID.equals(this.id)) {
                            messages.offer(record.value());
                        }
                    } catch (ParseException p) {
                        System.out.println("parsing failed for string " + record.value());
                    }
                }

            }

            streamer.interrupt();

            cons.close();

            try {
                streamer.join(2000);
                System.out.println("streamer joined");

            } catch (InterruptedException e) {
                System.out.println("streamer may not have joined");
            }

            System.out.println("consumer closed...session ended for " + session.getRemoteAddress().getAddress());
        }

        private class StreamerRunnable implements Runnable {

		    long t;
		    long msg_count =0;
		    long msg_total_bytes=0;
		    long start_time = System.currentTimeMillis();

            @Override
            public void run() {
                try {
                    while (!Thread.currentThread().isInterrupted()) {

                        if (Thread.currentThread().isInterrupted()) break;

                        String m = messages.take();
						endpoint.sendString(m);
						
						//Provide processing status information
						msg_total_bytes = msg_total_bytes + m.length();
	                    msg_count++;
	                    if((msg_count % 1000)==0){
	                        double time_delta = (System.currentTimeMillis()-start_time)/1000;
	                        double msg_rate = msg_count/time_delta;
	                        double byte_rate = msg_total_bytes/time_delta;
	                        double msg_avg_bytes = msg_total_bytes/msg_count;
	                        String temp = String.format("Msg count: %d Total bytes: %d Avg Msg Size: %.1f Msgs/s: %.2f Bytes/s: %.2f Total time: %.0f\n\n",
	                                         msg_count,  msg_total_bytes, msg_avg_bytes, msg_rate, byte_rate, time_delta );
	                        System.out.println(temp);

	                    }
                        //System.out.println("sent message: " + m);
                    }
                } catch (IOException | InterruptedException e) {
                    System.out.println("streamer caught exception, closing");
                }
            }
        }
    }
}
