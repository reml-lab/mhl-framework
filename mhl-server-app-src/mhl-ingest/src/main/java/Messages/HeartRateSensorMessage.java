package Messages;

//import org.json.JSONArray;
//import org.json.JSONException;
//import org.json.JSONObject;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * Created by erisinger on 1/23/17.
 */

public class HeartRateSensorMessage extends SensorMessage {

    long t;
    int heartRate;

    public HeartRateSensorMessage(String badgeID, long t, int rate) {
        super(badgeID, t);
        this.t = t;
        this.heartRate = rate;

//        try {
            metadata.put("sensor-type", "heart-rate");

            JSONArray vals = new JSONArray();
            JSONObject rateVal = new JSONObject();

            rateVal.put("heart-rate", this.heartRate);

            vals.add(rateVal);

            payload.put("t", this.t);
            payload.put("vals", vals);

//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
    }
}
