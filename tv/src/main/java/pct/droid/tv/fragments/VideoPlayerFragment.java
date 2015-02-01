package pct.droid.tv.fragments;

import android.annotation.TargetApi;
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
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import pct.droid.base.fragments.BaseVideoPlayerFragment;
import pct.droid.base.preferences.Prefs;
import pct.droid.base.providers.media.models.Media;
import pct.droid.base.subs.Caption;
import pct.droid.base.utils.AnimUtils;
import pct.droid.base.utils.PixelUtils;
import pct.droid.base.utils.PrefUtils;
import pct.droid.base.utils.StringUtils;
import pct.droid.tv.R;

public class VideoPlayerFragment extends BaseVideoPlayerFragment implements View.OnSystemUiVisibilityChangeListener {

	@InjectView(R.id.progress_indicator)
	ProgressBar mProgressIndicator;
	@InjectView(R.id.video_surface)
	SurfaceView videoSurface;
	@InjectView(R.id.subtitle_text)
	TextView mSubtitleText;
	@InjectView(R.id.control_layout)
	RelativeLayout mControlLayout;
	@InjectView(R.id.player_info)
	TextView mPlayerInfo;
	@InjectView(R.id.control_bar)
	SeekBar mControlBar;
	@InjectView(R.id.play_butotn)
	ImageButton playButton;
	@InjectView(R.id.currentTime)
	TextView mCurrentTimeTextView;
	@InjectView(R.id.length_time)
	TextView lengthTime;
	View mDecorView;

	private AudioManager mAudioManager;

	private long mLastSystemShowTime = System.currentTimeMillis();

	private static final int FADE_OUT_OVERLAY = 5000;
	private static final int FADE_OUT_INFO = 1000;

	private int mLastSystemUIVisibility;
	private boolean mOverlayVisible = true;

	private Handler mDisplayHandler;

	private int mSurfaceYDisplayRange;

	private int mAudioMax;
	private int mVol;

	private float mRestoreBrightness = -1f;


	@Override public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_videoplayer, container, false);
		return view;
	}

	@Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		ButterKnife.inject(this, view);
	}

	@Override public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setRetainInstance(true);

		videoSurface.setVisibility(View.VISIBLE);

		/* Services and miscellaneous */
		mAudioManager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
		mAudioMax = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

		mDisplayHandler = new Handler(Looper.getMainLooper());

		mDecorView = getActivity().getWindow().getDecorView();
		mDecorView.setOnSystemUiVisibilityChangeListener(this);

		mSubtitleText.setTextColor(PrefUtils.get(getActivity(), Prefs.SUBTITLE_COLOR, Color.WHITE));
		mSubtitleText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, PrefUtils.get(getActivity(), Prefs.SUBTITLE_SIZE, 16));

		mControlBar.setOnSeekBarChangeListener(mOnControlBarListener);

		getActivity().setVolumeControlStream(AudioManager.STREAM_MUSIC);
	}

	@Override
	public void onStop() {
		super.onStop();
		//restore brightness
		if (mRestoreBrightness != -1f) {
			int brightness = (int) (mRestoreBrightness * 255f);
			Settings.System.putInt(getActivity().getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, brightness);
		} else {
			Settings.System
					.putInt(getActivity().getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE,
							Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
		}
	}

	@Override public void onDestroyView() {
		super.onDestroyView();
		mAudioManager = null;
	}


	@Override protected SurfaceView getVideoSurface() {
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

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
				int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
						View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
				mDecorView.setSystemUiVisibility(uiOptions);
			} else {
				getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
				getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
			}

			mLastSystemShowTime = System.currentTimeMillis();
		}

		mOverlayVisible = true;
		mDisplayHandler.removeCallbacks(mOverlayHideRunnable);
		mDisplayHandler.postDelayed(mOverlayHideRunnable, FADE_OUT_OVERLAY);
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	public void hideOverlay() {
		// Can only hide 1000 millisec after show, because navbar doesn't seem to hide otherwise.
		if (mLastSystemShowTime + 1000 < System.currentTimeMillis()) {
			AnimUtils.fadeOut(mControlLayout);

			mDisplayHandler.removeCallbacks(mOverlayHideRunnable);
			mOverlayVisible = false;
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

	public void updatePlayPauseState() {
		if (isPlaying()) {
			playButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_av_pause));
		} else {
			playButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_av_play));
		}
	}

	private SeekBar.OnSeekBarChangeListener mOnControlBarListener = new SeekBar.OnSeekBarChangeListener() {
		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			setSeeking(true);
		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			setSeeking(false);
		}

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
			if (fromUser && isSeeking()) {
				setLastSub(null);
				setCurrentTime(progress);
				VideoPlayerFragment.this.onProgressChanged(getCurrentTime(), getDuration());
				checkSubs();
			}
		}
	};

	@Override public void onHardwareAccelerationError() {
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
	 * @param currentTime
	 * @param duration
	 */
	@Override
    protected void onProgressChanged(long currentTime, long duration) {
		mControlBar.setMax((int) duration);
		mControlBar.setProgress((int) currentTime);
		mControlBar.setSecondaryProgress(0); // hack to make the secondary progress appear on Android 5.0
		mControlBar.setSecondaryProgress(getStreamerProgress());
		if (getCurrentTime() >= 0) mCurrentTimeTextView.setText(StringUtils.millisToString(currentTime));
		if (getDuration() >= 0) lengthTime.setText(StringUtils.millisToString(duration));

		mControlBar.setSecondaryProgress(0); // hack to make the secondary progress appear on Android 5.0
		mControlBar.setSecondaryProgress(getStreamerProgress());
	}

	@OnClick(R.id.play_butotn)
    void onPlayPauseClick(){
		togglePlayPause();
	}

	@OnClick(R.id.rewindButton)
    void onRewindClick(){
		seekBackwardClick();
	}

	@OnClick(R.id.forwardButton)
    void onForwardClick(){
		seekForwardClick();
	}

	@OnClick(R.id.scaleButton)
    void onScaleClick(){
		scaleClick();
	}

	@OnClick(R.id.subsButton)
    void onSubsClick(){
		subsClick();
	}


}
