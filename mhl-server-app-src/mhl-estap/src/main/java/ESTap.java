/**
 * Created by erisinger on 9/21/17.
 */

import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress ;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by erisinger on 6/22/17.
 */
public class ESTap implements Runnable, TapReceiver{
    TransportClient client;
    Tap tap;
    JSONParser parser;
    DateFormat formatter = new SimpleDateFormat();

    long msg_count =0;
    long msg_total_bytes=0;
    long start_time = 0;
    long msg_index_count =1;
    int MSG_QUEUE_LENGTH = 500;
    long msg_index_timer = 0;
    int MSG_QUEUE_TIMEOUT = 5000;

    BulkRequestBuilder bulkRequest;

    public ESTap(String brokers, String channelString, String idString, String str_MSG_QUEUE_LENGTH, String str_MSG_QUEUE_TIMEOUT) {

        MSG_QUEUE_LENGTH = Integer.parseInt(str_MSG_QUEUE_LENGTH);
        MSG_QUEUE_TIMEOUT = Integer.parseInt(str_MSG_QUEUE_TIMEOUT);

        System.out.println("Setting up tap with MSG_QUEUE_LENGTH="+MSG_QUEUE_LENGTH
                + " MSG_QUEUE_TIMEOUT="+MSG_QUEUE_TIMEOUT);

        parser = new JSONParser();
        System.out.println("setting up -- please don't quit yet!");

			System.out.println("Attempting to connect");

			//connect to elasticsearch cluster
	        TransportAddress  address;
	        try {
				//This needs to be passed in as a parameter
	            address = new TransportAddress(InetAddress.getByName("mhl-es"), 9300);
	        } catch (UnknownHostException e) {
	            e.printStackTrace();
	            return;
	        }

	        Settings settings = Settings.builder()
	                .put("cluster.name", "mHealthLab")
	                .build();

	        client = new PreBuiltTransportClient(settings);
	        client.addTransportAddress(address);
	        bulkRequest = client.prepareBulk();


        //set up tap
        String[] channelsArray = channelString.split(",");
        ArrayList<String> channels = new ArrayList<>(Arrays.asList(channelsArray));

        String[] idsArray = idString.split(",");
        ArrayList<String> ids = new ArrayList<>(Arrays.asList(idsArray));

        start_time = System.currentTimeMillis();
        msg_index_timer = start_time;

        tap = new Tap(this, brokers, channels, ids);

    }

