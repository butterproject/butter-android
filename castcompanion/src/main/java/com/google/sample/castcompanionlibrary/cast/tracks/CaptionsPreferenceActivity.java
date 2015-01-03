/*
 * Copyright (C) 2014 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.sample.castcompanionlibrary.cast.tracks;

import static com.google.sample.castcompanionlibrary.utils.LogUtils.LOGE;

import com.google.sample.castcompanionlibrary.R;
import com.google.sample.castcompanionlibrary.cast.VideoCastManager;
import com.google.sample.castcompanionlibrary.cast.exceptions.CastException;
import com.google.sample.castcompanionlibrary.utils.LogUtils;
import com.google.sample.castcompanionlibrary.utils.Utils;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.provider.Settings;

/**
 * An Activity to show the Captions Preferences for Android versions prior to KitKat
 */
public class CaptionsPreferenceActivity extends PreferenceActivity {

    private static final String TAG = LogUtils.makeLogTag(CaptionsPreferenceActivity.class);
    private VideoCastManager mCastManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            mCastManager = VideoCastManager.getInstance();
        } catch (CastException e) {
            LOGE(TAG, "Failed to get an instance of VideoCastManager", e);
            finish();
            return;
        }
        if (!mCastManager.isFeatureEnabled(VideoCastManager.FEATURE_CAPTIONS_PREFERENCE)) {
            LOGE(TAG, "Did you forget to enable FEATURE_CAPTIONS_PREFERENCE when you initialized"
                    + " the VideoCastManage?");
            finish();
            return;
        }
        if (Utils.IS_KITKAT_OR_ABOVE) {
            startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
            finish();
            return;
        }
        addPreferencesFromResource(R.xml.caption_preference);
        mCastManager.getTracksPreferenceManager().setupPreferences(getPreferenceScreen());
    }
}
