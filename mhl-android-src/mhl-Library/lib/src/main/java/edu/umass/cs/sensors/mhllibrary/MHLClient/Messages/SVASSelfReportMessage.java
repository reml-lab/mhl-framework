package edu.umass.cs.sensors.mhllibrary.MHLClient.Messages;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by erisinger on 12/5/18.
 */

public class SVASSelfReportMessage extends SelfReportMessage {

    String TAG = "SVASSelfReportMessage";

    public SVASSelfReportMessage(String badgeID, long t, int stressRating, String serial) {
        super(badgeID, t);

        try {
            metadata.put("self-report-type", "svas");
            metadata.put("serial", serial);

            JSONObject stressVal = new JSONObject();
            stressVal.put("svas", stressRating);

            JSONArray arr = new JSONArray();
            arr.put(stressVal);

            payload.put("vals", arr);
            payload.put("t", t);

        } catch (JSONException e) {
            Log.d(TAG, e.getMessage());
        }
    }
}
