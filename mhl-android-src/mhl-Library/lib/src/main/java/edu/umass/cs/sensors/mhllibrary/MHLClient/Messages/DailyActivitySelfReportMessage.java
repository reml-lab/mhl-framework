package edu.umass.cs.sensors.mhllibrary.MHLClient.Messages;

import org.json.JSONException;

/**
 * Created by erisinger on 1/3/18.
 */

public class DailyActivitySelfReportMessage extends SelfReportMessage {

    public DailyActivitySelfReportMessage(String badgeID, long timestamp, String dailyActivity) {
        super(badgeID, timestamp);

        try {
            metadata.put("self-report-type", "daily-activity-report");
            payload.put("t", timestamp);

            payload.put("daily-activity", dailyActivity);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
