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

package com.google.sample.castcompanionlibrary.cast.dialog.video;

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
import com.google.sample.castcompanionlibrary.utils.FetchBitmapTask;
import com.google.sample.castcompanionlibrary.utils.LogUtils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.MediaRouteControllerDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * A custom {@link MediaRouteControllerDialog} that provides an album art, a play/pause button and
 * the ability to take user to the target activity when the album art is tapped.
 */
public class VideoMediaRouteControllerDialog extends MediaRouteControllerDialog {

    private static final String TAG =
            LogUtils.makeLogTag(VideoMediaRouteControllerDialog.class);

    private ImageView mIcon;
    private ImageView mPausePlay;
    private TextView mTitle;
    private TextView mSubTitle;
    private TextView mEmptyText;
    private ProgressBar mLoading;
    private Uri mIconUri;
    private VideoCastManager mCastManager;
    protected int mState;
    private VideoCastConsumerImpl castConsumerImpl;
    private Drawable mPauseDrawable;
    private Drawable mPlayDrawable;
    private Drawable mStopDrawable;
    private Context mContext;
    private View mIconContainer;
    private View mTextContainer;

    private int mStreamType;
    private FetchBitmapTask mFetchBitmap;

    public VideoMediaRouteControllerDialog(Context context, int theme) {
        super(context, theme);
    }

    @Override
    protected void onStop() {
        if (null != mCastManager) {
            mCastManager.removeVideoCastConsumer(castConsumerImpl);
        }
        if (mFetchBitmap != null) {
            mFetchBitmap.cancel(true);
            mFetchBitmap = null;
        }
        super.onStop();
    }

    /**
     * Creates a new VideoMediaRouteControllerDialog in the given context.
     */
    public VideoMediaRouteControllerDialog(Context context) {
        super(context, R.style.CastDialog);
        try {
            this.mContext = context;
            mCastManager = VideoCastManager.getInstance();
            mState = mCastManager.getPlaybackStatus();
            castConsumerImpl = new VideoCastConsumerImpl() {

                @Override
                public void onRemoteMediaPlayerStatusUpdated() {
                    mState = mCastManager.getPlaybackStatus();
                    updatePlayPauseState(mState);
                }

                /*
                 * (non-Javadoc)
                 * @see com.google.sample.castcompanionlibrary.cast.VideoCastConsumerImpl
                 * #onMediaChannelMetadataUpdated()
                 */
                @Override
                public void onRemoteMediaPlayerMetadataUpdated() {
                    updateMetadata();
                }

            };
            mCastManager.addVideoCastConsumer(castConsumerImpl);
            mPauseDrawable = context.getResources().getDrawable(R.drawable.ic_av_pause_sm_dark);
            mPlayDrawable = context.getResources().getDrawable(R.drawable.ic_av_play_sm_dark);
            mStopDrawable = context.getResources().getDrawable(R.drawable.ic_av_stop_sm_dark);
        } catch (CastException e) {
            LOGE(TAG, "Failed to update the content of dialog", e);
        } catch (IllegalStateException e) {
            LOGE(TAG, "Failed to update the content of dialog", e);
        }
    }

    /*
     * Hides/show the icon and metadata and play/pause if there is no media
     */
    private void hideControls(boolean hide, int resId) {
        int visibility = hide ? View.GONE : View.VISIBLE;
        mIcon.setVisibility(visibility);
        mIconContainer.setVisibility(visibility);
        mTextContainer.setVisibility(visibility);
        mEmptyText.setText(resId == 0 ? R.string.no_media_info : resId);
        mEmptyText.setVisibility(hide ? View.VISIBLE : View.GONE);
        if (hide) mPausePlay.setVisibility(visibility);
    }

    private void updateMetadata() {
        MediaInfo info = null;
        try {
            info = mCastManager.getRemoteMediaInformation();
        } catch (TransientNetworkDisconnectionException e) {
            hideControls(true, R.string.failed_no_connection_short);
            return;
        } catch (Exception e) {
            LOGE(TAG, "Failed to get media information", e);
        }
        if (null == info) {
            hideControls(true, R.string.no_media_info);
            return;
        }
        mStreamType = info.getStreamType();
        hideControls(false, 0);
        MediaMetadata mm = info.getMetadata();
        mTitle.setText(mm.getString(MediaMetadata.KEY_TITLE));
        mSubTitle.setText(mm.getString(MediaMetadata.KEY_SUBTITLE));
        setIcon(mm.hasImages() ? mm.getImages().get(0).getUrl() : null);
    }

    public void setIcon(Uri uri) {
        if (null != mIconUri && mIconUri.equals(uri)) {
            return;
        }
        mIconUri = uri;
        if (null == uri) {
            Bitmap bm = BitmapFactory.decodeResource(
                    mContext.getResources(), R.drawable.video_placeholder_200x200);
            mIcon.setImageBitmap(bm);
            return;
        }

        if (mFetchBitmap != null) {
            mFetchBitmap.cancel(true);
        }

        mFetchBitmap = new FetchBitmapTask() {
            @Override
            protected void onPostExecute(Bitmap bitmap) {
                mIcon.setImageBitmap(bitmap);
                if (this == mFetchBitmap) {
                    mFetchBitmap = null;
                }
            }
        };

        mFetchBitmap.start(mIconUri);
    }

