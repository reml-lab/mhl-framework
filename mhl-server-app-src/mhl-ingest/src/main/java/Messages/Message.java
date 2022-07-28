package Messages;

//import org.json.JSONArray;
//import org.json.JSONException;
//import org.json.JSONObject;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

/**
 * Created by erisinger on 1/23/17.
 */

public abstract class Message {

    JSONObject message = new JSONObject();
    JSONObject header = new JSONObject();
    JSONObject metadata = new JSONObject();
    JSONObject payload = new JSONObject();
    JSONArray timestamps = new JSONArray();

    String badgeID, channel, messageType, messageVersion;

    public Message(String badgeID, String channel, String messageType, long firstTimestamp) {
        this.badgeID = badgeID;
        this.messageType = messageType;
        this.channel = channel;
        this.messageVersion = "1.0";

        JSONObject stamp  = new JSONObject();
//        try {
            stamp.put("process", "android");
            stamp.put("location", "collection");
            stamp.put("t", firstTimestamp);

            timestamps.add(stamp);

            header.put("badge-id", badgeID);
            header.put("channel", channel);
            header.put("message-type", messageType);
            header.put("message-version", messageVersion);
            header.put("timestamps", timestamps);


//        } catch (ParseException e) {
//            e.printStackTrace();
//        }
    }

    public String toJSONString() {
        return getJSONObject().toString();
    }

    public void addTimestamp(JSONObject timestamp) {

//        try {
            timestamps = (JSONArray) header.get("timestamps");
            timestamps.add(timestamp);
            header.put("timestamps", timestamps);

//        } catch (JSONException e) {
//            e.printStackTrace();
//        }

    }

    public JSONObject getJSONObject() {
//        try {
            message.put("header", header);
            message.put("metadata", metadata);
            message.put("payload", payload);
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
        return message;
    }

}
