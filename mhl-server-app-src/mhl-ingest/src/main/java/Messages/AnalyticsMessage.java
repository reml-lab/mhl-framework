package Messages;

/**
 * Created by erisinger on 7/7/17.
 */
public abstract class AnalyticsMessage extends Message {

    public AnalyticsMessage(String id, long timestamp) {
        super(id, "analytics-message", "analytics-result", timestamp);
    }
}
