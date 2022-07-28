package edu.umass.cs.sensors.mhllibrary.MHLClient.Messages;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class TimeZoneSensorMessage extends SensorMessage {
    long t;

    public TimeZoneSensorMessage(String badgeID, long t, String timezone) {
        super(badgeID, t);
        this.t = t;

        try {
            metadata.put("sensor-type", "tz");

            JSONObject tzinfo = new JSONObject();
            tzinfo.put("tz", timezone);
            JSONArray vals = new JSONArray();
            vals.put(tzinfo);

            payload.put("t", t);
            payload.put("vals", vals);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}

