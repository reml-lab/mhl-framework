package edu.umass.cs.sensors.mhllibrary.MHLClient.Messages;

import org.json.JSONException;

public class StatusRecordSystemMessage extends SystemMessage {

    public StatusRecordSystemMessage(
            String badgeID,
            long firstTimestamp,
            String statusMessage) {

        super(badgeID, firstTimestamp);

        try {
            this.metadata.put("system-message-type", "status-record-message");
            this.payload.put("status", statusMessage);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
