package edu.umass.cs.sensors.mhllibrary.MHLAgentCore;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
//import android.icu.util.Calendar;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import android.content.res.Resources;

import java.util.Calendar;
import java.util.Date;
import java.lang.Math;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import edu.umass.cs.sensors.mhllibrary.Configuration.Configuration;
import edu.umass.cs.sensors.mhllibrary.Configuration.IdentityConfig;
import edu.umass.cs.sensors.mhllibrary.Configuration.StudyConfig;
import edu.umass.cs.sensors.mhllibrary.Configuration.NetworkConfig;

import edu.umass.cs.sensors.mhllibrary.R;

import org.json.JSONArray;
import org.json.JSONException;

//import androidx.work.ListenableWorker;
import edu.umass.cs.sensors.mhllibrary.Configuration.Configuration;
import edu.umass.cs.sensors.mhllibrary.MHLState.MHLEvent;
import edu.umass.cs.sensors.mhllibrary.MHLUtilities.AndroidOutputHandler;
import edu.umass.cs.sensors.mhllibrary.MHLUtilities.MHLOutputHandler;
import edu.umass.cs.sensors.mhllibrary.MHLState.MHLState;

public class StateOracle {

    protected static String TAG = "StateOracle";
    protected static long MINIMUM_DATA_DURATION = 10 * 60 * 1000;
    
    public static long currentTimeMillis(Context context){
        return System.currentTimeMillis();
    }

    public static boolean getBluetoothAvailability(Context context) {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        boolean bluetoothAvailable = mBluetoothAdapter != null && mBluetoothAdapter.isEnabled();

        return bluetoothAvailable;
    }

    public static Date addDay(Date dt){
        Calendar c = Calendar.getInstance();
        c.setTime(dt);
        c.add(Calendar.DATE, 1);
        Date dt_new = c.getTime();
        return(dt_new);
    }

    public static boolean getNetworkAvailability(Context context) {
        //check if we have network connectivity
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        return isConnected;
    }

    public static boolean isRegistrationDay(Context context) {
        return withinStudyPeriod(context) && getDayNumber(context) == 0;
    }

    public static  boolean isDataCollectionDay(Context context) {
        return withinStudyPeriod(context) && !isRegistrationDay(context);
    }

    public static long getStartDateTimeMillis(Context context) {

        Date start_date = StudyConfig.getStartDate(context);
        int start_hour = StudyConfig.getStartTimeHour(context);
        int start_minute = StudyConfig.getStartTimeMinute(context);

        Date startDateTime = new  Date(start_date.getYear(), start_date.getMonth(), start_date.getDate(), start_hour, start_minute);

        return startDateTime.getTime();
    }

    public static  long getStartDateMillis(Context context) {

        Date start_date = StudyConfig.getStartDate(context);

        return start_date.getTime();
    }

    public static  long getTodayStartDateTimeMillis(Context context) {

        Date today = new Date();
        int start_hour = StudyConfig.getStartTimeHour(context);
        int start_minute = StudyConfig.getStartTimeMinute(context);

        Date startDateTime = new  Date(today.getYear(), today.getMonth(), today.getDate(), start_hour, start_minute);

        return startDateTime.getTime();
    }


    public static long getStopDateTimeMillis(Context context) {

        Date stopDateTime;
        Date stop_date = StudyConfig.getStopDate(context);
        int stop_hour = StudyConfig.getStopTimeHour(context);
        int stop_minute = StudyConfig.getStopTimeMinute(context);
        int start_hour = StudyConfig.getStartTimeHour(context);

        if(stop_hour < start_hour){
            stopDateTime = new Date(stop_date.getYear(), stop_date.getMonth(), stop_date.getDate(), stop_hour, stop_minute);
            stopDateTime = addDay(stopDateTime);
        }
        else {
            stopDateTime = new Date(stop_date.getYear(), stop_date.getMonth(), stop_date.getDate(), stop_hour, stop_minute);
        }

        return stopDateTime.getTime();
    }

    public static long getStopDateMillis(Context context) {

        //Dat of the start of the last study day.
        //Study day could end on the next calendar day
        Date stop_date = StudyConfig.getStopDate(context);

        return stop_date.getTime();
    }

