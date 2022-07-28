package edu.umass.cs.sensors.mhllibrary.MHLClient.Messages;

/**
 * Created by erisinger on 1/3/18.
 */

public abstract class SelfReportMessage extends Message {

    public SelfReportMessage(String id, long firstTimestamp) {
        super(id, "data-message", "self-report", firstTimestamp);
    }

}
