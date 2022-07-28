package edu.umass.cs.sensors.mhllibrary.Configuration;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import edu.umass.cs.sensors.mhllibrary.MHLAgentCore.PreferencesAccessor;
import edu.umass.cs.sensors.mhllibrary.MHLUtilities.AndroidOutputHandler;
import edu.umass.cs.sensors.mhllibrary.MHLUtilities.MHLOutputHandler;

public class IdentityConfig{
    static  String TAG = "MHLIdentityConfig";


    public static Boolean set_from_JSONObject(Context context, JSONObject json_config){
        Boolean success = false;
        try{
            PreferencesAccessor.storePref(context,"badgeID", json_config.getString("badge-id"));
            PreferencesAccessor.storePref(context,"research_token", json_config.getString("research-token"));
        }
        catch (JSONException e) {
            AndroidOutputHandler outputHandler = new AndroidOutputHandler();
            outputHandler.printMessage(TAG, "JSON object exception");
        }
        return(success);
    }
    
    public static void setResearchToken(Context context,String research_token){
        PreferencesAccessor.storePref(context,"research_token", research_token);
    }

    public static void setBadgeID(Context context,String badgeID){
        PreferencesAccessor.storePref(context,"badgeID", badgeID);
    }

    
    public static String getBadgeID(Context context) {
        if( PreferencesAccessor.getStoredPref(context,"badgeID")!=null) {
            return PreferencesAccessor.getStoredPref(context,"badgeID");
        }
        else{
            return "";
        }
    }

    public static String getResearchToken(Context context) {
        if(PreferencesAccessor.getStoredPref(context,"research_token")!=null) {
            return  PreferencesAccessor.getStoredPref(context,"research_token");
        }
        else{
            return "";
        }
    }

    public static  Double getHomeLat(Context context) {
        if (PreferencesAccessor.getStoredPref(context,"home_lat") != null) {
            return Double.parseDouble(PreferencesAccessor.getStoredPref(context,"home_lat"));
        } else {
            return 0.;
        }
    }

    public static Double getHomeLon(Context context) {
        if (PreferencesAccessor.getStoredPref(context,"home_lon") != null) {
            return Double.parseDouble(PreferencesAccessor.getStoredPref(context,"home_lon"));
        } else {
            return 0.0;
        }
    }

    public static void setHomeLocation(Context context,double lat, double lon){
        AndroidOutputHandler outputHandler = new AndroidOutputHandler();
        outputHandler.printMessage(TAG, "Setting home location to: " + lat  + "," + lon);
        PreferencesAccessor.storePref(context,"home_lat",String.valueOf(lat));
        PreferencesAccessor.storePref(context,"home_lon",String.valueOf(lon));
    }
    
    public static String getCongif(Context context){
        String out = "  MHLab Identity Configuration:\n";
        out += "    research_token: " + getResearchToken(context) + "\n";
        out += "    badgeID: " + getBadgeID(context) + "\n";
        out += "    Home Lat:" + getHomeLat(context) + "\n";
        out += "    Home Lon:" + getHomeLon(context) + "\n";

        return out;
    }

}