    public static long getTodayStopDateTimeMillis(Context context) {

        Date stopDateTime;
        Date today = new Date();
        int stop_hour = StudyConfig.getStopTimeHour(context);
        int stop_minute = StudyConfig.getStopTimeMinute(context);
        int start_hour = StudyConfig.getStartTimeHour(context);

        if(stop_hour < start_hour){
            stopDateTime = new  Date(today.getYear(), today.getMonth(), today.getDate(), stop_hour, stop_minute);
            stopDateTime = addDay(stopDateTime);
        }
        else {
            stopDateTime = new  Date(today.getYear(), today.getMonth(), today.getDate(), stop_hour, stop_minute);
        }

        return stopDateTime.getTime();
    }

    //Get the number of millis to to end of the study day
    public static  long getMillisToTodayStopDateTime(Context context){
        long todayStopDateTimeMillis = getTodayStopDateTimeMillis(context);
        long now = currentTimeMillis(context);

        if(todayStopDateTimeMillis < now){
            return 0L;
        }
        else{
            return todayStopDateTimeMillis - now;
        }
    }

    public static  long getEveningSurveyNotificationStartTime(Context context){
        return getTodayStopDateTimeMillis(context) - 1000L*60*60;
    }

    public static long getMorningSurveyNotificationEndTime(Context context){
        return getTodayStartDateTimeMillis(context) + 1000L*60*60;
    }

    public static boolean eligibleForEveningSurveyNotification(Context context){

        boolean afterEveningSurveyNotificationStartTime = currentTimeMillis(context) > getEveningSurveyNotificationStartTime(context);
        boolean withinStudyPeriod = withinStudyPeriod(context);
        boolean withinDailyDataCollectionPeriod = withinDailyDataCollectionPeriod(context);
        boolean notificationSent = MHLState.getEventOccurred(context,MHLEvent.EVENING_SURVEY_NOTIFICATION,getDayNumber(context));
        boolean alreadyEntered = MHLState.getEventOccurred(context,MHLEvent.EVENING_SURVEY_ENTERED,getDayNumber(context));

        return (withinStudyPeriod &&
                withinDailyDataCollectionPeriod &&
                afterEveningSurveyNotificationStartTime &&
                (!notificationSent) &&
                (!alreadyEntered));
    }

    public static boolean eligibleForMorningSurveyNotification(Context context){

        boolean beforeMorningSurveyNotificationEndTime = getMorningSurveyNotificationEndTime(context)>currentTimeMillis(context);
        boolean withinStudyPeriod = withinStudyPeriod(context);
        boolean withinDailyDataCollectionPeriod = withinDailyDataCollectionPeriod(context);
        boolean notificationSent = MHLState.getEventOccurred(context,MHLEvent.MORNING_SURVEY_NOTIFICATION,getDayNumber(context));
        boolean alreadyEntered = MHLState.getEventOccurred(context,MHLEvent.MORNING_SURVEY_ENTERED,getDayNumber(context));

        return (withinStudyPeriod &&
                withinDailyDataCollectionPeriod &&
                beforeMorningSurveyNotificationEndTime &&
                (!notificationSent) &&
                (!alreadyEntered));
    }



    //Get the number of millis from start of study day
    public static long getMillisFromTodayStartDateTime(Context context){
        long todayStartDateTimeMillis = getTodayStartDateTimeMillis(context);
        long now = currentTimeMillis(context);

        if(todayStartDateTimeMillis > now){
            return 0L;
        }
        else{
            return now - todayStartDateTimeMillis;
        }

    }



    //Check if we are within the study period from start date/time to end date/time
    public static boolean withinStudyPeriod(Context context) {

        long now = currentTimeMillis(context);
        long start = getStartDateMillis(context); //Start for day 0 is midnight of first day
        long stop = getStopDateTimeMillis(context); //Last time is last minute of last day

        if (now >= start && now <= stop) {
            return true;
        } else {
            return false;
        }
    }

    //Check if we are before the study period
    public static boolean beforeStudyPeriod(Context context) {

        long now = currentTimeMillis(context);
        long start = getStartDateMillis(context); //Start for day 0 is midnight of first day
        long stop = getStopDateTimeMillis(context); //Last time is last minute of last day

        if (start>now ) {
            return true;
        } else {
            return false;
        }
    }

