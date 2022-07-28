package edu.umass.cs.sensors.mhllibrary.MHLAgentCore;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.EditText;

/**
 * Created by erisinger on 2/11/19.
 */

public class PreferencesAccessor {

    public static String getStoredPref(Context context, String name) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("prefs", 0);

        if (sharedPreferences.contains(name)) {
            return sharedPreferences.getString(name, "");
        } else {
            //System.out.println("PREFS: Unknown preference:" + name);
            return null;
        }
    }

    public static void storePref(Context context, String name, String pref) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("prefs", 0);
        SharedPreferences.Editor ed = sharedPreferences.edit();
        ed.putString(name, pref);
        ed.commit();
    }

    public static void deletePref(Context context, String name) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("prefs", 0);
        SharedPreferences.Editor ed = sharedPreferences.edit();
        ed.remove(name);
        ed.commit();
    }

    public static void clearAllStoredPrefs(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("prefs", 0);
        sharedPreferences.edit().clear().commit();
    }
}
