package org.nodejs.core;

import android.util.Log;

public class NodeJSCore {
    private static final String TAG = "nodejs-core";

    public static native void run(String mainJS);

    static {
        System.loadLibrary("nodeJNI");
        Log.d("NODEJS", "JNI Library loaded");
    }

}
