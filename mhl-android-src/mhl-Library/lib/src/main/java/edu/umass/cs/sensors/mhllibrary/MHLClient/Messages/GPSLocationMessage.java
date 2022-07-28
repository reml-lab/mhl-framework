package edu.umass.cs.sensors.mhllibrary.MHLClient.Messages;

import android.location.Location;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by erisinger on 8/14/17.
 */

public class GPSLocationMessage extends SensorMessage {

    double latitude, longitude, altitude;
    long timestamp;

    public GPSLocationMessage(String badgeID, Location location) {
        super(badgeID, location.getTime());
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        altitude = location.getAltitude();
        timestamp = location.getTime();

        try {
            metadata.put("sensor-type", "gps");

            JSONArray vals = new JSONArray();
            JSONObject lat = new JSONObject();
            JSONObject lon = new JSONObject();
            JSONObject alt = new JSONObject();

            lat.put("lat", this.latitude);
            lon.put("lon", this.longitude);
            alt.put("alt", this.altitude);

            vals.put(lat);
            vals.put(lon);
            vals.put(alt);

            payload.put("t", this.timestamp);
            payload.put("vals", vals);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


}
