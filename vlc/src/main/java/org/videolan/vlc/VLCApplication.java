package org.videolan.vlc;

import android.app.Application;
import android.content.Context;

/**
 * Created by Sebastiaan on 28-11-14.
 */
public class VLCApplication extends Application {

    private static Application mInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
    }

    public static Context getAppContext() {
        return mInstance;
    }

}
