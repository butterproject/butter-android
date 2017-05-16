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

package butter.droid.ui.player.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
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
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.TypedValue;
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
import butter.droid.base.fragments.dialog.FileSelectorDialogFragment;
import butter.droid.base.fragments.dialog.NumberPickerDialogFragment;
import butter.droid.base.fragments.dialog.StringArraySelectorDialogFragment;
import butter.droid.base.manager.internal.vlc.VlcPlayer;
import butter.droid.base.subs.Caption;
import butter.droid.base.torrent.StreamInfo;
import butter.droid.base.utils.AnimUtils;
import butter.droid.base.utils.LocaleUtils;
import butter.droid.base.utils.PixelUtils;
import butter.droid.base.utils.StringUtils;
import butter.droid.base.utils.VersionUtils;
import butter.droid.ui.beam.BeamPlayerActivity;
import butter.droid.ui.player.VideoPlayerActivity;
import butter.droid.widget.StrokedRobotoTextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.github.se_bastiaan.torrentstream.StreamStatus;
import com.github.se_bastiaan.torrentstream.Torrent;
import com.github.se_bastiaan.torrentstream.listeners.TorrentListener;
import java.io.File;
import java.util.Arrays;
import javax.inject.Inject;

public class PlayerFragment extends Fragment implements PlayerView, OnSystemUiVisibilityChangeListener, TorrentListener {

    private static final String ARG_STREAM_INFO = "butter.droid.base.ui.player.fragment.BaseVideoPlayerFragment.streamInfo";
    private static final String ARG_RESUME_POSITION = "butter.droid.base.ui.player.fragment.BaseVideoPlayerFragment.resumePosition";

    private static final String ACTION_SCALE = "butter.droid.tv.ui.player.video.action.SCALE";
    private static final String ACTION_CLOSE_CAPTION = "butter.droid.tv.ui.player.video.action.CLOSE_CAPTION";

    private static final int FADE_OUT_INFO = 1000;
    public static final int SUBTITLE_MINIMUM_SIZE = 10;

