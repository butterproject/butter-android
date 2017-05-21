/*
 * This file is part of Butter.
 *
 * Butter is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Butter is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Butter. If not, see <http://www.gnu.org/licenses/>.
 */

package butter.droid.ui.player.abs;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaControllerCompat.Callback;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnSystemUiVisibilityChangeListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import butter.droid.R;
import butter.droid.base.BuildConfig;
import butter.droid.base.manager.internal.vlc.VlcPlayer;
import butter.droid.base.ui.player.base.BaseVideoPlayerView;
import butter.droid.base.utils.AnimUtils;
import butter.droid.base.utils.LocaleUtils;
import butter.droid.base.utils.PixelUtils;
import butter.droid.base.utils.StringUtils;
import butter.droid.base.utils.VersionUtils;
import butter.droid.ui.player.VideoPlayerTouchHandler;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import javax.inject.Inject;

public class AbsPlayerFragment extends Fragment implements AbsPlayerView, BaseVideoPlayerView, OnSystemUiVisibilityChangeListener {

    protected static final String ARG_RESUME_POSITION = "butter.droid.ui.player.abs.AbsPlayerFragment.resumePosition";

    private static final String ACTION_SCALE = "butter.droid.ui.player.abs.action.SCALE";

    private static final int FADE_OUT_INFO = 1000;

