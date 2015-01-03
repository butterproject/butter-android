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
import com.google.android.gms.cast.MediaStatus;
import com.google.sample.castcompanionlibrary.R;
import com.google.sample.castcompanionlibrary.cast.VideoCastManager;
import com.google.sample.castcompanionlibrary.cast.exceptions.CastException;
import com.google.sample.castcompanionlibrary.cast.exceptions.NoConnectionException;
import com.google.sample.castcompanionlibrary.cast.exceptions.TransientNetworkDisconnectionException;
import com.google.sample.castcompanionlibrary.cast.tracks.ui.TracksChooserDialog;
import com.google.sample.castcompanionlibrary.utils.LogUtils;
import com.google.sample.castcompanionlibrary.utils.Utils;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

/**
 * This class provides an {@link Activity} that clients can easily add to their applications to
 * provide an out-of-the-box remote player when a video is casting to a cast device.
 * {@link VideoCastManager} can manage the lifecycle and presentation of this activity.
 * <p>
 * This activity provides a number of controllers for managing the playback of the remote content:
 * play/pause (or play/stop when a live stream is used) and seekbar (for non-live streams).
 * <p>
 * Clients who need to perform a pre-authorization process for playback can register a
 * {@link IMediaAuthListener} by calling
 * {@link VideoCastManager#startCastControllerActivity(android.content.Context,
 * IMediaAuthService)}.
 * In that case, this activity manages starting the {@link IMediaAuthService} and will register a
 * listener to handle the result.
 */
public class VideoCastControllerActivity extends ActionBarActivity implements IVideoCastController {

