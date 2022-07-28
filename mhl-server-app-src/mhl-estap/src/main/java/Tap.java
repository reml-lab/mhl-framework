import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by erisinger on 6/20/17.
 */
public class Tap {

    TapConsumer tapConsumer;
    TapReceiver tapReceiver;
    TapFilter tapFilter;

    Thread consumerThread;
    Thread filterThread;

    BlockingQueue<String> queue;

    /**
     * Constructor for pass-through filtering (returns all messages from the specified channels).
     * @param receiver
     * @param brokers
     * @param channels
     */

    private Tap(TapReceiver receiver, String brokers, List<String> channels) {
        this(receiver, brokers, channels, new ArrayList<String>());
    }

    /**
     * Constructor to filter messages using a list of ids.  TapFilter will return only messages from the
     * specified channels whose IDs are in the ids list.
     *
     * Currently set to private -- filtering will be a future update.
     * @param receiver
     * @param brokers
     * @param channels
     * @param ids
     */

    public Tap(TapReceiver receiver, String brokers, List<String> channels, List<String> ids) {
        this.tapReceiver = receiver;
        this.queue = new LinkedBlockingQueue<>();

        //TODO: set up TapConsumer using brokers and channels
        tapConsumer = new TapConsumer(brokers, channels);

        if (ids.size() > 0) {

            if (ids.get(0).equals("domino")) {
                System.out.println("unlocked: retrieving all data for channel(s) " + channels.toString());
                this.tapFilter = new TapFilter();
            } else {
                System.out.println("retrieving data for ID(s) " + ids.toString() + " in channel(s) " + channels.toString());
                this.tapFilter = new TapFilter(ids);
            }
        } else {
            System.out.println("must provide ID list!");
        }

        //launch consumer and filter
        filterThread = new Thread(tapFilter);
        consumerThread = new Thread(tapConsumer);
    }

    public void start() {
        filterThread.start();
        consumerThread.start();

        try {
            Thread.sleep(2000);
            System.out.println("setup complete -- safe to quit");
        } catch (InterruptedException e) {
            System.out.println("setup incomplete -- possible thread leak!");
            Thread.currentThread().interrupt();
        }
    }

    public void stop() {
        consumerThread.interrupt();
        filterThread.interrupt();

        //catch interruption before joins (below)
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {

        }

        try {
            consumerThread.join(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        try {
            filterThread.join(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * TapConsumer uses the brokers list and channels list to pull messages out of Kafka and places
     * them into a thread-safe queue.
     *
     * TapConsumer is the first stage of a two-stage process.  In the second stage, the TapFilter
     * dequeues the messages pulled by TapConsumer and filters them according to the ids list.
     */

    private class TapConsumer implements Runnable {
        KafkaConsumer<String, String> kafkaConsumer;

        public TapConsumer(String brokers, List<String> channels) {

            //set up kafka consumer
            Properties props = new Properties();

            // FIXME: why doesn't this use the passed-in bootstrap servers?
//            props.put("bootstrap.servers", "none.cs.umass.edu:9092,none.cs.umass.edu:9093,none.cs.umass.edu:9094");
            props.put("bootstrap.servers", brokers);

            props.put("group.id", "test" + System.currentTimeMillis());
            props.put("enable.auto.commit", "true");
            props.put("auto.commit.interval.ms", "1000");
            props.put("session.timeout.ms", "30000");
            props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
            props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

            kafkaConsumer = new KafkaConsumer<>(props);
            kafkaConsumer.subscribe(channels);
        }

        @Override
        public void run() {

            try {
                while (!Thread.currentThread().isInterrupted()) {

                    ConsumerRecords<String, String> records = kafkaConsumer.poll(2000);

                    if (Thread.currentThread().isInterrupted()) {
                        break;
                    }

                    for (ConsumerRecord<String, String> record : records) {

//                        System.out.println(record.value());

                        queue.put(record.value());
                    }
                }

            } catch (InterruptedException e) {

            } finally {
                System.out.print("waiting for Kafka consumer to close (may take 30 seconds)...");

                long time = System.currentTimeMillis();

                kafkaConsumer.close();

                long time2 = System.currentTimeMillis();

                System.out.println("Kafka consumer closed after " + ((time2 - time) / 1000.0) + " seconds");
            }
        }
    }

    /**
     * TapFilter dequeues the messages pulled by TapConsumer and filters them according to the ids list.  After
     * filtering, TapFilter passes the messages to the TapReceiver via the TapReceiver.receive(String) method.
     *
     * If no ids list is provided, the filter acts as a pass-through and returns all messages from the specified
     * channels.
     *
     * Left as future work for the moment.
     */

    private class TapFilter implements Runnable {

        private List<String> ids = null;

        public TapFilter() {
            //default constructor in case of no ID list (no filtering)

        }

        public TapFilter(List<String> ids) {
            this.ids = ids;
        }

        @Override
        public void run() {

            try {
                if (ids == null) {

                    //no filtering
                    while (!Thread.interrupted()) {
                        tapReceiver.receive(queue.take());
                    }

                } else {
                    JSONParser parser = new JSONParser();
                    JSONObject obj, header;

                    //filter on list of ids
                    String message, badgeID;
                    while (!Thread.interrupted()) {
                        message = queue.take();

                        //expand message into JSON object and filter on ID
                        try {
                            obj = (JSONObject)parser.parse(message);
                            header = (JSONObject)obj.get("header");
                            badgeID = (String)header.get("badge-id");

                            if (ids.contains(badgeID)) {
                                tapReceiver.receive(message);
                            }

                        } catch (ParseException e) {

                        }
                    }
                }
            } catch (InterruptedException e) {

            }
        }
    }
 }
