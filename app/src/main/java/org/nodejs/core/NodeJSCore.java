package org.nodejs.core;

import pct.droid.utils.LogUtils;

public class NodeJSCore {
    private static final String TAG = "nodejs-core";

    public static native void run(String mainJS);

    static {
        LogUtils.d("Loading libnodeJNI");
        System.loadLibrary("nodeJNI");
        LogUtils.d("libnodeJNI Loaded");
    }

}
