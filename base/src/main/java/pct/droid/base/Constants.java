package pct.droid.base;

public class Constants {

    public static Boolean DEBUG_ENABLED = BuildConfig.DEBUG; // will be set to true for debug builds and false for release builds
    public static final String LOG_TAG = "PopcornTimeForAndroid";
    public static final String PREFS_FILE = "PCT_Prefs";
    public static final String DATABASE_NAME = "PCT_DB.db";
    public static final Integer DATABASE_VERSION = 1;
    public static final String BUGSNAG_KEY = "b3d541b31a21687cdb2389f00cd58c62";
    public static final String GIT_URL = "https://git.popcorntime.io/";
    public static final String JIRA_API = GIT_URL + "jira/rest/api/2/";
    public static final String JIRA_PROJECT = "10200";

}