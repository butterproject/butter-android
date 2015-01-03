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

package com.google.sample.castcompanionlibrary.cast.reconnection;

import static com.google.sample.castcompanionlibrary.utils.LogUtils.LOGD;
import static com.google.sample.castcompanionlibrary.utils.LogUtils.LOGE;

import com.google.sample.castcompanionlibrary.cast.BaseCastManager;
import com.google.sample.castcompanionlibrary.cast.VideoCastManager;
import com.google.sample.castcompanionlibrary.cast.exceptions.NoConnectionException;
import com.google.sample.castcompanionlibrary.cast.exceptions
        .TransientNetworkDisconnectionException;
import com.google.sample.castcompanionlibrary.cast.player.VideoCastControllerActivity;
import com.google.sample.castcompanionlibrary.utils.LogUtils;
import com.google.sample.castcompanionlibrary.utils.Utils;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.os.SystemClock;

import java.util.Timer;
import java.util.TimerTask;

/**
 * A service to run in the background when the playback of a media starts, to help with reconnection
 * if needed. Due to various reasons, connectivity to the cast device can be lost; for example wifi
 * radio may turn off when device goes to sleep or user may step outside of the wifi range, etc.
 * This service helps with recovering the connectivity when circumstances are right, for example
 * when user steps back within the wifi range, etc. In order to avoid ending up with a background
 * service that lingers around longer than it is needed, this implementation uses certain heuristics
 * to stop itself when needed.
 */
public class ReconnectionService extends Service {

