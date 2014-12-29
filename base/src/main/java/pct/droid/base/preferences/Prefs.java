package pct.droid.base.preferences;

import android.content.Context;

import java.io.File;

import pct.droid.base.utils.PrefUtils;
import pct.droid.base.utils.StorageUtils;

public class Prefs {
    public static final String SUBTITLE_COLOR = "subtitle_color";
    public static final String SUBTITLE_SIZE = "subtitle_size";
    public static final String SUBTITLE_DEFAULT = "subtitle_default_language";
    public static final String STORAGE_LOCATION = "storage_location";
    public static final String HW_ACCELERATION = "hw_acceleration";
    public static final String AUTOMATIC_UPDATES = "auto_updates";
    public static final String DEFAULT_VIEW = "default_view";

    public static File getCacheDirectory(Context context) {
        if (!PrefUtils.get(context, Prefs.STORAGE_LOCATION, "").isEmpty()) {
            return new File(PrefUtils.get(context, Prefs.STORAGE_LOCATION, ""));
        }

        return StorageUtils.getIdealCacheDirectory(context);
    }
}
