package pct.droid.utils;

import android.util.Log;

import pct.droid.Constants;


public class LogUtils {

    public static void d(Object message) {
        d("LogUtils", message);
    }

    public static void d(Object tag, Object message) {
        if (Constants.LOG_ENABLED) {
            Log.d(tag.toString(), message.toString());
        }
    }

    public static void v(Object message) {
        v("LogUtils", message);
    }

    public static void v(Object tag, Object message) {
        if (Constants.LOG_ENABLED) {
            Log.v(tag.toString(), message.toString());
        }
    }

    public static void e(Object message) {
        e("LogUtils", message);
    }

    public static void e(Object tag, Object message) {
        if (Constants.LOG_ENABLED) {
            Log.e(tag.toString(), message.toString());
        }
    }

    public static void e(Object tag, Object message, Throwable t) {
        if (Constants.LOG_ENABLED) {
            Log.e(tag.toString(), message.toString(), t);
        }
    }

    public static void w(Object message) {
        w("LogUtils", message);
    }

    public static void w(Object tag, Object message) {
        if (Constants.LOG_ENABLED) {
            Log.w(tag.toString(), message.toString());
        }
    }

}
