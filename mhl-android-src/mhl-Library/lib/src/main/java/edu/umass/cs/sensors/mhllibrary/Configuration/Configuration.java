package edu.umass.cs.sensors.mhllibrary.Configuration;

import org.json.JSONObject;
import org.json.JSONException;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import android.content.Context;

import edu.umass.cs.sensors.mhllibrary.MHLAgentCore.PreferencesAccessor;
import edu.umass.cs.sensors.mhllibrary.MHLUtilities.MHLOutputHandler;
import edu.umass.cs.sensors.mhllibrary.MHLUtilities.AndroidOutputHandler;
import edu.umass.cs.sensors.mhllibrary.Configuration.IdentityConfig;
import edu.umass.cs.sensors.mhllibrary.Configuration.NetworkConfig;
import edu.umass.cs.sensors.mhllibrary.Configuration.StudyConfig;

public class Configuration {

    public static String getConfig(Context context){
        return IdentityConfig.getCongif(context) + StudyConfig.getConfig(context)+ NetworkConfig.getConfig(context);
    }

    public static void set_from_JSONObject(Context context, JSONObject json_config){
        IdentityConfig.set_from_JSONObject(context, json_config);
        StudyConfig.set_from_JSONObject(context, json_config);


    }

}
