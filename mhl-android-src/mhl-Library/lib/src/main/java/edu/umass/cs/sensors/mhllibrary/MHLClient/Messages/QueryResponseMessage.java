package edu.umass.cs.sensors.mhllibrary.MHLClient.Messages;

import org.json.JSONException;

/**
 * Created by erisinger on 10/2/18.
 */

public class QueryResponseMessage extends Message {
    public QueryResponseMessage(String id, long firstTimestamp, String query, String messageID, String responses, String response) {
        super(id, "data-message", "query-response", firstTimestamp);

        try {
            payload.put("t", firstTimestamp);
            payload.put("message-id", messageID);
            payload.put("query", query);
            payload.put("responses", responses);
            payload.put("response", response);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