    private static final String TAG = LogUtils.makeLogTag(ReconnectionService.class);
    private static final long EPSILON_MS = 500;
    private BroadcastReceiver mScreenOnOffBroadcastReceiver;
    private String mApplicationId;
    private String mDataNamespace;
    private Class<?> mTargetActivity;
    private VideoCastManager mCastManager;
    private BroadcastReceiver mWifiBroadcastReceiver;
    private boolean mWifiConnectivity = true;
    private Timer mEndTimer;
    private TimerTask mEndTimerTask;
    private static final int RECONNECTION_ATTEMPT_PERIOD_S = 15;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LOGD(TAG, "onStartCommand() is called");
        setupEndTimer();
        return Service.START_STICKY;
    }

    @Override
    public void onCreate() {
        LOGD(TAG, "onCreate() is called");
        readPersistedData();
        mCastManager = VideoCastManager
                .initialize(this, mApplicationId, mTargetActivity, mDataNamespace);
        if (!mCastManager.isConnected() && !mCastManager.isConnecting()) {
            mCastManager.reconnectSessionIfPossible(this, false);
        }

        // register a broadcast receiver to be notified when screen goes on or off
        IntentFilter screenOnOffIntentFilter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        screenOnOffIntentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        mScreenOnOffBroadcastReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                LOGD(TAG, "ScreenOnOffBroadcastReceiver: onReceive(): " + intent.getAction());
                long timeLeft = getTimeLeft();
                if (timeLeft < EPSILON_MS) {
                    handleTermination();
                }
            }
        };
        registerReceiver(mScreenOnOffBroadcastReceiver, screenOnOffIntentFilter);

        // register a wifi receiver to be notified of network state changes
        IntentFilter networkIntentFilter = new IntentFilter();
        networkIntentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        mWifiBroadcastReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                final String action = intent.getAction();
                if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
                    NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                    boolean connected = info.isConnected();
                    String networkSsid = null;
                    if (connected) {
                        networkSsid = Utils.getWifiSsid(context);
                    }
                    ReconnectionService.this.onWifiConnectivityChanged(connected, networkSsid);
                }
            }
        };
        registerReceiver(mWifiBroadcastReceiver, networkIntentFilter);

        super.onCreate();
    }

    public void onWifiConnectivityChanged(boolean connected, final String networkSsid) {
        LOGD(TAG, "WIFI connectivity changed to " + (connected ? "enabled" : "disabled"));
        if (connected && !mWifiConnectivity) {
            mWifiConnectivity = true;
            if (mCastManager.isFeatureEnabled(BaseCastManager.FEATURE_WIFI_RECONNECT)) {
                mCastManager.startCastDiscovery();
                mCastManager.reconnectSessionIfPossible(this, false, RECONNECTION_ATTEMPT_PERIOD_S,
                        networkSsid);
            }

        } else {
            mWifiConnectivity = connected;
        }
    }

    /*
     * Reads application ID and target activity from preference storage.
     */
    private void readPersistedData() {
        mApplicationId = Utils.getStringFromPreference(
                this, VideoCastManager.PREFS_KEY_APPLICATION_ID);
        String targetName = Utils.getStringFromPreference(
                this, VideoCastManager.PREFS_KEY_CAST_ACTIVITY_NAME);
        mDataNamespace = Utils.getStringFromPreference(
                this, VideoCastManager.PREFS_KEY_CAST_CUSTOM_DATA_NAMESPACE);
        try {
            if (null != targetName) {
                mTargetActivity = Class.forName(targetName);
            } else {
                mTargetActivity = VideoCastControllerActivity.class;
            }

        } catch (ClassNotFoundException e) {
            LOGE(TAG, "Failed to find the targetActivity class", e);
        }
    }

    @Override
    public void onDestroy() {
        LOGD(TAG, "onDestroy()");
        if (null != mScreenOnOffBroadcastReceiver) {
            unregisterReceiver(mScreenOnOffBroadcastReceiver);
            mScreenOnOffBroadcastReceiver = null;
        }

        if (null != mWifiBroadcastReceiver) {
            unregisterReceiver(mWifiBroadcastReceiver);
            mWifiBroadcastReceiver = null;
        }

        clearEndTimer();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void setupEndTimer() {
        LOGD(TAG, "setupEndTimer(): setting up a timer for the end of current media");
        long timeLeft = getTimeLeft();
        if (timeLeft <= 0) {
            stopSelf();
            return;
        }
        clearEndTimer();
        mEndTimer = new Timer();
        mEndTimerTask = new TimerTask() {
            @Override
            public void run() {
                LOGD(TAG, "setupEndTimer(): stopping ReconnectionService since reached the end of"
                        + " allotted time");
                handleTermination();
            }
        };
        mEndTimer.schedule(mEndTimerTask, timeLeft);
    }

    private void clearEndTimer() {
        if (null != mEndTimerTask) {
            mEndTimerTask.cancel();
            mEndTimerTask = null;
        }

        if (null != mEndTimer) {
            mEndTimer.cancel();
            mEndTimer = null;
        }
    }

    private long getTimeLeft() {
        long endTime = Utils.getLongFromPreference(this, BaseCastManager.PREFS_KEY_MEDIA_END, -1);
        return endTime - SystemClock.elapsedRealtime();
    }

    private void handleTermination() {
        if (!mCastManager.isConnected()) {
            mCastManager.removeRemoteControlClient();
            mCastManager.clearPersistedConnectionInfo(BaseCastManager.CLEAR_ALL);
            stopSelf();
        } else {
            // since we are connected and our timer has gone off, lets update the time remaining
            // on the media (since media may have been paused) and reset teh time left
            long timeLeft = 0;
            try {
                timeLeft = mCastManager.isRemoteStreamLive() ? 0 :
                        mCastManager.getTimeLeftForMedia();

            } catch (TransientNetworkDisconnectionException e) {
                LOGE(TAG, "Failed to calculate the time left for media due to lack of connectivity",
                        e);
            } catch (NoConnectionException e) {
                LOGE(TAG, "Failed to calculate the time left for media due to lack of connectivity",
                        e);
            }
            if (timeLeft < EPSILON_MS) {
                // no time left
                stopSelf();
            } else {
                // lets reset the counter
                Utils.saveLongToPreference(this.getApplicationContext(),
                        BaseCastManager.PREFS_KEY_MEDIA_END,
                        timeLeft + SystemClock.elapsedRealtime());
                LOGD(TAG, "handleTermination(): resetting the timer");
                setupEndTimer();
            }

        }
    }
}

