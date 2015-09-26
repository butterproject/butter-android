package pct.droid.tv.fragments;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ProgressBar;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;
import pct.droid.base.fragments.BaseVideoPlayerFragment;
import pct.droid.base.content.preferences.Prefs;
import pct.droid.base.subs.Caption;
import pct.droid.base.utils.PrefUtils;
import pct.droid.tv.R;
import pct.droid.tv.events.PausePlaybackEvent;
import pct.droid.tv.events.ProgressChangedEvent;
import pct.droid.tv.events.ScaleVideoEvent;
import pct.droid.tv.events.SeekBackwardEvent;
import pct.droid.tv.events.SeekForwardEvent;
import pct.droid.tv.events.StartPlaybackEvent;
import pct.droid.tv.events.ToggleSubsEvent;
import pct.droid.tv.events.UpdatePlaybackStateEvent;

public class PTVVideoPlayerFragment extends BaseVideoPlayerFragment {

    @Bind(R.id.progress_indicator)
    ProgressBar mProgressIndicator;

    @Bind(R.id.video_surface)
    SurfaceView videoSurface;

    @Bind(R.id.subtitle_text)
    TextView mSubtitleText;

    @Bind(R.id.player_info)
    TextView mPlayerInfo;


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
        ButterKnife.bind(this, view);
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
    public void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
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
                .setTitle(R.string.encountered_error_title)
                .setMessage(R.string.encountered_error)
                .create();
        dialog.show();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void showOverlay() {
        if (!mOverlayVisible) {
            updatePlayPauseState();
        }

        mOverlayVisible = true;
        mDisplayHandler.removeCallbacks(mOverlayHideRunnable);
        mDisplayHandler.postDelayed(mOverlayHideRunnable, FADE_OUT_OVERLAY);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void hideOverlay() {
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
        EventBus.getDefault().post(new UpdatePlaybackStateEvent(isPlaying()));
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
        EventBus.getDefault().post(new ProgressChangedEvent(currentTime, duration));
    }

    public void onEventMainThread(StartPlaybackEvent event) {
        play();
    }

    public void onEventMainThread(PausePlaybackEvent event) {
        pause();
    }

    public void onEventMainThread(SeekBackwardEvent event) {
        seekBackwardClick();
    }

    public void onEventMainThread(SeekForwardEvent event) {
        seekForwardClick();
    }

    public void onEventMainThread(ScaleVideoEvent event) {
        scaleClick();
    }

    public void onEventMainThread(ToggleSubsEvent event) {
        subsClick();
    }

    @Override
    protected void updateSubtitleSize(int size) {

    }

    @Override
    public void startBeamPlayerActivity() {

    }
}