    //Check if we are within the study period from start date/time to end date/time
    public static  boolean afterStudyPeriod(Context context) {

        long now = currentTimeMillis(context);
        long start = getStartDateMillis(context); //Start for day 0 is midnight of first day
        long stop = getStopDateTimeMillis(context); //Last time is last minute of last day

        if (stop<now ) {
            return true;
        } else {
            return false;
        }
    }

    //Check if we are within the data collection period for today from start hour/min to stop hour/min
    public static boolean withinDailyDataCollectionPeriod(Context context) {

        long start = getTodayStartDateTimeMillis(context);
        long stop = getTodayStopDateTimeMillis(context);
        long now = currentTimeMillis(context);

        if (now >= start && now <= stop) {
            return true;
        } else {
            return false;
        }
    }

    public static int getDailyDataCollectionPeriodLengthMins(Context context) {

        int start_hour = StudyConfig.getStartTimeHour(context);
        int start_minute = StudyConfig.getStartTimeMinute(context);
        int stop_hour = StudyConfig.getStopTimeHour(context);
        int stop_minute = StudyConfig.getStopTimeMinute(context);

        if(stop_hour<start_hour){
            return (60*(24+stop_hour) + stop_minute - 60*start_hour - start_minute);
        }
        else{
            return (60*stop_hour + stop_minute - 60*start_hour - start_minute);
        }


    }

    public static int getCycleLength(Context context){
        int duty_cycle_on_millis = StudyConfig.getDutyCycleOnInterval(context) * 1000 * 60;
        int duty_cycle_off_millis = StudyConfig.getDutyCycleOffInterval(context) * 1000 * 60;
        int duty_cycle_length_millis = duty_cycle_on_millis + duty_cycle_off_millis;

        return duty_cycle_length_millis;
    }

    public static long millisSinceDataCollectionPeriodStart(Context context){

        long start = getTodayStartDateTimeMillis(context);
        long now = currentTimeMillis(context);

        long duty_cycle_length_millis = (long)getCycleLength(context);

        long millis_since_period_start = (now - start)%duty_cycle_length_millis;

        return millis_since_period_start;
    }

    public static boolean withinDataCollectionCycle(Context context) {

        if (withinDailyDataCollectionPeriod(context) && getDayNumber(context) > 0) {

            long duty_cycle_on_millis = StudyConfig.getDutyCycleOnInterval(context) * 1000 * 60;

            if (millisSinceDataCollectionPeriodStart(context) <= duty_cycle_on_millis) {
                return true;
            } else {
                return false;
            }

        }
        else{
            return false;
        }
    }

    public static int getCycleIndex(Context context){
        long cycle = -1;
        if (withinDailyDataCollectionPeriod(context) && getDayNumber(context) > 0){

            long start = getTodayStartDateTimeMillis(context);
            long now = currentTimeMillis(context);

            long duty_cycle_length_millis = (long)getCycleLength(context);

            cycle = (now - start)/duty_cycle_length_millis;
        }
        return (int) cycle;
    }

    public static  long getStopTimeForDataCollectionPeriod(Context context, int cycleIndex){
        long start = getTodayStartDateTimeMillis(context);

        long duty_cycle_on_millis = StudyConfig.getDutyCycleOnInterval(context) * 1000 * 60;
        long duty_cycle_length_millis = (long)getCycleLength(context);

        long stopTime = start + cycleIndex*duty_cycle_length_millis + duty_cycle_on_millis;

        return(Math.min(stopTime,getTodayStopDateTimeMillis(context)));

    }

    public static  long getStartTimeForDataCollectionPeriod(Context context,int cycleIndex){
        long start = getTodayStartDateTimeMillis(context);

        long duty_cycle_length_millis = (long)getCycleLength(context);

        long startTime = start + (cycleIndex)*duty_cycle_length_millis ;

        return startTime;
    }

    public static int getDayNumber(Context context) {

        int dayNumber = -1;
        long ONE_DAY_MILLIS = 1000L * 60 * 60 * 24;

        if (withinStudyPeriod(context)) {

            long now = currentTimeMillis(context);
            long start = getStartDateMillis(context);

            long millisFromStart = now - start;

            dayNumber = (int)(millisFromStart / ONE_DAY_MILLIS);
        }

        return dayNumber;
    }

