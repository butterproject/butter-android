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

package com.google.sample.castcompanionlibrary.cast;

import static com.google.sample.castcompanionlibrary.utils.LogUtils.LOGD;
import static com.google.sample.castcompanionlibrary.utils.LogUtils.LOGE;

import com.google.android.gms.cast.ApplicationMetadata;
import com.google.android.gms.cast.Cast;
import com.google.android.gms.cast.Cast.CastOptions.Builder;
import com.google.android.gms.cast.Cast.MessageReceivedCallback;
import com.google.android.gms.cast.CastDevice;
import com.google.android.gms.cast.CastStatusCodes;
import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.MediaStatus;
import com.google.android.gms.cast.RemoteMediaPlayer;
import com.google.android.gms.cast.RemoteMediaPlayer.MediaChannelResult;
import com.google.android.gms.cast.TextTrackStyle;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.common.images.WebImage;
import com.google.sample.castcompanionlibrary.R;
import com.google.sample.castcompanionlibrary.cast.callbacks.IVideoCastConsumer;
import com.google.sample.castcompanionlibrary.cast.callbacks.VideoCastConsumerImpl;
import com.google.sample.castcompanionlibrary.cast.dialog.video.VideoMediaRouteDialogFactory;
import com.google.sample.castcompanionlibrary.cast.exceptions.CastException;
import com.google.sample.castcompanionlibrary.cast.exceptions.NoConnectionException;
import com.google.sample.castcompanionlibrary.cast.exceptions.OnFailedListener;
import com.google.sample.castcompanionlibrary.cast.exceptions
        .TransientNetworkDisconnectionException;
import com.google.sample.castcompanionlibrary.cast.player.IMediaAuthService;
import com.google.sample.castcompanionlibrary.cast.player.VideoCastControllerActivity;
import com.google.sample.castcompanionlibrary.cast.tracks.TracksPreferenceManager;
import com.google.sample.castcompanionlibrary.notification.VideoCastNotificationService;
import com.google.sample.castcompanionlibrary.remotecontrol.RemoteControlClientCompat;
import com.google.sample.castcompanionlibrary.remotecontrol.RemoteControlHelper;
import com.google.sample.castcompanionlibrary.remotecontrol.VideoIntentReceiver;
import com.google.sample.castcompanionlibrary.utils.FetchBitmapTask;
import com.google.sample.castcompanionlibrary.utils.LogUtils;
import com.google.sample.castcompanionlibrary.utils.Utils;
import com.google.sample.castcompanionlibrary.widgets.IMiniController;
import com.google.sample.castcompanionlibrary.widgets.MiniController;
import com.google.sample.castcompanionlibrary.widgets.MiniController
        .OnMiniControllerChangedListener;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources.NotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.RemoteControlClient;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceScreen;
import android.support.v7.app.MediaRouteDialogFactory;
import android.support.v7.media.MediaRouter.RouteInfo;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.accessibility.CaptioningManager;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.json.JSONObject;

/**
 * A concrete subclass of {@link BaseCastManager} that is suitable for casting video contents (it
 * also provides a single custom data channel/namespace if an out-of-bound communication is
 * needed).
 * <p>
 * This is a singleton that needs to be "initialized" (by calling <code>initialize()</code>) prior
 * to usage. Subsequent to initialization, an easier way to get access to the singleton class is to
 * call a variant of <code>getInstance()</code>. After initialization, callers can enable a number
 * of features (all features are off by default). To do so, call <code>enableFeature()</code> and
 * pass an OR-ed expression built from one ore more of the following constants:
 * <p>
 * <ul>
 * <li>FEATURE_DEBUGGING: to enable GMS level logging</li>
 * <li>FEATURE_NOTIFICATION: to enable system notifications</li>
 * <li>FEATURE_LOCKSCREEN: to enable lock-screen controls on supported versions</li>
 * <li>FEATURE_WIFI_RECONNECT: to enable reconnection logic</li>
 * </ul>
 * Callers can add {@link MiniController} components to their application pages by adding the
 * corresponding widget to their layout xml and then calling <code>addMiniController()</code>. This
 * class manages various states of the remote cast device.Client applications, however, can
 * complement the default behavior of this class by hooking into various callbacks that it provides
 * (see {@link IVideoCastConsumer}). Since the number of these callbacks is usually much larger
 * than what a single application might be interested in, there is a no-op implementation of this
 * interface (see {@link VideoCastConsumerImpl}) that applications can subclass to override only
 * those methods that they are interested in. Since this library depends on the cast
 * functionalities provided by the Google Play services, the library checks to ensure that the
 * right version of that service is installed. It also provides a simple static method
 * <code>checkGooglePlayServices()</code> that clients can call at an early stage of their
 * applications to provide a dialog for users if they need to update/activate their GMS library. To
 * learn more about this library, please read the documentation that is distributed as part of this
 * library.
 */
