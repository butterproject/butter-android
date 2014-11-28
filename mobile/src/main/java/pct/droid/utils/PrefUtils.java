package pct.droid.utils;

import android.content.Context;

import pct.droid.Constants;
import pct.droid.widget.ObscuredSharedPreferences;

public class PrefUtils {

    // Main functions below

    public static void clear(Context context) {
        getPrefs(context).edit().clear().apply();
    }

    public static void save(Context context, String key, String value) {
        getPrefs(context).edit().putString(key, value).apply();
    }

    public static String get(Context context, String key, String defaultValue) {
        return getPrefs(context).getString(key, defaultValue);
    }

    public static void save(Context context, String key, boolean value) {
        getPrefs(context).edit().putBoolean(key, value).apply();
    }

    public static Boolean get(Context context, String key, boolean defaultValue) {
        return getPrefs(context).getBoolean(key, defaultValue);
    }

    public static void save(Context context, String key, long value) {
        getPrefs(context).edit().putLong(key, value).apply();
    }

    public static long get(Context context, String key, long defaultValue) {
        return getPrefs(context).getLong(key, defaultValue);
    }

    public static void save(Context context, String key, int value) {
        getPrefs(context).edit().putInt(key, value).apply();
    }

    public static int get(Context context, String key, int defaultValue) {
        return getPrefs(context).getInt(key, defaultValue);
    }

    public static Boolean contains(Context context, String key) {
        return getPrefs(context).contains(key);
    }

    public static ObscuredSharedPreferences getPrefs(Context context) {
        return new ObscuredSharedPreferences(context, context.getSharedPreferences(Constants.PREFS_FILE, Context.MODE_PRIVATE));
    }

}
