package pct.droid.tv.fragments;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import pct.droid.base.fragments.BaseVideoPlayerFragment;
import pct.droid.base.preferences.Prefs;
import pct.droid.base.subs.Caption;
import pct.droid.base.utils.AnimUtils;
import pct.droid.base.utils.PrefUtils;
import pct.droid.base.utils.StringUtils;
import pct.droid.tv.R;

public class PTVVideoPlayerFragment extends BaseVideoPlayerFragment {

    @InjectView(R.id.progress_indicator)
    ProgressBar mProgressIndicator;
    @InjectView(R.id.video_surface)
    SurfaceView videoSurface;
    @InjectView(R.id.subtitle_text)
    TextView mSubtitleText;
    @InjectView(R.id.control_layout)
    ViewGroup mControlLayout;
    @InjectView(R.id.player_info)
    TextView mPlayerInfo;
    @InjectView(R.id.control_bar)
    ProgressBar mControlBar;
    @InjectView(R.id.play_button)
    ImageButton playButton;
    @InjectView(R.id.currentTime)
    TextView mCurrentTimeTextView;
    @InjectView(R.id.length_time)
    TextView lengthTime;


    private static final int FADE_OUT_OVERLAY = 5000;
    private static final int FADE_OUT_INFO = 1000;

    private boolean mOverlayVisible = true;

    private Handler mDisplayHandler;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_videoplayer, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.inject(this, view);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setRetainInstance(true);

        videoSurface.setVisibility(View.VISIBLE);

        mDisplayHandler = new Handler(Looper.getMainLooper());

        mSubtitleText.setTextColor(PrefUtils.get(getActivity(), Prefs.SUBTITLE_COLOR, Color.WHITE));
        mSubtitleText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, PrefUtils.get(getActivity(), Prefs.SUBTITLE_SIZE, 16));

        getActivity().setVolumeControlStream(AudioManager.STREAM_MUSIC);
    }

    @Override
    protected SurfaceView getVideoSurface() {
        return videoSurface;
    }


    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_BUTTON_B) {
            return true;
        }

        showOverlay();

        //taken from vlc
