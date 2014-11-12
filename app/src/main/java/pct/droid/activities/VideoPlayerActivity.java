package pct.droid.activities;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import org.videolan.libvlc.EventHandler;
import org.videolan.libvlc.IVideoPlayer;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.LibVlcUtil;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaList;

import java.lang.ref.WeakReference;

import butterknife.ButterKnife;
import butterknife.InjectView;
import pct.droid.R;
import pct.droid.utils.LogUtils;
import pct.droid.utils.PixelUtils;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class VideoPlayerActivity extends ActionBarActivity implements IVideoPlayer, View.OnSystemUiVisibilityChangeListener {
    public final static String TAG = "LibVLCAndroidSample/VideoActivity";

    public final static String LOCATION = "pct.droid.activities.VideoPlayerActivity.location";

    @InjectView(R.id.videoSurface)
    SurfaceView videoSurface;
    @InjectView(R.id.toolbar)
    Toolbar toolbar;
    @InjectView(R.id.controlLayout)
    RelativeLayout controlLayout;
    @InjectView(R.id.controlBar)
    SeekBar controlBar;
    @InjectView(R.id.playButton)
    ImageButton playButton;
    View decorView;

    private Handler mHandler;
    private String mFilePath;
    private SurfaceHolder mVideoSurfaceHolder;
    private LibVLC mLibVLC;
    private int mVideoWidth;
    private int mVideoHeight;
    private final static int VideoSizeChanged = -1;
    private boolean mCanSeek = false, mOverlayVisible = true;

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
    private float mRestoreAutoBrightness = -1;

    private int mLastSystemUIVisibility;
    private long mLastSystemShowTime = System.currentTimeMillis();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_videoplayer);
        ButterKnife.inject(this);
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

        getSupportActionBar().setTitle("Now Playing");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Receive path to play from intent
        Intent intent = getIntent();
        mFilePath = intent.getExtras().getString(LOCATION);

        LogUtils.d(TAG, "Player started: " + mFilePath);

        mVideoSurfaceHolder = videoSurface.getHolder();
        mVideoSurfaceHolder.addCallback(mVideoSurfaceCallback);

        controlBar.setOnSeekBarChangeListener(mOnControlBarListener);

        mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        mAudioMax = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

        showOverlay();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setSize(mVideoWidth, mVideoHeight);
    }

    @Override
    protected void onResume() {
        super.onResume();
        createPlayer(mFilePath);
    }

    @Override
    protected void onPause() {
        super.onPause();
        releasePlayer();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releasePlayer();
        mAudioManager = null;
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

        /* Offset for Mouse Events */
        int[] offset = new int[2];
        videoSurface.getLocationOnScreen(offset);
        int xTouch = Math.round((event.getRawX() - offset[0]) * mVideoWidth / videoSurface.getWidth());
        int yTouch = Math.round((event.getRawY() - offset[1]) * mVideoHeight / videoSurface.getHeight());

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // Audio
                mTouchY = event.getRawY();
                mVol = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                mTouchAction = TOUCH_NONE;
                // Seek
                mTouchX = event.getRawX();
                // Mouse events for the core
                LibVLC.sendMouseEvent(MotionEvent.ACTION_DOWN, 0, xTouch, yTouch);
                break;

            case MotionEvent.ACTION_MOVE:
                LibVLC.sendMouseEvent(MotionEvent.ACTION_MOVE, 0, xTouch, yTouch);

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
                LibVLC.sendMouseEvent(MotionEvent.ACTION_UP, 0, xTouch, yTouch);

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

    private SurfaceHolder.Callback mVideoSurfaceCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
        }

        @Override
        public void surfaceChanged(SurfaceHolder surfaceholder, int format, int width, int height) {
            if (mLibVLC != null) {
                mLibVLC.attachSurface(mVideoSurfaceHolder.getSurface(), VideoPlayerActivity.this);
            }
        }

        public void surfaceDestroyed(SurfaceHolder surfaceholder) {
        }
    };

    private void setSize(int width, int height) {
        mVideoWidth = width;
        mVideoHeight = height;
        if (mVideoWidth * mVideoHeight <= 1)
            return;

        // get screen size
        int w = getWindow().getDecorView().getWidth();
        int h = getWindow().getDecorView().getHeight();

        // getWindow().getDecorView() doesn't always take orientation into
        // account, we have to correct the values
        boolean isPortrait = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
        if (w > h && isPortrait || w < h && !isPortrait) {
            int i = w;
            w = h;
            h = i;
        }

        float videoAR = (float) mVideoWidth / (float) mVideoHeight;
        float screenAR = (float) w / (float) h;

        if (screenAR < videoAR)
            h = (int) (w / videoAR);
        else
            w = (int) (h * videoAR);

        // force surface buffer size
        mVideoSurfaceHolder.setFixedSize(mVideoWidth, mVideoHeight);

        // set display size
        LayoutParams lp = videoSurface.getLayoutParams();
        lp.width = w;
        lp.height = h;
        videoSurface.setLayoutParams(lp);
        videoSurface.invalidate();
    }

    @Override
    public void setSurfaceSize(int width, int height, int visible_width, int visible_height, int sar_num, int sar_den) {
        Message msg = Message.obtain(mEventHandler, VideoSizeChanged, width, height);
        msg.sendToTarget();
    }

    @Override
    public void eventHardwareAccelerationError() {

    }

    private void doSeekTouch(float coef, float gesturesize, boolean seek) {
        // No seek action if coef > 0.5 and gesturesize < 1cm
        if (coef > 0.5 || Math.abs(gesturesize) < 1 || !mCanSeek)
            return;

        if (mTouchAction != TOUCH_NONE && mTouchAction != TOUCH_SEEK)
            return;
        mTouchAction = TOUCH_SEEK;

        long length = mLibVLC.getLength();
        long time = mLibVLC.getTime();

        // Size of the jump, 10 minutes max (600000), with a bi-cubic progression, for a 8cm gesture
        int jump = (int) (Math.signum(gesturesize) * ((600000 * Math.pow((gesturesize / 8), 4)) + 3000));

        // Adjust the jump
        if ((jump > 0) && ((time + jump) > length))
            jump = (int) (length - time);
        if ((jump < 0) && ((time + jump) < 0))
            jump = (int) -time;

        //Jump !
        if (seek && length > 0)
            mLibVLC.setTime(time + jump);

        if (length > 0) {
            //Show the jump's size
            //showInfo(String.format("%s%s (%s)", jump >= 0 ? "+" : "",  Strings.millisToString(jump), Strings.millisToString(time + jump)), 1000);
        } else {
            //showInfo(R.string.unseekable_stream, 1000);
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
        //showInfo(getString(R.string.volume) + '\u00A0' + Integer.toString(vol),1000);
    }

    @TargetApi(android.os.Build.VERSION_CODES.FROYO)
    private void initBrightnessTouch() {
        float brightnesstemp = 0.6f;
        // Initialize the layoutParams screen brightness
        try {
            if (LibVlcUtil.isFroyoOrLater() && Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE) == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
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
        //showInfo(getString(R.string.brightness) + '\u00A0' + Math.round(lp.screenBrightness*15),1000);
    }

    public void playPauseClick(View v) {
        if (mLibVLC == null)
            return;

        if(mLibVLC.isPlaying()) {
            mLibVLC.pause();
        } else {
            mLibVLC.play();
        }
    }

    public void seekForwardClick(View v) {
        seek(10000);
    }

    public void seekBackwardClick(View v) {
        seek(10000);
    }

    public void seek(int delta) {
        // unseekable stream
        if(mLibVLC.getLength() <= 0 || !mCanSeek) return;

        long position = mLibVLC.getTime() + delta;
        if (position < 0) position = 0;
        mLibVLC.setTime(position);
        showOverlay();
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public void showOverlay() {
        if(!mOverlayVisible) {
            Animation fadeOutAnim = AnimationUtils.loadAnimation(VideoPlayerActivity.this, android.R.anim.fade_in);
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
        } else {
            mHandler.removeCallbacks(mOverlayHideRunnable);
        }

        mOverlayVisible = true;
        mHandler.postDelayed(mOverlayHideRunnable, 5000);
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public void hideOverlay() {
        // Can only hide 1000 millisec after show, because navbar doesn't seem to hide otherwise.
        if(mLastSystemShowTime + 1000 < System.currentTimeMillis()) {
            Animation fadeOutAnim = AnimationUtils.loadAnimation(VideoPlayerActivity.this, android.R.anim.fade_out);
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

    public boolean isOverlayVisible() {
        return mOverlayVisible;
    }

    public void updatePlayerViews() {
        if(mLibVLC == null)
            return;

        if(mLibVLC.isPlaying()) {
            playButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_av_pause));
        } else {
            playButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_av_play));
        }
    }

    private int setOverlayProgress() {
        if (mLibVLC == null) {
            return 0;
        }
        int time = (int) mLibVLC.getTime();
        int length = (int) mLibVLC.getLength();

        LogUtils.d("Progress: " + length + "/" + time);
        controlBar.setMax(length);
        controlBar.setProgress(time);
        /*if (mSysTime != null)
            mSysTime.setText(DateFormat.getTimeFormat(this).format(new Date(System.currentTimeMillis())));
        if (time >= 0) mTime.setText(Strings.millisToString(time));
        if (length >= 0) mLength.setText(mDisplayRemainingTime && length > 0
                ? "- " + Strings.millisToString(length - time)
                : Strings.millisToString(length));*/

        return time;
    }

    private void createPlayer(String media) {
        releasePlayer();
        try {
            if (media.length() > 0) {
                Toast toast = Toast.makeText(this, media, Toast.LENGTH_LONG);
                toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0,
                        0);
                toast.show();
            }

            // Create a new media player
            mLibVLC = LibVLC.getInstance();
            mLibVLC.setHardwareAcceleration(LibVLC.HW_ACCELERATION_AUTOMATIC);
            mLibVLC.setSubtitlesEncoding("");
            mLibVLC.setAout(LibVLC.AOUT_OPENSLES);
            mLibVLC.setTimeStretching(true);
            mLibVLC.setChroma("RV32");
            mLibVLC.setVerboseMode(true);
            LibVLC.restart(this);
            EventHandler.getInstance().addHandler(mEventHandler);
            mVideoSurfaceHolder.setFormat(PixelFormat.RGBX_8888);
            mVideoSurfaceHolder.setKeepScreenOn(true);
            MediaList list = mLibVLC.getMediaList();
            list.clear();
            list.add(new Media(mLibVLC, LibVLC.PathToURI(media)), false);
            mLibVLC.playIndex(0);
        } catch (Exception e) {
            Toast.makeText(this, "Error creating player!", Toast.LENGTH_LONG).show();
        }
    }

    private void releasePlayer() {
        if (mLibVLC == null)
            return;
        EventHandler.getInstance().removeHandler(mEventHandler);
        mLibVLC.stop();
        mLibVLC.detachSurface();
        mVideoSurfaceHolder = null;
        mLibVLC.closeAout();
        mLibVLC.destroy();
        mLibVLC = null;

        mVideoWidth = 0;
        mVideoHeight = 0;
    }

    private Handler mEventHandler = new VideoPlayerHandler(this);

    private static class VideoPlayerHandler extends Handler {
        private WeakReference<VideoPlayerActivity> mOwner;

        public VideoPlayerHandler(VideoPlayerActivity owner) {
            mOwner = new WeakReference<VideoPlayerActivity>(owner);
        }

        @Override
        public void handleMessage(Message msg) {
            VideoPlayerActivity player = mOwner.get();

            if (msg.what == VideoSizeChanged) {
                player.setSize(msg.arg1, msg.arg2);
                return;
            }

            // Libvlc events
            Bundle b = msg.getData();
            switch (b.getInt("event")) {
                case EventHandler.MediaParsedChanged:

                    break;
                case EventHandler.MediaPlayerEndReached:
                    player.releasePlayer();
                    break;
                case EventHandler.MediaPlayerTimeChanged:
                    if(player.isOverlayVisible()) player.setOverlayProgress();
                    break;
                case EventHandler.MediaPlayerPositionChanged:
                    LogUtils.d("PlayerData: " + b.toString());
                    player.mCanSeek = true;
                    break;
                case EventHandler.MediaPlayerPlaying:
                    //start playing
                case EventHandler.MediaPlayerPaused:
                    //player paused
                case EventHandler.MediaPlayerStopped:
                    //player stopped
                    player.updatePlayerViews();
                    break;
                default:
                    break;
            }
        }
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
            if (fromUser && mCanSeek) {
                mLibVLC.setTime(progress);
                setOverlayProgress();
            }

        }
    };

    private Runnable mOverlayHideRunnable = new Runnable() {
        @Override
        public void run() {
            hideOverlay();
        }
    };
}