package Messages;

/**
 * Created by erisinger on 7/7/17.
 */
public class StepDetectionMessage extends AnalyticsMessage {

    public StepDetectionMessage(String id, long timestamp, double val) {
        super(id, timestamp);

        metadata.put("analytics-type", "step-detection");
        payload.put("t", timestamp);
        payload.put("val", val);
    }
}
