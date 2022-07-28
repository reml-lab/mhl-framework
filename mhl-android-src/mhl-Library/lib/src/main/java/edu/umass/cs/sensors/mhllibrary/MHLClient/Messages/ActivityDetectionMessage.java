package edu.umass.cs.sensors.mhllibrary.MHLClient.Messages;

import com.google.android.gms.location.ActivityRecognition;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by erisinger on 3/25/19.
 */

public class ActivityDetectionMessage extends SensorMessage {

    public ActivityDetectionMessage(String badgeID, long firstTimestamp, int detectedActivity, double confidence) {
        super(badgeID, firstTimestamp);

        try {
            // update metadata
            metadata.put("sensor-type", "activity-detection");

            // update payload
            JSONObject offsetObj = new JSONObject(); // empty offset data to please classifier
            offsetObj.put("offset", 0);

            JSONObject actObj = new JSONObject();
            actObj.put("detected-activity", detectedActivity);

            JSONObject confObj = new JSONObject();
            confObj.put("confidence", confidence);

            JSONArray vals = new JSONArray();
            vals.put(offsetObj);
            vals.put(actObj);
            vals.put(confObj);

            payload.put("t", firstTimestamp);
            payload.put("vals", vals);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
