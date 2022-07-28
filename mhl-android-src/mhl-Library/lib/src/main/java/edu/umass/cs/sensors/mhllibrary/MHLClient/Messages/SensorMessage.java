package edu.umass.cs.sensors.mhllibrary.MHLClient.Messages;

/**
 * Created by erisinger on 1/23/17.
 */

public abstract class SensorMessage extends Message {

    public SensorMessage(String badgeID, long firstTimestamp) {
        super(badgeID, "data-message", "sensor-message", firstTimestamp);
    }
}
