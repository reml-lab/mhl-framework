package edu.umass.cs.sensors.mhllibrary.MHLClient.Messages;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by erisinger on 7/5/17.
 */

public class GSRSensorMessage extends SensorMessage {

    long t;
    int resistance;

    public GSRSensorMessage(String badgeID, long t, int resistance) {
        super(badgeID, t);
        this.t = t;
        this.resistance = resistance;

        try {
            metadata.put("sensor-type", "gsr");

            JSONArray vals = new JSONArray();
            JSONObject rateVal = new JSONObject();

            rateVal.put("gsr", this.resistance);

            vals.put(rateVal);

            payload.put("t", this.t);
            payload.put("vals", vals);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
