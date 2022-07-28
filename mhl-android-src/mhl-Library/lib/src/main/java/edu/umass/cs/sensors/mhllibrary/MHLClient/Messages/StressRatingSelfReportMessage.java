package edu.umass.cs.sensors.mhllibrary.MHLClient.Messages;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by erisinger on 1/28/19.
 */

public class StressRatingSelfReportMessage extends SelfReportMessage {

    String TAG = "StressRatingSelfReportMessage";

    public StressRatingSelfReportMessage(String badgeID, long t, int stressRating, String serial) {
        super(badgeID, t);

        try {
            metadata.put("self-report-type", "stress-rating");
            metadata.put("serial", serial);

            JSONObject stressVal = new JSONObject();
            stressVal.put("stress-rating", stressRating);

            JSONArray arr = new JSONArray();
            arr.put(stressVal);

            payload.put("vals", arr);
            payload.put("t", t);

        } catch (JSONException e) {
            Log.d(TAG, e.getMessage());
        }
    }
}
