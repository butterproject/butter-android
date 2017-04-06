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

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.StateListDrawable;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.TypedValue;
import android.view.LayoutInflater;
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
import butter.droid.manager.brightness.BrightnessManager;
import butter.droid.ui.player.VideoPlayerActivity;
import butter.droid.ui.player.fragment.VideoPlayerTouchHandler.OnVideoTouchListener;
import butter.droid.widget.StrokedRobotoTextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import javax.inject.Inject;

public class VideoPlayerFragment extends BaseVideoPlayerFragment implements VideoPlayerFView, OnSystemUiVisibilityChangeListener,
        OnVideoTouchListener {

    @Inject VideoPlayerFPresenter presenter;
    @Inject VideoPlayerTouchHandler touchHandler;
    @Inject BrightnessManager brightnessManager;

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.progress_indicator) ProgressBar progressIndicator;
    @BindView(R.id.video_surface) SurfaceView videoSurface;
    @BindView(R.id.subtitle_text) StrokedRobotoTextView subtitleText;
    @BindView(R.id.control_layout) RelativeLayout mControlLayout;
    @BindView(R.id.player_info) TextView mPlayerInfo;
    @BindView(R.id.control_bar) butter.droid.widget.SeekBar controlBar;
    @BindView(R.id.play_button) ImageButton playButton;
    @BindView(R.id.forward_button) ImageButton mForwardButton;
    @BindView(R.id.rewind_button) ImageButton mRewindButton;
    @BindView(R.id.subs_button) ImageButton mSubsButton;
    @BindView(R.id.current_time) TextView mCurrentTimeTextView;
    @BindView(R.id.length_time) TextView lengthTime;
    View decorView;

    private long mLastSystemShowTime = System.currentTimeMillis();

    private static final int FADE_OUT_OVERLAY = 5000;
    private static final int FADE_OUT_INFO = 1000;

    private int mLastSystemUIVisibility;

    private Handler mDisplayHandler;

    private float mVol;
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

        touchHandler.setListener(this);

        setupToolbar();
        setupDecorView();

        presenter.onViewCreated();

        if (LocaleUtils.isRTL(LocaleUtils.getCurrent())) {
            Drawable forward = mForwardButton.getDrawable();
            Drawable rewind = mRewindButton.getDrawable();
            mRewindButton.setImageDrawable(forward);
            mForwardButton.setImageDrawable(rewind);
        }

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

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mDisplayHandler = new Handler(Looper.getMainLooper());

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

        brightnessManager.restoreBrightness();
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

    private AppCompatActivity getAppCompatActivity() {
        return (AppCompatActivity) getActivity();
    }

    @Override
    protected SurfaceView getVideoSurface() {
        return videoSurface;
    }

    @Override
    public void onSystemUiVisibilityChange(int visibility) {
        if ((mLastSystemUIVisibility & View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) != 0 &&
                (visibility & View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) == 0) {
            showOverlay();
        }

        mLastSystemUIVisibility = visibility;
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


    public void showOverlay() {
        if (!overlayVisible) {
            updatePlayPauseState(true); // TODO: 4/2/17 Get State

            AnimUtils.fadeIn(mControlLayout);
            AnimUtils.fadeIn(toolbar);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
                decorView.setSystemUiVisibility(uiOptions);
            } else {
                getAppCompatActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
                getAppCompatActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            }

            mLastSystemShowTime = System.currentTimeMillis();
        }

        overlayVisible = true;
        mDisplayHandler.removeCallbacks(mOverlayHideRunnable);
        mDisplayHandler.postDelayed(mOverlayHideRunnable, FADE_OUT_OVERLAY);
    }

    public void hideOverlay() {
        // Can only hide 1000 millisec after show, because navbar doesn't seem to hide otherwise.
        if (mLastSystemShowTime + 1000 < System.currentTimeMillis()) {
            AnimUtils.fadeOut(mControlLayout);
            AnimUtils.fadeOut(toolbar);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN;
                decorView.setSystemUiVisibility(uiOptions);
            } else {
                getAppCompatActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                getAppCompatActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
            }

            mDisplayHandler.removeCallbacks(mOverlayHideRunnable);
            overlayVisible = false;
        }
    }

    protected void showPlayerInfo(String text) {
        mPlayerInfo.setVisibility(View.VISIBLE);
        mPlayerInfo.setText(text);
        mDisplayHandler.removeCallbacks(mInfoHideRunnable);
        mDisplayHandler.postDelayed(mInfoHideRunnable, FADE_OUT_INFO);
    }

    private void hidePlayerInfo() {
        if (mPlayerInfo.getVisibility() == View.VISIBLE) {
            Animation fadeOutAnim = AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_out);
            mPlayerInfo.startAnimation(fadeOutAnim);
        }
        mPlayerInfo.setVisibility(View.INVISIBLE);
    }

    public void updatePlayPauseState(final boolean playing) {
        if(!FragmentUtil.isAdded(this)) {
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

//    @Override
//    protected void onHardwareAccelerationError() {
//        AlertDialog dialog = new AlertDialog.Builder(getActivity())
//                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int id) {
//                        disableHardwareAcceleration();
//                        loadMedia();
//                    }
//                })
//                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int id) {
//                        getAppCompatActivity().finish();
//                    }
//                })
//                .setTitle(R.string.hardware_acceleration_error_title)
//                .setMessage(R.string.hardware_acceleration_error_message)
//                .create();
//        if (!getAppCompatActivity().isFinishing())
//            dialog.show();
//    }

    private Runnable mOverlayHideRunnable = new Runnable() {
        @Override
        public void run() {
            hideOverlay();
        }
    };

    private Runnable mInfoHideRunnable = new Runnable() {
        @Override
        public void run() {
            hidePlayerInfo();
        }
    };

    @Override public void showTimedCaptionText(final Caption text) {
        mDisplayHandler.post(new Runnable() {
            @Override
            public void run() {
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
        });
    }

    @Override public void setProgressVisible(boolean visible) {
        if(progressIndicator.getVisibility() == View.VISIBLE && visible)
            return;

        if(progressIndicator.getVisibility() == View.GONE && !visible)
            return;

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

        if (currentTime >= 0)
            mCurrentTimeTextView.setText(StringUtils.millisToString(currentTime));
        if (duration >= 0)
            lengthTime.setText(StringUtils.millisToString(duration));
    }

    public void enableSubsButton(boolean b) {
        mSubsButton.setVisibility(b ? View.VISIBLE : View.INVISIBLE);
    }

    protected void updateSubtitleSize(int size) {
        subtitleText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, size);
    }

    @OnClick(R.id.play_button) void onPlayPauseClick() {
        presenter.togglePlayPause();
    }

//    @OnClick(R.id.rewind_button) void onRewindClick() {
//        seekBackwardClick();
//    }
//
//    @OnClick(R.id.forward_button) void onForwardClick() {
//        seekForwardClick();
//    }

    @OnClick(R.id.scale_button) void onScaleClick() {
        presenter.onScaleClicked();
    }

    @OnClick(R.id.subs_button) void onSubsClick() {
        presenter.onSubsClicked();
    }

    public void startBeamPlayerActivity() {
//        getActivity().startActivity(BeamPlayerActivity.getIntent(getActivity(), callback.getInfo(), getCurrentTime()));
    }

    private void setupToolbar() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2) {
            getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            toolbar.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                    getResources().getDimensionPixelSize(R.dimen.abc_action_bar_default_height_material) +
                            PixelUtils.getStatusBarHeight(getActivity())));
            toolbar.setPadding(toolbar.getPaddingLeft(), PixelUtils.getStatusBarHeight(getActivity()), toolbar.getPaddingRight(),
                    toolbar.getPaddingBottom());
        }
    }

    private void setupDecorView() {
        decorView = getActivity().getWindow().getDecorView();
        decorView.setOnSystemUiVisibilityChangeListener(this);
    }

    @Override public void onSeekChange(final int jump) {
//        // Adjust the jump
//        if ((jump > 0) && ((getCurrentTime() + jump) > controlBar.getSecondaryProgress())) {
//            jump = (int) (controlBar.getSecondaryProgress() - getCurrentTime());
//        }
//        if ((jump < 0) && ((getCurrentTime() + jump) < 0)) {
//            jump = (int) -getCurrentTime();
//        }
//
//        long currentTime = getCurrentTime();
//        if (seek && controlBar.getSecondaryProgress() > 0) {
//            seek(jump);
//        }
//
//        if (getDuration() > 0) {
//            showPlayerInfo(String.format("%s%s (%s)", jump >= 0 ? "+" : "", StringUtils.millisToString(jump), StringUtils.millisToString(currentTime + jump)));
//        }
    }

    @Override public void onBrightnessChange(final float delta) {

    }

    @Override public void onVolumeChange(final float delta) {
//        float delta = -((diff / surfaceYDisplayRange) * mAudioMax);
//        mVol += delta;
//        int vol = (int) Math.min(Math.max(mVol, 0), mAudioMax);
//        if (delta != 0f) {
//            setAudioVolume(vol);
//        }
    }

    @Override public void onToggleOverlay() {
        if (overlayVisible) {
            hideOverlay();
        } else {
            showOverlay();
        }
    }

    //    private void setAudioVolume(int vol) {
//        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, vol, 0);
//
//        /* Since android 4.3, the safe volume warning dialog is displayed only with the FLAG_SHOW_UI flag.
//         * We don't want to always show the default UI volume, so show it only when volume is not set. */
//        int newVol = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
//        if (vol != newVol) {
//            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, vol, AudioManager.FLAG_SHOW_UI);
//        }
//
//        showPlayerInfo(getString(R.string.volume) + '\u00A0' + Integer.toString(vol));
//    }

    public static VideoPlayerFragment newInstance(final StreamInfo streamInfo, final long resumePosition) {
        VideoPlayerFragment fragment = new VideoPlayerFragment();
        fragment.setArguments(newInstanceArgs(streamInfo, resumePosition));
        return fragment;
    }


}
