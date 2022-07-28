package edu.umass.cs.sensors.mhllibrary.MHLClient.Messages;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SmokingCessationMessageRatingSelfReportMessage extends SelfReportMessage {

    public SmokingCessationMessageRatingSelfReportMessage(String id, long firstTimestamp, String question, String response, String serial) {
        super(id, firstTimestamp);

        try {
            metadata.put("self-report-type", "smoking-cessation-message-rating");
            metadata.put("serial", serial);

            JSONObject questionAndResponse = new JSONObject();

            questionAndResponse.put("question", question);
            questionAndResponse.put("response", response);

            JSONArray vals = new JSONArray();
            vals.put(questionAndResponse);

            payload.put("t", firstTimestamp);
            payload.put("vals", vals);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
