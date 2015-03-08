package org.videolan.vlc;

import android.app.Application;
import android.content.Context;

/**
 * Created by Sebastiaan on 28-11-14.
 */
public class VLCApplication extends Application {

    private static Application sInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
    }

    public static Context getAppContext() {
        return sInstance;
    }

}
