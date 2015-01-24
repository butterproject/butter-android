package pct.droid.base.utils;

import android.os.Build;

public class VersionUtils {

    public static boolean isLollipop(){
       return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    public static boolean isJellyBean(){
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
    }

}