    @Inject PlayerPresenter presenter;
    @Inject VideoPlayerTouchHandler touchHandler;
    @Inject VlcPlayer player;

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.progress_indicator) ProgressBar progressIndicator;
    @BindView(R.id.video_surface) SurfaceView videoSurface;
    @BindView(R.id.subtitle_text) StrokedRobotoTextView subtitleText;
    @BindView(R.id.control_layout) RelativeLayout controlLayout;
    @BindView(R.id.player_info) TextView playerInfo;
    @BindView(R.id.control_bar) butter.droid.widget.SeekBar controlBar;
    @BindView(R.id.play_button) ImageButton playButton;
    @BindView(R.id.forward_button) ImageButton forwardButton;
    @BindView(R.id.rewind_button) ImageButton fewindButton;
    @BindView(R.id.subs_button) ImageButton mSubsButton;
    @BindView(R.id.current_time) TextView mCurrentTimeTextView;
    @BindView(R.id.length_time) TextView lengthTime;
    View decorView;

    private MediaSessionCompat mediaSession;
    private MediaControllerCompat mediaController;
    private PlaybackStateCompat.Builder stateBuilder;
    private MediaMetadataCompat.Builder metadataBuilder;

    private int lastSystemUIVisibility;
    private boolean overlayVisible;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        VideoPlayerActivity activity = (VideoPlayerActivity) getActivity();
        activity.getComponent()
                .playerComponentBuilder()
                .videoPlayerFModule(new PlayerModule(this, activity))
                .build()
                .inject(this);

        StreamInfo streamInfo = getArguments().getParcelable(ARG_STREAM_INFO);
        long resumePosition = getArguments().getLong(ARG_RESUME_POSITION);

        mediaSession = new MediaSessionCompat(getContext(), BuildConfig.APPLICATION_ID);
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mediaSession.setMediaButtonReceiver(null);
        mediaSession.setCallback(new PlayerSessionCallback());

        stateBuilder = new PlaybackStateCompat.Builder()
                .setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE
                        | PlaybackStateCompat.ACTION_REWIND
                        | PlaybackStateCompat.ACTION_FAST_FORWARD
                        | PlaybackStateCompat.ACTION_SEEK_TO)
                .addCustomAction(ACTION_SCALE, getString(R.string.scale), R.drawable.ic_av_aspect_ratio)
                .addCustomAction(ACTION_CLOSE_CAPTION, getString(R.string.subtitles), R.drawable.ic_av_subs);

        mediaSession.setPlaybackState(stateBuilder.build());

        metadataBuilder = new MediaMetadataCompat.Builder();

        presenter.onCreate(streamInfo, resumePosition);
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
        setupControls();
        setProgressVisible(true);

        presenter.onViewCreated();
    }

    @Override public void onResume() {
        super.onResume();

        presenter.onResume();
    }

    @Override
    public void onPlaybackEndReached() {
        setKeepScreenOn(false);
    }

    @Override public void onPause() {
        super.onPause();

        presenter.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();

        presenter.onStop();
    }

    @Override public void onDestroy() {
        super.onDestroy();

        presenter.onDestroy();
    }

    @Override public void setupControls(final StreamInfo streamInfo) {
        mediaController = new MediaControllerCompat(getContext(), mediaSession);
        mediaController.registerCallback(controllerCallback);

        MediaControllerCompat.setMediaController(getActivity(), mediaController);

        metadataBuilder.putText(MediaMetadataCompat.METADATA_KEY_TITLE, streamInfo.getTitle());
        mediaSession.setMetadata(metadataBuilder.build());
    }

    @Override public void attachVlcViews() {
        player.attachToSurface(videoSurface);
    }

    @Override public void displayStreamProgress(final int progress) {
        stateBuilder.setBufferedPosition(progress);
        mediaSession.setPlaybackState(stateBuilder.build());
    }

    @Override public void displayTitle(final String title) {
        getAppCompatActivity().getSupportActionBar().setTitle(title);
        getAppCompatActivity().getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void setupSubtitles(@ColorInt final int color, final int size, @ColorInt final int strokeColor, final int strokeWidth) {
        subtitleText.setTextColor(color);
        subtitleText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, size);
        subtitleText.setStrokeColor(strokeColor);
        subtitleText.setStrokeWidth(TypedValue.COMPLEX_UNIT_DIP, strokeWidth);
    }

    @Override public void toggleOverlay() {
        if (overlayVisible) {
            hideOverlay();
        } else {
            showOverlay();
        }
    }

    @Override public void setKeepScreenOn(final boolean keep) {
        videoSurface.getHolder().setKeepScreenOn(keep);
    }

    @Override
    public void onSystemUiVisibilityChange(int visibility) {
        if ((lastSystemUIVisibility & View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) != 0 &&
                (visibility & View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) == 0) {
            showOverlay();
        }

        lastSystemUIVisibility = visibility;
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

    @Override public void showOverlay() {
        if (!overlayVisible) {
            AnimUtils.fadeIn(controlLayout);
            AnimUtils.fadeIn(toolbar);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
                decorView.setSystemUiVisibility(uiOptions);
            } else {
                getAppCompatActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
                getAppCompatActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            }

            touchHandler.delayOverlayHide();
        }

        overlayVisible = true;
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

    @Override public void showVolumeMessage(final int volume) {
        showPlayerInfo(getString(R.string.volume) + '\u00A0' + Integer.toString(volume));
    }

    protected void showPlayerInfo(String text) {
        playerInfo.setVisibility(View.VISIBLE);
        playerInfo.setText(text);
        playerInfo.removeCallbacks(infoHideRunnable);
        playerInfo.postDelayed(infoHideRunnable, FADE_OUT_INFO);
    }

    private void hidePlayerInfo() {
        if (playerInfo.getVisibility() == View.VISIBLE) {
            Animation fadeOutAnim = AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_out);
            playerInfo.startAnimation(fadeOutAnim);
        }
        playerInfo.setVisibility(View.INVISIBLE);
    }

    @Override public void onHardwareAccelerationError() {
        AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        presenter.requestDisableHardwareAcceleration();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        getAppCompatActivity().finish();
                    }
                })
                .setTitle(R.string.hardware_acceleration_error_title)
                .setMessage(R.string.hardware_acceleration_error_message)
                .create();
        if (!getAppCompatActivity().isFinishing())
            dialog.show();
    }

    @Override public void showTimedCaptionText(final Caption text) {
        if (text == null) {
            if (subtitleText.getText().length() > 0) {
                subtitleText.setText("");
            }
            return;
        }
        SpannableStringBuilder styledString = (SpannableStringBuilder) Html.fromHtml(text.content);

        ForegroundColorSpan[] toRemoveSpans = styledString.getSpans(0, styledString.length(), ForegroundColorSpan.class);
        for (ForegroundColorSpan remove : toRemoveSpans) {
            styledString.removeSpan(remove);
        }

        if (!subtitleText.getText().toString().equals(styledString.toString())) {
            subtitleText.setText(styledString);
        }
    }

    @Override public void updateControlsState(final boolean playing, final long progress, final int streamerProgress, final long length) {
        stateBuilder.setBufferedPosition(streamerProgress);
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

    @Override public void setProgressVisible(boolean visible) {
        progressIndicator.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    @Override public void startBeamPlayerActivity(@NonNull final StreamInfo streamInfo, final long currentTime) {
        getActivity().startActivity(BeamPlayerActivity.getIntent(getActivity(), streamInfo, currentTime));
    }

    public void enableSubsButton(boolean enable) {
        mSubsButton.setVisibility(enable ? View.VISIBLE : View.INVISIBLE);
    }

    @Override public void updateSubtitleSize(int size) {
        subtitleText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, size);
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

    @OnClick(R.id.subs_button) void onSubsClick() {
        mediaController.getTransportControls().sendCustomAction(ACTION_CLOSE_CAPTION, null);
    }

    @Override public void showSubsSelectorDialog() {
        if (getChildFragmentManager().findFragmentByTag("overlay_fragment") != null) {
            return;
        }

        final String[] subsOptions = {
                getString(R.string.subtitle_language),
                getString(R.string.subtitle_size),
                getString(R.string.subtitle_timing)
        };

        StringArraySelectorDialogFragment.show(getChildFragmentManager(), R.string.subtitle_settings, subsOptions,
                -1,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int position) {
                        switch (position) {
                            case 0:
                                presenter.showSubsLanguageSettings();
                                break;
                            case 1:
                                presenter.showSubsSizeSettings();
                                break;
                            case 2:
                                presenter.showSubsTimingSettings();
                                break;
                        }
                    }
                });
    }

    @Override public void showPickSubsDialog(final String[] readableNames, final String[] adapterSubtitles, final String currentSubsLang) {
        StringArraySelectorDialogFragment.showSingleChoice(
                getChildFragmentManager(),
                R.string.subtitles,
                readableNames,
                Arrays.asList(adapterSubtitles).indexOf(currentSubsLang),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, int position) {
                        if (position == adapterSubtitles.length - 1) {
                            presenter.showCustomSubsPicker();
                        } else {
                            presenter.onSubtitleLanguageSelected(adapterSubtitles[position]);
                        }
                        dialog.dismiss();
                    }
                });
    }

    @Override public void showSubsFilePicker() {
        FileSelectorDialogFragment.show(getChildFragmentManager(),
                new FileSelectorDialogFragment.Listener() {
                    @Override
                    public void onFileSelected(File f) {
                        presenter.onSubsFileSelected(f);
                        FileSelectorDialogFragment.hide();
                    }
                });
    }

    @Override public void displaySubsSizeDialog() {
        Bundle args = new Bundle();
        args.putString(NumberPickerDialogFragment.TITLE, getString(R.string.subtitle_size));
        args.putInt(NumberPickerDialogFragment.MAX_VALUE, 60);
        args.putInt(NumberPickerDialogFragment.MIN_VALUE, SUBTITLE_MINIMUM_SIZE);
        args.putInt(NumberPickerDialogFragment.DEFAULT_VALUE, 16);

        NumberPickerDialogFragment dialogFragment = new NumberPickerDialogFragment();
        dialogFragment.setArguments(args);
        dialogFragment.setOnResultListener(new NumberPickerDialogFragment.ResultListener() {
            @Override
            public void onNewValue(int value) {
                presenter.onSubsSizeChanged(value);
            }
        });
        dialogFragment.show(getChildFragmentManager(), "overlay_fragment");
    }

    @Override public void displaySubsTimingDialog(int subtitleOffset) {
        Bundle args = new Bundle();
        args.putString(NumberPickerDialogFragment.TITLE, getString(R.string.subtitle_timing));
        args.putInt(NumberPickerDialogFragment.MAX_VALUE, 3600);
        args.putInt(NumberPickerDialogFragment.MIN_VALUE, -3600);
        args.putInt(NumberPickerDialogFragment.DEFAULT_VALUE, subtitleOffset / 60);
        args.putBoolean(NumberPickerDialogFragment.FOCUSABLE, true);

        NumberPickerDialogFragment dialogFragment = new NumberPickerDialogFragment();
        dialogFragment.setArguments(args);
        dialogFragment.setOnResultListener(new NumberPickerDialogFragment.ResultListener() {
            @Override
            public void onNewValue(int value) {
                presenter.onSubsTimingChanged(value * 60);
            }
        });
        dialogFragment.show(getChildFragmentManager(), "overlay_fragment");
    }

    @Override public void onStreamPrepared(final Torrent torrent) {
        // nothing to do
    }

    @Override public void onStreamStarted(final Torrent torrent) {
        // nothing to do
    }

    @Override public void onStreamError(final Torrent torrent, final Exception e) {
        // nothing to do
    }

    @Override public void onStreamReady(final Torrent torrent) {
        // nothing to do
    }

    @Override public void onStreamProgress(final Torrent torrent, final StreamStatus streamStatus) {
        presenter.streamProgressUpdated(streamStatus.progress);
    }

    @Override public void onStreamStopped() {
        // nothing to do
    }

    private AppCompatActivity getAppCompatActivity() {
        return (AppCompatActivity) getActivity();
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

    private void setupControls() {
        if (LocaleUtils.isRTL(LocaleUtils.getCurrent())) {
            Drawable forward = forwardButton.getDrawable();
            Drawable rewind = fewindButton.getDrawable();
            fewindButton.setImageDrawable(forward);
            forwardButton.setImageDrawable(rewind);
        }
    }

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
            switch (action) {
                case ACTION_SCALE:
                    presenter.onScaleClicked();
                    break;
                case ACTION_CLOSE_CAPTION:
                    presenter.onSubsClicked();
                    break;
                default:
                    super.onCustomAction(action, extras);
            }
        }
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
                mCurrentTimeTextView.setText(StringUtils.millisToString(position));
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

    public static PlayerFragment newInstance(final StreamInfo streamInfo, final long resumePosition) {
        Bundle args = new Bundle(2);
        args.putParcelable(ARG_STREAM_INFO, streamInfo);
        args.putLong(ARG_RESUME_POSITION, resumePosition);

        PlayerFragment fragment = new PlayerFragment();
        fragment.setArguments(args);
        return fragment;
    }

}
