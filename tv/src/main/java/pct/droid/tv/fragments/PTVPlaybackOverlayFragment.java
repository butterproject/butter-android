/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package pct.droid.tv.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v17.leanback.app.PlaybackOverlayFragment;
import android.support.v17.leanback.app.PlaybackOverlaySupportFragment;
import android.support.v17.leanback.widget.AbstractDetailsDescriptionPresenter;
import android.support.v17.leanback.widget.Action;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.ClassPresenterSelector;
import android.support.v17.leanback.widget.ControlButtonPresenterSelector;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.ListRowPresenter;
import android.support.v17.leanback.widget.OnActionClickedListener;
import android.support.v17.leanback.widget.OnItemViewSelectedListener;
import android.support.v17.leanback.widget.PlaybackControlsRow;
import android.support.v17.leanback.widget.PlaybackControlsRow.ClosedCaptioningAction;
import android.support.v17.leanback.widget.PlaybackControlsRow.FastForwardAction;
import android.support.v17.leanback.widget.PlaybackControlsRow.PlayPauseAction;
import android.support.v17.leanback.widget.PlaybackControlsRow.RewindAction;
import android.support.v17.leanback.widget.PlaybackControlsRowPresenter;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.PresenterSelector;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.RowPresenter;
import android.support.v4.content.res.ResourcesCompat;
import android.util.Log;
import android.view.InputEvent;
import android.view.KeyEvent;

import de.greenrobot.event.EventBus;
import pct.droid.base.providers.subs.SubsProvider;
import pct.droid.base.torrent.StreamInfo;
import pct.droid.tv.R;
import pct.droid.tv.activities.PTVVideoPlayerActivity;
import pct.droid.tv.events.ConfigureSubtitleEvent;
import pct.droid.tv.events.PausePlaybackEvent;
import pct.droid.tv.events.PlaybackProgressChangedEvent;
import pct.droid.tv.events.ScaleVideoEvent;
import pct.droid.tv.events.SeekBackwardEvent;
import pct.droid.tv.events.SeekForwardEvent;
import pct.droid.tv.events.StartPlaybackEvent;
import pct.droid.tv.events.StreamProgressChangedEvent;
import pct.droid.tv.events.ToggleSubtitleEvent;
import pct.droid.tv.events.UpdatePlaybackStateEvent;

/*
 * Class for video playback with media control
 */
