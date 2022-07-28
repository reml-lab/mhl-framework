package edu.umass.cs.sensors.mhllibrary.MHLShepherd;

/**
 * Created by erisinger on 1/6/19.
 */
public interface MHLShepherd {

    public void addMonitor(MHLMonitor monitor);

    public void removeMonitor(MHLMonitor monitor);

    public void notifyStatusChange(MHLMonitor monitor);

    public void panic(MHLMonitor monitor, String panicMessage);

}