    private void updatePlayPauseState(int state) {
        if (null != mPausePlay) {
            switch (state) {
                case MediaStatus.PLAYER_STATE_PLAYING:
                    mPausePlay.setImageDrawable(getPauseStopButton());
                    adjustControlsVisibility(true);
                    break;
                case MediaStatus.PLAYER_STATE_PAUSED:
                    mPausePlay.setImageDrawable(mPlayDrawable);
                    adjustControlsVisibility(true);
                    break;
                case MediaStatus.PLAYER_STATE_IDLE:
                    mPausePlay.setVisibility(View.INVISIBLE);
                    setLoadingVisibility(false);

                    if (mState == MediaStatus.PLAYER_STATE_IDLE
                            && mCastManager.getIdleReason() == MediaStatus.IDLE_REASON_FINISHED) {
                        hideControls(true, R.string.no_media_info);
                    } else {
                        switch (mStreamType) {
                            case MediaInfo.STREAM_TYPE_BUFFERED:
                                mPausePlay.setVisibility(View.INVISIBLE);
                                setLoadingVisibility(false);
                                break;
                            case MediaInfo.STREAM_TYPE_LIVE:
                                int idleReason = mCastManager.getIdleReason();
                                if (idleReason == MediaStatus.IDLE_REASON_CANCELED) {
                                    mPausePlay.setImageDrawable(mPlayDrawable);
                                    adjustControlsVisibility(true);
                                } else {
                                    mPausePlay.setVisibility(View.INVISIBLE);
                                    setLoadingVisibility(false);
                                }
                                break;
                        }
                    }
                    break;
                case MediaStatus.PLAYER_STATE_BUFFERING:
                    adjustControlsVisibility(false);
                    break;
                default:
                    mPausePlay.setVisibility(View.INVISIBLE);
                    setLoadingVisibility(false);
            }
        }
    }

    private Drawable getPauseStopButton() {
        switch (mStreamType) {
            case MediaInfo.STREAM_TYPE_BUFFERED:
                return mPauseDrawable;
            case MediaInfo.STREAM_TYPE_LIVE:
                return mStopDrawable;
            default:
                return mPauseDrawable;
        }
    }

    private void setLoadingVisibility(boolean show) {
        mLoading.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void adjustControlsVisibility(boolean showPlayPause) {
        int visible = showPlayPause ? View.VISIBLE : View.INVISIBLE;
        mPausePlay.setVisibility(visible);
        setLoadingVisibility(!showPlayPause);
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (null != castConsumerImpl) {
            mCastManager.removeVideoCastConsumer(castConsumerImpl);
        }
        if (mFetchBitmap != null) {
            mFetchBitmap.cancel(true);
            mFetchBitmap = null;
        }
    }

    /**
     * Initializes this dialog's set of playback buttons and adds click listeners.
     */
    @Override
    public View onCreateMediaControlView(Bundle savedInstanceState) {
        LayoutInflater inflater = getLayoutInflater();
        View controls = inflater.inflate(R.layout.custom_media_route_controller_controls_dialog,
                null);

        loadViews(controls);
        mState = mCastManager.getPlaybackStatus();
        updateMetadata();
        updatePlayPauseState(mState);
        setupCallbacks();
        return controls;
    }

    private void setupCallbacks() {

        mPausePlay.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (null == mCastManager) {
                    return;
                }
                try {
                    adjustControlsVisibility(false);
                    mCastManager.togglePlayback();
                } catch (CastException e) {
                    adjustControlsVisibility(true);
                    LOGE(TAG, "Failed to toggle playback", e);
                } catch (TransientNetworkDisconnectionException e) {
                    adjustControlsVisibility(true);
                    LOGE(TAG, "Failed to toggle playback due to network issues", e);
                } catch (NoConnectionException e) {
                    adjustControlsVisibility(true);
                    LOGE(TAG, "Failed to toggle playback due to network issues", e);
                }
            }
        });

        mIcon.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                showTargetActivity();
            }

        });

        mTextContainer.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                showTargetActivity();
            }

        });
    }

    private void showTargetActivity() {
        if (null != mCastManager
                && null != mCastManager.getTargetActivity()) {
            try {
                mCastManager.onTargetActivityInvoked(mContext);
            } catch (TransientNetworkDisconnectionException e) {
                LOGE(TAG, "Failed to start the target activity due to network issues", e);
            } catch (NoConnectionException e) {
                LOGE(TAG, "Failed to start the target activity due to network issues", e);
            }
            cancel();
        }
    }

    private void loadViews(View controls) {
        mIcon = (ImageView) controls.findViewById(R.id.iconView);
        mIconContainer = controls.findViewById(R.id.iconContainer);
        mTextContainer = controls.findViewById(R.id.textContainer);
        mPausePlay = (ImageView) controls.findViewById(R.id.playPauseView);
        mTitle = (TextView) controls.findViewById(R.id.titleView);
        mSubTitle = (TextView) controls.findViewById(R.id.subTitleView);
        mLoading = (ProgressBar) controls.findViewById(R.id.loadingView);
        mEmptyText = (TextView) controls.findViewById(R.id.emptyView);
    }
}
