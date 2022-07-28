package edu.umass.cs.sensors.mhllibrary.MHLClient.Messages;

/**
 * Created by erisinger on 4/8/19.
 */

public abstract class ConnectionEventMessage extends Message {

    public ConnectionEventMessage(String badgeID, long firstTimestamp) {
        super(badgeID, "system-message", "connection-event-message", firstTimestamp);
    }
}