public class PTVPlaybackOverlayFragment extends PlaybackOverlaySupportFragment
        implements OnActionClickedListener,
        OnItemViewSelectedListener,
        PlaybackOverlaySupportFragment.InputEventHandler {
    private static final String TAG = "PlaybackOverlayFragment";
    private static final int BACKGROUND_TYPE = PlaybackOverlayFragment.BG_LIGHT;

    private static final int MODE_NOTHING = 0;
    private static final int MODE_FAST_FORWARD = 1;
    private static final int MODE_REWIND = 2;

    private ArrayObjectAdapter mRowsAdapter;
    private ArrayObjectAdapter mPrimaryActionsAdapter;
    private ArrayObjectAdapter mSecondaryActionsAdapter;
    private ClosedCaptioningAction mClosedCaptioningAction;
    private FastForwardAction mFastForwardAction;
    private ScaleVideoAction mScaleVideoAction;
    private PlayPauseAction mPlayPauseAction;
    private RewindAction mRewindAction;
    private PlaybackControlsRowPresenter mPlaybackControlsRowPresenter;
    private PlaybackControlsRow mPlaybackControlsRow;
    private Handler mHandler;
    private Runnable mRunnable;
    private StreamInfo mStreamInfo;

    private long mSelectedActionId = 0;
    private int mCurrentMode = MODE_NOTHING;
    private boolean mKeepEventBusRegistration = false;
    private boolean mIsMediaReady = false;
    private int mSeek;
    private int mBufferedTime;
    private int mCurrentTime;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        super.onCreate(savedInstanceState);

        Intent intent = getActivity().getIntent();
        if (intent != null) {
            mStreamInfo = intent.getParcelableExtra(PTVVideoPlayerActivity.INFO);
        }

        setFadeCompleteListener(new OnFadeCompleteListener() {

            @Override
            public void onFadeInComplete() {
                super.onFadeInComplete();
                mPlaybackControlsRow.setCurrentTime(mCurrentTime);
                mPlaybackControlsRow.setBufferedProgress(mBufferedTime);
                if (mRowsAdapter != null) mRowsAdapter.notifyArrayItemRangeChanged(0, mRowsAdapter.size());
            }
        });

        mHandler = new Handler();

        setBackgroundType(BACKGROUND_TYPE);
        setFadingEnabled(false);

        initialisePlaybackControlPresenter();
        setupPlaybackControlItemsToInitialisingState();

        setOnItemViewSelectedListener(this);
        setInputEventHandler(this);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!EventBus.getDefault().isRegistered(this)) EventBus.getDefault().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (!mKeepEventBusRegistration) {
            // Event is unregistered before playback paused event is sent
            mPlayPauseAction.setIndex(PlayPauseAction.PLAY);
            setFadingEnabled(false);
            notifyPlaybackControlActionChanged(mPlayPauseAction);
            EventBus.getDefault().unregister(this);
        }
    }

    @Override
    public void onActionClicked(Action action) {
        if (action.getId() == mPlayPauseAction.getId()) {
            invokeTogglePlaybackAction(mPlayPauseAction.getIndex() == PlayPauseAction.PLAY);
        }
        else if (action.getId() == mScaleVideoAction.getId()) {
            invokeScaleVideoAction();
        }
        else if (action.getId() == mClosedCaptioningAction.getId()) {
            invokeOpenSubtitleSettingsAction();
        }

        if (action instanceof PlaybackControlsRow.MultiAction) {
            notifyPlaybackControlActionChanged(action);
        }
    }

    @Override
    public void onItemSelected(
            Presenter.ViewHolder itemViewHolder,
            Object item,
            RowPresenter.ViewHolder rowViewHolder,
            Row row) {
        mCurrentMode = MODE_NOTHING;
        mSelectedActionId = 0;
        if (item != null && item instanceof Action) {
            Action action = (Action) item;
            mSelectedActionId = action.getId();
        }
    }

    @Override
    public boolean handleInputEvent(InputEvent event) {
        if (event instanceof KeyEvent) {
            KeyEvent keyEvent = (KeyEvent) event;
            if (keyEvent.getKeyCode() != KeyEvent.KEYCODE_DPAD_CENTER) return false;
            if (keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                if (mSelectedActionId == mFastForwardAction.getId()) {
                    if (keyEvent.getRepeatCount() == 0) {
                        mCurrentMode = MODE_FAST_FORWARD;
                        invokeFastForwardAction();
                    }
                }
                else if (mSelectedActionId == mRewindAction.getId()) {
                    if (keyEvent.getRepeatCount() == 0) {
                        mCurrentMode = MODE_REWIND;
                        invokeRewindAction();
                    }
                }
            }
            else if (keyEvent.getAction() == KeyEvent.ACTION_UP) {
                mCurrentMode = MODE_NOTHING;
            }
        }
        return false;
    }

    @SuppressWarnings("unused")
    public void onEvent(Object event) {
        if (event instanceof UpdatePlaybackStateEvent) {
            UpdatePlaybackStateEvent updatePlaybackStateEvent = (UpdatePlaybackStateEvent) event;
            if (updatePlaybackStateEvent.isPlaying()) {
                if (!mIsMediaReady) {
                    setupPlaybackControlItemsToReadyState();
                    mIsMediaReady = true;
                }

                mPlayPauseAction.setIndex(PlayPauseAction.PAUSE);
                setFadingEnabled(true);
            }
            else {
                mPlayPauseAction.setIndex(PlayPauseAction.PLAY);
                setFadingEnabled(false);
            }

            notifyPlaybackControlActionChanged(mPlayPauseAction);
            if (mRowsAdapter != null) mRowsAdapter.notifyArrayItemRangeChanged(0, mRowsAdapter.size());
        }
        else if (event instanceof PlaybackProgressChangedEvent) {
            // Ignore if currently seeking
            PlaybackProgressChangedEvent progressChangedEvent = (PlaybackProgressChangedEvent) event;
            if (mPlaybackControlsRow.getTotalTime() == 0) {
                mPlaybackControlsRow.setTotalTime((int) progressChangedEvent.getDuration());
            }
            if (mSeek != 0 && mCurrentMode != MODE_NOTHING) {
                return;
            }
            mCurrentTime = (int) progressChangedEvent.getCurrentTime();
            if (!isHidden()) {
                mPlaybackControlsRow.setCurrentTime(mCurrentTime);
                if (mRowsAdapter != null) mRowsAdapter.notifyArrayItemRangeChanged(0, mRowsAdapter.size());
            }
        }
        else if (event instanceof StreamProgressChangedEvent) {
            StreamProgressChangedEvent streamProgressChangedEvent = (StreamProgressChangedEvent) event;
            mBufferedTime = (int) streamProgressChangedEvent.getBufferedTime();
            if (!isHidden()) {
                mPlaybackControlsRow.setBufferedProgress(mBufferedTime);
                if (mRowsAdapter != null) mRowsAdapter.notifyArrayItemRangeChanged(0, mRowsAdapter.size());
            }
        }
        else if (event instanceof ToggleSubtitleEvent) {
            ToggleSubtitleEvent toggleSubtitleEvent = (ToggleSubtitleEvent) event;
            if (toggleSubtitleEvent.isEnabled()) {
                mClosedCaptioningAction.setIndex(ClosedCaptioningAction.ON);
            }
            else {
                mClosedCaptioningAction.setIndex(ClosedCaptioningAction.OFF);
            }

            notifyPlaybackControlActionChanged(mClosedCaptioningAction);
        }
    }

    private void initialisePlaybackControlPresenter() {
        ClassPresenterSelector presenterSelector = new ClassPresenterSelector();
        mPlaybackControlsRowPresenter = new PlaybackControlsRowPresenter(new DescriptionPresenter());
        mPlaybackControlsRowPresenter.setSecondaryActionsHidden(false);

        presenterSelector.addClassPresenter(PlaybackControlsRow.class, mPlaybackControlsRowPresenter);
        presenterSelector.addClassPresenter(ListRow.class, new ListRowPresenter());

        mRowsAdapter = new ArrayObjectAdapter(presenterSelector);
        setAdapter(mRowsAdapter);
    }

    private void setupPlaybackControlItemsActions() {
        mPlaybackControlsRowPresenter.setOnActionClickedListener(this);
    }

    private void setupPlaybackControlItemsToInitialisingState() {
        mRowsAdapter.clear();
        mPlaybackControlsRow = new PlaybackControlsRow(mStreamInfo);
        mPlaybackControlsRow.setCurrentTime(0);
        mPlaybackControlsRow.setBufferedProgress(0);

        ControlButtonPresenterSelector presenterSelector = new ControlButtonPresenterSelector();
        mPrimaryActionsAdapter = new ArrayObjectAdapter(presenterSelector);
        mPlaybackControlsRow.setPrimaryActionsAdapter(mPrimaryActionsAdapter);

        Activity activity = getActivity();
        mPlayPauseAction = new PlayPauseAction(activity);
        mPrimaryActionsAdapter.add(mPlayPauseAction);

        setupSecondaryRowPlaybackControl(presenterSelector);

        mRowsAdapter.add(mPlaybackControlsRow);
        mRowsAdapter.notifyArrayItemRangeChanged(0, mRowsAdapter.size());
    }

    private void setupPlaybackControlItemsToReadyState() {
        mRowsAdapter.clear();
        mPlaybackControlsRow = new PlaybackControlsRow(mStreamInfo);
        mPlaybackControlsRow.setCurrentTime(0);
        mPlaybackControlsRow.setBufferedProgress(0);

        ControlButtonPresenterSelector presenterSelector = new ControlButtonPresenterSelector();
        setupPrimaryRowPlaybackControl(presenterSelector);
        setupSecondaryRowPlaybackControl(presenterSelector);

        mRowsAdapter.add(mPlaybackControlsRow);
        mRowsAdapter.notifyArrayItemRangeChanged(0, mRowsAdapter.size());

        setupPlaybackControlItemsActions();
    }

    private void setupPrimaryRowPlaybackControl(ControlButtonPresenterSelector presenterSelector) {
        mPrimaryActionsAdapter = new ArrayObjectAdapter(presenterSelector);
        mPlaybackControlsRow.setPrimaryActionsAdapter(mPrimaryActionsAdapter);

        Activity activity = getActivity();
        mPlayPauseAction = new PlayPauseAction(activity);
        mFastForwardAction = new FastForwardAction(activity);
        mRewindAction = new RewindAction(activity);

        // Add main controls to primary adapter.
        mPrimaryActionsAdapter.add(mRewindAction);
        mPrimaryActionsAdapter.add(mPlayPauseAction);
        mPrimaryActionsAdapter.add(mFastForwardAction);
    }

    private void setupSecondaryRowPlaybackControl(PresenterSelector presenterSelector) {
        mSecondaryActionsAdapter = new ArrayObjectAdapter(presenterSelector);
        mPlaybackControlsRow.setSecondaryActionsAdapter(mSecondaryActionsAdapter);

        Activity activity = getActivity();

        mScaleVideoAction = new ScaleVideoAction(activity);
        mClosedCaptioningAction = new ClosedCaptioningAction(activity);

        if (mStreamInfo.getSubtitleLanguage() != null && !mStreamInfo.getSubtitleLanguage().equals(SubsProvider.SUBTITLE_LANGUAGE_NONE)) {
            mClosedCaptioningAction.setIndex(ClosedCaptioningAction.ON);
        } else {
            mClosedCaptioningAction.setIndex(ClosedCaptioningAction.OFF);
        }

        // Add rest of controls to secondary adapter.
        mSecondaryActionsAdapter.add(mScaleVideoAction);
        mSecondaryActionsAdapter.add(mClosedCaptioningAction);
    }

    private void notifyPlaybackControlActionChanged(Action action) {
        if (action == null) return;
        ArrayObjectAdapter adapter = mPrimaryActionsAdapter;
        if (adapter.indexOf(action) >= 0) {
            adapter.notifyArrayItemRangeChanged(adapter.indexOf(action), 1);
            return;
        }
        adapter = mSecondaryActionsAdapter;
        if (adapter.indexOf(action) >= 0) {
            adapter.notifyArrayItemRangeChanged(adapter.indexOf(action), 1);
        }
    }

    private void invokeOpenSubtitleSettingsAction() {
        EventBus.getDefault().post(new ConfigureSubtitleEvent());
    }

    private void invokeTogglePlaybackAction(boolean play) {
        if (play) {
            EventBus.getDefault().post(new StartPlaybackEvent());
            setFadingEnabled(true);
        } else {
            EventBus.getDefault().post(new PausePlaybackEvent());
            setFadingEnabled(false);
        }
    }

    private void invokeFastForwardAction() {
        final int refreshDuration = 100;
        mRunnable = new Runnable() {
            @Override
            public void run() {
                if (mCurrentMode == MODE_FAST_FORWARD) {
                    int currentTime = mPlaybackControlsRow.getCurrentTime();
                    currentTime += SeekForwardEvent.MINIMUM_SEEK_SPEED;

                    if (currentTime < mPlaybackControlsRow.getTotalTime()) {
                        mPlaybackControlsRow.setCurrentTime(currentTime);
                        mRowsAdapter.notifyArrayItemRangeChanged(0, 1);
                        mSeek += SeekForwardEvent.MINIMUM_SEEK_SPEED;
                    }

                    mHandler.postDelayed(this, refreshDuration);
                }
                else if (mSelectedActionId == mFastForwardAction.getId()) {
                    triggerFastForwardEvent();
                }
            }
        };
        mHandler.postDelayed(mRunnable, refreshDuration);
    }

    private void invokeRewindAction() {
        final int refreshDuration = 100;
        mRunnable = new Runnable() {
            @Override
            public void run() {
                if (mCurrentMode == MODE_REWIND) {
                    int currentTime = mPlaybackControlsRow.getCurrentTime();
                    currentTime -= SeekBackwardEvent.MINIMUM_SEEK_SPEED;

                    if (currentTime > 0) {
                        mPlaybackControlsRow.setCurrentTime(currentTime);
                        mRowsAdapter.notifyArrayItemRangeChanged(0, 1);
                        mSeek += SeekBackwardEvent.MINIMUM_SEEK_SPEED;
                    }

                    mHandler.postDelayed(this, refreshDuration);
                }
                else if (mSelectedActionId == mRewindAction.getId()) {
                    triggerRewindEvent();
                }
            }
        };
        mHandler.postDelayed(mRunnable, refreshDuration);
    }

    private void triggerFastForwardEvent() {
        SeekForwardEvent event = new SeekForwardEvent();
        event.setSeek(mSeek);
        mSeek = 0;
        EventBus.getDefault().post(event);
    }

    private void triggerRewindEvent() {
        SeekBackwardEvent event = new SeekBackwardEvent();
        event.setSeek(mSeek);
        mSeek = 0;
        EventBus.getDefault().post(event);
    }

    private void invokeScaleVideoAction() {
        EventBus.getDefault().post(new ScaleVideoEvent());
    }

    public void setKeepEventBusRegistration(boolean keepEventBusRegistration) {
        this.mKeepEventBusRegistration = keepEventBusRegistration;
    }

    class DescriptionPresenter extends AbstractDetailsDescriptionPresenter {
        @Override
        protected void onBindDescription(ViewHolder viewHolder, Object item) {
            if (!(item instanceof StreamInfo)) return;
            StreamInfo streamInfo = (StreamInfo) item;
            if (streamInfo.isShow()) {
                viewHolder.getTitle().setText(streamInfo.getShowTitle());
                viewHolder.getSubtitle().setText(streamInfo.getShowEpisodeTitle());
            }
            else {
                viewHolder.getTitle().setText(streamInfo.getTitle());
            }
        }
    }

    public static class ScaleVideoAction extends Action {
        public ScaleVideoAction(Context context) {
            super(R.id.control_scale);
            setIcon(ResourcesCompat.getDrawable(
                context.getResources(),
                R.drawable.ic_av_aspect_ratio,
                null));
            setLabel1(context.getString(R.string.scale));
        }
    }
}