    private static final String TAG = LogUtils.makeLogTag(VideoCastControllerActivity.class);
    private VideoCastManager mCastManager;
    private ImageView mPlayPause;
    private TextView mLiveText;
    private TextView mStart;
    private TextView mEnd;
    private SeekBar mSeekbar;
    private TextView mLine1;
    private TextView mLine2;
    private ProgressBar mLoading;
    private ImageView mClosedCaptionIcon;
    private float mVolumeIncrement;
    private View mControllers;
    private Drawable mPauseDrawable;
    private Drawable mPlayDrawable;
    private Drawable mStopDrawable;
    private VideoCastControllerFragment mVideoCastControllerFragment;
    private OnVideoCastControllerListener mListener;
    private int mStreamType;
    public static final float DEFAULT_VOLUME_INCREMENT = 0.05f;
    private ImageView mBackgroundImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cast_activity);
        loadAndSetupViews();
        mVolumeIncrement = Utils.getFloatFromPreference(
                this, VideoCastManager.PREFS_KEY_VOLUME_INCREMENT);
        if (mVolumeIncrement == Float.MIN_VALUE) {
            mVolumeIncrement = DEFAULT_VOLUME_INCREMENT;
        }
        try {
            mCastManager = VideoCastManager.getInstance(this);
        } catch (CastException e) {
            // logged already
            finish();
        }

        setupActionBar();
        Bundle extras = getIntent().getExtras();
        if (null == extras) {
            finish();
            return;
        }

        FragmentManager fm = getSupportFragmentManager();
        mVideoCastControllerFragment = (VideoCastControllerFragment) fm.findFragmentByTag("task");

        // if fragment is null, it means this is the first time, so create it
        if (mVideoCastControllerFragment == null) {
            mVideoCastControllerFragment = VideoCastControllerFragment.newInstance(extras);
            fm.beginTransaction().add(mVideoCastControllerFragment, "task").commit();
            mListener = mVideoCastControllerFragment;
            setOnVideoCastControllerChangedListener(mListener);
        } else {
            mListener = mVideoCastControllerFragment;
            mListener.onConfigurationChanged();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.cast_player_menu, menu);
        mCastManager.addMediaRouterButton(menu, R.id.media_route_menu_item);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return true;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (mCastManager.onDispatchVolumeKeyEvent(event, mVolumeIncrement)) {
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    protected void onResume() {
        LOGD(TAG, "onResume() was called");
        try {
            mCastManager = VideoCastManager.getInstance(VideoCastControllerActivity.this);
        } catch (CastException e) {
            // logged already
        }

        super.onResume();
    }

    private void loadAndSetupViews() {
        mBackgroundImage = (ImageView) findViewById(R.id.background_image);
        mPauseDrawable = getResources().getDrawable(R.drawable.ic_av_pause_dark);
        mPlayDrawable = getResources().getDrawable(R.drawable.ic_av_play_dark);
        mStopDrawable = getResources().getDrawable(R.drawable.ic_av_stop_dark);
        mPlayPause = (ImageView) findViewById(R.id.imageView1);
        mLiveText = (TextView) findViewById(R.id.liveText);
        mStart = (TextView) findViewById(R.id.startText);
        mEnd = (TextView) findViewById(R.id.endText);
        mSeekbar = (SeekBar) findViewById(R.id.seekBar1);
        mLine1 = (TextView) findViewById(R.id.textView1);
        mLine2 = (TextView) findViewById(R.id.textView2);
        mLoading = (ProgressBar) findViewById(R.id.progressBar1);
        mControllers = findViewById(R.id.controllers);
        mClosedCaptionIcon = (ImageView) findViewById(R.id.cc);
        updateClosedCaption(CC_DISABLED);
        mPlayPause.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                try {
                    mListener.onPlayPauseClicked(v);
                } catch (TransientNetworkDisconnectionException e) {
                    LOGE(TAG, "Failed to toggle playback due to temporary network issue", e);
                    Utils.showErrorDialog(VideoCastControllerActivity.this,
                            R.string.failed_no_connection_trans);
                } catch (NoConnectionException e) {
                    LOGE(TAG, "Failed to toggle playback due to network issues", e);
                    Utils.showErrorDialog(VideoCastControllerActivity.this,
                            R.string.failed_no_connection);
                } catch (Exception e) {
                    LOGE(TAG, "Failed to toggle playback due to other issues", e);
                    Utils.showErrorDialog(VideoCastControllerActivity.this,
                            R.string.failed_perform_action);
                }
            }
        });

        mSeekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                try {
                    if (null != mListener) {
                        mListener.onStopTrackingTouch(seekBar);
                    }
                } catch (Exception e) {
                    LOGE(TAG, "Failed to complete seek", e);
                    finish();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                try {
                    if (null != mListener) {
                        mListener.onStartTrackingTouch(seekBar);
                    }
                } catch (Exception e) {
                    LOGE(TAG, "Failed to start seek", e);
                    finish();
                }
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                    boolean fromUser) {
                mStart.setText(Utils.formatMillis(progress));
                try {
                    if (null != mListener) {
                        mListener.onProgressChanged(seekBar, progress, fromUser);
                    }
                } catch (Exception e) {
                    LOGE(TAG, "Failed to set teh progress result", e);
                }
            }
        });

        mClosedCaptionIcon.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    new TracksChooserDialog(mCastManager.getRemoteMediaInformation(),
                            mVideoCastControllerFragment)
                            .show(getSupportFragmentManager(), "dlg");
                } catch (TransientNetworkDisconnectionException e) {
                    LOGE(TAG, "Failed to get the media", e);
                } catch (NoConnectionException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void setupActionBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if(toolbar != null) {
            toolbar.setTitle("");
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public void showLoading(boolean visible) {
        mLoading.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
    }

    // -------------- IVideoCastController implementation ---------------- //
    @Override
    public void adjustControllersForLiveStream(boolean isLive) {
        int visibility = isLive ? View.INVISIBLE : View.VISIBLE;
        mLiveText.setVisibility(isLive ? View.VISIBLE : View.INVISIBLE);
        mStart.setVisibility(visibility);
        mEnd.setVisibility(visibility);
        mSeekbar.setVisibility(visibility);
    }

    @Override
    public void updateClosedCaption(int status) {
        switch (status) {
            case CC_ENABLED:
                mClosedCaptionIcon.setVisibility(View.VISIBLE);
                mClosedCaptionIcon.setEnabled(true);
                break;
            case CC_DISABLED:
                mClosedCaptionIcon.setVisibility(View.VISIBLE);
                mClosedCaptionIcon.setEnabled(false);
                break;
            case CC_HIDDEN:
                mClosedCaptionIcon.setVisibility(View.GONE);
                break;
        }
    }

    @Override
    public void setPlaybackStatus(int state) {
        LOGD(TAG, "setPlaybackStatus(): state = " + state);
        switch (state) {
            case MediaStatus.PLAYER_STATE_PLAYING:
                mLoading.setVisibility(View.INVISIBLE);
                mPlayPause.setVisibility(View.VISIBLE);

                if (mStreamType == MediaInfo.STREAM_TYPE_LIVE) {
                    mPlayPause.setImageDrawable(mStopDrawable);
                } else {
                    mPlayPause.setImageDrawable(mPauseDrawable);
                }

                mLine2.setText(getString(R.string.casting_to_device,
                        mCastManager.getDeviceName()));
                mControllers.setVisibility(View.VISIBLE);
                break;
            case MediaStatus.PLAYER_STATE_PAUSED:
                mControllers.setVisibility(View.VISIBLE);
                mLoading.setVisibility(View.INVISIBLE);
                mPlayPause.setVisibility(View.VISIBLE);
                mPlayPause.setImageDrawable(mPlayDrawable);
                mLine2.setText(getString(R.string.casting_to_device,
                        mCastManager.getDeviceName()));
                break;
            case MediaStatus.PLAYER_STATE_IDLE:
                mLoading.setVisibility(View.INVISIBLE);
                mPlayPause.setImageDrawable(mPlayDrawable);
                mPlayPause.setVisibility(View.VISIBLE);
                mLine2.setText(getString(R.string.casting_to_device,
                        mCastManager.getDeviceName()));
                break;
            case MediaStatus.PLAYER_STATE_BUFFERING:
                mPlayPause.setVisibility(View.INVISIBLE);
                mLoading.setVisibility(View.VISIBLE);
                mLine2.setText(getString(R.string.loading));
                break;
            default:
                break;
        }
    }

    @Override
    public void updateSeekbar(int position, int duration) {
        mSeekbar.setProgress(position);
        mSeekbar.setMax(duration);
        mStart.setText(Utils.formatMillis(position));
        mEnd.setText(Utils.formatMillis(duration));
    }

    @SuppressWarnings("deprecation")
    @Override
    public void setImage(Bitmap bitmap) {
        if (null != bitmap) {
            mBackgroundImage.setImageBitmap(bitmap);
        }
    }

    @Override
    public void setLine1(String text) {
        if (null == text) {
            text = "";
        }
        mLine1.setText(text);

    }

    @Override
    public void setLine2(String text) {
        if (null == text) {
            text = "";
        }
        mLine2.setText(text);

    }

    @Override
    public void setOnVideoCastControllerChangedListener(OnVideoCastControllerListener listener) {
        if (null != listener) {
            this.mListener = listener;
        }
    }

    @Override
    public void setStreamType(int streamType) {
        this.mStreamType = streamType;
    }

    @Override
    public void updateControllersStatus(boolean enabled) {
        mControllers.setVisibility(enabled ? View.VISIBLE : View.INVISIBLE);
        if (enabled) {
            adjustControllersForLiveStream(mStreamType == MediaInfo.STREAM_TYPE_LIVE);
        }
    }

    @Override
    public void closeActivity() {
        finish();
    }
}
