package edu.umass.cs.sensors.mhllibrary.MHLState;

import java.util.Date;

import android.content.Context;
import android.util.Log;

import edu.umass.cs.sensors.mhllibrary.Configuration.StudyConfig;
import edu.umass.cs.sensors.mhllibrary.MHLAgentCore.MHLAndroidAgentCore;
import edu.umass.cs.sensors.mhllibrary.MHLAgentCore.PreferencesAccessor;
import edu.umass.cs.sensors.mhllibrary.MHLClient.Messages.StatusRecordSystemMessage;
import edu.umass.cs.sensors.mhllibrary.MHLClient.Messages.SystemMessage;
import edu.umass.cs.sensors.mhllibrary.MHLUtilities.MHLOutputHandler;
import edu.umass.cs.sensors.mhllibrary.MHLUtilities.AndroidOutputHandler;


public class MHLState {
    static final String TAG = "MHLState";

    private static String getFieldName(MHLEvent event, String field, int studyDay){
        return event.name()+"-" + field + "-" + String.valueOf(studyDay);
    }

    public static void setEventTimestamp(Context context, MHLEvent event) {
        setEventTimestamp(context, event, 0);
    }

    public static void setEventTimestamp(Context context, MHLEvent event, int studyDay){

        String count_field = getFieldName(event, "count", studyDay);
        String datetime_field = getFieldName(event, "datetime", studyDay);

        if(PreferencesAccessor.getStoredPref(context,count_field)==null){
            PreferencesAccessor.storePref(context,count_field, "1");
        }
        else{
            int count = 1+Integer.parseInt(PreferencesAccessor.getStoredPref(context,count_field));
            PreferencesAccessor.storePref(context,count_field, String.valueOf(count));
        }

        PreferencesAccessor.storePref(context,datetime_field,String.valueOf(System.currentTimeMillis()));

        //Ben: added this as messages were only being sent in event with data method
        // generate and submit status record message
        MHLAndroidAgentCore agent = MHLAndroidAgentCore.getInstance(context);
        StatusRecordSystemMessage statusMessage = new StatusRecordSystemMessage(
                agent.getBadgeID(),
                System.currentTimeMillis(),
                "event: " + event.name() + " -- study day: " + studyDay + " -- data: " + "event occurred");

        agent.addMessage(statusMessage.toJSONString());

        Log.d(TAG, "Added status report message to agent: " + statusMessage.toJSONString());

    }

    public static void setEventWithData(Context context, MHLEvent event, String data){
        setEventWithData(context, event, 0 , data);
    }

    public static void setEventWithData(Context context, MHLEvent event, int studyDay, String data){

        String data_field = getFieldName(event, "data", studyDay);
        setEventTimestamp(context, event, studyDay);
        PreferencesAccessor.storePref(context,data_field, data);

        // generate and submit status record message
        MHLAndroidAgentCore agent = MHLAndroidAgentCore.getInstance(context);
        StatusRecordSystemMessage statusMessage = new StatusRecordSystemMessage(
                agent.getBadgeID(),
                System.currentTimeMillis(),
                "event: " + event.name() + " -- study day: " + studyDay + " -- data: " + data);

        agent.addMessage(statusMessage.toJSONString());

        Log.d(TAG, "Added status report message to agent: " + statusMessage.toJSONString());
    }

    public static int getEventCount(Context context, MHLEvent event){
        return getEventCount(context, event, 0);
    }

    public static int getEventCount(Context context, MHLEvent event, int studyDay){
        String count_field = getFieldName(event, "count", studyDay);
        if(PreferencesAccessor.getStoredPref(context,count_field)==null){
            return 0;
        }
        else {
            return Integer.parseInt(PreferencesAccessor.getStoredPref(context,count_field));
        }
    }

    public static long getEventTimestamp(Context context, MHLEvent event){
        return getEventTimestamp(context, event, 0);
    }

    public static long getEventTimestamp(Context context, MHLEvent event,int studyDay){

        String datetime_field = getFieldName(event, "datetime", studyDay);
        String time = PreferencesAccessor.getStoredPref(context,datetime_field);

        if(time==null){
            return -1L;
        }
        else{
            return Long.parseLong(time);
        }
    }

    public static  boolean getEventOccurred(Context context, MHLEvent event){
        return getEventOccurred(context, event, 0);
    }

    public static  boolean getEventOccurred(Context context, MHLEvent event, int studyDay){
        return (getEventCount(context, event, studyDay)>0);
    }

    public static String getEventData(Context context, MHLEvent event){
        return getEventData(context, event, 0);
    }

    public static String getEventData(Context context, MHLEvent event,int studyDay ){
        String data_field = getFieldName(event, "data", studyDay);
        return PreferencesAccessor.getStoredPref(context,data_field);
    }

    public static void deleteEvent(Context context, MHLEvent event){
        deleteEvent(context, event, 0);
    }

