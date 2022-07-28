package edu.umass.cs.sensors.mhllibrary.MHLClient.Messages;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SCHMorningInventorySelfReportMessage extends SelfReportMessage {

    public SCHMorningInventorySelfReportMessage(String id, long firstTimestamp, JSONObject inventory) {
        super(id, firstTimestamp);

        try {
            metadata.put("self-report-type", "sch-morning-inventory");

            JSONArray incomingPayload = inventory.getJSONArray("payload");
            payload.put("t", firstTimestamp);
            payload.put("responses", incomingPayload);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
