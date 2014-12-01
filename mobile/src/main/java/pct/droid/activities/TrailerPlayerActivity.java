package pct.droid.activities;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import pct.droid.base.PopcornApplication;
import pct.droid.R;
import pct.droid.base.providers.media.types.Media;
import pct.droid.base.utils.LogUtils;
import pct.droid.base.utils.PixelUtils;
import pct.droid.base.utils.StringUtils;
import pct.droid.base.youtube.YouTubeData;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class TrailerPlayerActivity extends BaseActivity implements View.OnSystemUiVisibilityChangeListener {

    public final static String LOCATION = "stream_url";
    public final static String DATA = "video_data";

    protected String mMsgErrorTitle = "Communications Error";
    protected String mMsgError = "An error occurred during the retrieval of the video.  This could be due to network issues or YouTube protocols.  Please try again later.";

    View decorView;
    @InjectView(R.id.progressIndicator)
    ProgressBar progressIndicator;
    @InjectView(R.id.trailerView)
    VideoView videoView;
    @InjectView(R.id.toolbar)
    Toolbar toolbar;
    @InjectView(R.id.controlLayout)
    RelativeLayout controlLayout;
    @InjectView(R.id.playerInfo)
    TextView playerInfo;
    @InjectView(R.id.controlBar)
    SeekBar controlBar;
    @InjectView(R.id.playButton)
    ImageButton playButton;
    @InjectView(R.id.currentTime)
    TextView currentTime;
    @InjectView(R.id.lengthTime)
    TextView lengthTime;
    @InjectView(R.id.scaleButton)
    ImageButton scaleButton;
    @InjectView(R.id.subsButton)
    ImageButton subsButton;

    private static final int FADE_OUT_OVERLAY = 5000;
    private static final int FADE_OUT_INFO = 1000;
    private static final int UPDATE_PROGRESS = 100;

    private Handler mHandler;
    private int mDuration = 0;
    private int mCurrentTime = 0;
    private int mBufferingProgress = 0;
    private boolean mOverlayVisible = true;

    private static final int TOUCH_NONE = 0;
    private static final int TOUCH_VOLUME = 1;
    private static final int TOUCH_BRIGHTNESS = 2;
    private static final int TOUCH_SEEK = 3;
    private int mTouchAction;
    private int mSurfaceYDisplayRange;
    private float mTouchY, mTouchX;

    private AudioManager mAudioManager;
    private int mAudioMax;
    private float mVol;
    private boolean mIsFirstBrightnessGesture = true;
    private float mRestoreAutoBrightness = -1f;

    private int mLastSystemUIVisibility;
    private long mLastSystemShowTime = System.currentTimeMillis();

    @Override
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_videoplayer);
        ButterKnife.inject(this);

        // Disable buttons in trailer layout
        scaleButton.setVisibility(View.GONE);
        subsButton.setVisibility(View.GONE);

        setSupportActionBar(toolbar);

        mHandler = new Handler(Looper.getMainLooper());
        decorView = getWindow().getDecorView();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            decorView.setOnSystemUiVisibilityChangeListener(this);
        }

        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            toolbar.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, getResources().getDimensionPixelSize(R.dimen.abc_action_bar_default_height_material) + PixelUtils.getStatusBarHeight(this)));
            toolbar.setPadding(toolbar.getPaddingLeft(), PixelUtils.getStatusBarHeight(this), toolbar.getPaddingRight(), toolbar.getPaddingBottom());
        }

        if(getIntent().hasExtra(DATA)) {
            Media media = getIntent().getParcelableExtra(DATA);
            if(media != null && media.title != null) {
                getSupportActionBar().setTitle(getString(R.string.trailer) + ": " + media.title);
            } else {
                getSupportActionBar().setTitle(getString(R.string.trailer));
            }
        } else {
            getSupportActionBar().setTitle(getString(R.string.trailer));
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Receive path to play from intent
        Intent intent = getIntent();
        String location = intent.getStringExtra(LOCATION);

        LogUtils.i("Player started: " + location);

        controlBar.setOnSeekBarChangeListener(mOnControlBarListener);

        mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        mAudioMax = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

        videoView.setOnPreparedListener(mOnPreparedListener);
        videoView.setOnErrorListener(mOnErrorListener);
        videoView.setOnCompletionListener(mOnCompletionListener);
        videoView.setKeepScreenOn(true);

        showOverlay();
        mProgressRunnable.run();

        videoView.setVisibility(View.VISIBLE);
        QueryYouTubeTask queryYouTubeTask = new QueryYouTubeTask();
        queryYouTubeTask.execute(YouTubeData.getYouTubeVideoId(location));

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(videoView != null && videoView.getDuration() > 0) {
            mProgressRunnable.run();
            showOverlay();
            hideInfo();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mHandler.removeCallbacksAndMessages(null);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mRestoreAutoBrightness != -1f) {
            int brightness = (int) (mRestoreAutoBrightness*255f);
            Settings.System.putInt(getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS,
                    brightness);
            Settings.System.putInt(getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS_MODE,
                    Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ((PopcornApplication) getApplication()).stopStreamer();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        DisplayMetrics screen = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(screen);

        if (mSurfaceYDisplayRange == 0)
            mSurfaceYDisplayRange = Math.min(screen.widthPixels, screen.heightPixels);

        float y_changed = event.getRawY() - mTouchY;
        float x_changed = event.getRawX() - mTouchX;

        // coef is the gradient's move to determine a neutral zone
        float coef = Math.abs (y_changed / x_changed);
        float xgesturesize = ((x_changed / screen.xdpi) * 2.54f);

        int[] offset = new int[2];
        videoView.getLocationOnScreen(offset);

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // Audio
                mTouchY = event.getRawY();
                mVol = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                mTouchAction = TOUCH_NONE;
                // Seek
                mTouchX = event.getRawX();
                break;

            case MotionEvent.ACTION_MOVE:
                if (coef > 2) {
                    mTouchY = event.getRawY();
                    mTouchX = event.getRawX();
                    if ((int)mTouchX > (screen.widthPixels / 2)){
                        doVolumeTouch(y_changed);
                    }
                    if ((int)mTouchX < (screen.widthPixels / 2)){
                        doBrightnessTouch(y_changed);
                    }
                } else {
                    // Seek (Right or Left move)
                    doSeekTouch(coef, xgesturesize, false);
                }
                break;

            case MotionEvent.ACTION_UP:
                if (mTouchAction == TOUCH_NONE) {
                    if (!mOverlayVisible) {
                        showOverlay();
                    } else {
                        hideOverlay();
                    }
                } else {
                    showOverlay();
                }

                doSeekTouch(coef, xgesturesize, true);
                break;
        }
        return super.onTouchEvent(event);
    }


    @Override
    public void onSystemUiVisibilityChange(int visibility) {
        if((mLastSystemUIVisibility & View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) != 0 && (visibility & View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) == 0) {
            showOverlay();
        }

        mLastSystemUIVisibility = visibility;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void doSeekTouch(float coef, float gesturesize, boolean seek) {
        // No seek action if coef > 0.5 and gesturesize < 1cm
        if (coef > 0.5 || Math.abs(gesturesize) < 1)
            return;

        if (mTouchAction != TOUCH_NONE && mTouchAction != TOUCH_SEEK)
            return;
        mTouchAction = TOUCH_SEEK;

        // Size of the jump, 10 minutes max (600000), with a bi-cubic progression, for a 8cm gesture
        int jump = (int) (Math.signum(gesturesize) * ((600000 * Math.pow((gesturesize / 8), 4)) + 3000));

        // Adjust the jump
        if ((jump > 0) && ((mCurrentTime + jump) > mDuration))
            jump = (mDuration - mCurrentTime);
        if ((jump < 0) && ((mCurrentTime + jump) < 0))
            jump = -mCurrentTime;

        //Jump !
        if (seek && mDuration > 0)
            seek(mCurrentTime + jump);

        if (mDuration > 0) {
            showInfo(String.format("%s%s (%s)", jump >= 0 ? "+" : "",  StringUtils.millisToString(jump), StringUtils.millisToString(mCurrentTime + jump)));
        }
    }

    private void doVolumeTouch(float y_changed) {
        if (mTouchAction != TOUCH_NONE && mTouchAction != TOUCH_VOLUME)
            return;
        float delta = - ((y_changed * 2f / mSurfaceYDisplayRange) * mAudioMax);
        mVol += delta;
        int vol = (int) Math.min(Math.max(mVol, 0), mAudioMax);
        if (delta != 0f) {
            setAudioVolume(vol);
        }
    }

    private void setAudioVolume(int vol) {
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, vol, 0);
        mTouchAction = TOUCH_VOLUME;
        showInfo(getString(R.string.volume) + '\u00A0' + Integer.toString(vol));
    }

    @TargetApi(android.os.Build.VERSION_CODES.FROYO)
    private void initBrightnessTouch() {
        float brightnesstemp = 0.6f;
        // Initialize the layoutParams screen brightness
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO && Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE) == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
                Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
                mRestoreAutoBrightness = android.provider.Settings.System.getInt(getContentResolver(), android.provider.Settings.System.SCREEN_BRIGHTNESS) / 255.0f;
            } else {
                brightnesstemp = android.provider.Settings.System.getInt(getContentResolver(), android.provider.Settings.System.SCREEN_BRIGHTNESS) / 255.0f;
            }
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.screenBrightness = brightnesstemp;
        getWindow().setAttributes(lp);
        mIsFirstBrightnessGesture = false;
    }

    private void doBrightnessTouch(float y_changed) {
        if (mTouchAction != TOUCH_NONE && mTouchAction != TOUCH_BRIGHTNESS)
            return;
        if (mIsFirstBrightnessGesture) initBrightnessTouch();
        mTouchAction = TOUCH_BRIGHTNESS;

        // Set delta : 2f is arbitrary for now, it possibly will change in the future
        float delta = - y_changed / mSurfaceYDisplayRange * 2f;

        changeBrightness(delta);
    }

    private void changeBrightness(float delta) {
        // Estimate and adjust Brightness
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.screenBrightness =  Math.min(Math.max(lp.screenBrightness + delta, 0.01f), 1);
        // Set Brightness
        getWindow().setAttributes(lp);
        showInfo(getString(R.string.brightness) + '\u00A0' + Math.round(lp.screenBrightness * 15));
    }

    public void playPauseClick(View v) {
        if(videoView != null && videoView.canPause()) {
            if(videoView.isPlaying()) {
                videoView.pause();
            } else {
                videoView.start();
            }
            updatePlayPause();
        }
    }

    public void seekForwardClick(View v) {
        seek(10000);
    }

    public void seekBackwardClick(View v) {
        seek(-10000);
    }

    public void seek(long delta) {
        if(mDuration <= 0) return;

        long position = mCurrentTime + delta;
        if (position < 0) position = 0;
        videoView.seekTo((int)position);
        showOverlay();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void showOverlay() {
        if(!mOverlayVisible) {
            updatePlayPause();

            Animation fadeOutAnim = AnimationUtils.loadAnimation(TrailerPlayerActivity.this, android.R.anim.fade_in);
            controlLayout.setVisibility(View.VISIBLE);
            controlLayout.startAnimation(fadeOutAnim);
            toolbar.setVisibility(View.VISIBLE);
            toolbar.startAnimation(fadeOutAnim);

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
                decorView.setSystemUiVisibility(uiOptions);
            } else {
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            }

            mLastSystemShowTime = System.currentTimeMillis();
        }

        mOverlayVisible = true;
        mHandler.removeCallbacks(mOverlayHideRunnable);
        mHandler.postDelayed(mOverlayHideRunnable, FADE_OUT_OVERLAY);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void hideOverlay() {
        // Can only hide 1000 millisec after show, because navbar doesn't seem to hide otherwise.
        if(mLastSystemShowTime + 1000 < System.currentTimeMillis()) {
            Animation fadeOutAnim = AnimationUtils.loadAnimation(TrailerPlayerActivity.this, android.R.anim.fade_out);
            controlLayout.startAnimation(fadeOutAnim);
            controlLayout.setVisibility(View.GONE);
            toolbar.startAnimation(fadeOutAnim);
            toolbar.setVisibility(View.GONE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN;
                decorView.setSystemUiVisibility(uiOptions);
            } else {
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
            }

            mHandler.removeCallbacks(mOverlayHideRunnable);
            mOverlayVisible = false;
        }
    }

    private void showInfo(String text) {
        playerInfo.setVisibility(View.VISIBLE);
        playerInfo.setText(text);
        mHandler.removeCallbacks(mInfoHideRunnable);
        mHandler.postDelayed(mInfoHideRunnable, FADE_OUT_INFO);
    }

    private void hideInfo() {
        if (playerInfo.getVisibility() == View.VISIBLE) {
            Animation fadeOutAnim = AnimationUtils.loadAnimation(TrailerPlayerActivity.this, android.R.anim.fade_out);
            playerInfo.startAnimation(fadeOutAnim);
        }
        playerInfo.setVisibility(View.INVISIBLE);
    }

    public void updatePlayPause() {
        if(videoView == null)
            return;

        if(videoView.isPlaying()) {
            playButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_av_pause));
        } else {
            playButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_av_play));
        }
    }

    private int setOverlayProgress() {
        if (videoView == null) {
            return 0;
        }

        mDuration = videoView.getDuration();
        mCurrentTime = videoView.getCurrentPosition();

        if(!mOverlayVisible) {
            return mCurrentTime;
        }

        controlBar.setMax(mDuration);
        controlBar.setProgress(mCurrentTime);
        controlBar.setSecondaryProgress(0); // hack to make the secondary progress appear on Android 5.0
        controlBar.setSecondaryProgress(mBufferingProgress);
        if (mCurrentTime >= 0) currentTime.setText(StringUtils.millisToString(mCurrentTime));
        if (mDuration >= 0) lengthTime.setText(StringUtils.millisToString(mDuration));

        return mCurrentTime;
    }

    private SeekBar.OnSeekBarChangeListener mOnControlBarListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser) {
                videoView.seekTo(progress);
            }
        }
    };

    private Runnable mOverlayHideRunnable = new Runnable() {
        @Override
        public void run() {
            hideOverlay();
        }
    };

    private Runnable mInfoHideRunnable = new Runnable() {
        @Override
        public void run() {
            hideInfo();
        }
    };

    private Runnable mProgressRunnable = new Runnable() {
        @Override
        public void run() {
            int pos = setOverlayProgress();
            mHandler.postDelayed(mProgressRunnable, UPDATE_PROGRESS - (pos % UPDATE_PROGRESS));
            updatePlayPause();
        }
    };

    MediaPlayer.OnPreparedListener mOnPreparedListener = new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mp) {
            mp.setOnBufferingUpdateListener(mOnBufferingListener);
            progressIndicator.setVisibility(View.GONE);
            videoView.start();
        }
    };

    MediaPlayer.OnErrorListener mOnErrorListener = new MediaPlayer.OnErrorListener() {
        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            return false;
        }
    };

    MediaPlayer.OnCompletionListener mOnCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mediaPlayer) {
            updatePlayPause();
            finish();
        }
    };

    MediaPlayer.OnBufferingUpdateListener mOnBufferingListener = new MediaPlayer.OnBufferingUpdateListener() {
        @Override
        public void onBufferingUpdate(MediaPlayer mediaPlayer, int progress) {
            mBufferingProgress = mDuration / 100 * progress;
        }
    };

    private class QueryYouTubeTask extends AsyncTask<String, Void, Uri> {

        private boolean mShowedError = false;

        @Override
        protected Uri doInBackground(String... params) {
            String uriStr = null;
            String quality = "17";   // 3gpp medium quality, which should be fast enough to view over EDGE connection
            String videoId = params[0];

            if(isCancelled())
                return null;

            try {
                WifiManager wifiManager = (WifiManager) TrailerPlayerActivity.this.getSystemService(Context.WIFI_SERVICE);
                TelephonyManager telephonyManager = (TelephonyManager) TrailerPlayerActivity.this.getSystemService(Context.TELEPHONY_SERVICE);

                // if we have a fast connection (wifi or 3g), then we'll get a high quality YouTube video
                if(wifiManager.isWifiEnabled() && wifiManager.getConnectionInfo() != null && wifiManager.getConnectionInfo().getIpAddress() != 0) {
                    quality = "22";
                } else if(telephonyManager.getDataState() == TelephonyManager.DATA_CONNECTED &&
                                (
                                        telephonyManager.getNetworkType() == TelephonyManager.NETWORK_TYPE_UMTS ||
                                                telephonyManager.getNetworkType() == TelephonyManager.NETWORK_TYPE_HSUPA ||
                                                telephonyManager.getNetworkType() == TelephonyManager.NETWORK_TYPE_HSPA ||
                                                telephonyManager.getNetworkType() == TelephonyManager.NETWORK_TYPE_HSDPA ||
                                                telephonyManager.getNetworkType() == TelephonyManager.NETWORK_TYPE_EVDO_0 ||
                                                telephonyManager.getNetworkType() == TelephonyManager.NETWORK_TYPE_EVDO_A
                                )
                        ) {
                    quality = "18";
                }

                if(isCancelled())
                    return null;

                ////////////////////////////////////
                // calculate the actual URL of the video, encoded with proper YouTube token
                uriStr = YouTubeData.calculateYouTubeUrl(quality, true, videoId);

                if(isCancelled())
                    return null;

            } catch(Exception e) {
                Log.e(this.getClass().getSimpleName(), "Error occurred while retrieving information from YouTube.", e);
            }

            if(uriStr != null){
                return Uri.parse(uriStr);
            } else {
                return null;
            }
        }

        @Override
        protected void onPostExecute(Uri result) {
            super.onPostExecute(result);

            try {
                if(isCancelled())
                    return;

                if(result == null){
                    throw new RuntimeException("Invalid NULL Url.");
                }

                videoView.setVideoURI(result);
                videoView.start();
            } catch(Exception e){
                Log.e(this.getClass().getSimpleName(), "Error playing video!", e);

                if(!mShowedError){
                    showErrorAlert();
                }
            }
        }

        private void showErrorAlert() {
            try {
                AlertDialog.Builder lBuilder = new AlertDialog.Builder(TrailerPlayerActivity.this);
                lBuilder.setTitle(mMsgErrorTitle);
                lBuilder.setCancelable(false);
                lBuilder.setMessage(mMsgError);

                lBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface pDialog, int pWhich) {
                        TrailerPlayerActivity.this.finish();
                    }
                });

                AlertDialog lDialog = lBuilder.create();
                lDialog.show();
            } catch(Exception e){
                Log.e(this.getClass().getSimpleName(), "Problem showing error dialog.", e);
            }
        }

        @Override
        protected void onProgressUpdate(Void... pValues) {
            super.onProgressUpdate(pValues);
        }

    }

}