    public static void deleteEvent(Context context, MHLEvent event, int studyDay){

        String datetime_field = getFieldName(event, "datetime", studyDay);
        String count_field = getFieldName(event, "count", studyDay);
        String data_field = getFieldName(event, "data", studyDay);

        if(PreferencesAccessor.getStoredPref(context,datetime_field)!=null) {
            PreferencesAccessor.deletePref(context,datetime_field );
        }
        if(PreferencesAccessor.getStoredPref(context,count_field)!=null){
            PreferencesAccessor.deletePref(context,count_field);
        }
        if(PreferencesAccessor.getStoredPref(context,data_field)!=null){
            PreferencesAccessor.deletePref(context,data_field);
        }
    }


    public static  void clearAllState(Context context) {
        PreferencesAccessor.clearAllStoredPrefs(context);
    }

    public static String getStateReport(Context context, int studyDay) {
        String report = "\nMHLab State Report\n";
        report += "--------------------\n";

        if (getEventOccurred(context,MHLEvent.REGISTRATION_COMPELTED)) {
            Date date = new Date(getEventTimestamp(context,MHLEvent.REGISTRATION_COMPELTED));
            report += "Registration Date: " + date.toString() + "\n";
        }

        if (getEventOccurred(context,MHLEvent.DATA_SAMPLE_COLLECTED)) {
            Date date = new Date(getEventTimestamp(context,MHLEvent.DATA_SAMPLE_COLLECTED));
            report += "Data Sample Collected Date: " + date.toString() + "\n";
        }

        if (getEventOccurred(context,MHLEvent.HOME_LOCATION_SET)) {
            Date date = new Date(getEventTimestamp(context, MHLEvent.HOME_LOCATION_SET));
            report += "Home Location Set Date: " + date.toString() + "\n";
        }

        for (int d = 0; d <= studyDay; d++) {
            report += "\nStudy Day " + d + "\n";
            report += "-------------------\n";


            if (getEventOccurred(context,MHLEvent.MORNING_SURVEY_NOTIFICATION, d)) {
                Date date = new Date(getEventTimestamp(context,MHLEvent.MORNING_SURVEY_NOTIFICATION, d));
                report += "Morning Survey Notification: Last Sent at " + date.toString() + "\n";
                report += "Number of Morning Survey Notifications Sent: " + getEventCount(context,MHLEvent.MORNING_SURVEY_NOTIFICATION, d) + "\n";
            } else {
                report += "Morning Survey: Not Sent \n";
            }

            if (getEventOccurred(context,MHLEvent.MORNING_SURVEY_ENTERED, d)) {
                Date date = new Date(getEventTimestamp(context,MHLEvent.MORNING_SURVEY_ENTERED, d));
                report += "Morning Survey: Last Completed at " + date.toString() + "\n";
                report += "Number of Morning Surveys Entered: " + getEventCount(context,MHLEvent.MORNING_SURVEY_ENTERED, d) + "\n";

            } else {
                report += "Morning Survey: Not Completed \n";
            }

            if (getEventOccurred(context,MHLEvent.EVENING_SURVEY_NOTIFICATION, d)) {
                Date date = new Date(getEventTimestamp(context,MHLEvent.EVENING_SURVEY_NOTIFICATION, d));
                report += "Evening Survey Notification: Last Sent at " + date.toString() + "\n";
                report += "Number of Evening Survey Notifications Sent: " + getEventCount(context,MHLEvent.EVENING_SURVEY_NOTIFICATION, d) + "\n";
            } else {
                report += "Evening Survey: Not Sent \n";
            }

            if (getEventOccurred(context,MHLEvent.EVENING_SURVEY_ENTERED, d)) {
                Date date = new Date(getEventTimestamp(context,MHLEvent.EVENING_SURVEY_ENTERED, d));
                report += "Evening Survey: Last Completed at " + date.toString() + "\n";
                report += "Number of Evening Surveys Entered: " + getEventCount(context,MHLEvent.EVENING_SURVEY_ENTERED, d) + "\n";
            } else {
                report += "Evening Survey: Not Completed \n";
            }

            if("SCH-Amherst".equals(StudyConfig.getStudyName(context))) {
                int count = getEventCount(context,MHLEvent.STRESS_RATING_NOTIFICATION, d);
                report += "Number of stress notifications sent: " + count + "\n";

                count = getEventCount(context,MHLEvent.STRESS_RATING_ENTERED, d);
                report += "Number of stress ratings entered: " + count + "\n";
            }
            if("SCH-UMMS".equals(StudyConfig.getStudyName(context))) {
                int count = getEventCount(context,MHLEvent.SERVER_EMA_NOTIFICATION, d);
                report += "Number of smoking message notifications received: " + count + "\n";

                count = getEventCount(context,MHLEvent.SERVER_EMA_ENTERED, d);
                report += "Number of smoking message ratings entered: " + count + "\n";
            }





        }
        return report;
    }
}
