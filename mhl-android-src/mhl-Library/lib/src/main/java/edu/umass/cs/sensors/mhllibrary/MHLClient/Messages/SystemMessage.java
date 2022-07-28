package edu.umass.cs.sensors.mhllibrary.MHLClient.Messages;

/**
 * Created by erisinger on 3/7/19.
 */

public abstract class SystemMessage extends Message {

//    public SystemMessage(String badgeID, long firstTimestamp, String messageType) {
//        super(badgeID, "system-message", messageType, firstTimestamp);
//    }

    public SystemMessage(String badgeID, long firstTimestamp) {
        super(badgeID, "system-message", "system-message", firstTimestamp);
    }
}