//			switch (keyCode) {
//				case KeyEvent.KEYCODE_F:
//				case KeyEvent.KEYCODE_MEDIA_FAST_FORWARD:
//					seekDelta(10000);
//					return true;
//				case KeyEvent.KEYCODE_R:
//				case KeyEvent.KEYCODE_MEDIA_REWIND:
//					seekDelta(-10000);
//					return true;
//				case KeyEvent.KEYCODE_BUTTON_R1:
//					seekDelta(60000);
//					return true;
//				case KeyEvent.KEYCODE_BUTTON_L1:
//					seekDelta(-60000);
//					return true;
//				case KeyEvent.KEYCODE_BUTTON_A:
//					if (mOverlayProgress.getVisibility() == View.VISIBLE)
//						return false;
//				case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
//				case KeyEvent.KEYCODE_MEDIA_PLAY:
//				case KeyEvent.KEYCODE_MEDIA_PAUSE:
//				case KeyEvent.KEYCODE_SPACE:
//					if (mIsNavMenu)
//						return navigateDvdMenu(keyCode);
//					else
//						doPlayPause();
//					return true;
//				case KeyEvent.KEYCODE_O:
//				case KeyEvent.KEYCODE_BUTTON_Y:
//				case KeyEvent.KEYCODE_MENU:
//					showAdvancedOptions(mAdvOptions);
//					return true;
//				case KeyEvent.KEYCODE_V:
//				case KeyEvent.KEYCODE_MEDIA_AUDIO_TRACK:
//				case KeyEvent.KEYCODE_BUTTON_X:
//					onAudioSubClick(mTracks);
//					return true;
//				case KeyEvent.KEYCODE_N:
//					showNavMenu();
//					return true;
//				case KeyEvent.KEYCODE_A:
//					resizeVideo();
//					return true;
//				case KeyEvent.KEYCODE_M:
//				case KeyEvent.KEYCODE_VOLUME_MUTE:
//					updateMute();
//					return true;
//				case KeyEvent.KEYCODE_S:
//				case KeyEvent.KEYCODE_MEDIA_STOP:
//					finish();
//					return true;
//				case KeyEvent.KEYCODE_DPAD_UP:
//				case KeyEvent.KEYCODE_DPAD_DOWN:
//				case KeyEvent.KEYCODE_DPAD_LEFT:
//				case KeyEvent.KEYCODE_DPAD_RIGHT:
//				case KeyEvent.KEYCODE_DPAD_CENTER:
//				case KeyEvent.KEYCODE_ENTER:
//					if (mIsNavMenu)
//						return navigateDvdMenu(keyCode);
//					else
//						return super.onKeyDown(keyCode, event);
//				case KeyEvent.KEYCODE_J:
//					delayAudio(-50000l);
//					break;
//				case KeyEvent.KEYCODE_K:
//					delayAudio(50000l);
//					break;
//				case KeyEvent.KEYCODE_G:
//					delaySubs(-50000l);
//					break;
//				case KeyEvent.KEYCODE_H:
//					delaySubs(50000l);
//					break;
//			}
        return true;
    }


    /**
     * When playback has finished, finish the player activity
     */
    @Override
    public void onPlaybackEndReached() {
        getActivity().finish();
    }

    @Override
    protected void onErrorEncountered() {
        /* Encountered Error, exit player with a message */
        AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        getActivity().finish();
                    }
                })
                .setTitle("Encountered error")
                .setMessage("Encountered error")
                .create();
        dialog.show();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void showOverlay() {
        if (!mOverlayVisible) {
            updatePlayPauseState();

            AnimUtils.fadeIn(mControlLayout);
        }

        mOverlayVisible = true;
        mDisplayHandler.removeCallbacks(mOverlayHideRunnable);
        mDisplayHandler.postDelayed(mOverlayHideRunnable, FADE_OUT_OVERLAY);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void hideOverlay() {
        AnimUtils.fadeOut(mControlLayout);

        mDisplayHandler.removeCallbacks(mOverlayHideRunnable);
        mOverlayVisible = false;
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

    public void updatePlayPauseState() {
        if (isPlaying()) {
            playButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_av_pause));
        } else {
            playButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_av_play));
        }
    }

    @Override
    public void onHardwareAccelerationError() {
        AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        disableHardwareAcceleration();
                        loadMedia();
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        getActivity().finish();
                    }
                })
                .setTitle(R.string.hardware_acceleration_error_title)
                .setMessage(R.string.hardware_acceleration_error_message)
                .create();
        if (!getActivity().isFinishing())
            dialog.show();
    }

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

    @Override
    protected void showTimedCaptionText(final Caption text) {
        mDisplayHandler.post(new Runnable() {
            @Override
            public void run() {
                if (text == null) {
                    if (mSubtitleText.getText().length() > 0) {
                        mSubtitleText.setText("");
                    }
                    return;
                }
                SpannableStringBuilder styledString = (SpannableStringBuilder) Html.fromHtml(text.content);

                ForegroundColorSpan[] toRemoveSpans = styledString.getSpans(0, styledString.length(), ForegroundColorSpan.class);
                for (ForegroundColorSpan remove : toRemoveSpans) {
                    styledString.removeSpan(remove);
                }

                if (!mSubtitleText.getText().toString().equals(styledString.toString())) {
                    mSubtitleText.setText(styledString);
                }
            }
        });
    }

    @Override
    protected void setProgressVisible(boolean visible) {
        mProgressIndicator.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    /**
     * Updates the overlay when the media playback progress has changed
     *
     * @param currentTime
     * @param duration
     */
    @Override
    protected void onProgressChanged(long currentTime, long duration) {
        mControlBar.setMax((int) duration);
        mControlBar.setProgress((int) currentTime);
        mControlBar.setSecondaryProgress(0); // hack to make the secondary progress appear on Android 5.0
        mControlBar.setSecondaryProgress(getStreamerProgress());
        if (getCurrentTime() >= 0)
            mCurrentTimeTextView.setText(StringUtils.millisToString(currentTime));
        if (getDuration() >= 0) lengthTime.setText(StringUtils.millisToString(duration));

        mControlBar.setSecondaryProgress(0); // hack to make the secondary progress appear on Android 5.0
        mControlBar.setSecondaryProgress(getStreamerProgress());
    }

    @OnClick(R.id.play_button)
    void onPlayPauseClick() {
        togglePlayPause();
    }

    @OnClick(R.id.rewindButton)
    void onRewindClick() {
        seekBackwardClick();
    }

    @OnClick(R.id.forwardButton)
    void onForwardClick() {
        seekForwardClick();
    }

    @OnClick(R.id.scaleButton)
    void onScaleClick() {
        scaleClick();
    }

    @OnClick(R.id.subsButton)
    void onSubsClick() {
        subsClick();
    }


}
