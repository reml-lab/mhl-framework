package edu.umass.cs.sensors.mhllibrary.MHLUtilities;

import android.util.Log;

/**
 * Created by erisinger on 1/8/19.
 */
public class AndroidOutputHandler implements MHLOutputHandler {

    public void handleException(Exception e) {
        e.printStackTrace();
    }

    public void printMessage(String tag, String message) {
        Log.i(tag, message);
    }
}
