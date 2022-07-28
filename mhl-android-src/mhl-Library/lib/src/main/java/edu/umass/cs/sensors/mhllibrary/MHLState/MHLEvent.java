package edu.umass.cs.sensors.mhllibrary.MHLState;

public enum MHLEvent {
    STRESS_RATING_ENTERED("stress-rating-entered"),
    MORNING_SURVEY_ENTERED("morning-survey-entered"),
    EVENING_SURVEY_ENTERED("evening-survey-entered"),
    SERVER_EMA_ENTERED("server-ema-entered"),
    SERVER_EMA_AVAILABLE("server-ema-available"),
    SERVER_EMA_NOTIFICATION("server-ema-notification"),
    REGISTRATION_COMPELTED("registration-completed"),
    DCSW_CHECKIN("dcsw-check-in"),
    STUDY_PAUSED("study-paused"),
    STUDY_RESUMED("study-resumed"),
    MORNING_SURVEY_NOTIFICATION("morning-survey-notification"),
    EVENING_SURVEY_NOTIFICATION("evening-survey-notification"),
    STRESS_RATING_NOTIFICATION("stress-rating-notification"),
    BLUETOOTH_NOTIFICATION("bluetooth-notification"),
    NETWORK_NOTIFICATION("network-notification"),
    //STUDY_DAY("study-day"),
    HOME_LOCATION_SET("home-location-set"),
    COLLECTING_DATA_SAMPLE("collecting-data-sample"),
    DATA_SAMPLE_COLLECTED("collected-data-sample");

    private final String name;

    MHLEvent(String name) {
        this.name = name;
    }

}
