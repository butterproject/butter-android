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

package com.google.sample.castcompanionlibrary.cast.player;

import static com.google.sample.castcompanionlibrary.utils.LogUtils.LOGD;
import static com.google.sample.castcompanionlibrary.utils.LogUtils.LOGE;

import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.MediaStatus;
import com.google.android.gms.cast.MediaTrack;
import com.google.android.gms.cast.RemoteMediaPlayer;
import com.google.sample.castcompanionlibrary.R;
import com.google.sample.castcompanionlibrary.cast.VideoCastManager;
import com.google.sample.castcompanionlibrary.cast.callbacks.VideoCastConsumerImpl;
import com.google.sample.castcompanionlibrary.cast.exceptions.CastException;
import com.google.sample.castcompanionlibrary.cast.exceptions.NoConnectionException;
import com.google.sample.castcompanionlibrary.cast.exceptions.TransientNetworkDisconnectionException;
import com.google.sample.castcompanionlibrary.utils.FetchBitmapTask;
import com.google.sample.castcompanionlibrary.utils.LogUtils;
import com.google.sample.castcompanionlibrary.utils.Utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.View;
import android.widget.SeekBar;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * A fragment that provides a mechanism to retain the state and other needed objects for
 * {@link VideoCastControllerActivity} (or more generally, for any class implementing
 * {@link IVideoCastController} interface). This can come very handy when setup of that activity
 * allows for a configuration changes. Most of the logic required for
 * {@link VideoCastControllerActivity} is maintained in this fragment to enable application
 * developers provide a different implementation, if desired.
 * <p/>
 * This fragment also provides an implementation of {@link IMediaAuthListener} which can be useful
 * if a pre-authorization is required for playback of a media.
 */
