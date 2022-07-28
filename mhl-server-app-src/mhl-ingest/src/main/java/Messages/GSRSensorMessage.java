package Messages;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

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

//        try {
            metadata.put("sensor-type", "gsr");

            JSONArray vals = new JSONArray();
            JSONObject rateVal = new JSONObject();

            rateVal.put("gsr", this.resistance);

            vals.add(rateVal);

            payload.put("t", this.t);
            payload.put("vals", vals);

//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
    }
}