    @Override
    public void run() {
        tap.start();

        while (!Thread.currentThread().isInterrupted()) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
//                e.printStackTrace();
                break;
            }
        }

        System.out.println("Stopping tap");

        tap.stop();
        client.close();
    }




    @Override
    public void receive(String message) {
        JSONObject obj, header, metadata, payload, val, out;
        JSONArray vals;

        msg_total_bytes = msg_total_bytes + message.length();

        try {
            obj = (JSONObject) parser.parse(message);
            header = (JSONObject) obj.get("header");
            metadata = (JSONObject) obj.get("metadata");
            payload = (JSONObject) obj.get("payload");

            //Indexing of all sensor data
            if (header.get("message-type").equals("sensor-message")) {

                //if (metadata.containsKey("ring")) {
                  //  if (((Number) metadata.get("ring")).doubleValue() == 2) {

                        String sensorType = (String) metadata.get("sensor-type");

                        // index into Elasticsearch
                        vals = (JSONArray) payload.get("vals");

                        out = new JSONObject();
                        out.put("badge-id", header.get("badge-id"));

                        long timestamp = ((Number) payload.get("t")).longValue();
                        String datetime = ((String) payload.get("d"));

                        out.put("timestamp", timestamp);
                        out.put("datetime", datetime);

                        for (Object o : vals) {
                            val = (JSONObject) o;
                            Object[] keys = val.keySet().toArray();
                            String key = (String) keys[0];

                            out.put(key, val.get(key));
                        }

                        index("sensor-" + sensorType, sensorType + "-reading", out.toJSONString(),0);
                   // }
                //}
            }

            //Indexing of all monitor reports
            else if (header.get("message-type").equals("monitor-report")) {

                if (metadata.get("monitor-type").equals("band-monitor")) {
                    out = new JSONObject();
                    out.put("badge-id", header.get("badge-id"));
                    out.put("timestamp", ((Number) payload.get("t")).longValue());

                    out.put("wearing", ((Number) payload.get("wearing")).intValue());

                    index("monitor-report", "band-monitor", out.toString(),0);

                } else if (metadata.get("monitor-type").equals("app-usage-monitor")) {
                    out = new JSONObject();
                    out.put("badge-id", header.get("badge-id"));
                    out.put("timestamp", ((Number) payload.get("t")).longValue());

                    out.put("package", ((Number) payload.get("package")).intValue());
                    out.put("event-type", ((Number) payload.get("event-type")).intValue());

                    index("monitor-report", "app-usage-monitor", out.toString(),0);
                }
            }

            //Indexing of all assessment results
            else if (header.get("message-type").equals("assessment-result")) {
                out = new JSONObject();
                out.put("badge-id", header.get("badge-id"));
                out.put("timestamp", ((Number) payload.get("t")).longValue());

                out.put("results", payload.toJSONString());

                if (metadata.containsKey("result-type") && metadata.get("result-type").equals("bubble-test")) {
                    index("assessment-result", "bubble-test", out.toString(),0);
                } else {

                    index("assessment-result", "assessment-result", out.toString(),0);
                }
            }

            //Indexing of all-self reports
            else if (header.get("message-type").equals("self-report")) {
                if (metadata.get("self-report-type").equals("daily-activity-report")) {
                    out = new JSONObject();
                    out.put("badge-id", header.get("badge-id"));
                    out.put("timestamp", ((Number) payload.get("t")).longValue());

                    out.put("self-report-type", metadata.get("self-report-type"));
                    out.put("daily-activity", payload.get("daily-activity"));

                    index("self-report", "daily-activity-report", out.toString(),1);

                } else if (metadata.get("self-report-type").equals("sch-evening-inventory")) {
                    out = new JSONObject();
                    out.put("badge-id", header.get("badge-id"));
                    out.put("timestamp", ((Number) payload.get("t")).longValue());

                    out.put("self-report-type", metadata.get("self-report-type"));


                    JSONObject outgoingResponses = new JSONObject();
                    JSONArray incomingResponses = (JSONArray) payload.get("responses");

                    for (Object o : incomingResponses) {
                        JSONObject section = (JSONObject) o;

                        JSONArray questions = (JSONArray) section.get("questions");

                        for (Object q : questions) {
                            JSONObject questionObject = (JSONObject) q;

                            String question = (String) questionObject.get("question");
                            String response = "" + questionObject.get("response");

                            outgoingResponses.put(question, response);
                        }
                    }

                    out.put("responses", outgoingResponses);
                    index("self-report", "sch-evening-inventory", out.toString(),1);

                } else if (metadata.get("self-report-type").equals("sch-morning-inventory")) {
                    out = new JSONObject();
                    out.put("badge-id", header.get("badge-id"));
                    out.put("timestamp", ((Number) payload.get("t")).longValue());

                    out.put("self-report-type", metadata.get("self-report-type"));

                    JSONObject outgoingResponses = new JSONObject();
                    JSONArray incomingResponses = (JSONArray) payload.get("responses");

                    for (Object o : incomingResponses) {
                        JSONObject section = (JSONObject) o;

                        JSONArray questions = (JSONArray) section.get("questions");

                        for (Object q : questions) {
                            JSONObject questionObject = (JSONObject) q;

                            String question = (String) questionObject.get("question");
                            String response = "" + questionObject.get("response");

                            outgoingResponses.put(question, response);
                        }
                    }

                    out.put("responses", outgoingResponses);

                    index("self-report", "sch-morning-inventory", out.toString(),1);

                } else if (metadata.get("self-report-type").equals("smoking-cessation-message-rating")) {
                    out = new JSONObject();
                    out.put("badge-id", header.get("badge-id"));
                    out.put("timestamp", ((Number) payload.get("t")).longValue());

                    out.put("self-report-type", metadata.get("self-report-type"));

                    vals = (JSONArray) payload.get("vals");

                    JSONObject questionAndResponse = (JSONObject)vals.get(0);

                    String serial = (String)metadata.get("serial-number");

                    out.put("responses", questionAndResponse);
                    out.put("serial-number", serial);

                    index("smoking-cessation-message-rating", "smoking-cessation-message-rating", out.toString(),1);
                } else if (metadata.get("self-report-type").equals("stress-rating")) {

                    out = new JSONObject();
                    out.put("badge-id", header.get("badge-id"));
                    out.put("timestamp", ((Number) payload.get("t")).longValue());

                    out.put("self-report-type", "stress-rating");

                    // get stress rating, add to output
                    vals = (JSONArray) payload.get("vals");
                    JSONObject rating = (JSONObject) vals.get(0);

                    out.put("stress-rating", rating.get("stress-rating"));

                    index("self-report", "stress-rating", out.toString(),1);

                } else {
                    System.out.println("Unrecognized self report: " + obj.toJSONString());
                }
            }

            //Indexing of all analytics results
            else if (header.get("message-type").equals("analytics-result")) {

                if (metadata.containsKey("ring")) {

                    if ((long) metadata.get("ring") == 2) {

                        String analyticsType = (String) metadata.get("analytics-type");

                        out = new JSONObject();
                        out.put("badge-id", header.get("badge-id"));
                        out.put("timestamp", ((Number) payload.get("t")).longValue());

                        vals = (JSONArray) payload.get("vals");

                        for (Object o : vals) {
                            val = (JSONObject) o;
                            Object[] keys = val.keySet().toArray();
                            String key = (String) keys[0];

                            out.put(key, val.get(key));
                        }

                        System.out.println("produced experimental object: " + out.toJSONString());

                        index("analytics-" + analyticsType, analyticsType + "-result", out.toJSONString(),0);
                    }

                }
            }

            //Indexing of raw bytes
            else if (header.get("message-type").equals("raw-bytes-message")) {
                out = new JSONObject();
                out.put("badge-id", header.get("badge-id"));
                out.put("timestamp", ((Number) payload.get("t")).longValue());
                out.put("raw-bytes", payload.get("raw"));

                index("raw-bytes-data", "raw-bytes", out.toString(),0);

            }

        } catch (ParseException | NullPointerException | IllegalArgumentException e) {
            e.printStackTrace();
        }

        msg_count++;
        if((msg_count % 1000)==0){
            double time_delta = (System.currentTimeMillis()-start_time)/1000;
            double msg_rate = msg_count/time_delta;
            double byte_rate = msg_total_bytes/time_delta;
            double msg_avg_bytes = msg_total_bytes/msg_count;
            System.out.printf("ESTap -- Msg count: %d Total bytes: %d Avg Msg Size: %.1f Msgs/s: %.2f Bytes/s: %.2f Total time: %.0f\n\n",
                    msg_count,  msg_total_bytes, msg_avg_bytes, msg_rate, byte_rate, time_delta );

        }

    }

    public void index(String index, String type, String message, int priority) {

        IndexResponse response = null;
        long time = System.currentTimeMillis();
        double diff = 0;
        boolean success = false;
        int tries = 0;
        long time_delta = time - msg_index_timer;

        //queue message for bulk indexing
        bulkRequest.add(client.prepareIndex(index, type).setSource(message,XContentType.JSON));

        //If enough messages are queued, or enough time has passed, or a message hase priority 1,
        //index the current set of messages.
        if(msg_index_count>=MSG_QUEUE_LENGTH || time_delta>=MSG_QUEUE_TIMEOUT || priority==1 ){

            time = System.currentTimeMillis();

            //This could fail if ES does not index. Ignore for now...
            BulkResponse bulkResponse = bulkRequest.get();

            msg_index_timer = System.currentTimeMillis();
            time_delta = msg_index_timer - time;
            System.out.printf("ESTap -- Indexed %d messages in %d ms\n\n", msg_index_count, time_delta);

            bulkRequest = client.prepareBulk();
            msg_index_count =0;
        }
        else {
            msg_index_count++;
        }

    }

    private String getDateString() {
        return ("[" + formatter.format(new Date()) + "]");
    }
}

