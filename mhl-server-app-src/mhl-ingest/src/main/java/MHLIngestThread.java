import javax.net.ssl.SSLSocket;
import java.net.SocketTimeoutException; 
import java.io.*;
import java.util.*;

import Messages.StepDetectionMessage;
//import com.oracle.javafx.jmx.json.JSONException;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
//import scala.util.parsing.json.JSON;

/**
 * Created by erisinger on 12/22/16.
 */
public class MHLIngestThread implements Runnable{
    SSLSocket socket;
    private KafkaProducer<String, String> producer;
    Thread consumerThread = null;
    boolean coordinated;
    String brokers;
    String coordinationServer;
    int messageCount = 0;
    Thread monitor;
    String badgeID = null;
    String researchToken = null;
    long lastStep = System.currentTimeMillis();
    BufferedWriter out;
    BufferedReader in;
    
    private int ADD_TIMES = 0;
    private int PUBLISH_TO_BADGE_ID_TOPIC =0;

    private boolean USE_KAFKA_PRODUCER=true;
    private boolean USE_KAFKA_CONSUMER=false;
    
    UserManager usermanager; 

    //coordinated, try-coordinated and semi-coordinated
    public MHLIngestThread(SSLSocket s, String brokers, String cs, boolean coord) {
        this.socket = s;
        this.brokers = brokers;
        this.coordinationServer = cs;
        this.coordinated = coord;
        
        this.usermanager = new UserManager("/user_manager_data/db.sqlite3");

        //this.monitor = new Thread(new DCRSMonitorRunnable());
    }

    //uncoordinated
    public MHLIngestThread(SSLSocket s, String brokers) {
        this.socket = s;
        this.brokers = brokers;
        this.usermanager = new UserManager("/user_manager_data/db.sqlite3");        
    }

    private class MHLMonitorRunnable implements Runnable {

        @Override
        public void run() {
            try {
                while (!Thread.interrupted()) {

                    if (Thread.interrupted()) break;

                    Thread.sleep(1000);

                    if (messageCount < 10) {
                        //TODO:
                        System.out.println("[Monitor] Panic: rate dropped to " + messageCount);
                    }

                    messageCount = 0;
                    
                }
            } catch (InterruptedException e) {
                System.out.println("[MONITOR] Exception: " + e);
            }

            System.out.println("[MONITOR] Shut down cleanly");
        }
    }

