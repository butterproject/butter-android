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
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
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
import butter.droid.base.subs.Caption;
import butter.droid.base.torrent.StreamInfo;
import butter.droid.base.ui.player.fragment.BaseVideoPlayerFragment;
import butter.droid.base.utils.AnimUtils;
import butter.droid.base.utils.FragmentUtil;
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
import javax.inject.Inject;

public class VideoPlayerFragment extends BaseVideoPlayerFragment implements VideoPlayerFView, OnSystemUiVisibilityChangeListener {

    private static final int FADE_OUT_INFO = 1000;

    @Inject VideoPlayerFPresenter presenter;
    @Inject VideoPlayerTouchHandler touchHandler;

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

    private int lastSystemUIVisibility;
    private boolean overlayVisible;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        VideoPlayerActivity activity = (VideoPlayerActivity) getActivity();
        activity.getComponent()
                .videoPlayerFComponentBuilder()
                .videoPlayerFModule(new VideoPlayerFModule(this, activity))
                .build()
                .inject(this);

        super.onCreate(savedInstanceState);

        mShowReload = true;
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

        getAppCompatActivity().setSupportActionBar(toolbar);
        toolbar.setOnTouchListener(touchHandler);

        setupToolbar();
        setupDecorView();
        setupProgressBar();
        setupControls();

        presenter.onViewCreated();

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        controlBar.setOnSeekBarChangeListener(controlBarListener);
        getAppCompatActivity().setVolumeControlStream(AudioManager.STREAM_MUSIC);
    }

    @Override
    public void onPlaybackEndReached() {
        setKeepScreenOn(false);
    }

    @Override
    public void onStop() {
        super.onStop();

        presenter.onStop();
    }

    @Override public void displayStreamProgress(final int progress) {
        controlBar.setSecondaryProgress(progress);
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

    @Override
    protected SurfaceView getVideoSurface() {
        return videoSurface;
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

    public void updatePlayPauseState(final boolean playing) {
        if (!FragmentUtil.isAdded(this)) {
            return;
        }

        if (playing) {
            playButton.setImageResource(R.drawable.ic_av_pause);
            playButton.setContentDescription(getString(R.string.pause));
        } else {
            playButton.setImageResource(R.drawable.ic_av_play);
            playButton.setContentDescription(getString(R.string.play));
        }
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

    }

    @Override public void updateSurfaceSize(final int width, final int height) {
        SurfaceHolder holder = getVideoSurface().getHolder();
        holder.setFixedSize(width, height);

        ViewGroup.LayoutParams lp = getVideoSurface().getLayoutParams();
        lp.width = width;
        lp.height = height;
        getVideoSurface().setLayoutParams(lp);
        getVideoSurface().invalidate();
    }

    @Override public void setProgressVisible(boolean visible) {
        if (progressIndicator.getVisibility() == View.VISIBLE && visible) {
            return;
        }

        if (progressIndicator.getVisibility() == View.GONE && !visible) {
            return;
        }

        progressIndicator.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    /**
     * Updates the overlay when the media playback progress has changed
     *
     * @param currentTime Current progress time
     * @param duration Duration of full medias
     */
    @Override public void onProgressChanged(long currentTime, int streamProgress, long duration) {
        controlBar.setMax((int) duration);
        controlBar.setProgress((int) currentTime);
        controlBar.setSecondaryProgress(0); // hack to make the secondary progress appear on Android 5.0
        controlBar.setSecondaryProgress(streamProgress);

        if (currentTime >= 0) {
            mCurrentTimeTextView.setText(StringUtils.millisToString(currentTime));
        }
        if (duration >= 0) {
            lengthTime.setText(StringUtils.millisToString(duration));
        }
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
        presenter.togglePlayPause();
    }

    @OnClick(R.id.rewind_button) void onRewindClick() {
        presenter.seekBackwardClick();
    }

    @OnClick(R.id.forward_button) void onForwardClick() {
        presenter.seekForwardClick();
    }

    @OnClick(R.id.scale_button) void onScaleClick() {
        presenter.onScaleClicked();
    }

    @OnClick(R.id.subs_button) void onSubsClick() {
        presenter.onSubsClicked();
    }

    private AppCompatActivity getAppCompatActivity() {
        return (AppCompatActivity) getActivity();
    }


    private void setupToolbar() {
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
            progressDrawable = (LayerDrawable) getResources().getDrawable(R.drawable.scrubber_progress_horizontal);
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
            presenter.onStartSeeking();
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            presenter.onStopSeeking();
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser) {
                presenter.onProgressChanged(progress);
            }
        }
    };

    public static VideoPlayerFragment newInstance(final StreamInfo streamInfo, final long resumePosition) {
        VideoPlayerFragment fragment = new VideoPlayerFragment();
        fragment.setArguments(newInstanceArgs(streamInfo, resumePosition));
        return fragment;
    }

}
