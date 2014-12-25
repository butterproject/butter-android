package pct.droid.base.utils;

import android.util.Log;

import pct.droid.base.Constants;

/**
 * Class to simplify logging
 */
public class LogUtils {

    public static void d(Object message) {
        d(Constants.LOG_TAG, message);
    }

    public static void d(Object tag, Object message) {
        if (Constants.DEBUG_ENABLED && message != null) {
            Log.d(tag.toString(), message.toString());
        }
    }

    public static void v(Object message) {
        v(Constants.LOG_TAG, message);
    }

    public static void v(Object tag, Object message) {
        if (Constants.DEBUG_ENABLED && message != null) {
            Log.v(tag.toString(), message.toString());
        }
    }

    public static void e(Object message) {
        e(Constants.LOG_TAG, message);
    }

    public static void e(Object tag, Object message) {
        if (Constants.DEBUG_ENABLED && message != null) {
            Log.e(tag.toString(), message.toString());
        }
    }

    public static void e(Object tag, Object message, Throwable t) {
        if (Constants.DEBUG_ENABLED && message != null) {
            Log.e(tag.toString(), message.toString(), t);
        }
    }

    public static void w(Object message) {
        w(Constants.LOG_TAG, message);
    }

    public static void w(Object tag, Object message) {
        if (Constants.DEBUG_ENABLED && message != null) {
            Log.w(tag.toString(), message.toString());
        }
    }

    public static void i(Object message) {
        i(Constants.LOG_TAG, message);
    }

    public static void i(Object tag, Object message) {
        Log.i(tag.toString(), message.toString());
    }

}
