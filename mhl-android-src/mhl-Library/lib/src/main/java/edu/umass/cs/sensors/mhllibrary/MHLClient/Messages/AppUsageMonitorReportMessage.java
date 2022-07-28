package edu.umass.cs.sensors.mhllibrary.MHLClient.Messages;

import org.json.JSONException;

/**
 * Created by erisinger on 9/11/17.
 */

public class AppUsageMonitorReportMessage extends MonitorReportMessage {

    public AppUsageMonitorReportMessage(String badgeID, long timestamp, int packageCode, int eventType) {
        super(badgeID, timestamp);

        try {
            metadata.put("monitor-type", "app-usage-monitor");
            payload.put("t", timestamp);

            payload.put("package", packageCode);
            payload.put("event-type", eventType);

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
}