public class VideoCastManager extends BaseCastManager
        implements OnMiniControllerChangedListener, OnFailedListener {

    public static final String EXTRA_HAS_AUTH = "hasAuth";
    public static final String EXTRA_MEDIA = "media";
    public static final String EXTRA_START_POINT = "startPoint";
    public static final String EXTRA_SHOULD_START = "shouldStart";
    public static final String EXTRA_CUSTOM_DATA = "customData";
    public static final long DEFAULT_LIVE_STREAM_DURATION_MS = 2 * 3600 * 1000L; // 2hrs

    /**
     * Volume can be controlled at two different layers, one is at the "stream" level and one at
     * the "device" level. <code>VolumeType</code> encapsulates these two types.
     */
    public static enum VolumeType {
        STREAM,
        DEVICE;
    }

    private static final String TAG = LogUtils.makeLogTag(VideoCastManager.class);
    private static VideoCastManager sInstance;
    private final Class<?> mTargetActivity;
    private final Set<IMiniController> mMiniControllers;
    private final AudioManager mAudioManager;
    private RemoteMediaPlayer mRemoteMediaPlayer;
    private RemoteControlClientCompat mRemoteControlClientCompat;
    private VolumeType mVolumeType = VolumeType.DEVICE;
    private int mState = MediaStatus.PLAYER_STATE_IDLE;
    private int mIdleReason;
    private final ComponentName mMediaButtonReceiverComponent;
    private final String mDataNamespace;
    private Cast.MessageReceivedCallback mDataChannel;
    private Set<IVideoCastConsumer> mVideoConsumers = Collections
            .synchronizedSet(new HashSet<IVideoCastConsumer>());
    private IMediaAuthService mAuthService;
    private long mLiveStreamDuration = DEFAULT_LIVE_STREAM_DURATION_MS;
    private TracksPreferenceManager mTrackManager;

    /**
     * Initializes the VideoCastManager for clients. Before clients can use VideoCastManager, they
     * need to initialize it by calling this static method. Then clients can obtain an instance of
     * this singleton class by calling {@link getInstance()} or {@link getInstance(Context)}.
     * Failing to initialize this class before requesting an instance will result in a
     * {@link CastException} exception.
     *
     * @param context
     * @param applicationId the unique ID for your application
     * @param targetActivity this points to the activity that should be invoked when user clicks on
     * the icon in the {@link MiniController}. Often this is the activity that hosts the local
     * player.
     * @param dataNamespace if not <code>null</code>, a custom data channel with this namespace
     * will be created.
     * @return
     * @see getInstance()
     */
    public static synchronized VideoCastManager initialize(Context context,
            String applicationId, Class<?> targetActivity, String dataNamespace) {
        if (null == sInstance) {
            LOGD(TAG, "New instance of VideoCastManager is created");
            if (ConnectionResult.SUCCESS != GooglePlayServicesUtil
                    .isGooglePlayServicesAvailable(context)) {
                String msg = "Couldn't find the appropriate version of Google Play Services";
                LOGE(TAG, msg);
            }
            sInstance = new VideoCastManager(context, applicationId, targetActivity, dataNamespace);
            mCastManager = sInstance;
        }
        return sInstance;
    }

    @Override
    protected void onFeaturesUpdated(int capabilities) {
        if (isFeatureEnabled(FEATURE_CAPTIONS_PREFERENCE)) {
            mTrackManager = new TracksPreferenceManager(mContext.getApplicationContext());
            registerCaptionListener(mContext.getApplicationContext());
        }
    }

    /**
     * Returns the initialized instances of this class. If it is not initialized yet, a
     * {@link CastException} will be thrown.
     *
     * @return
     * @throws CastException
     * @see initialize()
     */
    public static VideoCastManager getInstance() throws CastException {
        if (null == sInstance) {
            LOGE(TAG, "No VideoCastManager instance was built, you need to build one first");
            throw new CastException();
        }
        return sInstance;
    }

    /**
     * Returns the initialized instances of this class. If it is not initialized yet, a
     * {@link CastException} will be thrown. The {@link Context} that is passed as the argument
     * will be used to update the context for the <code>VideoCastManager</code> instance. The main
     * purpose of updating context is to enable the library to provide {@link Context} related
     * functionalities, e.g. it can create an error dialog if needed. This method is preferred over
     * the similar one without a context argument.
     *
     * @param context the current Context
     * @return
     * @throws CastException
     * @see {@link initialize()}, {@link setContext()}
     */
    public static VideoCastManager getInstance(Context context) throws CastException {
        if (null == sInstance) {
            LOGE(TAG, "No VideoCastManager instance was built, you need to build one first "
                    + "(called from Context: " + context + ")");
            throw new CastException();
        }
        LOGD(TAG, "Updated context to: " + context);
        sInstance.mContext = context;
        return sInstance;
    }

    protected VideoCastManager(Context context, String applicationId, Class<?> targetActivity,
            String dataNamespace) {
        super(context, applicationId);
        LOGD(TAG, "VideoCastManager is instantiated");
        mDataNamespace = dataNamespace;
        if (null == targetActivity) {
            targetActivity = VideoCastControllerActivity.class;
        }
        mTargetActivity = targetActivity;
        Utils.saveStringToPreference(mContext, PREFS_KEY_CAST_ACTIVITY_NAME,
                mTargetActivity.getName());
        if (null != mDataNamespace) {
            Utils.saveStringToPreference(mContext, PREFS_KEY_CAST_CUSTOM_DATA_NAMESPACE,
                    dataNamespace);
        }

        mMiniControllers = Collections.synchronizedSet(new HashSet<IMiniController>());

        mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        mMediaButtonReceiverComponent = new ComponentName(context, VideoIntentReceiver.class);
    }

    /*============================================================================================*/
    /*========== MiniControllers management ======================================================*/
    /*============================================================================================*/

    /**
     * Updates the information and state of a MiniController.
     *
     * @throws TransientNetworkDisconnectionException
     * @throws NoConnectionException
     */
    private void updateMiniController(IMiniController controller)
            throws TransientNetworkDisconnectionException, NoConnectionException {
        checkConnectivity();
        checkRemoteMediaPlayerAvailable();
        if (mRemoteMediaPlayer.getStreamDuration() > 0 || isRemoteStreamLive()) {
            MediaInfo mediaInfo = getRemoteMediaInformation();
            MediaMetadata mm = mediaInfo.getMetadata();
            controller.setStreamType(mediaInfo.getStreamType());
            controller.setPlaybackStatus(mState, mIdleReason);
            controller.setSubTitle(mContext.getResources().getString(R.string.casting_to_device,
                    mDeviceName));
            controller.setTitle(mm.getString(MediaMetadata.KEY_TITLE));
            if (!mm.getImages().isEmpty()) {
                controller.setIcon(mm.getImages().get(0).getUrl());
            }
        }
    }

    /*
     * Updates the information and state of all MiniControllers
     */
    private void updateMiniControllers() {
        if (null != mMiniControllers) {
            synchronized (mMiniControllers) {
                for (final IMiniController controller : mMiniControllers) {
                    try {
                        updateMiniController(controller);
                    } catch (Exception e) {/* silent failure */
                    }
                }
            }
        }
    }

    /*
     * (non-Javadoc)
     * @see com.google.sample.castcompanionlibrary.widgets.MiniController.
     * OnMiniControllerChangedListener#onPlayPauseClicked(android.view.View)
     * @throws TransientNetworkDisconnectionException
     * @throws NoConnectionException
     * @throws CastException
     */
    @Override
    public void onPlayPauseClicked(View v) throws CastException,
            TransientNetworkDisconnectionException, NoConnectionException {
        checkConnectivity();
        if (mState == MediaStatus.PLAYER_STATE_PLAYING) {
            pause();
        } else {
            boolean isLive = isRemoteStreamLive();
            if ((mState == MediaStatus.PLAYER_STATE_PAUSED && !isLive)
                    || (mState == MediaStatus.PLAYER_STATE_IDLE && isLive)) {
                play();
            }
        }
    }

    /*
     * (non-Javadoc)
     * @see com.google.sample.castcompanionlibrary.widgets.MiniController.
     * OnMiniControllerChangedListener #onTargetActivityInvoked(android.content.Context)
     */
    @Override
    public void onTargetActivityInvoked(Context ctx) throws TransientNetworkDisconnectionException,
            NoConnectionException {
        Intent intent = new Intent(ctx, mTargetActivity);
        intent.putExtra(EXTRA_MEDIA, Utils.fromMediaInfo(getRemoteMediaInformation()));
        ctx.startActivity(intent);
    }

    /**
     * Updates the visibility of the mini controllers. In most cases, clients do not need to use
     * this as the {@link VideoCastManager} handles the visibility.
     *
     * @param visible
     */
    public void updateMiniControllersVisibility(boolean visible) {
        LOGD(TAG, "updateMiniControllersVisibility() reached with visibility: " + visible);
        if (null != mMiniControllers) {
            synchronized (mMiniControllers) {
                for (IMiniController controller : mMiniControllers) {
                    controller.setVisibility(visible ? View.VISIBLE : View.GONE);
                }
            }
        }
    }

    /*============================================================================================*/
    /*========== VideoCastControllerActivity management ==========================================*/
    /*============================================================================================*/

    /**
     * Launches the VideoCastControllerActivity that provides a default Cast Player page.
     *
     * @param context
     * @param mediaWrapper a bundle wrapper for the media that is or will be casted
     * @param position (in milliseconds) is the starting point of the media playback
     * @param shouldStart indicates if the remote playback should start after launching the new
     * page
     * @param customData Optional {@link JSONObject}
     */
    public void startCastControllerActivity(Context context, Bundle mediaWrapper, int position,
            boolean shouldStart, JSONObject customData) {
        Intent intent = new Intent(context, VideoCastControllerActivity.class);
        intent.putExtra(EXTRA_MEDIA, mediaWrapper);
        intent.putExtra(EXTRA_START_POINT, position);
        intent.putExtra(EXTRA_SHOULD_START, shouldStart);
        if (null != customData) {
            intent.putExtra(EXTRA_CUSTOM_DATA, customData.toString());
        }
        context.startActivity(intent);
    }

    /**
     * Launches the {@link VideoCastControllerActivity} that provides a default Cast Player page.
     *
     * @param context
     * @param mediaWrapper a bundle wrapper for the media that is or will be casted
     * @param position (in milliseconds) is the starting point of the media playback
     * @param shouldStart indicates if the remote playback should start after launching the new
     * page
     */
    public void startCastControllerActivity(Context context, Bundle mediaWrapper, int position,
            boolean shouldStart) {
        Intent intent = new Intent(context, VideoCastControllerActivity.class);
        intent.putExtra(EXTRA_MEDIA, mediaWrapper);
        intent.putExtra(EXTRA_START_POINT, position);
        intent.putExtra(EXTRA_SHOULD_START, shouldStart);
        context.startActivity(intent);
    }

    /**
     * Launches the {@link VideoCastControllerActivity} that provides a default Cast Player page.
     * This variation should be used when an {@link IMediaAuthService} needs to be used.
     *
     * @param context
     * @param authService
     */
    public void startCastControllerActivity(Context context, IMediaAuthService authService) {
        if (null != authService) {
            this.mAuthService = authService;
            Intent intent = new Intent(context, VideoCastControllerActivity.class);
            intent.putExtra(EXTRA_HAS_AUTH, true);
            context.startActivity(intent);
        }
    }

    /**
     * Launches the {@link VideoCastControllerActivity} that provides a default Cast Player page.
     *
     * @param ctx
     * @param mediaInfo pointing to the media that is or will be casted
     * @param position (in milliseconds) is the starting point of the media playback
     * @param shouldStart indicates if the remote playback should start after launching the new
     * page
     */
    public void startCastControllerActivity(Context ctx,
            MediaInfo mediaInfo, int position, boolean shouldStart) {
        startCastControllerActivity(ctx, Utils.fromMediaInfo(mediaInfo), position, shouldStart);
    }

    /**
     * Returns the instance of {@link IMediaAuthService}, or null if there is no such instance.
     *
     * @return
     */
    public IMediaAuthService getMediaAuthService() {
        return mAuthService;
    }

    /**
     * Removes the pointer to the {@link IMediaAuthService} so it can be GC.
     */
    public void removeMediaAuthService() {
        mAuthService = null;
    }

    /*============================================================================================*/
    /*========== Utility Methods =================================================================*/
    /*============================================================================================*/

    /**
     * Returns the active {@link RemoteMediaPlayer} instance. Since there are a number of media
     * control APIs that this library do not provide a wrapper for, client applications can call
     * those methods directly after obtaining an instance of the active {@link RemoteMediaPlayer}.
     *
     * @return
     */
    public final RemoteMediaPlayer getRemoteMediaPlayer() {
        return mRemoteMediaPlayer;
    }

    /**
     * Determines if the media that is loaded remotely is a live stream or not.
     *
     * @return
     * @throws TransientNetworkDisconnectionException
     * @throws NoConnectionException
     */
    public final boolean isRemoteStreamLive() throws TransientNetworkDisconnectionException,
            NoConnectionException {
        checkConnectivity();
        MediaInfo info = getRemoteMediaInformation();
        return null != info && info.getStreamType() == MediaInfo.STREAM_TYPE_LIVE;
    }

    /**
     * A helper method to determine if, given a player state and an idle reason (if the state is
     * idle) will warrant having a UI for remote presentation of the remote content.
     *
     * @param state
     * @param idleReason
     * @return
     * @throws TransientNetworkDisconnectionException
     * @throws NoConnectionException
     */
    public boolean shouldRemoteUiBeVisible(int state, int idleReason)
            throws TransientNetworkDisconnectionException,
            NoConnectionException {
        switch (state) {
            case MediaStatus.PLAYER_STATE_PLAYING:
            case MediaStatus.PLAYER_STATE_PAUSED:
            case MediaStatus.PLAYER_STATE_BUFFERING:
                return true;
            case MediaStatus.PLAYER_STATE_IDLE:
                if (!isRemoteStreamLive()) {
                    return false;
                }
                return idleReason == MediaStatus.IDLE_REASON_CANCELED;
            default:
                break;
        }
        return false;
    }

    /*
     * A simple check to make sure mRemoteMediaPlayer is not null
     */
    private void checkRemoteMediaPlayerAvailable() throws NoConnectionException {
        if (null == mRemoteMediaPlayer) {
            throw new NoConnectionException();
        }
    }

    /**
     * Sets the type of volume.
     *
     * @param vType
     */
    public final void setVolumeType(VolumeType vType) {
        mVolumeType = vType;
    }

    /**
     * Returns the url for the movie that is currently playing on the remote device. If there is no
     * connection, this will return <code>null</code>.
     *
     * @return
     * @throws NoConnectionException If no connectivity to the device exists
     * @throws TransientNetworkDisconnectionException If framework is still trying to recover from
     * a possibly transient loss of network
     */
    public String getRemoteMovieUrl() throws TransientNetworkDisconnectionException,
            NoConnectionException {
        checkConnectivity();
        if (null != mRemoteMediaPlayer && null != mRemoteMediaPlayer.getMediaInfo()) {
            MediaInfo info = mRemoteMediaPlayer.getMediaInfo();
            mRemoteMediaPlayer.getMediaStatus().getPlayerState();
            return info.getContentId();
        } else {
            throw new NoConnectionException();
        }
    }

    /**
     * Indicates if the remote movie is currently playing (or buffering).
     *
     * @return
     * @throws NoConnectionException
     * @throws TransientNetworkDisconnectionException
     */
    public boolean isRemoteMoviePlaying() throws TransientNetworkDisconnectionException,
            NoConnectionException {
        checkConnectivity();
        return mState == MediaStatus.PLAYER_STATE_BUFFERING
                || mState == MediaStatus.PLAYER_STATE_PLAYING;
    }

    /**
     * Returns <code>true</code> if the remote connected device is playing a movie.
     *
     * @return
     * @throws NoConnectionException
     * @throws TransientNetworkDisconnectionException
     */
    public boolean isRemoteMoviePaused() throws TransientNetworkDisconnectionException,
            NoConnectionException {
        checkConnectivity();
        return mState == MediaStatus.PLAYER_STATE_PAUSED;
    }

    /**
     * Returns <code>true</code> only if there is a media on the remote being played, paused or
     * buffered.
     *
     * @return
     * @throws NoConnectionException
     * @throws TransientNetworkDisconnectionException
     */
    public boolean isRemoteMediaLoaded() throws TransientNetworkDisconnectionException,
            NoConnectionException {
        checkConnectivity();
        return isRemoteMoviePaused() || isRemoteMoviePlaying();
    }

    /**
     * Returns the {@link MediaInfo} for the current media
     *
     * @return
     * @throws NoConnectionException If no connectivity to the device exists
     * @throws TransientNetworkDisconnectionException If framework is still trying to recover from
     * a possibly transient loss of network
     */
    public MediaInfo getRemoteMediaInformation() throws TransientNetworkDisconnectionException,
            NoConnectionException {
        checkConnectivity();
        checkRemoteMediaPlayerAvailable();
        return mRemoteMediaPlayer.getMediaInfo();
    }

    /**
     * Gets the remote's system volume. If no device is connected to, or if an exception is thrown,
     * this returns -1. It internally detects what type of volume is used.
     *
     * @throws NoConnectionException If no connectivity to the device exists
     * @throws TransientNetworkDisconnectionException If framework is still trying to recover from
     * a possibly transient loss of network
     */
    public double getVolume() throws TransientNetworkDisconnectionException, NoConnectionException {
        checkConnectivity();
        if (mVolumeType == VolumeType.STREAM) {
            checkRemoteMediaPlayerAvailable();
            return mRemoteMediaPlayer.getMediaStatus().getStreamVolume();
        } else {
            return Cast.CastApi.getVolume(mApiClient);
        }
    }

    /**
     * Sets the volume. It internally determines if this should be done for <code>stream</code> or
     * <code>device</code> volume.
     *
     * @param volume Should be a value between 0 and 1, inclusive.
     * @throws NoConnectionException
     * @throws TransientNetworkDisconnectionException
     * @throws CastException If setting system volume fails
     */
    public void setVolume(double volume) throws CastException,
            TransientNetworkDisconnectionException, NoConnectionException {
        checkConnectivity();
        if (volume > 1.0) {
            volume = 1.0;
        } else if (volume < 0) {
            volume = 0.0;
        }
        if (mVolumeType == VolumeType.STREAM) {
            checkRemoteMediaPlayerAvailable();
            mRemoteMediaPlayer.setStreamVolume(mApiClient, volume).setResultCallback(
                    new ResultCallback<RemoteMediaPlayer.MediaChannelResult>() {

                        @Override
                        public void onResult(MediaChannelResult result) {
                            if (!result.getStatus().isSuccess()) {
                                onFailed(R.string.failed_setting_volume,
                                        result.getStatus().getStatusCode());
                            }
                        }
                    }
            );
        } else {
            try {
                Cast.CastApi.setVolume(mApiClient, volume);
            } catch (IOException e) {
                throw new CastException(e);
            } catch (IllegalStateException e) {
                throw new CastException(e);
            } catch (IllegalArgumentException e) {
                throw new CastException(e);
            }
        }
    }

    /**
     * Increments (or decrements) the volume by the given amount. It internally determines if this
     * should be done for <code>stream</code> or <code>device</code> volume.
     *
     * @param delta
     * @throws NoConnectionException If no connectivity to the device exists
     * @throws TransientNetworkDisconnectionException If framework is still trying to recover from
     * a possibly transient loss of network
     * @throws CastException
     */
    public void incrementVolume(double delta) throws CastException,
            TransientNetworkDisconnectionException, NoConnectionException {
        checkConnectivity();
        double vol = getVolume() + delta;
        if (vol > 1) {
            vol = 1;
        } else if (vol < 0) {
            vol = 0;
        }
        setVolume(vol);
    }

    /**
     * Increments or decrements volume by <code>delta</code> if <code>delta &gt; 0</code> or
     * <code>delta &lt; 0</code>, respectively. Note that the volume range is between 0 and {@link
     * RouteInfo.getVolumeMax()}.
     *
     * @param delta
     */
    public void updateVolume(int delta) {
        if (null != mMediaRouter.getSelectedRoute()) {
            RouteInfo info = mMediaRouter.getSelectedRoute();
            info.requestUpdateVolume(delta);
        }
    }

    /**
     * Returns <code>true</code> if remote device is muted. It internally determines if this should
     * be done for <code>stream</code> or <code>device</code> volume.
     *
     * @return
     * @throws NoConnectionException
     * @throws TransientNetworkDisconnectionException
     */
    public boolean isMute() throws TransientNetworkDisconnectionException, NoConnectionException {
        checkConnectivity();
        if (mVolumeType == VolumeType.STREAM) {
            checkRemoteMediaPlayerAvailable();
            return mRemoteMediaPlayer.getMediaStatus().isMute();
        } else {
            return Cast.CastApi.isMute(mApiClient);
        }
    }

    /**
     * Mutes or un-mutes the volume. It internally determines if this should be done for
     * <code>stream</code> or <code>device</code> volume.
     *
     * @param mute
     * @throws CastException
     * @throws NoConnectionException
     * @throws TransientNetworkDisconnectionException
     */
    public void setMute(boolean mute) throws CastException, TransientNetworkDisconnectionException,
            NoConnectionException {
        checkConnectivity();
        if (mVolumeType == VolumeType.STREAM) {
            checkRemoteMediaPlayerAvailable();
            mRemoteMediaPlayer.setStreamMute(mApiClient, mute);
        } else {
            try {
                Cast.CastApi.setMute(mApiClient, mute);
            } catch (Exception e) {
                LOGE(TAG, "Failed to set volume", e);
                throw new CastException("Failed to set volume", e);
            }
        }
    }

    /**
     * Returns the duration of the media that is loaded, in milliseconds.
     *
     * @return
     * @throws NoConnectionException
     * @throws TransientNetworkDisconnectionException
     */
    public long getMediaDuration() throws TransientNetworkDisconnectionException,
            NoConnectionException {
        checkConnectivity();
        checkRemoteMediaPlayerAvailable();
        return mRemoteMediaPlayer.getStreamDuration();
    }

    /**
     * Returns the time left (in milliseconds) of the current media. If there is no
     * {@code RemoteMediaPlayer}, it returns -1.
     *
     * @throws TransientNetworkDisconnectionException
     * @throws NoConnectionException
     */
    public long getTimeLeftForMedia()
            throws TransientNetworkDisconnectionException, NoConnectionException {
        checkConnectivity();
        if (null == mRemoteMediaPlayer) {
            return -1;
        }
        return isRemoteStreamLive() ? mLiveStreamDuration : mRemoteMediaPlayer.getStreamDuration() -
                mRemoteMediaPlayer.getApproximateStreamPosition();
    }

    /**
     * Returns the current (approximate) position of the current media, in milliseconds.
     *
     * @return
     * @throws NoConnectionException
     * @throws TransientNetworkDisconnectionException
     */
    public long getCurrentMediaPosition() throws TransientNetworkDisconnectionException,
            NoConnectionException {
        checkConnectivity();
        checkRemoteMediaPlayerAvailable();
        return mRemoteMediaPlayer.getApproximateStreamPosition();
    }

    /**
     * Returns the target activity that points to the player page
     *
     * @return
     */
    public Class<?> getTargetActivity() {
        return mTargetActivity;
    }

    /*============================================================================================*/
    /*========== Notification Service ============================================================*/
    /*============================================================================================*/

    /*
     * Starts a service that can last beyond the lifetime of the application to provide
     * notifications. The service brings itself down when needed. The service will be started only
     * if the notification feature has been enabled during the initialization.
     * @see {@link BaseCastManager#enableFeatures()}
     */
    private boolean startNotificationService(boolean visibility) {
        if (!isFeatureEnabled(FEATURE_NOTIFICATION)) {
            return true;
        }
        LOGD(TAG, "startNotificationService()");
        Intent service = new Intent(mContext, VideoCastNotificationService.class);
        service.setPackage(mContext.getPackageName());
        service.setAction(VideoCastNotificationService.ACTION_VISIBILITY);
        service.putExtra(VideoCastNotificationService.NOTIFICATION_VISIBILITY, !mUiVisible);
        return null != mContext.startService(service);
    }

    private void stopNotificationService() {
        if (!isFeatureEnabled(FEATURE_NOTIFICATION)) {
            return;
        }
        if (null != mContext) {
            LOGD(TAG, "stopNotificationService(): Stopping the notification service");
            mContext.stopService(new Intent(mContext, VideoCastNotificationService.class));
        }
    }

    /*============================================================================================*/
    /*========== Implementing Cast.Listener ======================================================*/
    /*============================================================================================*/

    private void onApplicationDisconnected(int errorCode) {
        LOGD(TAG, "onApplicationDisconnected() reached with error code: " + errorCode);
        updateRemoteControl(false);
        if (null != mRemoteControlClientCompat && isFeatureEnabled(FEATURE_LOCKSCREEN)) {
            mRemoteControlClientCompat.removeFromMediaRouter(mMediaRouter);
        }
        synchronized (mVideoConsumers) {
            for (IVideoCastConsumer consumer : mVideoConsumers) {
                try {
                    consumer.onApplicationDisconnected(errorCode);
                } catch (Exception e) {
                    LOGE(TAG, "onApplicationDisconnected(): Failed to inform " + consumer, e);
                }
            }
        }
        if (null != mMediaRouter) {
            LOGD(TAG, "onApplicationDisconnected(): Cached RouteInfo: " + getRouteInfo());
            LOGD(TAG, "onApplicationDisconnected(): Selected RouteInfo: " +
                    mMediaRouter.getSelectedRoute());
            if (mMediaRouter.getSelectedRoute().equals(getRouteInfo())) {
                LOGD(TAG, "onApplicationDisconnected(): Setting route to default");
                mMediaRouter.selectRoute(mMediaRouter.getDefaultRoute());
            }
        }
        onDeviceSelected(null);
        updateMiniControllersVisibility(false);
        stopNotificationService();
    }

    private void onApplicationStatusChanged() {
        String appStatus = null;
        if (!isConnected()) {
            return;
        }
        try {
            appStatus = Cast.CastApi.getApplicationStatus(mApiClient);
            LOGD(TAG, "onApplicationStatusChanged() reached: "
                    + Cast.CastApi.getApplicationStatus(mApiClient));
            synchronized (mVideoConsumers) {
                for (IVideoCastConsumer consumer : mVideoConsumers) {
                    try {
                        consumer.onApplicationStatusChanged(appStatus);
                    } catch (Exception e) {
                        LOGE(TAG, "onApplicationStatusChanged(): Failed to inform " + consumer, e);
                    }
                }
            }
        } catch (IllegalStateException e1) {
            // no use in logging this
        }
    }

    private void onVolumeChanged() {
        LOGD(TAG, "onVolumeChanged() reached");
        double volume = 0;
        try {
            volume = getVolume();
            boolean isMute = isMute();
            synchronized (mVideoConsumers) {
                for (IVideoCastConsumer consumer : mVideoConsumers) {
                    try {
                        consumer.onVolumeChanged(volume, isMute);
                    } catch (Exception e) {
                        LOGE(TAG, "onVolumeChanged(): Failed to inform " + consumer, e);
                    }
                }
            }
        } catch (Exception e1) {
            LOGE(TAG, "Failed to get volume", e1);
        }

    }

    @Override
    protected void onApplicationConnected(ApplicationMetadata appMetadata,
            String applicationStatus, String sessionId, boolean wasLaunched) {
        LOGD(TAG, "onApplicationConnected() reached with sessionId: " + sessionId
                + ", and mReconnectionStatus=" + mReconnectionStatus);

        if (mReconnectionStatus == ReconnectionStatus.IN_PROGRESS) {
            // we have tried to reconnect and successfully launched the app, so
            // it is time to select the route and make the cast icon happy :-)
            List<RouteInfo> routes = mMediaRouter.getRoutes();
            if (null != routes) {
                String routeId = Utils.getStringFromPreference(mContext, PREFS_KEY_ROUTE_ID);
                for (RouteInfo routeInfo : routes) {
                    if (routeId.equals(routeInfo.getId())) {
                        // found the right route
                        LOGD(TAG, "Found the correct route during reconnection attempt");
                        mReconnectionStatus = ReconnectionStatus.FINALIZE;
                        mMediaRouter.selectRoute(routeInfo);
                        break;
                    }
                }
            }
        }

        startNotificationService(mUiVisible);
        try {
            attachDataChannel();
            attachMediaChannel();
            mSessionId = sessionId;
            // saving device for future retrieval; we only save the last session info
            Utils.saveStringToPreference(mContext, PREFS_KEY_SESSION_ID, mSessionId);
            mRemoteMediaPlayer.requestStatus(mApiClient).
                    setResultCallback(new ResultCallback<RemoteMediaPlayer.MediaChannelResult>() {

                        @Override
                        public void onResult(MediaChannelResult result) {
                            if (!result.getStatus().isSuccess()) {
                                onFailed(R.string.failed_status_request,
                                        result.getStatus().getStatusCode());
                            }

                        }
                    });
            synchronized (mVideoConsumers) {
                for (IVideoCastConsumer consumer : mVideoConsumers) {
                    try {
                        consumer.onApplicationConnected(appMetadata, mSessionId, wasLaunched);
                    } catch (Exception e) {
                        LOGE(TAG, "onApplicationConnected(): Failed to inform " + consumer, e);
                    }
                }
            }
        } catch (TransientNetworkDisconnectionException e) {
            LOGE(TAG, "Failed to attach media/data channel due to network issues", e);
            onFailed(R.string.failed_no_connection_trans, NO_STATUS_CODE);
        } catch (NoConnectionException e) {
            LOGE(TAG, "Failed to attach media/data channel due to network issues", e);
            onFailed(R.string.failed_no_connection, NO_STATUS_CODE);
        }

    }

    /*
     * (non-Javadoc)
     * @see com.google.sample.castcompanionlibrary.cast.BaseCastManager# onConnectivityRecovered()
     */
    @Override
    public void onConnectivityRecovered() {
        reattachMediaChannel();
        reattachDataChannel();
        super.onConnectivityRecovered();
    }

    /*
     * (non-Javadoc)
     * @see com.google.android.gms.cast.CastClient.Listener#onApplicationStopFailed (int)
     */
    @Override
    public void onApplicationStopFailed(int errorCode) {
        synchronized (mVideoConsumers) {
            for (IVideoCastConsumer consumer : mVideoConsumers) {
                try {
                    consumer.onApplicationStopFailed(errorCode);
                } catch (Exception e) {
                    LOGE(TAG, "onApplicationLaunched(): Failed to inform " + consumer, e);
                }
            }
        }
    }

    @Override
    public void onApplicationConnectionFailed(int errorCode) {
        LOGD(TAG, "onApplicationConnectionFailed() reached with errorCode: " + errorCode);
        if (mReconnectionStatus == ReconnectionStatus.IN_PROGRESS) {
            if (errorCode == CastStatusCodes.APPLICATION_NOT_RUNNING) {
                // while trying to re-establish session, we
                // found out that the app is not running so we need
                // to disconnect
                mReconnectionStatus = ReconnectionStatus.INACTIVE;
                onDeviceSelected(null);
            }
            return;
        } else {
            boolean showError = false;
            synchronized (mVideoConsumers) {
                for (IVideoCastConsumer consumer : mVideoConsumers) {
                    try {
                        showError = showError || consumer.onApplicationConnectionFailed(errorCode);
                    } catch (Exception e) {
                        LOGE(TAG, "onApplicationConnectionFailed(): Failed to inform " + consumer, e);
                    }
                }
            }
            if (showError) {
                switch (errorCode) {
                    case CastStatusCodes.APPLICATION_NOT_FOUND:
                        LOGD(TAG, "onApplicationConnectionFailed(): failed due to: " +
                                "ERROR_APPLICATION_NOT_FOUND");
                        Utils.showErrorDialog(mContext, R.string.failed_to_find_app);
                        break;
                    case CastStatusCodes.TIMEOUT:
                        LOGD(TAG, "onApplicationConnectionFailed(): failed due to: ERROR_TIMEOUT");
                        Utils.showErrorDialog(mContext, R.string.failed_app_launch_timeout);
                        break;
                    default:
                        LOGD(TAG, "onApplicationConnectionFailed(): failed due to: errorcode="
                                + errorCode);
                        Utils.showErrorDialog(mContext, R.string.failed_to_launch_app);
                        break;
                }
            }

            onDeviceSelected(null);
            if (null != mMediaRouter) {
                LOGD(TAG, "onApplicationConnectionFailed(): Setting route to default");
                mMediaRouter.selectRoute(mMediaRouter.getDefaultRoute());
            }
        }
    }

    /*============================================================================================*/
    /*========== Playback methods ================================================================*/
    /*============================================================================================*/

    /**
     * Loads a media. For this to succeed, you need to have successfully launched the application.
     *
     * @param media
     * @param autoPlay If <code>true</code>, playback starts after load
     * @param position Where to start the playback (only used if autoPlay is <code>true</code>.
     * Units is milliseconds.
     * @throws NoConnectionException
     * @throws TransientNetworkDisconnectionException
     */
    public void loadMedia(MediaInfo media, boolean autoPlay, int position)
            throws TransientNetworkDisconnectionException, NoConnectionException {
        loadMedia(media, autoPlay, position, null);
    }

    /**
     * Loads a media. For this to succeed, you need to have successfully launched the application.
     *
     * @param media
     * @param autoPlay If <code>true</code>, playback starts after load
     * @param position Where to start the playback (only used if autoPlay is <code>true</code>).
     * Units is milliseconds.
     * @param customData Optional {@link JSONObject} data to be passed to the cast device
     * @throws NoConnectionException
     * @throws TransientNetworkDisconnectionException
     */
    public void loadMedia(MediaInfo media, boolean autoPlay, int position, JSONObject customData)
            throws TransientNetworkDisconnectionException, NoConnectionException {
        loadMedia(media, null, autoPlay, position, customData);
    }

    /**
     * Loads a media. For this to succeed, you need to have successfully launched the application.
     *
     * @param media
     * @param activeTracks An array containing the list of track IDs to be set active for this
     * media upon a successful load
     * @param autoPlay If <code>true</code>, playback starts after load
     * @param position Where to start the playback (only used if autoPlay is <code>true</code>).
     * Units is milliseconds.
     * @param customData Optional {@link JSONObject} data to be passed to the cast device
     * @throws NoConnectionException
     * @throws TransientNetworkDisconnectionException
     */
    public void loadMedia(MediaInfo media, final long[] activeTracks, boolean autoPlay,
            int position, JSONObject customData)
            throws TransientNetworkDisconnectionException, NoConnectionException {
        LOGD(TAG, "loadMedia");
        checkConnectivity();
        if (media == null) {
            return;
        }
        if (mRemoteMediaPlayer == null) {
            LOGE(TAG, "Trying to load a video with no active media session");
            throw new NoConnectionException();
        }

        mRemoteMediaPlayer.load(mApiClient, media, autoPlay, position, customData)
                .setResultCallback(new ResultCallback<RemoteMediaPlayer.MediaChannelResult>() {

                    @Override
                    public void onResult(MediaChannelResult result) {
                        if (!result.getStatus().isSuccess()) {
                            onFailed(R.string.failed_load, result.getStatus().getStatusCode());
                        } else if (activeTracks != null) {
                            setActiveTrackIds(activeTracks);
                        }
                    }
                });
    }

    /**
     * Plays the loaded media.
     *
     * @param position Where to start the playback. Units is milliseconds.
     * @throws NoConnectionException
     * @throws TransientNetworkDisconnectionException
     */
    public void play(int position) throws TransientNetworkDisconnectionException,
            NoConnectionException {
        checkConnectivity();
        LOGD(TAG, "attempting to play media at position " + position + " seconds");
        if (mRemoteMediaPlayer == null) {
            LOGE(TAG, "Trying to play a video with no active media session");
            throw new NoConnectionException();
        }
        seekAndPlay(position);
    }

    /**
     * Resumes the playback from where it was left (can be the beginning).
     *
     * @param customData Optional {@link JSONObject} data to be passed to the cast device
     * @throws NoConnectionException
     * @throws TransientNetworkDisconnectionException
     */
    public void play(JSONObject customData) throws
            TransientNetworkDisconnectionException, NoConnectionException {
        LOGD(TAG, "play(customData)");
        checkConnectivity();
        if (mRemoteMediaPlayer == null) {
            LOGE(TAG, "Trying to play a video with no active media session");
            throw new NoConnectionException();
        }
        mRemoteMediaPlayer.play(mApiClient, customData)
                .setResultCallback(new ResultCallback<MediaChannelResult>() {

                    @Override
                    public void onResult(MediaChannelResult result) {
                        if (!result.getStatus().isSuccess()) {
                            onFailed(R.string.failed_to_play, result.getStatus().getStatusCode());
                        }
                    }

                });
    }

    /**
     * Resumes the playback from where it was left (can be the beginning).
     *
     * @throws CastException
     * @throws NoConnectionException
     * @throws TransientNetworkDisconnectionException
     */
    public void play() throws CastException, TransientNetworkDisconnectionException,
            NoConnectionException {
        play(null);
    }

    /**
     * Stops the playback of media/stream
     *
     * @param customData Optional {@link JSONObject}
     * @throws TransientNetworkDisconnectionException
     * @throws NoConnectionException
     */
    public void stop(JSONObject customData) throws
            TransientNetworkDisconnectionException, NoConnectionException {
        LOGD(TAG, "stop()");
        checkConnectivity();
        if (mRemoteMediaPlayer == null) {
            LOGE(TAG, "Trying to stop a stream with no active media session");
            throw new NoConnectionException();
        }
        mRemoteMediaPlayer.stop(mApiClient, customData).setResultCallback(
                new ResultCallback<MediaChannelResult>() {

                    @Override
                    public void onResult(MediaChannelResult result) {
                        if (!result.getStatus().isSuccess()) {
                            onFailed(R.string.failed_to_stop, result.getStatus().getStatusCode());
                        }
                    }

                }
        );
    }

    /**
     * Stops the playback of media/stream
     *
     * @throws CastException
     * @throws TransientNetworkDisconnectionException
     * @throws NoConnectionException
     */
    public void stop() throws CastException,
            TransientNetworkDisconnectionException, NoConnectionException {
        stop(null);
    }

    /**
     * Pauses the playback.
     *
     * @throws CastException
     * @throws NoConnectionException
     * @throws TransientNetworkDisconnectionException
     */
    public void pause() throws CastException, TransientNetworkDisconnectionException,
            NoConnectionException {
        pause(null);
    }

    /**
     * Pauses the playback.
     *
     * @param customData Optional {@link JSONObject} data to be passed to the cast device
     * @throws NoConnectionException
     * @throws TransientNetworkDisconnectionException
     */
    public void pause(JSONObject customData) throws
            TransientNetworkDisconnectionException, NoConnectionException {
        LOGD(TAG, "attempting to pause media");
        checkConnectivity();
        if (mRemoteMediaPlayer == null) {
            LOGE(TAG, "Trying to pause a video with no active media session");
            throw new NoConnectionException();
        }
        mRemoteMediaPlayer.pause(mApiClient, customData)
                .setResultCallback(new ResultCallback<MediaChannelResult>() {

                    @Override
                    public void onResult(MediaChannelResult result) {
                        if (!result.getStatus().isSuccess()) {
                            onFailed(R.string.failed_to_pause, result.getStatus().getStatusCode());
                        }
                    }

                });
    }

    /**
     * Seeks to the given point without changing the state of the player, i.e. after seek is
     * completed, it resumes what it was doing before the start of seek.
     *
     * @param position in milliseconds
     * @throws NoConnectionException
     * @throws TransientNetworkDisconnectionException
     */
    public void seek(int position) throws TransientNetworkDisconnectionException,
            NoConnectionException {
        LOGD(TAG, "attempting to seek media");
        checkConnectivity();
        if (mRemoteMediaPlayer == null) {
            LOGE(TAG, "Trying to seek a video with no active media session");
            throw new NoConnectionException();
        }
        mRemoteMediaPlayer.seek(mApiClient,
                position,
                RemoteMediaPlayer.RESUME_STATE_UNCHANGED).
                setResultCallback(new ResultCallback<MediaChannelResult>() {

                    @Override
                    public void onResult(MediaChannelResult result) {
                        if (!result.getStatus().isSuccess()) {
                            onFailed(R.string.failed_seek, result.getStatus().getStatusCode());
                        }
                    }

                });
    }

    /**
     * Seeks to the given point and starts playback regardless of the starting state.
     *
     * @param position in milliseconds
     * @throws NoConnectionException
     * @throws TransientNetworkDisconnectionException
     */
    public void seekAndPlay(int position) throws TransientNetworkDisconnectionException,
            NoConnectionException {
        LOGD(TAG, "attempting to seek media");
        checkConnectivity();
        if (mRemoteMediaPlayer == null) {
            LOGE(TAG, "Trying to seekAndPlay a video with no active media session");
            throw new NoConnectionException();
        }
        ResultCallback<MediaChannelResult> resultCallback =
                new ResultCallback<MediaChannelResult>() {

                    @Override
                    public void onResult(MediaChannelResult result) {
                        if (!result.getStatus().isSuccess()) {
                            onFailed(R.string.failed_seek, result.getStatus().getStatusCode());
                        }
                    }

                };
        mRemoteMediaPlayer.seek(mApiClient,
                position,
                RemoteMediaPlayer.RESUME_STATE_PLAY).setResultCallback(resultCallback);
    }

    /**
     * Toggles the playback of the movie.
     *
     * @throws CastException
     * @throws NoConnectionException
     * @throws TransientNetworkDisconnectionException
     */
    public void togglePlayback() throws CastException, TransientNetworkDisconnectionException,
            NoConnectionException {
        checkConnectivity();
        boolean isPlaying = isRemoteMoviePlaying();
        if (isPlaying) {
            pause();
        } else {
            if (mState == MediaStatus.PLAYER_STATE_IDLE
                    && mIdleReason == MediaStatus.IDLE_REASON_FINISHED) {
                loadMedia(getRemoteMediaInformation(), true, 0);
            } else {
                play();
            }
        }
    }

    private void attachMediaChannel() throws TransientNetworkDisconnectionException,
            NoConnectionException {
        LOGD(TAG, "attachMedia()");
        checkConnectivity();
        if (null == mRemoteMediaPlayer) {
            mRemoteMediaPlayer = new RemoteMediaPlayer();

            mRemoteMediaPlayer.setOnStatusUpdatedListener(
                    new RemoteMediaPlayer.OnStatusUpdatedListener() {

                        @Override
                        public void onStatusUpdated() {
                            LOGD(TAG, "RemoteMediaPlayer::onStatusUpdated() is reached");
                            VideoCastManager.this.onRemoteMediaPlayerStatusUpdated();
                        }
                    }
            );

            mRemoteMediaPlayer.setOnMetadataUpdatedListener(
                    new RemoteMediaPlayer.OnMetadataUpdatedListener() {
                        @Override
                        public void onMetadataUpdated() {
                            LOGD(TAG, "RemoteMediaPlayer::onMetadataUpdated() is reached");
                            VideoCastManager.this.onRemoteMediaPlayerMetadataUpdated();
                        }
                    }
            );

        }
        try {
            LOGD(TAG, "Registering MediaChannel namespace");
            Cast.CastApi.setMessageReceivedCallbacks(mApiClient, mRemoteMediaPlayer.getNamespace(),
                    mRemoteMediaPlayer);
        } catch (Exception e) {
            LOGE(TAG, "Failed to set up media channel", e);
        }
    }

    private void reattachMediaChannel() {
        if (null != mRemoteMediaPlayer && null != mApiClient) {
            try {
                LOGD(TAG, "Registering MediaChannel namespace");
                Cast.CastApi.setMessageReceivedCallbacks(mApiClient,
                        mRemoteMediaPlayer.getNamespace(), mRemoteMediaPlayer);
            } catch (IOException e) {
                LOGE(TAG, "Failed to setup media channel", e);
            } catch (IllegalStateException e) {
                LOGE(TAG, "Failed to setup media channel", e);
            }
        }
    }

    private void detachMediaChannel() {
        LOGD(TAG, "trying to detach media channel");
        if (null != mRemoteMediaPlayer) {
            if (null != mRemoteMediaPlayer && null != Cast.CastApi) {
                try {
                    Cast.CastApi.removeMessageReceivedCallbacks(mApiClient,
                            mRemoteMediaPlayer.getNamespace());
                } catch (Exception e) {
                    LOGE(TAG, "Failed to detach media channel", e);
                }
            }
            mRemoteMediaPlayer = null;
        }
    }

    /**
     * Returns the playback status of the remote device.
     *
     * @return Returns one of the values
     * <ul>
     * <li> <code>MediaStatus.PLAYER_STATE_PLAYING</code>
     * <li> <code>MediaStatus.PLAYER_STATE_PAUSED</code>
     * <li> <code>MediaStatus.PLAYER_STATE_BUFFERING</code>
     * <li> <code>MediaStatus.PLAYER_STATE_IDLE</code>
     * <li> <code>MediaStatus.PLAYER_STATE_UNKNOWN</code>
     * </ul>
     */
    public int getPlaybackStatus() {
        return mState;
    }

    /**
     * Returns the Idle reason, defined in <code>MediaStatus.IDLE_*</code>. Note that the returned
     * value is only meaningful if the status is truly <code>MediaStatus.PLAYER_STATE_IDLE
     * </code>
     *
     * @return
     */
    public int getIdleReason() {
        return mIdleReason;
    }

    /*============================================================================================*/
    /*========== DataChannel callbacks and methods ===============================================*/
    /*============================================================================================*/

    /*
     * If a data namespace was provided when initializing this class, we set things up for a data
     * channel
     * @throws NoConnectionException
     * @throws TransientNetworkDisconnectionException
     */
    private void attachDataChannel() throws TransientNetworkDisconnectionException,
            NoConnectionException {
        if (TextUtils.isEmpty(mDataNamespace)) {
            return;
        }
        if (mDataChannel != null) {
            return;
        }
        checkConnectivity();
        mDataChannel = new MessageReceivedCallback() {

            @Override
            public void onMessageReceived(CastDevice castDevice, String namespace, String message) {
                synchronized (mVideoConsumers) {
                    for (IVideoCastConsumer consumer : mVideoConsumers) {
                        try {
                            consumer.onDataMessageReceived(message);
                        } catch (Exception e) {
                            LOGE(TAG, "onMessageReceived(): Failed to inform " + consumer, e);
                        }
                    }
                }
            }
        };
        try {
            Cast.CastApi.setMessageReceivedCallbacks(mApiClient, mDataNamespace, mDataChannel);
        } catch (IOException e) {
            LOGE(TAG, "Failed to setup data channel", e);
        } catch (IllegalStateException e) {
            LOGE(TAG, "Failed to setup data channel", e);
        }
    }

    private void reattachDataChannel() {
        if (!TextUtils.isEmpty(mDataNamespace) && null != mDataChannel && null != mApiClient) {
            try {
                Cast.CastApi.setMessageReceivedCallbacks(mApiClient, mDataNamespace, mDataChannel);
            } catch (IOException e) {
                LOGE(TAG, "Failed to setup data channel", e);
            } catch (IllegalStateException e) {
                LOGE(TAG, "Failed to setup data channel", e);
            }
        }
    }

    private void onMessageSendFailed(int errorCode) {
        synchronized (mVideoConsumers) {
            for (IVideoCastConsumer consumer : mVideoConsumers) {
                try {
                    consumer.onDataMessageSendFailed(errorCode);
                } catch (Exception e) {
                    LOGE(TAG, "onMessageSendFailed(): Failed to inform " + consumer, e);
                }
            }
        }
    }

    /**
     * Sends the <code>message</code> on the data channel for the namespace that was provided
     * during the initialization of this class. If <code>messageId &gt; 0</code>, then it has to be
     * a unique identifier for the message; this id will be returned if an error occurs. If
     * <code>messageId == 0</code>, then an auto-generated unique identifier will be created and
     * returned for the message.
     *
     * @param message
     * @throws IllegalStateException If the namespace is empty or null
     * @throws NoConnectionException If no connectivity to the device exists
     * @throws TransientNetworkDisconnectionException If framework is still trying to recover from
     * a possibly transient loss of network
     */
    public void sendDataMessage(String message) throws TransientNetworkDisconnectionException,
            NoConnectionException {
        if (TextUtils.isEmpty(mDataNamespace)) {
            throw new IllegalStateException("No Data Namespace is configured");
        }
        checkConnectivity();
        Cast.CastApi.sendMessage(mApiClient, mDataNamespace, message)
                .setResultCallback(new ResultCallback<Status>() {

                    @Override
                    public void onResult(Status result) {
                        if (!result.isSuccess()) {
                            VideoCastManager.this.onMessageSendFailed(result.getStatusCode());
                        }
                    }
                });
    }

    /**
     * Remove the custom data channel, if any. It returns <code>true</code> if it succeeds
     * otherwise if it encounters an error or if no connection exists or if no custom data channel
     * exists, then it returns <code>false</code>
     *
     * @return
     */
    public boolean removeDataChannel() {
        if (TextUtils.isEmpty(mDataNamespace)) {
            return false;
        }
        try {
            if (null != Cast.CastApi && null != mApiClient) {
                Cast.CastApi.removeMessageReceivedCallbacks(mApiClient, mDataNamespace);
            }
            mDataChannel = null;
            Utils.saveStringToPreference(mContext, PREFS_KEY_CAST_CUSTOM_DATA_NAMESPACE, null);
            return true;
        } catch (Exception e) {
            LOGE(TAG, "Failed to remove namespace: " + mDataNamespace, e);
        }
        return false;

    }

    /*============================================================================================*/
    /*========== MediaChannel callbacks ==========================================================*/
    /*============================================================================================*/

    /*
     * This is called by onStatusUpdated() of the RemoteMediaPlayer
     */
    private void onRemoteMediaPlayerStatusUpdated() {
        LOGD(TAG, "onRemoteMediaPlayerStatusUpdated() reached");
        if (null == mApiClient || null == mRemoteMediaPlayer ||
                null == mRemoteMediaPlayer.getMediaStatus()) {
            LOGD(TAG, "mApiClient or mRemoteMediaPlayer is null, so will not proceed");
            return;
        }
        MediaStatus mediaStatus = mRemoteMediaPlayer.getMediaStatus();
        mState = mediaStatus.getPlayerState();
        mIdleReason = mediaStatus.getIdleReason();

        try {
            double volume = getVolume();
            boolean isMute = isMute();
            boolean makeUiHidden = false;
            if (mState == MediaStatus.PLAYER_STATE_PLAYING) {
                LOGD(TAG, "onRemoteMediaPlayerStatusUpdated(): Player status = playing");
                updateRemoteControl(true);
                long mediaDurationLeft = getTimeLeftForMedia();
                startReconnectionService(mediaDurationLeft);
            } else if (mState == MediaStatus.PLAYER_STATE_PAUSED) {
                LOGD(TAG, "onRemoteMediaPlayerStatusUpdated(): Player status = paused");
                updateRemoteControl(false);
            } else if (mState == MediaStatus.PLAYER_STATE_IDLE) {
                LOGD(TAG, "onRemoteMediaPlayerStatusUpdated(): Player status = idle");
                updateRemoteControl(false);
                switch (mIdleReason) {
                    case MediaStatus.IDLE_REASON_FINISHED:
                        removeRemoteControlClient();
                        makeUiHidden = true;
                        break;
                    case MediaStatus.IDLE_REASON_ERROR:
                        // something bad happened on the cast device
                        LOGD(TAG, "onRemoteMediaPlayerStatusUpdated(): IDLE reason = ERROR");
                        makeUiHidden = true;
                        removeRemoteControlClient();
                        onFailed(R.string.failed_receiver_player_error, NO_STATUS_CODE);
                        break;
                    case MediaStatus.IDLE_REASON_CANCELED:
                        LOGD(TAG, "onRemoteMediaPlayerStatusUpdated(): IDLE reason = CANCELLED");
                        makeUiHidden = !isRemoteStreamLive();
                        break;
                }
                if (makeUiHidden) {
                    stopReconnectionService();
                }

            } else if (mState == MediaStatus.PLAYER_STATE_BUFFERING) {
                LOGD(TAG, "onRemoteMediaPlayerStatusUpdated(): Player status = buffering");
            } else {
                LOGD(TAG, "onRemoteMediaPlayerStatusUpdated(): Player status = unknown");
                makeUiHidden = true;
            }
            if (makeUiHidden) {
                stopNotificationService();
            }
            updateMiniControllersVisibility(!makeUiHidden);
            updateMiniControllers();
            synchronized (mVideoConsumers) {
                for (IVideoCastConsumer consumer : mVideoConsumers) {
                    try {
                        consumer.onRemoteMediaPlayerStatusUpdated();
                        consumer.onVolumeChanged(volume, isMute);
                    } catch (Exception e) {
                        LOGE(TAG, "onRemoteMediaPlayerStatusUpdated(): Failed to inform "
                                + consumer, e);
                    }
                }
            }
        } catch (TransientNetworkDisconnectionException e) {
            LOGE(TAG, "Failed to get volume state due to network issues", e);
        } catch (NoConnectionException e) {
            LOGE(TAG, "Failed to get volume state due to network issues", e);
        }

    }

    /*
     * This is called by onMetadataUpdated() of RemoteMediaPlayer
     */
    public void onRemoteMediaPlayerMetadataUpdated() {
        LOGD(TAG, "onRemoteMediaPlayerMetadataUpdated() reached");
        updateLockScreenMetadata();
        synchronized (mVideoConsumers) {
            for (IVideoCastConsumer consumer : mVideoConsumers) {
                try {
                    consumer.onRemoteMediaPlayerMetadataUpdated();
                } catch (Exception e) {
                    LOGE(TAG, "onRemoteMediaPlayerMetadataUpdated(): Failed to inform " + consumer,
                            e);
                }
            }
        }
        try {
            updateLockScreenImage(getRemoteMediaInformation());
        } catch (TransientNetworkDisconnectionException e) {
            LOGE(TAG, "Failed to update lock screen metadata due to a network issue", e);
        } catch (NoConnectionException e) {
            LOGE(TAG, "Failed to update lock screen metadata due to a network issue", e);
        }
    }

    /*============================================================================================*/
    /*========== RemoteControlClient management ==================================================*/
    /*============================================================================================*/

    /*
     * Sets up the {@link RemoteControlClient} for this application. It also handles the audio
     * focus.
     */
    @SuppressLint("InlinedApi")
    private void setUpRemoteControl(final MediaInfo info) {
        if (!isFeatureEnabled(BaseCastManager.FEATURE_LOCKSCREEN)) {
            return;
        }
        LOGD(TAG, "setupRemoteControl() was called");
        mAudioManager.requestAudioFocus(null, AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK);

        ComponentName eventReceiver = new ComponentName(
                mContext, VideoIntentReceiver.class.getName());
        mAudioManager.registerMediaButtonEventReceiver(eventReceiver);

        if (mRemoteControlClientCompat == null) {
            Intent intent = new Intent(Intent.ACTION_MEDIA_BUTTON);
            intent.setComponent(mMediaButtonReceiverComponent);
            mRemoteControlClientCompat = new RemoteControlClientCompat(
                    PendingIntent.getBroadcast(mContext, 0, intent, 0));
            RemoteControlHelper.registerRemoteControlClient(mAudioManager,
                    mRemoteControlClientCompat);
        }
        mRemoteControlClientCompat.addToMediaRouter(mMediaRouter);
        mRemoteControlClientCompat.setTransportControlFlags(
                RemoteControlClient.FLAG_KEY_MEDIA_PLAY_PAUSE);
        if (null == info) {
            mRemoteControlClientCompat.setPlaybackState(RemoteControlClient.PLAYSTATE_PAUSED);
            return;
        } else {
            mRemoteControlClientCompat.setPlaybackState(RemoteControlClient.PLAYSTATE_PLAYING);
        }

        // Update the remote control's image
        updateLockScreenImage(info);

        // update the remote control's metadata
        updateLockScreenMetadata();
    }

    /*
     * Updates lock screen image
     */
    private void updateLockScreenImage(final MediaInfo info) {
        if (null == info) {
            return;
        }
        setBitmapForLockScreen(info);
    }

    /*
     * Returns the {@link Bitmap} appropriate for the right size image for lock screen. In ICS and
     * JB, the image shown on the lock screen is a small size bitmap but for KitKat, the image is a
     * full-screen image so we need to separately handle these two cases. Should not be called on
     * the main thread.
     */
    private void setBitmapForLockScreen(MediaInfo video) {
        if (null == video || mRemoteControlClientCompat == null) {
            return;
        }
        Uri imgUrl = null;
        Bitmap bm = null;
        List<WebImage> images = video.getMetadata().getImages();
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2) {
            if (images.size() > 1) {
                imgUrl = images.get(1).getUrl();
            } else if (images.size() == 1) {
                imgUrl = images.get(0).getUrl();
            } else if (null != mContext) {
                // we don't have a url for image so get a placeholder image from resources
                bm = BitmapFactory.decodeResource(mContext.getResources(),
                        R.drawable.dummy_album_art_large);
            }
        } else if (!images.isEmpty()) {
            imgUrl = images.get(0).getUrl();
        } else {
            // we don't have a url for image so get a placeholder image from resources
            bm = BitmapFactory.decodeResource(mContext.getResources(),
                    R.drawable.dummy_album_art_small);
        }
        if (null != bm) {
            mRemoteControlClientCompat.editMetadata(false).putBitmap(
                    RemoteControlClientCompat.MetadataEditorCompat.
                            METADATA_KEY_ARTWORK, bm).apply();
        } else {

            new FetchBitmapTask() {
                @Override
                protected void onPostExecute(Bitmap bitmap) {
                    if (mRemoteControlClientCompat != null) {
                        mRemoteControlClientCompat.editMetadata(false).putBitmap(
                                RemoteControlClientCompat.MetadataEditorCompat.
                                        METADATA_KEY_ARTWORK, bitmap).apply();
                    }
                }
            }.start(imgUrl);
        }
    }

    /*
     * Updates the playback status of the RemoteControlClient
     */
    @SuppressLint("InlinedApi")
    private void updateRemoteControl(boolean playing) {
        LOGD(TAG, "updateRemoteControl()");
        if (!isFeatureEnabled(FEATURE_LOCKSCREEN)) {
            return;
        }
        if (!isConnected()) {
            return;
        }
        try {
            if (null == mRemoteControlClientCompat && playing) {
                setUpRemoteControl(getRemoteMediaInformation());
            }
            if (mRemoteControlClientCompat != null) {
                int playState = isRemoteStreamLive() ? RemoteControlClient.PLAYSTATE_BUFFERING
                        : RemoteControlClient.PLAYSTATE_PLAYING;
                mRemoteControlClientCompat
                        .setPlaybackState(playing ? playState
                                : RemoteControlClient.PLAYSTATE_PAUSED);
            }
        } catch (TransientNetworkDisconnectionException e) {
            LOGE(TAG, "Failed to setup RCC due to network issues", e);
        } catch (NoConnectionException e) {
            LOGE(TAG, "Failed to setup RCC due to network issues", e);
        }
    }

    /*
     * On ICS and JB, lock screen metadata is one liner: Title - Album Artist - Album. On KitKat, it
     * has two lines: Title , Album Artist - Album
     */
    private void updateLockScreenMetadata() {
        if (null == mRemoteControlClientCompat || !isFeatureEnabled(FEATURE_LOCKSCREEN)) {
            return;
        }

        try {
            // Update the remote controls
            MediaInfo info = getRemoteMediaInformation();
            if (null == info) {
                return;
            }
            final MediaMetadata mm = info.getMetadata();

            mRemoteControlClientCompat.editMetadata(false)
                    .putString(MediaMetadataRetriever.METADATA_KEY_TITLE,
                            mm.getString(MediaMetadata.KEY_TITLE))
                    .putString(MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST,
                            mContext.getResources().getString(
                                    R.string.casting_to_device, getDeviceName()))
                    .putLong(MediaMetadataRetriever.METADATA_KEY_DURATION,
                            info.getStreamDuration())
                    .apply();
        } catch (NotFoundException e) {
            LOGE(TAG, "Failed to update RCC due to resource not found", e);
        } catch (TransientNetworkDisconnectionException e) {
            LOGE(TAG, "Failed to update RCC due to network issues", e);
        } catch (NoConnectionException e) {
            LOGE(TAG, "Failed to update RCC due to network issues", e);
        }
    }

    /*
     * Removes the remote control client
     */
    public void removeRemoteControlClient() {
        if (isFeatureEnabled(FEATURE_LOCKSCREEN)) {
            mAudioManager.abandonAudioFocus(null);
            if (null != mRemoteControlClientCompat) {
                LOGD(TAG, "removeRemoteControlClient(): Removing RemoteControlClient");
                RemoteControlHelper.unregisterRemoteControlClient(mAudioManager,
                        mRemoteControlClientCompat);
                mRemoteControlClientCompat = null;
            }
        }
    }

    /*============================================================================================*/
    /*========== Registering IVideoCastConsumer listeners ========================================*/
    /*============================================================================================*/

    /**
     * Registers an {@link IVideoCastConsumer} interface with this class. Registered listeners will
     * be notified of changes to a variety of lifecycle and media status changes through the
     * callbacks that the interface provides.
     *
     * @param listener
     * @see VideoCastConsumerImpl
     */
    public synchronized void addVideoCastConsumer(IVideoCastConsumer listener) {
        if (null != listener) {
            super.addBaseCastConsumer(listener);
            synchronized (mVideoConsumers) {
                mVideoConsumers.add(listener);
            }
            LOGD(TAG, "Successfully added the new CastConsumer listener " + listener);
        }
    }

    /**
     * Unregisters an {@link IVideoCastConsumer}.
     *
     * @param listener
     */
    public synchronized void removeVideoCastConsumer(IVideoCastConsumer listener) {
        if (null != listener) {
            super.removeBaseCastConsumer(listener);
            synchronized (mVideoConsumers) {
                mVideoConsumers.remove(listener);
            }
        }
    }

    /*============================================================================================*/
    /*========== Registering IMiniController listeners ===========================================*/
    /*============================================================================================*/

    /**
     * Adds a new {@link IMiniController} component. Callers need to provide their own
     * {@link OnMiniControllerChangedListener}.
     *
     * @param miniController
     * @param OnMiniControllerChangedListener
     * @see setOnMiniControllerChangedListener
     */
    public void addMiniController(IMiniController miniController,
            OnMiniControllerChangedListener onChangedListener) {
        if (null != miniController) {
            boolean result = false;
            synchronized (mMiniControllers) {
                result = mMiniControllers.add(miniController);
            }
            if (result) {
                miniController.setOnMiniControllerChangedListener(null == onChangedListener ? this
                        : onChangedListener);
                try {
                    if (isConnected() && isRemoteMediaLoaded()) {
                        updateMiniController(miniController);
                        miniController.setVisibility(View.VISIBLE);
                    }
                } catch (TransientNetworkDisconnectionException e) {
                    LOGE(TAG, "Failed to get the status of media playback on receiver", e);
                } catch (NoConnectionException e) {
                    LOGE(TAG, "Failed to get the status of media playback on receiver", e);
                }
                LOGD(TAG, "Successfully added the new MiniController " + miniController);
            } else {
                LOGD(TAG, "Attempting to adding " + miniController + " but it was already " +
                        "registered, skipping this step");
            }
        }
    }

    /**
     * Adds a new {@link IMiniController} component and assigns {@link VideoCastManager} as the
     * {@link OnMiniControllerChangedListener} for this component.
     *
     * @param miniController
     */
    public void addMiniController(IMiniController miniController) {
        addMiniController(miniController, null);
    }

    /**
     * Removes a {@link IMiniController} listener from the list of listeners.
     *
     * @param listener
     */
    public void removeMiniController(IMiniController listener) {
        if (null != listener) {
            synchronized (mMiniControllers) {
                mMiniControllers.remove(listener);
            }
        }
    }

    /*============================================================================================*/
    /*========== Implementing abstract methods of BaseCastManage =================================*/
    /*============================================================================================*/

    @Override
    protected void onDeviceUnselected() {
        LOGD(TAG, "onDeviceUnselected");
        stopNotificationService();
        detachMediaChannel();
        removeDataChannel();
        mState = MediaStatus.PLAYER_STATE_IDLE;
    }

    @Override
    protected Builder getCastOptionBuilder(CastDevice device) {
        Builder builder = Cast.CastOptions.builder(mSelectedCastDevice, new CastListener());
        if (isFeatureEnabled(FEATURE_DEBUGGING)) {
            builder.setVerboseLoggingEnabled(true);
        }
        return builder;
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        LOGD(TAG, "onConnectionFailed()");
        super.onConnectionFailed(result);
        updateRemoteControl(false);
        stopNotificationService();

    }

    @Override
    public void onDisconnected(boolean stopAppOnExit, boolean clearPersistedConnectionData,
            boolean setDefaultRoute) {
        super.onDisconnected(stopAppOnExit, clearPersistedConnectionData, setDefaultRoute);
        updateMiniControllersVisibility(false);
        if (clearPersistedConnectionData && !mConnectionSuspended) {
            removeRemoteControlClient();
        }
        mState = MediaStatus.PLAYER_STATE_IDLE;
    }

    @Override
    protected MediaRouteDialogFactory getMediaRouteDialogFactory() {
        return new VideoMediaRouteDialogFactory();
    }

    class CastListener extends Cast.Listener {

        /*
         * (non-Javadoc)
         * @see com.google.android.gms.cast.Cast.Listener#onApplicationDisconnected (int)
         */
        @Override
        public void onApplicationDisconnected(int statusCode) {
            VideoCastManager.this.onApplicationDisconnected(statusCode);
        }

        /*
         * (non-Javadoc)
         * @see com.google.android.gms.cast.Cast.Listener#onApplicationStatusChanged ()
         */
        @Override
        public void onApplicationStatusChanged() {
            VideoCastManager.this.onApplicationStatusChanged();
        }

        @Override
        public void onVolumeChanged() {
            VideoCastManager.this.onVolumeChanged();
        }
    }

    @Override
    public void onFailed(int resourceId, int statusCode) {
        LOGD(TAG, "onFailed: " + mContext.getString(resourceId) + ", code: " + statusCode);
        super.onFailed(resourceId, statusCode);
    }

    /**
     * Clients can call this method to delegate handling of the volume. Clients should override
     * {@link dispatchEvent} and call this method:
     * <pre>
     public boolean dispatchKeyEvent(KeyEvent event) {
        if (mCastManager.onDispatchVolumeKeyEvent(event, VOLUME_DELTA)) {
            return true;
        }
        return super.dispatchKeyEvent(event);
     }
     * </pre>
     *
     * @param event The dispatched event.
     * @param volumeDelta The amount by which volume should be increased or decreased in each step
     * @return <code>true</code> if volume is handled by the library, <code>false</code> otherwise.
     */
    public boolean onDispatchVolumeKeyEvent(KeyEvent event, double volumeDelta) {
        if (isConnected()) {
            boolean isKeyDown = event.getAction() == KeyEvent.ACTION_DOWN;
            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_VOLUME_UP:
                    if (changeVolume(volumeDelta, isKeyDown)) {
                        return true;
                    }
                    break;
                case KeyEvent.KEYCODE_VOLUME_DOWN:
                    if (changeVolume(-volumeDelta, isKeyDown)) {
                        return true;
                    }
                    break;
            }
        }
        return false;
    }

    private boolean changeVolume(double volumeIncrement, boolean isKeyDown) {
        if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) &&
                getPlaybackStatus() == MediaStatus.PLAYER_STATE_PLAYING &&
                isFeatureEnabled(BaseCastManager.FEATURE_LOCKSCREEN)) {
            return false;
        }

        if (isKeyDown) {
            try {
                incrementVolume(volumeIncrement);
            } catch (CastException e) {
                LOGE(TAG, "Failed to change volume", e);
            } catch (TransientNetworkDisconnectionException e) {
                LOGE(TAG, "Failed to change volume", e);
            } catch (NoConnectionException e) {
                LOGE(TAG, "Failed to change volume", e);
            }
        }
        return true;
    }

    /**
     * Set the live stream duration; this is purely used in the reconnection logic. If this method
     * is not called, the default value {@code DEFAULT_LIVE_STREAM_DURATION_MS} is used.
     *
     * @param duration Duration, specified in milliseconds.
     */
    public void setLiveStreamDuration(long duration) {
        mLiveStreamDuration = duration;
    }

    /**
     * Sets the active tracks for the currently loaded media.
     */
    public void setActiveTrackIds(long[] trackIds) {
        if (null == mRemoteMediaPlayer || null == mRemoteMediaPlayer.getMediaInfo()) {
            return;
        }
        mRemoteMediaPlayer.setActiveMediaTracks(mApiClient, trackIds)
                .setResultCallback(new ResultCallback<MediaChannelResult>() {
                    @Override
                    public void onResult(
                            MediaChannelResult mediaChannelResult) {
                        LOGD(TAG, "Setting track result was successful? " + mediaChannelResult
                                .getStatus().isSuccess());
                        if (!mediaChannelResult.getStatus().isSuccess()) {
                            LOGD(TAG, "Failed since: " + mediaChannelResult.getStatus()
                                    + " and status code:" + mediaChannelResult.getStatus()
                                    .getStatusCode());
                        }
                    }
                });
    }

    /**
     * Sets or updates the style of the Text Track.
     */
    public void setTextTrackStyle(TextTrackStyle style) {
        mRemoteMediaPlayer.setTextTrackStyle(mApiClient, style)
                .setResultCallback(new ResultCallback<MediaChannelResult>() {
                    @Override
                    public void onResult(MediaChannelResult result) {
                        if (!result.getStatus().isSuccess()) {
                            onFailed(R.string.failed_to_set_track_style,
                                    result.getStatus().getStatusCode());
                        }
                    }
                });
        synchronized (mVideoConsumers) {
            for (IVideoCastConsumer consumer : mVideoConsumers) {
                try {
                    consumer.onTextTrackStyleChanged(style);
                } catch (Exception e) {
                    LOGE(TAG, "onTextTrackStyleChanged(): Failed to inform " + consumer, e);
                }
            }
        }
    }

    /**
     * Signals a change in the Text Track style. Clients should not call this directly.
     */
    public void onTextTrackStyleChanged(TextTrackStyle style) {
        LOGD(TAG, "onTextTrackStyleChanged() reached");
        if (mRemoteMediaPlayer == null || mRemoteMediaPlayer.getMediaInfo() == null) {
            return;
        }
        mRemoteMediaPlayer.setTextTrackStyle(mApiClient, style)
                .setResultCallback(new ResultCallback<MediaChannelResult>() {
                    @Override
                    public void onResult(MediaChannelResult result) {
                        if (!result.getStatus().isSuccess()) {
                            onFailed(R.string.failed_to_set_track_style,
                                    result.getStatus().getStatusCode());
                        }
                    }
                });
        synchronized (mVideoConsumers) {
            for (IVideoCastConsumer consumer : mVideoConsumers) {
                try {
                    consumer.onTextTrackStyleChanged(style);
                } catch (Exception e) {
                    LOGE(TAG, "onTextTrackStyleChanged(): Failed to inform " + consumer, e);
                }
            }
        }
    }

    /**
     * Signals a change in the Text Track on/off state. Clients should not call this directly.
     */
    public void onTextTrackEnabledChanged(boolean isEnabled) {
        LOGD(TAG, "onTextTrackEnabledChanged() reached");
        if (isEnabled) {

        } else {
            setActiveTrackIds(new long[]{});
        }

        synchronized (mVideoConsumers) {
            for (IVideoCastConsumer consumer : mVideoConsumers) {
                try {
                    consumer.onTextTrackEnabledChanged(isEnabled);
                } catch (Exception e) {
                    LOGE(TAG, "onTextTrackEnabledChanged(): Failed to inform " + consumer, e);
                }
            }
        }
    }

    /**
     * Signals a change in the Text Track locale. Clients should not call this directly.
     */
    public void onTextTrackLocaleChanged(Locale locale) {
        LOGD(TAG, "onTextTrackLocaleChanged() reached");
        for (IVideoCastConsumer consumer : mVideoConsumers) {
            try {
                consumer.onTextTrackLocaleChanged(locale);
            } catch (Exception e) {
                LOGE(TAG, "onTextTrackLocaleChanged(): Failed to inform " + consumer, e);
            }
        }
    }

    @SuppressLint("NewApi")
    private void registerCaptionListener(final Context context) {
        if (Utils.IS_KITKAT_OR_ABOVE) {
            CaptioningManager captioningManager =
                    (CaptioningManager) context.getSystemService(Context.CAPTIONING_SERVICE);
            captioningManager.addCaptioningChangeListener(
                    new CaptioningManager.CaptioningChangeListener() {
                        @Override
                        public void onEnabledChanged(boolean enabled) {
                            onTextTrackEnabledChanged(enabled);
                        }

                        @Override
                        public void onUserStyleChanged(
                                CaptioningManager.CaptionStyle userStyle) {
                            onTextTrackStyleChanged(mTrackManager.getTextTrackStyle());
                        }

                        @Override
                        public void onFontScaleChanged(float fontScale) {
                            onTextTrackStyleChanged(mTrackManager.getTextTrackStyle());
                        }

                        @Override
                        public void onLocaleChanged(Locale locale) {
                            onTextTrackLocaleChanged(locale);
                        }
                    }
            );
        }
    }

    /**
     * Updates the summary of the captions between "on" and "off" based on the user selected
     * preferences. This can be called by the caller application when they add captions settings to
     * their preferences. Preferably this should be called in the {@link onResume()} of the
     * PreferenceActivity so that it gets updated when needed.
     */
    public void updateCaptionSummary(String captionScreenKey, PreferenceScreen preferenceScreen) {
        int status = R.string.info_na;
        if (isFeatureEnabled(FEATURE_CAPTIONS_PREFERENCE)) {
            status = mTrackManager.isCaptionEnabled() ? R.string.on : R.string.off;
        }
        preferenceScreen.findPreference(captionScreenKey)
                .setSummary(status);
    }

    /**
     * Returns the instance of {@link TracksPreferenceManager} that is being used.
     */
    public TracksPreferenceManager getTracksPreferenceManager() {
        return mTrackManager;
    }

    /**
     * Returns the list of current active tracks. If there is no remote media, then this will
     * return <code>null</code>.
     */
    public long[] getActiveTrackIds() {
        if (mRemoteMediaPlayer != null && mRemoteMediaPlayer.getMediaStatus() != null) {
            return mRemoteMediaPlayer.getMediaStatus().getActiveTrackIds();
        }
        return null;
    }

}
