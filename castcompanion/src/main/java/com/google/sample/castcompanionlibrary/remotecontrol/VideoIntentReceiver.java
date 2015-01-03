/*
 * Copyright (C) 2013 Google Inc. All Rights Reserved.
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

package com.google.sample.castcompanionlibrary.remotecontrol;

import static com.google.sample.castcompanionlibrary.utils.LogUtils.LOGD;
import static com.google.sample.castcompanionlibrary.utils.LogUtils.LOGE;

import com.google.sample.castcompanionlibrary.cast.VideoCastManager;
import com.google.sample.castcompanionlibrary.cast.exceptions.CastException;
import com.google.sample.castcompanionlibrary.notification.VideoCastNotificationService;
import com.google.sample.castcompanionlibrary.utils.LogUtils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;

/**
 * A {@link BroadcastReceiver} for receiving media button actions (from the lock screen) as well as
 * the the status bar notification media actions.
 */
public class VideoIntentReceiver extends BroadcastReceiver {

    private static final String TAG = LogUtils.makeLogTag(VideoIntentReceiver.class);

    @Override
    public void onReceive(Context context, Intent intent) {
        VideoCastManager castMgr = null;
        try {
            castMgr = VideoCastManager.getInstance();
        } catch (CastException e1) {
            LOGE(TAG, "onReceive(): No CastManager instance exists");
        }
        String action = intent.getAction();
        if (null == action) {
            return;
        }
        if (action.equals(VideoCastNotificationService.ACTION_TOGGLE_PLAYBACK)) {
            try {
                if (null != castMgr) {
                    LOGD(TAG, "Toggling playback via CastManager");
                    castMgr.togglePlayback();
                } else {
                    LOGD(TAG, "Toggling playback via NotificationService");
                    startService(context, VideoCastNotificationService.ACTION_TOGGLE_PLAYBACK);
                }

            } catch (Exception e) {
                LOGE(TAG, "onReceive(): Failed to toggle playback", e);
                startService(context, VideoCastNotificationService.ACTION_TOGGLE_PLAYBACK);
            }
        } else if (action.equals(VideoCastNotificationService.ACTION_STOP)) {

            try {
                if (null != castMgr) {
                    LOGD(TAG, "Calling stopApplication from intent");
                    castMgr.disconnect();
                } else {
                    startService(context, VideoCastNotificationService.ACTION_STOP);
                }
            } catch (Exception e) {
                LOGE(TAG, "onReceive(): Failed to stop application", e);
                startService(context, VideoCastNotificationService.ACTION_STOP);
            }
        } else if (action.equals(Intent.ACTION_MEDIA_BUTTON)) {

            KeyEvent keyEvent = (KeyEvent) intent.getExtras().get(Intent.EXTRA_KEY_EVENT);
            if (keyEvent.getAction() != KeyEvent.ACTION_DOWN) {
                return;
            }

            switch (keyEvent.getKeyCode()) {
                case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                    try {
                        castMgr.togglePlayback();
                    } catch (Exception e) {
                        // already logged
                    }
                    break;
            }
        }
    }

    private void startService(Context context, String action) {
        Intent serviceIntent = new Intent(action);
        serviceIntent.setPackage(context.getPackageName());
        context.startService(serviceIntent);
    }

}
