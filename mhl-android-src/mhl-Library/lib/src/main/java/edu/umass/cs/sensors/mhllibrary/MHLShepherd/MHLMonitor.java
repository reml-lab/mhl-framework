package edu.umass.cs.sensors.mhllibrary.MHLShepherd;

/**
 * Created by erisinger on 1/6/19.
 */
public abstract class MHLMonitor {
    protected long timeOfLastStatus;
    protected String monitorType;
    protected MHLShepherd shepherd;

    public enum STATUS {
        NORMAL, WARNING, ERROR, UNKNOWN;
    }

    protected MHLMonitor(MHLShepherd shepherd) {
        timeOfLastStatus = System.currentTimeMillis();
        this.shepherd = shepherd;
    }

    public String getType() {
        return monitorType;
    }

    public abstract MHLMonitor.STATUS getStatus();

    public abstract String getStatusMessage();
}