    @Inject AbsPlayerPresenter presenter;
    @Inject VideoPlayerTouchHandler touchHandler;
    @Inject VlcPlayer player;

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.video_surface) SurfaceView videoSurface;
    @BindView(R.id.control_bar) butter.droid.widget.SeekBar controlBar;
    @BindView(R.id.control_layout) RelativeLayout controlLayout;
    @BindView(R.id.progress_indicator) ProgressBar progressIndicator;
    @BindView(R.id.play_button) ImageButton playButton;
    @BindView(R.id.forward_button) ImageButton forwardButton;
    @BindView(R.id.rewind_button) ImageButton rewindButton;
    @BindView(R.id.player_info) TextView playerInfo;
    @BindView(R.id.current_time) TextView currentTimeTextView;
    @BindView(R.id.length_time) TextView lengthTime;
    View decorView;

    protected MediaSessionCompat mediaSession;
    protected MediaControllerCompat mediaController;
    protected PlaybackStateCompat.Builder stateBuilder;
    private MediaMetadataCompat.Builder metadataBuilder;

    private int lastSystemUIVisibility;
    protected boolean overlayVisible;

    @Override public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        mediaSession = new MediaSessionCompat(getContext(), BuildConfig.APPLICATION_ID);
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mediaSession.setMediaButtonReceiver(null);
        mediaSession.setCallback(new PlayerSessionCallback());

        stateBuilder = new PlaybackStateCompat.Builder()
                .setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE
                        | PlaybackStateCompat.ACTION_REWIND
                        | PlaybackStateCompat.ACTION_FAST_FORWARD
                        | PlaybackStateCompat.ACTION_SEEK_TO)
                .addCustomAction(ACTION_SCALE, getString(R.string.scale), R.drawable.ic_av_aspect_ratio);

        metadataBuilder = new MediaMetadataCompat.Builder();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_videoplayer, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.setOnTouchListener(touchHandler);
        ButterKnife.bind(this, view);

        setupToolbar();
        setupDecorView();
        setupProgressBar();
        initControlls();
        setProgressVisible(true);

        presenter.onViewCreated();
    }

    @Override public void onResume() {
        super.onResume();

        presenter.onResume();
    }

    @Override public void onPause() {
        super.onPause();

        presenter.onPause();
    }

    @Override public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);

        presenter.onSaveInstanceState(outState);
    }

    @Override public void onStop() {
        super.onStop();

        presenter.onStop();
    }

    @Override public void onDestroy() {
        super.onDestroy();

        presenter.onDestroy();
    }

    @Override public void displayTitle(final String title) {
        getAppCompatActivity().getSupportActionBar().setTitle(title);
        getAppCompatActivity().getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override public void toggleOverlay() {
        if (overlayVisible) {
            hideOverlay();
        } else {
            showOverlay();
        }
    }

    @Override public void hideOverlay() {
        if (overlayVisible) {
            AnimUtils.fadeOut(controlLayout);
            AnimUtils.fadeOut(toolbar);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN;
                decorView.setSystemUiVisibility(uiOptions);
            } else {
                getAppCompatActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                getAppCompatActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
            }

            overlayVisible = false;
        }
    }

    @CallSuper @Override public void setupControls(final String title) {
        mediaController = new MediaControllerCompat(getContext(), mediaSession);
        mediaController.registerCallback(controllerCallback);

        MediaControllerCompat.setMediaController(getActivity(), mediaController);

        mediaSession.setPlaybackState(stateBuilder.build());

        metadataBuilder.putText(MediaMetadataCompat.METADATA_KEY_TITLE, title);
        mediaSession.setMetadata(metadataBuilder.build());
    }

    @Override public void attachVlcViews() {
        player.attachToSurface(videoSurface);
    }

    @Override public void showOverlay() {
        if (!overlayVisible) {
            AnimUtils.fadeIn(controlLayout);
            AnimUtils.fadeIn(toolbar);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
                decorView.setSystemUiVisibility(uiOptions);
            } else {
                getAppCompatActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
                getAppCompatActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            }

            touchHandler.delayOverlayHide();
        }

        overlayVisible = true;
    }

    @Override public void setProgressVisible(boolean visible) {
        progressIndicator.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    @Override public void setKeepScreenOn(final boolean keep) {
        videoSurface.getHolder().setKeepScreenOn(keep);
    }

    @Override
    public void onPlaybackEndReached() {
        setKeepScreenOn(false);
    }

    @Override public void onErrorEncountered() {
        /* Encountered Error, exit player with a message */
        AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        getAppCompatActivity().finish();
                    }
                })
                .setTitle(R.string.encountered_error_title)
                .setMessage(R.string.encountered_error)
                .create();
        dialog.show();
    }

    @Override public void updateControlsState(final boolean playing, final long progress, final long length) {
        stateBuilder.setState(playing ? PlaybackStateCompat.STATE_PLAYING : PlaybackStateCompat.STATE_PAUSED, progress, 1);
        mediaSession.setPlaybackState(stateBuilder.build());

        metadataBuilder.putLong(MediaMetadataCompat.METADATA_KEY_DURATION, length);
        mediaSession.setMetadata(metadataBuilder.build());
    }

    @Override public void updateSurfaceSize(final int width, final int height) {
        SurfaceHolder holder = videoSurface.getHolder();
        holder.setFixedSize(width, height);

        ViewGroup.LayoutParams lp = videoSurface.getLayoutParams();
        lp.width = width;
        lp.height = height;
        videoSurface.setLayoutParams(lp);
    }

    @Override public void detachMediaSession() {
        mediaSession.release();
        mediaController.unregisterCallback(controllerCallback);
    }

    @Override public void onSystemUiVisibilityChange(int visibility) {
        if ((lastSystemUIVisibility & View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) != 0
                && (visibility & View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) == 0) {
            showOverlay();
        }

        lastSystemUIVisibility = visibility;
    }

    @Override public void close() {
        getActivity().finish();
    }

    @Override public void saveState(final Bundle outState, final long resumePosition) {
        outState.putLong(ARG_RESUME_POSITION, resumePosition);
    }

    @Override public void showVolumeMessage(final int volume) {
        showPlayerInfo(getString(R.string.volume) + '\u00A0' + Integer.toString(volume));
    }

    @OnClick(R.id.play_button) void onPlayPauseClick() {
        if (mediaController.getPlaybackState().getState() == PlaybackStateCompat.STATE_PLAYING) {
            mediaController.getTransportControls().pause();
        } else {
            mediaController.getTransportControls().play();
        }
    }

    @OnClick(R.id.rewind_button) void onRewindClick() {
        mediaController.getTransportControls().rewind();
    }

    @OnClick(R.id.forward_button) void onForwardClick() {
        mediaController.getTransportControls().fastForward();
    }

    @OnClick(R.id.scale_button) void onScaleClick() {
        mediaController.getTransportControls().sendCustomAction(ACTION_SCALE, null);
    }

    protected void showPlayerInfo(String text) {
        playerInfo.setVisibility(View.VISIBLE);
        playerInfo.setText(text);
        playerInfo.removeCallbacks(infoHideRunnable);
        playerInfo.postDelayed(infoHideRunnable, FADE_OUT_INFO);
    }

    protected long getResumePosition(@Nullable Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            return savedInstanceState.getLong(ARG_RESUME_POSITION);
        } else {
            return getArguments().getLong(ARG_RESUME_POSITION);
        }
    }

    protected boolean onCustomAction(final String action) {
        switch (action) {
            case ACTION_SCALE:
                presenter.onScaleClicked();
                return true;
            default:
                return false;
        }
    }

    private void setupToolbar() {
        getAppCompatActivity().setSupportActionBar(toolbar);
        toolbar.setOnTouchListener(touchHandler);

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2) {
            getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            int statusBarHeight = PixelUtils.getStatusBarHeight(getActivity());
            toolbar.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                    getResources().getDimensionPixelSize(R.dimen.abc_action_bar_default_height_material) + statusBarHeight));
            toolbar.setPadding(toolbar.getPaddingLeft(), statusBarHeight, toolbar.getPaddingRight(), toolbar.getPaddingBottom());
        }
    }

    private void setupDecorView() {
        decorView = getActivity().getWindow().getDecorView();
        decorView.setOnSystemUiVisibilityChangeListener(this);
    }

    private void setupProgressBar() {
        int color = ContextCompat.getColor(getContext(), R.color.primary);
        LayerDrawable progressDrawable;
        if (!VersionUtils.isLollipop()) {
            progressDrawable = (LayerDrawable) ContextCompat.getDrawable(getContext(), R.drawable.scrubber_progress_horizontal);
        } else {
            if (controlBar.getProgressDrawable() instanceof StateListDrawable) {
                StateListDrawable stateListDrawable = (StateListDrawable) controlBar.getProgressDrawable();
                progressDrawable = (LayerDrawable) stateListDrawable.getCurrent();
            } else {
                progressDrawable = (LayerDrawable) controlBar.getProgressDrawable();
            }
        }
        progressDrawable.findDrawableByLayerId(android.R.id.progress).setColorFilter(color, PorterDuff.Mode.SRC_IN);
        progressDrawable.findDrawableByLayerId(android.R.id.secondaryProgress).setColorFilter(color, PorterDuff.Mode.SRC_IN);

        controlBar.setProgressDrawable(progressDrawable);
        controlBar.getThumbDrawable().setColorFilter(color, PorterDuff.Mode.SRC_IN);
        controlBar.setOnSeekBarChangeListener(controlBarListener);
    }

    private void initControlls() {
        if (LocaleUtils.isRTL(LocaleUtils.getCurrent())) {
            Drawable forward = forwardButton.getDrawable();
            Drawable rewind = rewindButton.getDrawable();
            rewindButton.setImageDrawable(forward);
            forwardButton.setImageDrawable(rewind);
        }
    }

    private AppCompatActivity getAppCompatActivity() {
        return (AppCompatActivity) getActivity();
    }

    private void hidePlayerInfo() {
        if (playerInfo.getVisibility() == View.VISIBLE) {
            Animation fadeOutAnim = AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_out);
            playerInfo.startAnimation(fadeOutAnim);
        }
        playerInfo.setVisibility(View.INVISIBLE);
    }

    private Callback controllerCallback = new Callback() {

        @Override public void onPlaybackStateChanged(final PlaybackStateCompat state) {
            if (state.getState() == PlaybackStateCompat.STATE_PLAYING) {
                playButton.setImageResource(R.drawable.ic_av_pause);
                playButton.setContentDescription(getString(R.string.pause));
            } else {
                playButton.setImageResource(R.drawable.ic_av_play);
                playButton.setContentDescription(getString(R.string.play));
            }

            long position = state.getPosition();
            controlBar.setProgress((int) position);
            controlBar.setSecondaryProgress(0); // hack to make the secondary progress appear on Android 5.0
            controlBar.setSecondaryProgress((int) state.getBufferedPosition());

            if (position >= 0) {
                currentTimeTextView.setText(StringUtils.millisToString(position));
            }

        }

        @Override public void onMetadataChanged(final MediaMetadataCompat metadata) {

            long duration = metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION);
            controlBar.setMax((int) duration);

            if (duration >= 0) {
                lengthTime.setText(StringUtils.millisToString(duration));
            }

        }
    };

    private Runnable infoHideRunnable = new Runnable() {
        @Override
        public void run() {
            hidePlayerInfo();
        }
    };

    private SeekBar.OnSeekBarChangeListener controlBarListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            // nothing to do
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            // nothing to do
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser) {
                mediaController.getTransportControls().seekTo(progress);
            }
        }
    };

    private class PlayerSessionCallback extends MediaSessionCompat.Callback {

        @Override public void onPlay() {
            presenter.play();
        }

        @Override public void onPause() {
            presenter.pause();
        }

        @Override public void onFastForward() {
            presenter.seekForwardClick();
        }

        @Override public void onRewind() {
            presenter.seekBackwardClick();
        }

        @Override public void onSeekTo(final long pos) {
            presenter.onProgressChanged((int) pos);
        }

        @Override public void onCustomAction(final String action, final Bundle extras) {
            if (!AbsPlayerFragment.this.onCustomAction(action)) {
                super.onCustomAction(action, extras);
            }
        }
    }

}
