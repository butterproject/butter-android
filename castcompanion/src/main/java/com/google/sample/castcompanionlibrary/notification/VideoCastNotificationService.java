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

package com.google.sample.castcompanionlibrary.notification;

import static com.google.sample.castcompanionlibrary.utils.LogUtils.LOGD;
import static com.google.sample.castcompanionlibrary.utils.LogUtils.LOGE;

import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.MediaStatus;
import com.google.sample.castcompanionlibrary.R;
import com.google.sample.castcompanionlibrary.cast.VideoCastManager;
import com.google.sample.castcompanionlibrary.cast.callbacks.VideoCastConsumerImpl;
import com.google.sample.castcompanionlibrary.cast.exceptions.CastException;
import com.google.sample.castcompanionlibrary.cast.exceptions.NoConnectionException;
import com.google.sample.castcompanionlibrary.cast.exceptions.TransientNetworkDisconnectionException;
import com.google.sample.castcompanionlibrary.cast.player.VideoCastControllerActivity;
import com.google.sample.castcompanionlibrary.utils.FetchBitmapTask;
import com.google.sample.castcompanionlibrary.utils.LogUtils;
import com.google.sample.castcompanionlibrary.utils.Utils;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.widget.RemoteViews;

/**
 * A service to provide status bar Notifications when we are casting. For JB+ versions, notification
 * area provides a play/pause toggle and an "x" button to disconnect but that for GB, we do not
 * show that due to the framework limitations.
 */
public class VideoCastNotificationService extends Service {

    private static final String TAG = LogUtils.makeLogTag(VideoCastNotificationService.class);

    public static final String ACTION_TOGGLE_PLAYBACK =
            "com.google.sample.castcompanionlibrary.action.toggleplayback";
    public static final String ACTION_STOP =
            "com.google.sample.castcompanionlibrary.action.stop";
    public static final String ACTION_VISIBILITY =
            "com.google.sample.castcompanionlibrary.action.notificationvisibility";

    private static final int NOTIFICATION_ID = 1;
    public static final String NOTIFICATION_VISIBILITY = "visible";

    private String mApplicationId;
    private Bitmap mVideoArtBitmap;
    private Uri mVideoArtUri;
    private boolean mIsPlaying;
    private Class<?> mTargetActivity;
    private String mDataNamespace;
    private int mStatus;
    private int mOldStatus = -1;
    private Notification mNotification;
    private boolean mVisible;
    boolean mIsIcsOrAbove = Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH;
    boolean mIsLollipopOrAbove = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    private VideoCastManager mCastManager;
    private VideoCastConsumerImpl mConsumer;
    private FetchBitmapTask mBitmapDecoderTask;

    @Override
    public void onCreate() {
        super.onCreate();
        LOGD(TAG, "onCreate()");
        readPersistedData();
        mCastManager = VideoCastManager
                .initialize(this, mApplicationId, mTargetActivity, mDataNamespace);
        if (!mCastManager.isConnected() && !mCastManager.isConnecting()) {
            mCastManager.reconnectSessionIfPossible(this, false);
        }
        mConsumer = new VideoCastConsumerImpl() {
            @Override
            public void onApplicationDisconnected(int errorCode) {
                LOGD(TAG, "onApplicationDisconnected() was reached, stopping the notification"
                        + " service");
                stopSelf();
            }

            @Override
            public void onRemoteMediaPlayerStatusUpdated() {
                int mediaStatus = mCastManager.getPlaybackStatus();
                VideoCastNotificationService.this.onRemoteMediaPlayerStatusUpdated(mediaStatus);
            }

            @Override
            public void onUiVisibilityChanged(boolean visible) {
                mVisible = !visible;
                if (mVisible && null != mNotification) {
                    startForeground(NOTIFICATION_ID, mNotification);
                    mCastManager.setContext(VideoCastNotificationService.this);
                } else {
                    stopForeground(true);
                }
            }
        };
        mCastManager.addVideoCastConsumer(mConsumer);
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LOGD(TAG, "onStartCommand");
        if (null != intent) {

            String action = intent.getAction();
            if (ACTION_TOGGLE_PLAYBACK.equals(action) && mIsIcsOrAbove) {
                LOGD(TAG, "onStartCommand(): Action: ACTION_TOGGLE_PLAYBACK");
                togglePlayback();
            } else if (ACTION_STOP.equals(action) && mIsIcsOrAbove) {
                LOGD(TAG, "onStartCommand(): Action: ACTION_STOP");
                stopApplication();
            } else if (ACTION_VISIBILITY.equals(action)) {
                mVisible = intent.getBooleanExtra(NOTIFICATION_VISIBILITY, false);
                LOGD(TAG, "onStartCommand(): Action: ACTION_VISIBILITY " + mVisible);
                if (mVisible && null != mNotification) {
                    startForeground(NOTIFICATION_ID, mNotification);
                    mCastManager.setContext(this);
                } else {
                    stopForeground(true);
                }
            } else {
                LOGD(TAG, "onStartCommand(): Action: none");
            }

        } else {
            LOGD(TAG, "onStartCommand(): Intent was null");
        }

        return Service.START_STICKY;
    }