    //Ben: This is only looking at what should be collected
    //not what has been collected.
    public static boolean sufficientDataForStressRating(Context context) {
        if(withinDataCollectionCycle(context) && millisSinceDataCollectionPeriodStart(context) > MINIMUM_DATA_DURATION) {
            return true;
        }
        else{
            return false;
        }
    }


    //Ben: need to add code to handle pause events.
    public static String dataCollectionOnOrOff(Context context){
        if(withinDataCollectionCycle(context)){
            return("ON");
        }
        else if(withinDataCollectionCycle(context)==false && withinStudyPeriod(context)==true){
            return("OFF");
        }
        else if(beforeStudyPeriod(context)==true){
            return("BEFORE");
        }
        else if(afterStudyPeriod(context)==true){
            return("AFTER");
        }
        else{
            return("UNKNOWN");
        }
    }


    public static int timeSinceLastDCSWCheckIn(Context context) {
        int timeSinceLastCheckIn=-1;

        if(MHLState.getEventOccurred(context,MHLEvent.DCSW_CHECKIN,getDayNumber(context))){
            long dcswTime = MHLState.getEventTimestamp(context,MHLEvent.DCSW_CHECKIN,getDayNumber(context));
            long millisSinceCheckIn = currentTimeMillis(context) - dcswTime;
            timeSinceLastCheckIn = (int)(millisSinceCheckIn / 1000L);
        }

        return timeSinceLastCheckIn;
    }

    public static  boolean eligibleForStressRatingRequest(Context context) {
        return stressRatingBlackoutRemaining(context) <= 0 && sufficientDataForStressRating(context);
    }

    public static double getStressRatingInterval(Context context) {

        int num_self_report;
        int collection_period_length;
        double ratingFreq;

        num_self_report = StudyConfig.getNumSelfReport(context);
        collection_period_length = getDailyDataCollectionPeriodLengthMins(context);
        ratingFreq = (collection_period_length - 30.0) / num_self_report;

        return ratingFreq;
    }

    public static long stressRatingBlackoutRemaining(Context context) {

        double ratingFreq = getStressRatingInterval(context);

        long ratingFreqMillis = (long)ratingFreq * 60 * 1000;

        long elapsedMillis = timeSinceLastStressRatingRequest(context);

        long timeRemaining = Math.max(0,ratingFreqMillis - elapsedMillis);

        return timeRemaining;
    }

    public static  long timeSinceLastStressRatingRequest(Context context) {

        if(MHLState.getEventOccurred(context,MHLEvent.STRESS_RATING_NOTIFICATION,getDayNumber(context))){
            return currentTimeMillis(context) - MHLState.getEventTimestamp(context,MHLEvent.STRESS_RATING_NOTIFICATION, getDayNumber(context));
        }
        else{
            return getMillisFromTodayStartDateTime(context);
        }
    }



    /*public void showStatusToast() {
        showStatusToast(Toast.LENGTH_LONG);
    }

    public void showStatusToast(int length) {
        Toast.makeText(context, getStatusText(), length).show();
    }*/



    public static boolean checkForCompletedDailyEvent(Context context,MHLEvent event) {
        //Check to see if an event that should have happened today has happened or not.
        return MHLState.getEventOccurred(context,event,getDayNumber(context));
    }


    public static boolean hasRetriesRemaining(Context context,int retriesSoFar) {
        return retriesRemaining(context,retriesSoFar) > 0;
    }

    public static long getBackoffForRetry(Context context, int retryNumber) {
        if (!hasRetriesRemaining(context,retryNumber)) {
            return -1L;
        }

        long[] backoffSchedule = NetworkConfig.getBackoffSchedule(context);

        return backoffSchedule[retryNumber];
    }

    public static int retriesRemaining(Context context, int retriesSoFar) {
        int backoffCount = NetworkConfig.getBackoffSchedule(context).length;

        if (retriesSoFar < backoffCount) {
            return backoffCount - retriesSoFar;
        }

        return 0;
    }

    public static boolean airplaneModeOn(Context context) {
        // https://stackoverflow.com/questions/41982582/how-can-i-detect-if-device-is-in-airplane-mode
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return Settings.System.getInt(context.getContentResolver(),
                    Settings.System.AIRPLANE_MODE_ON, 0) != 0;
        } else {
            return Settings.Global.getInt(context.getContentResolver(),
                    Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
        }
    }
}