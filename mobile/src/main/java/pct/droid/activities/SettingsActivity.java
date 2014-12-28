package pct.droid.activities;

import android.os.Bundle;

import pct.droid.R;

public class SettingsActivity extends BaseActivity {

    public static final String SUBTITLE_COLOR = "subtitle_color";
    public static final String SUBTITLE_SIZE = "subtitle_size";
    public static final String SUBTITLE_DEFAULT = "subtitle_default_language";
    public static final String STORAGE_LOCATION = "storage_location";
    public static final String HW_ACCELERATION = "hw_acceleration";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, R.layout.activity_settings);
    }
}
