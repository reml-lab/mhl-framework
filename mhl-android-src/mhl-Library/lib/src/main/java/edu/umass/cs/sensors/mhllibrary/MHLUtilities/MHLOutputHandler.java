package edu.umass.cs.sensors.mhllibrary.MHLUtilities;

/**
 * Created by erisinger on 1/8/19.
 */
public interface MHLOutputHandler {

    public void printMessage(String tag, String message);

    public void handleException(Exception e);
}
