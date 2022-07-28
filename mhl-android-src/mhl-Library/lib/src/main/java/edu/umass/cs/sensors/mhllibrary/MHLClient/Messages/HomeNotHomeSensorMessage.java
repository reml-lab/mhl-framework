package edu.umass.cs.sensors.mhllibrary.MHLClient.Messages;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by erisinger on 3/25/19.
 */

public class HomeNotHomeSensorMessage extends SensorMessage {

    public HomeNotHomeSensorMessage(String badgeID, long firstTimeStamp, int homeNotHome) {
        super(badgeID, firstTimeStamp);

        try {
            // flesh out metadata
            metadata.put("sensor-type", "home-not-home");

            //flesh out payload
            JSONObject obj = new JSONObject();
            obj.put("home", homeNotHome);

            JSONArray vals = new JSONArray();
            vals.put(obj);

            payload.put("t", firstTimeStamp);
            payload.put("vals", vals);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
