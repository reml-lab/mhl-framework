package Messages;

/**
 * Created by erisinger on 4/11/17.
 */

public abstract class MonitorReportMessage extends Message {

    public MonitorReportMessage(String id, long firstTimestamp) {
        super(id, "system-message", "monitor-report", firstTimestamp);
    }
}