public class VideoCastControllerFragment extends Fragment implements OnVideoCastControllerListener,
        IMediaAuthListener {

    private static final String EXTRAS = "extras";
    private static final String TAG = LogUtils.makeLogTag(VideoCastControllerFragment.class);
    private MediaInfo mSelectedMedia;
    private VideoCastManager mCastManager;
    private IMediaAuthService mMediaAuthService;
    private Thread mAuthThread;
    private Timer mMediaAuthTimer;
    private Handler mHandler;
    protected boolean mAuthSuccess = true;
    private IVideoCastController mCastController;
    private FetchBitmapTask mImageAsyncTask;
    private Timer mSeekbarTimer;
    private int mPlaybackState;
    private MyCastConsumer mCastConsumer;
    private OverallState mOverallState = OverallState.UNKNOWN;
    private UrlAndBitmap mUrlAndBitmap;
    private static boolean sDialogCanceled = false;
    private boolean mIsFresh = true;

    private enum OverallState {
        AUTHORIZING, PLAYBACK, UNKNOWN;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        sDialogCanceled = false;
        mCastController = (IVideoCastController) activity;
        mHandler = new Handler();
        try {
            mCastManager = VideoCastManager.getInstance(activity);
        } catch (CastException e) {
            // logged already
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCastConsumer = new MyCastConsumer();
        Bundle bundle = getArguments();
        if (null == bundle) {
            return;
        }
        Bundle extras = bundle.getBundle(EXTRAS);
        Bundle mediaWrapper = extras.getBundle(VideoCastManager.EXTRA_MEDIA);

        // Retain this fragment across configuration changes.
        setRetainInstance(true);

        if (extras.getBoolean(VideoCastManager.EXTRA_HAS_AUTH)) {
            mOverallState = OverallState.AUTHORIZING;
            mMediaAuthService = mCastManager.getMediaAuthService();
            handleMediaAuthTask(mMediaAuthService);
            showImage(Utils.getImageUri(mMediaAuthService.getMediaInfo(), 1));
        } else if (null != mediaWrapper) {
            mOverallState = OverallState.PLAYBACK;
            boolean shouldStartPlayback = extras.getBoolean(VideoCastManager.EXTRA_SHOULD_START);
            String customDataStr = extras.getString(VideoCastManager.EXTRA_CUSTOM_DATA);
            JSONObject customData = null;
            if (!TextUtils.isEmpty(customDataStr)) {
                try {
                    customData = new JSONObject(customDataStr);
                } catch (JSONException e) {
                    LOGE(TAG, "Failed to unmarshalize custom data string: customData="
                            + customDataStr, e);
                }
            }
            MediaInfo info = Utils.toMediaInfo(mediaWrapper);
            int startPoint = extras.getInt(VideoCastManager.EXTRA_START_POINT, 0);
            onReady(info, shouldStartPlayback, startPoint, customData);
        }
    }

    /*
     * Starts a background thread for starting the Auth Service
     */
    private void handleMediaAuthTask(final IMediaAuthService authService) {
        mCastController.showLoading(true);
        mCastController.setLine2(null != authService.getPendingMessage()
                ? authService.getPendingMessage() : "");
        mAuthThread = new Thread(new Runnable() {

            @Override
            public void run() {
                if (null != authService) {
                    try {
                        authService.setOnResult(VideoCastControllerFragment.this);
                        authService.start();
                    } catch (Exception e) {
                        LOGE(TAG, "mAuthService.start() encountered exception", e);
                        mAuthSuccess = false;
                    }
                }
            }
        });
        mAuthThread.start();

        // start a timeout timer; we don't want authorization process to take too long
        mMediaAuthTimer = new Timer();
        mMediaAuthTimer.schedule(new MediaAuthServiceTimerTask(mAuthThread),
                authService.getTimeout());
    }

    /*
     * A TimerTask that will be called when the timeout timer expires
     */
    class MediaAuthServiceTimerTask extends TimerTask {

        private final Thread mThread;

        public MediaAuthServiceTimerTask(Thread thread) {
            this.mThread = thread;
        }

        @Override
        public void run() {
            if (null != mThread) {
                LOGD(TAG, "Timer is expired, going to interrupt the thread");
                mThread.interrupt();
                mHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        mCastController.showLoading(false);
                        showErrorDialog(getString(R.string.failed_authorization_timeout));
                        mAuthSuccess = false;
                        if (null != mMediaAuthService
                                && mMediaAuthService.getStatus() == MediaAuthStatus.PENDING) {
                            mMediaAuthService.abort(MediaAuthStatus.ABORT_TIMEOUT);
                        }
                    }
                });

            }
        }

    }

    private class MyCastConsumer extends VideoCastConsumerImpl {

        @Override
        public void onDisconnected() {
            mCastController.closeActivity();
        }

        @Override
        public void onApplicationDisconnected(int errorCode) {
            mCastController.closeActivity();
        }

        @Override
        public void onRemoteMediaPlayerMetadataUpdated() {
            try {
                mSelectedMedia = mCastManager.getRemoteMediaInformation();
                updateClosedCaptionState();
                updateMetadata();
            } catch (TransientNetworkDisconnectionException e) {
                LOGE(TAG, "Failed to update the metadata due to network issues", e);
            } catch (NoConnectionException e) {
                LOGE(TAG, "Failed to update the metadata due to network issues", e);
            }
        }

        @Override
        public void onFailed(int resourceId, int statusCode) {
            LOGD(TAG, "onFailed(): " + getString(resourceId) + ", status code: " + statusCode);
            if (statusCode == RemoteMediaPlayer.STATUS_FAILED
                    || statusCode == RemoteMediaPlayer.STATUS_TIMED_OUT) {
                Utils.showErrorDialog(getActivity(), resourceId);
                mCastController.closeActivity();
            }
        }

        @Override
        public void onRemoteMediaPlayerStatusUpdated() {
            updatePlayerStatus();
        }

        @Override
        public void onConnectionSuspended(int cause) {
            mCastController.updateControllersStatus(false);
        }

        @Override
        public void onConnectivityRecovered() {
            mCastController.updateControllersStatus(true);
        }

    }

    private class UpdateSeekbarTask extends TimerTask {

        @Override
        public void run() {
            mHandler.post(new Runnable() {

                @Override
                public void run() {
                    int currentPos = 0;
                    if (mPlaybackState == MediaStatus.PLAYER_STATE_BUFFERING) {
                        return;
                    }
                    if (!mCastManager.isConnected()) {
                        return;
                    }
                    try {
                        int duration = (int) mCastManager.getMediaDuration();
                        if (duration > 0) {
                            try {
                                currentPos = (int) mCastManager.getCurrentMediaPosition();
                                mCastController.updateSeekbar(currentPos, duration);
                            } catch (Exception e) {
                                LOGE(TAG, "Failed to get current media position", e);
                            }
                        }
                    } catch (TransientNetworkDisconnectionException e) {
                        LOGE(TAG, "Failed to update the progress bar due to network issues", e);
                    } catch (NoConnectionException e) {
                        LOGE(TAG, "Failed to update the progress bar due to network issues", e);
                    }

                }
            });
        }
    }

    private void onReady(MediaInfo mediaInfo, boolean shouldStartPlayback, int startPoint,
            JSONObject customData) {
        mSelectedMedia = mediaInfo;
        updateClosedCaptionState();
        try {
            mCastController.setStreamType(mSelectedMedia.getStreamType());
            if (shouldStartPlayback) {
                // need to start remote playback
                mPlaybackState = MediaStatus.PLAYER_STATE_BUFFERING;
                mCastController.setPlaybackStatus(mPlaybackState);
                mCastManager.loadMedia(mSelectedMedia, true, startPoint, customData);
            } else {
                // we don't change the status of remote playback
                if (mCastManager.isRemoteMoviePlaying()) {
                    mPlaybackState = MediaStatus.PLAYER_STATE_PLAYING;
                } else {
                    mPlaybackState = MediaStatus.PLAYER_STATE_PAUSED;
                }
                mCastController.setPlaybackStatus(mPlaybackState);
            }
        } catch (Exception e) {
            LOGE(TAG, "Failed to get playback and media information", e);
            mCastController.closeActivity();
        }
        updateMetadata();
        restartTrickplayTimer();
    }

    private void updateClosedCaptionState() {
        int state = IVideoCastController.CC_HIDDEN;
        if (mCastManager.isFeatureEnabled(VideoCastManager.FEATURE_CAPTIONS_PREFERENCE)
                && mSelectedMedia != null
                && mCastManager.getTracksPreferenceManager().isCaptionEnabled()) {
            List<MediaTrack> tracks = mSelectedMedia.getMediaTracks();
            state = tracks == null || tracks.isEmpty() ? IVideoCastController.CC_DISABLED
                    : IVideoCastController.CC_ENABLED;
        }
        mCastController.updateClosedCaption(state);
    }

    private void stopTrickplayTimer() {
        LOGD(TAG, "Stopped TrickPlay Timer");
        if (null != mSeekbarTimer) {
            mSeekbarTimer.cancel();
        }
    }

    private void restartTrickplayTimer() {
        stopTrickplayTimer();
        mSeekbarTimer = new Timer();
        mSeekbarTimer.scheduleAtFixedRate(new UpdateSeekbarTask(), 100, 1000);
        LOGD(TAG, "Restarted TrickPlay Timer");
    }

    private void updateOverallState() {
        IMediaAuthService authService;
        switch (mOverallState) {
            case AUTHORIZING:
                authService = mCastManager.getMediaAuthService();
                if (null != authService) {
                    mCastController.setLine2(null != authService.getPendingMessage()
                            ? authService.getPendingMessage() : "");
                    mCastController.showLoading(true);
                }
                break;
            case PLAYBACK:
                // nothing yet, may be needed in future
                break;
            default:
                break;
        }
    }

    private void updateMetadata() {
        Uri imageUrl = null;
        if (null == mSelectedMedia) {
            if (null != mMediaAuthService) {
                imageUrl = Utils.getImageUri(mMediaAuthService.getMediaInfo(), 1);
            }
        } else {
            imageUrl = Utils.getImageUri(mSelectedMedia, 1);
        }
        showImage(imageUrl);
        if (null == mSelectedMedia) {
            return;
        }
        MediaMetadata mm = mSelectedMedia.getMetadata();
        mCastController.setLine1(null != mm.getString(MediaMetadata.KEY_TITLE)
                ? mm.getString(MediaMetadata.KEY_TITLE) : "");
        boolean isLive = mSelectedMedia.getStreamType() == MediaInfo.STREAM_TYPE_LIVE;
        mCastController.adjustControllersForLiveStream(isLive);
    }

    private void updatePlayerStatus() {
        int mediaStatus = mCastManager.getPlaybackStatus();
        LOGD(TAG, "updatePlayerStatus(), state: " + mediaStatus);
        if (null == mSelectedMedia) {
            return;
        }
        mCastController.setStreamType(mSelectedMedia.getStreamType());
        if (mediaStatus == MediaStatus.PLAYER_STATE_BUFFERING) {
            mCastController.setLine2(getString(R.string.loading));
        } else {
            mCastController.setLine2(getString(R.string.casting_to_device,
                    mCastManager.getDeviceName()));
        }
        switch (mediaStatus) {
            case MediaStatus.PLAYER_STATE_PLAYING:
                mIsFresh = false;
                if (mPlaybackState != MediaStatus.PLAYER_STATE_PLAYING) {
                    mPlaybackState = MediaStatus.PLAYER_STATE_PLAYING;
                    mCastController.setPlaybackStatus(mPlaybackState);
                }
                break;
            case MediaStatus.PLAYER_STATE_PAUSED:
                mIsFresh = false;
                if (mPlaybackState != MediaStatus.PLAYER_STATE_PAUSED) {
                    mPlaybackState = MediaStatus.PLAYER_STATE_PAUSED;
                    mCastController.setPlaybackStatus(mPlaybackState);
                }
                break;
            case MediaStatus.PLAYER_STATE_BUFFERING:
                mIsFresh = false;
                if (mPlaybackState != MediaStatus.PLAYER_STATE_BUFFERING) {
                    mPlaybackState = MediaStatus.PLAYER_STATE_BUFFERING;
                    mCastController.setPlaybackStatus(mPlaybackState);
                }
                break;
            case MediaStatus.PLAYER_STATE_IDLE:
                switch (mCastManager.getIdleReason()) {
                    case MediaStatus.IDLE_REASON_FINISHED:
                        if (!mIsFresh) {
                            mCastController.closeActivity();
                        }
                        break;
                    case MediaStatus.IDLE_REASON_CANCELED:
                        try {
                            if (mCastManager.isRemoteStreamLive()) {
                                if (mPlaybackState != MediaStatus.PLAYER_STATE_IDLE) {
                                    mPlaybackState = MediaStatus.PLAYER_STATE_IDLE;
                                    mCastController.setPlaybackStatus(mPlaybackState);
                                }
                            }
                        } catch (TransientNetworkDisconnectionException e) {
                            LOGD(TAG, "Failed to determine if stream is live", e);
                        } catch (NoConnectionException e) {
                            LOGD(TAG, "Failed to determine if stream is live", e);
                        }
                    default:
                        break;
                }
                break;

            default:
                break;
        }
    }

    @Override
    public void onDestroy() {
        LOGD(TAG, "onDestroy()");
        stopTrickplayTimer();
        cleanup();
        super.onDestroy();
    }

    @Override
    public void onResume() {
        LOGD(TAG, "onResume() was called");
        try {
            mCastManager = VideoCastManager.getInstance(getActivity());
            boolean shouldFinish = !(mCastManager.isConnected() || mCastManager.isConnecting())
                    || (mCastManager.getPlaybackStatus() == MediaStatus.PLAYER_STATE_IDLE
                    && mCastManager.getIdleReason() == MediaStatus.IDLE_REASON_FINISHED
                    && !mIsFresh);
            if (shouldFinish) {
                mCastController.closeActivity();
            }
            mCastManager.addVideoCastConsumer(mCastConsumer);
            mCastManager.incrementUiCounter();
            if (!mIsFresh) {
                updatePlayerStatus();
            }

            // updating metadata in case someone else has changed it and we are resuming the
            // activity
            try {
                mSelectedMedia = mCastManager.getRemoteMediaInformation();
                updateClosedCaptionState();
                updateMetadata();
            } catch (TransientNetworkDisconnectionException e) {
                LOGE(TAG, "Failed to update the metadata due to network issues", e);
            } catch (NoConnectionException e) {
                LOGE(TAG, "Failed to update the metadata due to network issues", e);
            }

        } catch (CastException e) {
            // logged already
        }
        super.onResume();
    }

    @Override
    public void onPause() {
        mCastManager.removeVideoCastConsumer(mCastConsumer);
        mCastManager.decrementUiCounter();
        mIsFresh = false;
        super.onPause();
    }

    /**
     * Call this static method to create an instance of this fragment.
     */
    public static VideoCastControllerFragment newInstance(Bundle extras) {
        VideoCastControllerFragment f = new VideoCastControllerFragment();
        Bundle b = new Bundle();
        b.putBundle(EXTRAS, extras);
        f.setArguments(b);
        return f;
    }

    /*
     * Gets the image at the given url and populates the image view with that. It tries to cache the
     * image to avoid unnecessary network calls.
     */
    private void showImage(final Uri url) {
        if (mImageAsyncTask != null) {
            mImageAsyncTask.cancel(true);
        }
        if (null == url) {
            mCastController.setImage(BitmapFactory.decodeResource(getActivity().getResources(),
                    R.drawable.dummy_album_art_large));
            return;
        }
        if (null != mUrlAndBitmap && mUrlAndBitmap.isMatch(url)) {
            // we can reuse mBitmap
            mCastController.setImage(mUrlAndBitmap.mBitmap);
            return;
        }
        mUrlAndBitmap = null;
        mImageAsyncTask = new FetchBitmapTask() {
            @Override
            protected void onPostExecute(Bitmap bitmap) {
                if (null != bitmap) {
                    mUrlAndBitmap = new UrlAndBitmap();
                    mUrlAndBitmap.mBitmap = bitmap;
                    mUrlAndBitmap.mUrl = url;
                    mCastController.setImage(bitmap);
                }
                if (this == mImageAsyncTask) {
                    mImageAsyncTask = null;
                }
            }
        };
        mImageAsyncTask.start(url);
    }

    /*
     * A modal dialog with an OK button, where upon clicking on it, will finish the activity. We use
     * a DialogFragment so during configuration changes, system manages the dialog for us.
     */
    public static class ErrorDialogFragment extends DialogFragment {

        private IVideoCastController mController;
        private static final String MESSAGE = "message";

        public static ErrorDialogFragment newInstance(String message) {
            ErrorDialogFragment frag = new ErrorDialogFragment();
            Bundle args = new Bundle();
            args.putString(MESSAGE, message);
            frag.setArguments(args);
            return frag;
        }

        @Override
        public void onAttach(Activity activity) {
            mController = (IVideoCastController) activity;
            super.onAttach(activity);
            setCancelable(false);
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            String message = getArguments().getString(MESSAGE);
            return new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.error)
                    .setMessage(message)
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            sDialogCanceled = true;
                            mController.closeActivity();
                        }
                    })
                    .create();
        }
    }

    /*
     * Shows an error dialog
     */
    private void showErrorDialog(String message) {
        ErrorDialogFragment.newInstance(message).show(getFragmentManager(), "dlg");
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mImageAsyncTask != null) {
            mImageAsyncTask.cancel(true);
        }
    }

    // ------- Implementation of OnVideoCastControllerListener interface ----------------- //
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        try {
            if (mPlaybackState == MediaStatus.PLAYER_STATE_PLAYING) {
                mPlaybackState = MediaStatus.PLAYER_STATE_BUFFERING;
                mCastController.setPlaybackStatus(mPlaybackState);
                mCastManager.play(seekBar.getProgress());
            } else if (mPlaybackState == MediaStatus.PLAYER_STATE_PAUSED) {
                mCastManager.seek(seekBar.getProgress());
            }
            restartTrickplayTimer();
        } catch (Exception e) {
            LOGE(TAG, "Failed to complete seek", e);
            mCastController.closeActivity();
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        stopTrickplayTimer();

    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
    }

    @Override
    public void onPlayPauseClicked(View v) throws CastException,
            TransientNetworkDisconnectionException, NoConnectionException {
        LOGD(TAG, "isConnected returning: " + mCastManager.isConnected());
        togglePlayback();
    }

    private void togglePlayback() throws CastException, TransientNetworkDisconnectionException,
            NoConnectionException {
        switch (mPlaybackState) {
            case MediaStatus.PLAYER_STATE_PAUSED:
                mCastManager.play();
                mPlaybackState = MediaStatus.PLAYER_STATE_BUFFERING;
                restartTrickplayTimer();
                break;
            case MediaStatus.PLAYER_STATE_PLAYING:
                mCastManager.pause();
                mPlaybackState = MediaStatus.PLAYER_STATE_BUFFERING;
                break;
            case MediaStatus.PLAYER_STATE_IDLE:
                if ((mSelectedMedia.getStreamType() == MediaInfo.STREAM_TYPE_LIVE)
                        && (mCastManager.getIdleReason() == MediaStatus.IDLE_REASON_CANCELED)) {
                    mCastManager.play();
                } else {
                    mCastManager.loadMedia(mSelectedMedia, true, 0);
                }
                mPlaybackState = MediaStatus.PLAYER_STATE_BUFFERING;
                restartTrickplayTimer();
                break;
            default:
                break;
        }
        mCastController.setPlaybackStatus(mPlaybackState);
    }

    @Override
    public void onConfigurationChanged() {
        updateOverallState();
        if (null == mSelectedMedia) {
            if (null != mMediaAuthService) {
                showImage(Utils.getImageUri(mMediaAuthService.getMediaInfo(), 1));
            }
        } else {
            updateMetadata();
            updatePlayerStatus();
            mCastController.updateControllersStatus(mCastManager.isConnected());

        }
    }

    // ------- Implementation of IMediaAuthListener interface --------------------------- //
    @Override
    public void onResult(MediaAuthStatus status, final MediaInfo info, final String message,
            final int startPoint, final JSONObject customData) {
        if (status == MediaAuthStatus.RESULT_AUTHORIZED && mAuthSuccess) {
            // successful authorization
            mMediaAuthService = null;
            if (null != mMediaAuthTimer) {
                mMediaAuthTimer.cancel();
            }
            mSelectedMedia = info;
            updateClosedCaptionState();
            mHandler.post(new Runnable() {

                @Override
                public void run() {
                    mOverallState = OverallState.PLAYBACK;
                    onReady(info, true, startPoint, customData);
                }
            });
        } else {
            if (null != mMediaAuthTimer) {
                mMediaAuthTimer.cancel();
            }
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mOverallState = OverallState.UNKNOWN;
                    showErrorDialog(message);
                }
            });

        }
    }

    @Override
    public void onFailure(final String failureMessage) {
        if (null != mMediaAuthTimer) {
            mMediaAuthTimer.cancel();
        }
        mHandler.post(new Runnable() {

            @Override
            public void run() {
                mOverallState = OverallState.UNKNOWN;
                showErrorDialog(failureMessage);
            }
        });

    }

    @Override
    public void onTracksSelected(List<MediaTrack> tracks) {
        long[] tracksArray;
        if (tracks.size() == 0) {
            tracksArray = new long[]{};
        } else {
            tracksArray = new long[tracks.size()];
            for (int i = 0; i < tracks.size(); i++) {
                tracksArray[i] = tracks.get(i).getId();
            }
        }
        mCastManager.setActiveTrackIds(tracksArray);
        if (tracks.size() > 0) {
            mCastManager.setTextTrackStyle(mCastManager.getTracksPreferenceManager()
                    .getTextTrackStyle());
        }
    }

    // ----------- Some utility methods --------------------------------------------------------- //

    /*
     * A simple class that holds a URL and a bitmap, mainly used to cache the fetched image
     */
    private class UrlAndBitmap {

        private Bitmap mBitmap;
        private Uri mUrl;

        private boolean isMatch(Uri url) {
            return null != url && null != mBitmap && url.equals(mUrl);
        }
    }

    /*
     * Cleanup of threads and timers and bitmap and ...
     */
    private void cleanup() {
        IMediaAuthService authService = mCastManager.getMediaAuthService();
        if (null != mMediaAuthTimer) {
            mMediaAuthTimer.cancel();
        }
        if (null != mAuthThread) {
            mAuthThread = null;
        }
        if (null != mCastManager.getMediaAuthService()) {
            authService.setOnResult(null);
            mCastManager.removeMediaAuthService();
        }
        if (null != mCastManager) {
            mCastManager.removeVideoCastConsumer(mCastConsumer);
        }
        if (null != mHandler) {
            mHandler.removeCallbacksAndMessages(null);
        }
        if (null != mUrlAndBitmap) {
            mUrlAndBitmap.mBitmap = null;
        }
        if (!sDialogCanceled && null != mMediaAuthService) {
            mMediaAuthService.abort(MediaAuthStatus.ABORT_USER_CANCELLED);
        }

        mCastManager.clearContext(getActivity());
    }

}