    private void setupNotification(final MediaInfo info)
            throws TransientNetworkDisconnectionException, NoConnectionException {
        if (null == info) {
            return;
        }
        if (null != mBitmapDecoderTask) {
            mBitmapDecoderTask.cancel(false);
        }
        Uri imgUri = null;
        try {
            if (!info.getMetadata().hasImages()) {
                build(info, null, mIsPlaying);
                return;
            } else {
                imgUri = info.getMetadata().getImages().get(0).getUrl();
                if (imgUri.equals(mVideoArtUri)) {
                    build(info, mVideoArtBitmap, mIsPlaying);
                    return ;
                }
            }
        } catch (CastException e) {
            LOGE(TAG, "Failed to build notification");
        }

        mBitmapDecoderTask = new FetchBitmapTask(400, 400) {
            @Override
            protected void onPostExecute(Bitmap bitmap) {
                try {
                    mVideoArtBitmap = Utils.scaleCenterCrop(bitmap, 256, 256);
                    build(info, mVideoArtBitmap, mIsPlaying);
                } catch (CastException e) {
                    LOGE(TAG, "Failed to set notification for " + info.toString(), e);
                } catch (TransientNetworkDisconnectionException e) {
                    LOGE(TAG, "Failed to set notification for " + info.toString(), e);
                } catch (NoConnectionException e) {
                    LOGE(TAG, "Failed to set notification for " + info.toString(), e);
                }
                if (mVisible) {
                    startForeground(NOTIFICATION_ID, mNotification);
                }
                if (this == mBitmapDecoderTask) {
                    mBitmapDecoderTask = null;
                }
            }
        };
        mBitmapDecoderTask.start(imgUri);
    }

