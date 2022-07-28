package edu.umass.cs.sensors.mhllibrary.MHLClient.Messages;

import org.json.JSONException;

/**
 * Created by erisinger on 7/5/17.
 */

public class BandMonitorReportMessage extends MonitorReportMessage {

    public BandMonitorReportMessage(String badgeID, long timestamp, int wearing) {
        super(badgeID, timestamp);

        try {
            metadata.put("monitor-type", "band-monitor");
            payload.put("t", timestamp);

            // 1 = connected, wearing; 0 = connected, not wearing; -1 = not connected
            payload.put("wearing", wearing);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
