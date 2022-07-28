package edu.umass.cs.sensors.mhllibrary.MHLClient.Messages;

/**
 * Created by erisinger on 3/7/19.
 */

public class NewUserMessage extends SystemMessage {

    public NewUserMessage(String badgeID) {
        super(badgeID, System.currentTimeMillis());
    }
}