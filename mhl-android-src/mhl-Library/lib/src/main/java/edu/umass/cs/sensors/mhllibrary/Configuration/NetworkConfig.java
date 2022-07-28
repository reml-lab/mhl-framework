package edu.umass.cs.sensors.mhllibrary.Configuration;

import android.content.Context;
import android.content.res.TypedArray;

import edu.umass.cs.sensors.mhllibrary.MHLAgentCore.PreferencesAccessor;
import edu.umass.cs.sensors.mhllibrary.MHLUtilities.AndroidOutputHandler;
import edu.umass.cs.sensors.mhllibrary.MHLUtilities.MHLOutputHandler;
import edu.umass.cs.sensors.mhllibrary.R;

public class NetworkConfig{

    public static void set_from_values(Context context, String server_ip, int secure_port, int insecure_port){
        PreferencesAccessor.storePref(context,"server_ip", server_ip);
        PreferencesAccessor.storePref(context,"secure_port", String.valueOf(secure_port));
        PreferencesAccessor.storePref(context,"insecure_port", String.valueOf(insecure_port));
    }


    public static String getServerIP(Context context) {
        if(PreferencesAccessor.getStoredPref(context,"server_ip")!=null){
            return PreferencesAccessor.getStoredPref(context,"server_ip");
        }
        else {
            String default_server_ip = context.getString(R.string.server_ip);
            return default_server_ip;
        }
    }

    public static int getInsecurePort(Context context) {
        if(PreferencesAccessor.getStoredPref(context,"insecure_port")!=null) {
            return Integer.parseInt(PreferencesAccessor.getStoredPref(context,"insecure_port"));
        }
        else{
            int default_insecure_port =  Integer.parseInt(context.getString(R.string.secure_port));
            return default_insecure_port;
        }
    }

    public static int getSecurePort(Context context) {
        if(PreferencesAccessor.getStoredPref(context,"secure_port")!=null) {
            return Integer.parseInt(PreferencesAccessor.getStoredPref(context,"secure_port"));
        }
        else{
            int default_secure_port =  Integer.parseInt(context.getString(R.string.secure_port));
            return default_secure_port;
        }
    }

    public static long[] getBackoffSchedule(Context context) {

        int[] xmlArray = context.getResources().getIntArray(R.array.backoff_schedule);
        long[] backoffSchedule = new long[xmlArray.length];

        for (int i = 0; i < xmlArray.length; i++) {
            backoffSchedule[i] = (long)xmlArray[i];
        }

        return backoffSchedule;
    }

    public static String getConfig(Context context){
        String out = "  MHLab Network Configuration:\n";
        out += "    server_ip: " + getServerIP(context) + "\n";
        out += "    insecure_port: " + getInsecurePort(context) + "\n";
        out += "    secure_port: " +getSecurePort(context) + "\n";
        return out;
    }


}