    @Override
    public void run() {
        ArrayList<String> pertainsToIDs = new ArrayList<>();
        String inputLine;
        String publishToChannel = null;
        JSONObject shibboleth = null;
        boolean isFieldDevice = false;
        
        long t;
        long msg_count =0;
        long msg_total_bytes=0;
        long start_time = System.currentTimeMillis();
        int ssl_read_retry = 0;
        

        String ip = socket.getInetAddress().getHostAddress();

        if (brokers == null) {
            System.out.println("aborted -- broker list is null!");
            return;
        }

        System.out.println((new Date()).toString() + ": initiating handshake with " + ip + "...");

        try{
            
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            
            //get shibboleth with configuration data, check not null
            if ((shibboleth = handshake(in, out, ip)) == null) {
                System.out.println("handshake failed with client " + ip);
                return;
            }

            badgeID = (String)shibboleth.get("badge-id");

            System.out.println("success...serving client Badge ID:" + badgeID);

            String processType = null;
            processType = (String)shibboleth.get("process-type");

            //every remote process wanting to submit or retrieve data must register as one of the following process types
            if (processType == null) {
                System.out.println("aborting: no process type");
            }

            //static publishToChannel is NOT being used right now in favor of a per-message channel query
            if (processType.equals("field-device")){
                isFieldDevice = true;
                publishToChannel = "data-message";

                //for a field device, the badge ID and pertains-to ID list are the same
                pertainsToIDs.add(badgeID);
            } else {

                if (processType.equals("analytics")){
                    publishToChannel = "analytics-message";

                } else if (processType.equals("visualization")) {
                    publishToChannel = "system-message";
                } else if (processType.equals("custom")) {
                    //this is a theoretical type that grants access to all channels

                } else {
                    System.out.println("aborting on unrecognized process type: " + processType);
                    return;
                }

                //for analytics and visualizations, drain pertains-to JSON array to pertainsToIDs array list
                for (Object o : (JSONArray)shibboleth.get("pertains-to")) {
                    pertainsToIDs.add((String)o);
                }
            }

            //handshake succeeded -- set up kafka components as needed
            if(USE_KAFKA_PRODUCER){configureProducer(brokers);}
            if(USE_KAFKA_CONSUMER){configureConsumer(out, badgeID, pertainsToIDs, brokers, processType);}

            HeartBeatRunnable  heartbeatRunnable = new HeartBeatRunnable(out, 1000);
            Thread heartbeatThread = new Thread(heartbeatRunnable);
            heartbeatThread.start();


            //consume data from mobile device until null (or socket closed)
            JSONParser parser = new JSONParser();
            JSONObject obj = null;
            JSONObject header = null;
            JSONArray timestamps, data;
            JSONObject receiptStamp, transmissionStamp;

            String version, dataType;

            long time;
            double x, y, z;

            Messages.SensorMessage message;

            
            if (monitor != null) {
                monitor.start();
            } else {
                System.out.println("monitor is null!");
            }
            
            //Continue readinging unless input in is null
            while (in!=null) {

                //Try to read from socket. This will raise 
                //SocketTimeoutException if read timeout fails
                try{
                    inputLine = in.readLine();
                    ssl_read_retry=0;
                }
                catch (SocketTimeoutException e) {
                    ssl_read_retry++;
                    
                    //Close if no read for 120s
                    if(ssl_read_retry>120){
                        return;
                    }
                    else{                    
                        inputLine = "";
                        continue;
                    }
                }

                if(inputLine==null){
                    break;
                }
                
                msg_total_bytes = msg_total_bytes + inputLine.length();
                
                //Send heartbeat lines via producer
                if(inputLine.length()<=2){
                    obj = new JSONObject();
                    obj.put("badge-id",badgeID);
                    obj.put("message-type","heartbeat");
                    emitStringWithTopic(obj.toJSONString(), "heartbeat-message");
                    continue;
                }
                
                //Otherwise, got a general object, try to parse.
                try {
                    obj = (JSONObject)parser.parse(inputLine);
                    //get message header
                    header = (JSONObject) obj.get("header");
                    //TODO: replace with process-type-based channel configuration
                    publishToChannel = (String)header.get("channel");

                    emitStringWithTopic(inputLine, publishToChannel);

                    //Provide processing status information
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

                } catch (ParseException e) {
                    System.out.println("parsing failed for message " + inputLine + " for badge ID " + badgeID);
                    e.printStackTrace();
                }
            }



        } catch (IOException e) {
            System.out.println("producer: socket closed...");
            System.out.println(e);
        } finally {
            //clean up
            System.out.println((new Date()).toString() + ": producer cleaning up client " + ip + "...");

            if (monitor != null) {
                monitor.interrupt();
            }

            if (producer != null) {
                producer.close();
            }

            if (consumerThread != null && !consumerThread.isInterrupted()) {
                consumerThread.interrupt();
            }

            try {
                if (socket != null) {
                    socket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            System.out.println("producer: cleanup complete");
        }
    }

    public JSONObject handshake(BufferedReader in, BufferedWriter out, String ip) {
        JSONParser parser = new JSONParser();
        JSONObject jsonObject;

        //construct and send prompt
        JSONObject prompt = new JSONObject();
        prompt.put("type", "prompt");
        prompt.put("timestamp", "" + System.currentTimeMillis());
        prompt.put("process-ip", ip);
        prompt.put("prompt-version", "1.0");

        String response = null;
        try {
            //transmit prompt
            System.out.println("Sending handshake prompt: " + prompt.toJSONString());
            out.write(prompt.toJSONString() + "\n");
            out.flush();

            //wait for shibboleth
            System.out.println("Awaiting handshake response...");
            response = in.readLine();
                        
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Connection lost during handshake for IP " + ip);
            return null;
        }

        if (response == null) {
            System.out.println("Got null response during handshake for IP " + ip);
            return null;
        }

        System.out.println("Got handshake response from client: "+response);

        //parse response
        try {
            jsonObject = (JSONObject)parser.parse(response);

        } catch (ParseException e) {
            System.out.print("parsing failed for response " + response + " for ip " + ip);
            return null;
        }
        
        //Implement updated handshake with field device
        JSONObject shibboleth = new JSONObject(jsonObject);
        
        if(shibboleth.get("badge-id")==null || 
           shibboleth.get("research-token")==null || 
           shibboleth.get("research-token") == "" || 
           shibboleth.get("type") == null || 
           shibboleth.get("process-type") == null){
               
            System.out.println("Found null values in connection info for IP " + ip);
            return null;            
        } 
        else if (!((String)shibboleth.get("type")).equals("shibboleth")) {
            System.out.println("Missing 'shibboleth' for IP " + ip);
            return null;
        }

        //TODO: handle response version -- if 2.0, unpack message templates and set up mappings
        badgeID = (String)shibboleth.get("badge-id");
        researchToken = (String)shibboleth.get("research-token");
        int configVersion = ((Long)shibboleth.get("config-version")).intValue();

        System.out.println("Got Research Token: "+ researchToken
            + " Badge ID: "+badgeID
            + " Configuration Version: " + configVersion); 
        
        if(!usermanager.isKnownUser(researchToken)){
           System.out.println("The research token " +  researchToken + " is not in the user database");
           
           JSONObject hsresponse = new JSONObject();
           hsresponse.put("type", "err");
           System.out.println("Sending message:" + hsresponse.toJSONString()); 
           try {
               out.write(hsresponse.toJSONString() + "\n");
               out.flush();
           } catch (IOException e) {
               e.printStackTrace();
           }
           
           return null;
        }
        else if(!badgeID.equals("") && !badgeID.equals(usermanager.getBadgeID(researchToken))) {
           System.out.println("The research token " +  researchToken + " does not match badge ID " + badgeID + "in the user database");
           
           //Have a unknown user, send error
           JSONObject hsresponse = new JSONObject();
           hsresponse.put("type", "err");
           System.out.println("Sending message:" + hsresponse.toJSONString()); 
           try {
               out.write(hsresponse.toJSONString() + "\n");
               out.flush();
           } catch (IOException e) {
               e.printStackTrace();
           }
           
           return null;
        }           
        else if(badgeID.equals("") || (configVersion != usermanager.getConfigVersion(researchToken)) ){
            JSONObject config = usermanager.getConfig(researchToken);
            
            System.out.println("Known research token but not configured. Sending configuration message: " + config.toJSONString() + ". Client will need to reconnect after configuring.");                  
            try {
                out.write(config.toJSONString() + "\n");
                out.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
            //Send null shibboleth to force client to re-connect with correct credentials
            return null;
        }
        else{
            System.out.println("Got valid ResearchToken: " + researchToken +" and BadgeID: " + badgeID + ". Sending ack");
            JSONObject hsresponse = new JSONObject();
            hsresponse.put("type", "ack");
            try {
                out.write(hsresponse.toJSONString() + "\n");
                out.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return shibboleth;   
        }
    }
    
    private void emitStringWithTopic(String str, String topic){

        if(USE_KAFKA_PRODUCER){
            //Publish data to the message-type topic
            ProducerRecord<String, String> data = new ProducerRecord<String, String>(topic, str);
            producer.send(data);

            //Optionally also publish to badge-id topic
            if(PUBLISH_TO_BADGE_ID_TOPIC==1) {
                data = new ProducerRecord<String, String>(badgeID, str);
                producer.send(data);
            }
            producer.flush();
        }
        else{
            System.out.println("[STDOUT Emitter] Topic: " + topic + " Content: " + str); 
        }

    }

    private void configureProducer(String brokers){
        Properties props = new Properties();
        props.put("bootstrap.servers", brokers);
        props.put("acks", "all");
        props.put("retries", 0);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("retry.backoff.ms", "500");
        props.put("session.timeout.ms", "120000");

        producer = new KafkaProducer<String, String>(props);
    }

    private void configureConsumer(BufferedWriter out, String badge, ArrayList<String> pertainsToList, String brokers, String processType){
        Properties props = new Properties();
        props.put("bootstrap.servers", brokers);
        props.put("group.id", "SDCRS" + System.currentTimeMillis());
        props.put("enable.auto.commit", "true");
        props.put("auto.commit.interval.ms", "1000");
        props.put("session.timeout.ms", "120000");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

        //launch runnable
        ArrayList<String> topicsList = new ArrayList<>();

        //subscribed topics depend on remote process type (field device vs. analytics vs. visualization)
        if (processType.equals("field-device")){
            topicsList.add("outgoing-message");
        } else if (processType.equals("analytics")){
            topicsList.add("data-message");
            topicsList.add("analytics-message");
        } else if (processType.equals("visualization")){
            topicsList.add("data-message");
            topicsList.add("analytics-message");
        } else {
            System.out.println("unrecognized process type: " + processType);
        }

        //all process types are obligated to subscribe to system messages
        topicsList.add("outgoing-message");

        KafkaConsumerRunnable consumerRunnable = new KafkaConsumerRunnable(props, badge, topicsList, out, pertainsToList);
        consumerThread = new Thread(consumerRunnable);
        consumerThread.start();
    }

    private class KafkaConsumerRunnable implements Runnable {
        BufferedWriter output;
        KafkaConsumer<String, String> consumer;
        String badgeID;
        ArrayList<String> pertainsToIDs;

        public KafkaConsumerRunnable(Properties props, String badge, List<String> topics, BufferedWriter out, ArrayList<String> ids) {
            this.output = out;
            this.badgeID = badge;
            this.pertainsToIDs = ids;

            //kafka consumer code
            consumer = new KafkaConsumer<>(props);
            consumer.subscribe(topics);
        }

        public void run() {
            JSONParser parser = new JSONParser();
            JSONObject header, metadata, payload, obj = null;
            String id, messageType, analyticsType;
            long timestamp, lastStep;
            double val;

            if (output == null) {
                System.out.println("output is null!");
                return;
            }

            try {

                while (!Thread.currentThread().isInterrupted()){

                    ConsumerRecords<String, String> records = consumer.poll(2000);

                    //capture time for data retrieval timestamp
                    timestamp = System.currentTimeMillis();

//                    System.out.println("" + records.count() + " records retrieved");

                    //check for a living socket connection every two seconds
                    //And send heartbeat
                    if (records.count() == 0) {
                        output.write("H\n");
                        output.flush();
                    }

                    for (ConsumerRecord<String, String> record : records) {

                        //parse incoming objects
                        try {
                            obj = (JSONObject)parser.parse(record.value());
                        } catch (ParseException e) {
                            e.printStackTrace();
                            continue;
                        }

                        header = (JSONObject)obj.get("header");

                        //filter on badge ID
                        id = (String)header.get("badge-id");
                        if (!pertainsToIDs.contains(id)) {
                            continue;
                        }

                        //optional: add retrieval timestamp
                        JSONArray timestamps = (JSONArray)header.get("timestamps");

                        //TODO: add timestamp here


                        header.put("timestamps", timestamps);
                        obj.put("header", header);

                        //send filtered messages to remote process
                        String outString = obj.toJSONString();
                        if (outString != null) {
                            output.write(outString + "\n");
                            output.flush();
                        }
                    }
                }

            } catch (IOException e) {
                System.out.print("consumer: socket closed -- exiting...");

            } finally {
                System.out.print("consumer: closing consumer...");
                consumer.close();
                System.out.println("consumer: cleanup complete for ID " + badgeID);
            }
        }
    }
    
    private class HeartBeatRunnable implements Runnable {
        BufferedWriter output;
        KafkaConsumer<String, String> consumer;
        String badgeID;
        ArrayList<String> pertainsToIDs;
        int interval;
        long timestamp, lastStep;

        public HeartBeatRunnable(BufferedWriter output, int interval) {
            this.interval = interval;
            this.output   = output;
        }

        public void run() {

            if (output == null) {
                System.out.println("[HeartBeat] Output BufferedWriteris null!");
                return;
            }

            try {

                while (!Thread.currentThread().isInterrupted()){

                    output.write("H\n");
                    output.flush();
                    Thread.sleep(this.interval);
                    
                }

            } 
            catch (IOException e) {
                System.out.println("[HeartBeat] socket closed. Exiting.");
            }
            catch (InterruptedException e){
                System.out.println("[HeartBeat] thread interrupted.");
            } 
            finally {
                System.out.println("[HeartBeat] cleanup complete.");
            }
        }
    }
    
}