    /**
     * Removes the existing notification.
     */
    private void removeNotification() {
        ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).
                cancel(NOTIFICATION_ID);
    }

    private void onRemoteMediaPlayerStatusUpdated(int mediaStatus) {
        mStatus = mediaStatus;
        if (mOldStatus == mStatus) {
            // not need to make any updates here
            return;
        }
        mOldStatus = mStatus;
        LOGD(TAG, "onRemoteMediaPlayerStatusUpdated() reached with status: " + mStatus);
        try {
            switch (mediaStatus) {
                case MediaStatus.PLAYER_STATE_BUFFERING: // (== 4)
                    mIsPlaying = false;
                    setupNotification(mCastManager.getRemoteMediaInformation());
                    break;
                case MediaStatus.PLAYER_STATE_PLAYING: // (== 2)
                    mIsPlaying = true;
                    setupNotification(mCastManager.getRemoteMediaInformation());
                    break;
                case MediaStatus.PLAYER_STATE_PAUSED: // (== 3)
                    mIsPlaying = false;
                    setupNotification(mCastManager.getRemoteMediaInformation());
                    break;
                case MediaStatus.PLAYER_STATE_IDLE: // (== 1)
                    mIsPlaying = false;
                    if (!mCastManager.shouldRemoteUiBeVisible(mediaStatus,
                            mCastManager.getIdleReason())) {
                        stopForeground(true);
                    } else {
                        setupNotification(mCastManager.getRemoteMediaInformation());
                    }
                    break;
                case MediaStatus.PLAYER_STATE_UNKNOWN: // (== 0)
                    mIsPlaying = false;
                    stopForeground(true);
                    break;
                default:
                    break;
            }
        } catch (TransientNetworkDisconnectionException e) {
            LOGE(TAG, "Failed to update the playback status due to network issues", e);
        } catch (NoConnectionException e) {
            LOGE(TAG, "Failed to update the playback status due to network issues", e);
        }
    }

    /*
     * (non-Javadoc)
     * @see android.app.Service#onDestroy()
     */
    @Override
    public void onDestroy() {
        if (null != mBitmapDecoderTask) {
            mBitmapDecoderTask.cancel(false);
        }
        LOGD(TAG, "onDestroy was called");
        removeNotification();
        if (null != mCastManager && null != mConsumer) {
            mCastManager.removeVideoCastConsumer(mConsumer);
            mCastManager = null;
        }
    }

    /*
     * Build the RemoteViews for the notification. We also need to add the appropriate "back stack"
     * so when user goes into the CastPlayerActivity, she can have a meaningful "back" experience.
     */
    private RemoteViews build(MediaInfo info, Bitmap bitmap, boolean isPlaying)
            throws CastException, TransientNetworkDisconnectionException, NoConnectionException {
        Log.d(TAG, "Build version is: " + Build.VERSION.SDK_INT);
        if (mIsLollipopOrAbove) {
            buildForLollipopAndAbove(info, bitmap, isPlaying);
            return null;
        }
        Bundle mediaWrapper = Utils.fromMediaInfo(mCastManager.getRemoteMediaInformation());
        Intent contentIntent = new Intent(this, mTargetActivity);

        contentIntent.putExtra("media", mediaWrapper);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);

        stackBuilder.addParentStack(mTargetActivity);

        stackBuilder.addNextIntent(contentIntent);
        if (stackBuilder.getIntentCount() > 1) {
            stackBuilder.editIntentAt(1).putExtra("media", mediaWrapper);
        }

        // Gets a PendingIntent containing the entire back stack
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(NOTIFICATION_ID, PendingIntent.FLAG_UPDATE_CURRENT);

        MediaMetadata mm = info.getMetadata();

        RemoteViews rv = new RemoteViews(getPackageName(), R.layout.custom_notification);
        if (mIsIcsOrAbove) {
            addPendingIntents(rv, isPlaying, info);
        }
        if (null != bitmap) {
            rv.setImageViewBitmap(R.id.iconView, bitmap);
        } else {
            bitmap = BitmapFactory.decodeResource(getResources(),
                    R.drawable.dummy_album_art);
            rv.setImageViewBitmap(R.id.iconView, bitmap);
        }
        rv.setTextViewText(R.id.titleView, mm.getString(MediaMetadata.KEY_TITLE));
        String castingTo = getResources().getString(R.string.casting_to_device,
                mCastManager.getDeviceName());
        rv.setTextViewText(R.id.subTitleView, castingTo);
        mNotification = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_stat_action_notification)
                .setContentIntent(resultPendingIntent)
                .setContent(rv)
                .setAutoCancel(false)
                .setOngoing(true)
                .build();

        // to get around a bug in GB version, we add the following line
        // see https://code.google.com/p/android/issues/detail?id=30495
        mNotification.contentView = rv;

        return rv;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void buildForLollipopAndAbove(MediaInfo info, Bitmap bitmap, boolean isPlaying)
            throws CastException, TransientNetworkDisconnectionException, NoConnectionException {

        // Playback PendingIntent
        Intent playbackIntent = new Intent(ACTION_TOGGLE_PLAYBACK);
        playbackIntent.setPackage(getPackageName());
        PendingIntent playbackPendingIntent = PendingIntent
                .getBroadcast(this, 0, playbackIntent, 0);

        // Disconnect PendingIntent
        Intent stopIntent = new Intent(ACTION_STOP);
        stopIntent.setPackage(getPackageName());
        PendingIntent stopPendingIntent = PendingIntent.getBroadcast(this, 0, stopIntent, 0);

        // Main Content PendingIntent
        Bundle mediaWrapper = Utils.fromMediaInfo(mCastManager.getRemoteMediaInformation());
        Intent contentIntent = new Intent(this, mTargetActivity);
        contentIntent.putExtra("media", mediaWrapper);

        // Media metadata
        MediaMetadata mm = info.getMetadata();
        String castingTo = getResources().getString(R.string.casting_to_device,
                mCastManager.getDeviceName());
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(mTargetActivity);
        stackBuilder.addNextIntent(contentIntent);
        if (stackBuilder.getIntentCount() > 1) {
            stackBuilder.editIntentAt(1).putExtra("media", mediaWrapper);
        }
        PendingIntent contentPendingIntent =
                stackBuilder.getPendingIntent(NOTIFICATION_ID, PendingIntent.FLAG_UPDATE_CURRENT);

        mNotification = new Notification.Builder(this)
                .setSmallIcon(R.drawable.ic_stat_action_notification)
                .setContentTitle(mm.getString(MediaMetadata.KEY_TITLE))
                .setContentText(castingTo)
                .setContentIntent(contentPendingIntent)
                .setLargeIcon(bitmap)
                .addAction(isPlaying ? R.drawable.ic_av_pause_light : R.drawable.ic_av_play_light,
                        "Pause", playbackPendingIntent)
                .addAction(R.drawable.ic_cast_stop, "Disconnect", stopPendingIntent)
                .setStyle(new Notification.MediaStyle()
                                .setShowActionsInCompactView(new int[]{0,1}))
                .setOngoing(true)
                .setShowWhen(false)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .build();

    }

    private void addPendingIntents(RemoteViews rv, boolean isPlaying, MediaInfo info) {
        Intent playbackIntent = new Intent(ACTION_TOGGLE_PLAYBACK);
        playbackIntent.setPackage(getPackageName());
        PendingIntent playbackPendingIntent = PendingIntent
                .getBroadcast(this, 0, playbackIntent, 0);

        Intent stopIntent = new Intent(ACTION_STOP);
        stopIntent.setPackage(getPackageName());
        PendingIntent stopPendingIntent = PendingIntent.getBroadcast(this, 0, stopIntent, 0);

        rv.setOnClickPendingIntent(R.id.playPauseView, playbackPendingIntent);
        rv.setOnClickPendingIntent(R.id.removeView, stopPendingIntent);

        if (isPlaying) {
            if (info.getStreamType() == MediaInfo.STREAM_TYPE_LIVE) {
                rv.setImageViewResource(R.id.playPauseView, R.drawable.ic_av_stop_sm_dark);
            } else {
                rv.setImageViewResource(R.id.playPauseView, R.drawable.ic_av_pause_sm_dark);
            }

        } else {
            rv.setImageViewResource(R.id.playPauseView, R.drawable.ic_av_play_sm_dark);
        }
    }

    private void togglePlayback() {
        try {
            mCastManager.togglePlayback();
        } catch (Exception e) {
            LOGE(TAG, "Failed to toggle the playback", e);
        }
    }

    /*
     * We try to disconnect application but even if that fails, we need to remove notification since
     * that is the only way to get rid of it without going to the application
     */
    private void stopApplication() {
        try {
            LOGD(TAG, "Calling stopApplication");
            mCastManager.disconnect();
        } catch (Exception e) {
            LOGE(TAG, "Failed to disconnect application", e);
        }
        LOGD(TAG, "Stopping the notification service");
        stopSelf();
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
}
