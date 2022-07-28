package edu.umass.cs.sensors.mhllibrary.MHLClient.Messages;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SCHEveningInventorySelfReportMessage extends SelfReportMessage {

    public SCHEveningInventorySelfReportMessage(String id, long firstTimestamp, JSONObject inventory) {
        super(id, firstTimestamp);

        try {
            metadata.put("self-report-type", "sch-evening-inventory");

            JSONArray incomingPayload = inventory.getJSONArray("payload");
            payload.put("t", firstTimestamp);
            payload.put("responses", incomingPayload);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
