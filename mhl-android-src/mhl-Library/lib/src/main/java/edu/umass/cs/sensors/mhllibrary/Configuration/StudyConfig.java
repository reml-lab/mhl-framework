package edu.umass.cs.sensors.mhllibrary.Configuration;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import edu.umass.cs.sensors.mhllibrary.MHLAgentCore.PreferencesAccessor;
import edu.umass.cs.sensors.mhllibrary.MHLUtilities.AndroidOutputHandler;
import edu.umass.cs.sensors.mhllibrary.MHLUtilities.MHLOutputHandler;

public class StudyConfig {

    static String TAG = "MHLStudyConfig";

    public static Boolean set_from_JSONObject(Context context, JSONObject json_config){

        Boolean success = false;

        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
        AndroidOutputHandler outputHandler = new AndroidOutputHandler();

        try{
            if(json_config.has("study_name")){
                String study_name = json_config.getString("study-name");
                PreferencesAccessor.storePref(context, "study_name", study_name);
            }

            if(json_config.has("config-version")){
                int config_version = json_config.getInt("config-version");
                PreferencesAccessor.storePref(context, "config_version", String.valueOf(config_version));
            }

            if(json_config.has("start-date")){
                Date start_date = dateFormatter.parse(json_config.getString("start-date"));
                PreferencesAccessor.storePref(context, "start_date", dateFormatter.format(start_date));
            }

            if(json_config.has("stop-date")){
                Date stop_date = dateFormatter.parse(json_config.getString("stop-date"));
                PreferencesAccessor.storePref(context, "stop_date", dateFormatter.format(stop_date));
            }

            if(json_config.has("start-time")){
                String[] arr_start_time = json_config.getString("start-time").split(":");
                int start_time_hour = Integer.parseInt(arr_start_time[0]);
                int start_time_minute = Integer.parseInt(arr_start_time[1]);
                PreferencesAccessor.storePref(context, "start_time_hour", String.format("%02d",start_time_hour) );
                PreferencesAccessor.storePref(context, "start_time_minute", String.format("%02d",start_time_minute));
            }

            if(json_config.has("stop-time")){
                String[] arr_stop_time = json_config.getString("stop-time").split(":");
                int stop_time_hour = Integer.parseInt(arr_stop_time[0]);
                int stop_time_minute = Integer.parseInt(arr_stop_time[1]);
                PreferencesAccessor.storePref(context, "stop_time_hour", String.format("%02d",stop_time_hour) );
                PreferencesAccessor.storePref(context, "stop_time_minute",  String.format("%02d",stop_time_minute));
            }

            if(json_config.has("duty-cycle-on-interval")){
                int duty_cycle_on_interval = json_config.getInt("duty-cycle-on-interval");
                PreferencesAccessor.storePref(context, "duty_cycle_on_interval", String.valueOf(duty_cycle_on_interval));
            }

            if(json_config.has("duty-cycle-off-interval")){
                int duty_cycle_off_interval = json_config.getInt("duty-cycle-off-interval");
                PreferencesAccessor.storePref(context, "duty_cycle_off_interval", String.valueOf(duty_cycle_off_interval));
            }

            if(json_config.has("num-self-report")){
                int num_self_report = json_config.getInt("num-self-report");
                PreferencesAccessor.storePref(context, "num_self_report", String.valueOf(num_self_report));
            }
            
            success=true;
        }
        catch (JSONException e) {
            outputHandler.printMessage(TAG, "JSON object exception");
        }
        catch (ParseException e){
            outputHandler.printMessage(TAG, "Parsing exception");
        }

        return(success);
    }

    public static void setStudyName(Context context,String study_name ){
        PreferencesAccessor.storePref(context,"study_name", study_name);
    }
    public static void setConfigVersion(Context context,int config_version) {PreferencesAccessor.storePref(context,"config_version", String.valueOf(config_version)); }

    public static void setStartDate(Context context,Date start_date){
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
        PreferencesAccessor.storePref(context,"start_date", dateFormatter.format(start_date));
    }
    public static void setStartTimeHour(Context context,int start_time_hour) {PreferencesAccessor.storePref(context,"start_time_hour", String.format("%02d",start_time_hour) );}
    public static void setStartTimeMinute(Context context,int start_time_minute ) { PreferencesAccessor.storePref(context,"start_time_minute", String.format("%02d",start_time_minute));}

    public static void setStopDate(Context context,Date stop_date){
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
        PreferencesAccessor.storePref(context,"stop_date", dateFormatter.format(stop_date));
    }
    public static void setStopTimeHour(Context context,int stop_time_hour ) {PreferencesAccessor.storePref(context,"stop_time_hour", String.format("%02d",stop_time_hour) );}
    public static void setStopTimeMinute(Context context,int stop_time_minute) {PreferencesAccessor.storePref(context,"stop_time_minute",  String.format("%02d",stop_time_minute));}

    public static void setDutyCycleOnInterval(Context context,int duty_cycle_on_interval){PreferencesAccessor.storePref(context,"duty_cycle_on_interval", String.valueOf(duty_cycle_on_interval));}
    public static void setDutyCycleOffInterval(Context context,int duty_cycle_off_interval){ PreferencesAccessor.storePref(context,"duty_cycle_off_interval", String.valueOf(duty_cycle_off_interval));}
    public static void setNumSelfReport(Context context,int num_self_report){PreferencesAccessor.storePref(context,"num_self_report", String.valueOf(num_self_report));}

