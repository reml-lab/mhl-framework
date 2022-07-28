package edu.umass.cs.sensors.mhllibrary.MHLClient.Messages;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by erisinger on 10/17/18.
 */

/**
 * Created by erisinger on 10/17/18.
 */

public class PPGSensorMessage extends SensorMessage {
    long t;

    public PPGSensorMessage(String badgeID, long t, JSONArray readings) {
        super(badgeID, t);
        this.t = t;

        try {
            metadata.put("sensor-type", "ppg");

            JSONArray vals = new JSONArray();
            JSONObject ppgReading = new JSONObject();

            vals.put(ppgReading);

            payload.put("t", this.t);
            payload.put("vals", readings);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
