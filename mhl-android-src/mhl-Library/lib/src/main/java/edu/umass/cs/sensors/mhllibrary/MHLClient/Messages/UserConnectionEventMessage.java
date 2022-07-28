package edu.umass.cs.sensors.mhllibrary.MHLClient.Messages;

import org.json.JSONException;

/**
 * Created by erisinger on 4/18/19.
 */

public class UserConnectionEventMessage extends ConnectionEventMessage {

    public UserConnectionEventMessage(String badgeID, String study, long firstTimestamp, String startTime, String stopTime) {
        super(badgeID, firstTimestamp);

        try {
            metadata.put("connection-event-type", "connection");
            payload.put("study", study);
            payload.put("start-time", startTime);
            payload.put("stop-time", stopTime);

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
}
