package edu.umass.cs.sensors.mhllibrary.MHLShepherd;

import android.util.Log;

import java.util.ArrayList;

/**
 * Created by erisinger on 1/6/19.
 */
public class DummyShepherd implements MHLShepherd {
    private ArrayList<MHLMonitor> monitors;

    final String TAG = "DummyShepherd";

    public DummyShepherd() {
        monitors = new ArrayList<>();
    }

    public void addMonitor(MHLMonitor monitor) {
        monitors.add(monitor);

        Log.i(TAG, "added connectivityMonitor: " + monitor.getType());
    }

    public void removeMonitor(MHLMonitor monitor) {
        monitors.remove(monitor);

        Log.i(TAG, "removed connectivityMonitor: " + monitor.getType());
    }

    public void notifyStatusChange(MHLMonitor monitor) {
        Log.i(TAG, "notified of status change for " + monitor.getType() + ": " + monitor.getStatusMessage());
    }

    public void panic(MHLMonitor monitor, String panicMessage) {
        Log.i(TAG, "shepherd received panic message from connectivityMonitor " + monitor.getType() + ":\n" + panicMessage);
    }
}
