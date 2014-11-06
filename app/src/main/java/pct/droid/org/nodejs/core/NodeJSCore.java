package pct.droid.org.nodejs.core;

import pct.droid.utils.LogUtils;

public class NodeJSCore {
    private static final String TAG = "nodejs-core";

    public static native void run(String mainJS);

    static {
        LogUtils.d(TAG, "Loading libnodeJNI");
        System.loadLibrary("nodeJNI");
        LogUtils.d(TAG, "libnodeJNI Loaded");
    }

}
