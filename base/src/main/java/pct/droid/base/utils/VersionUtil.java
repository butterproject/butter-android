package pct.droid.base.utils;

import android.os.Build;

public class VersionUtil {

    public static boolean isLollipop(){
       return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }
}
