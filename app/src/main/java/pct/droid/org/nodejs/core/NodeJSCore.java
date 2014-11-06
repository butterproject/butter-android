package pct.droid.org.nodejs.core;

/**
 * Created by wally on 06/11/14.
 */
public class NodeJSCore {
    private static final String TAG = "nodejs-core";

    public static native void run(String mainJS);

    static {
        System.loadLibrary("nodeJNI");
    }

}
