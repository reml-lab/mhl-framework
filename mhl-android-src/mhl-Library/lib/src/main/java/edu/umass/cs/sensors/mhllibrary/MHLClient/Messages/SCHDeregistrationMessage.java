package edu.umass.cs.sensors.mhllibrary.MHLClient.Messages;

public class SCHDeregistrationMessage extends Message {

    public SCHDeregistrationMessage(String id, long firstTimestamp) {
        super(id, "system-message", "sch-deregistration-message", firstTimestamp);
    }
}