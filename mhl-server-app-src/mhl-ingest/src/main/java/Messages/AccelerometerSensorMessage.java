package Messages;

//import org.json.JSONArray;
//import org.json.JSONException;
//import org.json.JSONObject;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

//import com.oracle.javafx.jmx.json.JSONException;
//import scala.util.parsing.json.JSONArray;
//import scala.util.parsing.json.JSONObject;

/**
 * Created by erisinger on 1/23/17.
 */

public class AccelerometerSensorMessage extends SensorMessage {

    long t;
    double x, y, z;

    public AccelerometerSensorMessage(String badgeID, long t, double x, double y, double z) {
        super(badgeID, t);
        this.t = t;
        this.x = x;
        this.y = y;
        this.z = z;

//        try {
            metadata.put("sensor-type", "accelerometer");

            JSONArray vals = new JSONArray();
            JSONObject xVal = new JSONObject();
            JSONObject yVal = new JSONObject();
            JSONObject zVal = new JSONObject();

            xVal.put("x", this.x);
            yVal.put("y", this.y);
            zVal.put("z", this.z);

            vals.add(xVal);
            vals.add(yVal);
            vals.add(zVal);

            payload.put("t", this.t);
            payload.put("vals", vals);

//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
    }

}