    public static String getStudyName(Context context){
        String study_name="";
        if(PreferencesAccessor.getStoredPref(context,"study_name")!=null){
            study_name = PreferencesAccessor.getStoredPref(context,"study_name");
        }
        return study_name;
    }

    public static  int getConfigVersion(Context context) {
        int config_version = 0;
        if(PreferencesAccessor.getStoredPref(context,"config_version")!=null){
            config_version = Integer.parseInt(PreferencesAccessor.getStoredPref(context,"config_version"));
        }
        return config_version;
    }

    public static Date getStartDate(Context context){
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
        Date start_date = new Date();
        try{
            if(PreferencesAccessor.getStoredPref(context,"start_date")!=null){
               start_date = dateFormatter.parse(PreferencesAccessor.getStoredPref(context,"start_date"));
            }
        }
        catch (ParseException e){
            AndroidOutputHandler outputHandler = new AndroidOutputHandler();
            outputHandler.printMessage(TAG, "Date parsing exception. Using default start date.");
        }
        return start_date;
    }

    public static int getStartTimeHour(Context context) {
        int start_time_hour = 8;
        if(PreferencesAccessor.getStoredPref(context,"start_time_hour")!=null){
            start_time_hour = Integer.parseInt(PreferencesAccessor.getStoredPref(context,"start_time_hour"));
        }
        return start_time_hour;
    }

    public static  int getStartTimeMinute(Context context) {
        int  start_time_minute = 0;
        if(PreferencesAccessor.getStoredPref(context,"start_time_minute")!=null){
            start_time_minute = Integer.parseInt(PreferencesAccessor.getStoredPref(context,"start_time_minute"));
        }
        return start_time_minute;
    }

    public static  Date getStopDate(Context context){
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
        Date stop_date = new Date();
        try{
            if(PreferencesAccessor.getStoredPref(context,"stop_date")!=null){
                stop_date = dateFormatter.parse(PreferencesAccessor.getStoredPref(context,"stop_date"));
            }
        }
            catch (ParseException e){
            AndroidOutputHandler outputHandler = new AndroidOutputHandler();
            outputHandler.printMessage(TAG, "Parsing exception. Using defualt start date.");
        }
        return stop_date;
    }

    public static  int getStopTimeHour(Context context) {
        int stop_time_hour = 23;
        if(PreferencesAccessor.getStoredPref(context,"stop_time_hour")!=null){
            stop_time_hour =Integer.parseInt(PreferencesAccessor.getStoredPref(context,"stop_time_hour"));
        }
        return stop_time_hour;
    }

    public static  int getStopTimeMinute(Context context) {
        int  stop_time_minute=59;
        if(PreferencesAccessor.getStoredPref(context,"stop_time_minute")!=null){
            stop_time_minute=Integer.parseInt(PreferencesAccessor.getStoredPref(context,"stop_time_minute"));
        }
        return stop_time_minute;
    }

    public static  int getDutyCycleOnInterval(Context context){
        int duty_cycle_on_interval=15;
        if(PreferencesAccessor.getStoredPref(context,"duty_cycle_on_interval")!=null){
            duty_cycle_on_interval=Integer.parseInt(PreferencesAccessor.getStoredPref(context,"duty_cycle_on_interval"));
        }
        return duty_cycle_on_interval;
    }

    public static  int getDutyCycleOffInterval(Context context){
        int  duty_cycle_off_interval=15;
        if(PreferencesAccessor.getStoredPref(context,"duty_cycle_off_interval")!=null){
            duty_cycle_off_interval=Integer.parseInt(PreferencesAccessor.getStoredPref(context,"duty_cycle_off_interval"));
        }
        return duty_cycle_off_interval;
    }

    public static  int getNumSelfReport(Context context){
        int  num_self_report=0;
        if(PreferencesAccessor.getStoredPref(context,"num_self_report")!=null){
            num_self_report=Integer.parseInt(PreferencesAccessor.getStoredPref(context,"num_self_report"));
        }
        return num_self_report;
    }
    
    public static String getConfig(Context context){
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
        String out = "  MHLab Study Configuration:\n";
        out += "    study_name: " + getStudyName(context) + "\n";
        out += "    config_version: " + getConfigVersion(context) + "\n";
        out += "    start_date: " + dateFormatter.format(getStartDate(context)) + "\n";
        out += "    stop_date: " + dateFormatter.format(getStopDate(context)) + "\n";
        out += "    start_time: " + String.format("%02d",getStartTimeHour(context)) + ":" + String.format("%02d",getStartTimeMinute(context)) +  "\n";
        out += "    stop_time: " + String.format("%02d",getStopTimeHour(context)) + ":" + String.format("%02d",getStopTimeMinute(context)) + "\n";
        out += "    duty_cycle_on_interval: " + getDutyCycleOnInterval(context) + "\n";
        out += "    duty_cycle_off_interval: " +getDutyCycleOffInterval(context) + "\n";
        out += "    num_self_report: " +getNumSelfReport(context) + "\n";
        return out;
    }

}
