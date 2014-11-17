package org.nodejs.core;

import pct.droid.utils.LogUtils;

public class NodeJSCore {
    private static final String TAG = "nodejs-core";

    public static native void run(String mainJS);

    static {
        System.loadLibrary("nodeJNI");
        LogUtils.i("libnodeJNI Loaded");
    }

}
