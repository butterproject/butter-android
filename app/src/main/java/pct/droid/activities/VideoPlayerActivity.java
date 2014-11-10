package pct.droid.activities;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import org.videolan.libvlc.EventHandler;
import org.videolan.libvlc.IVideoPlayer;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaList;

import java.lang.ref.WeakReference;

import butterknife.ButterKnife;
import butterknife.InjectView;
import pct.droid.R;
import pct.droid.utils.LogUtils;
import pct.droid.utils.PixelUtils;

public class VideoPlayerActivity extends ActionBarActivity implements SurfaceHolder.Callback, IVideoPlayer {
    public final static String TAG = "LibVLCAndroidSample/VideoActivity";

    public final static String LOCATION = "pct.droid.activities.VideoPlayerActivity.location";

    @InjectView(R.id.videoSurface)
    SurfaceView videoSurface;
    @InjectView(R.id.toolbar)
    Toolbar toolbar;
    @InjectView(R.id.controlBar)
    SeekBar controlBar;

    private String mFilePath;
    private SurfaceHolder holder;
    private LibVLC mLibVLC;
    private int mVideoWidth;
    private int mVideoHeight;
    private final static int VideoSizeChanged = -1;
    private boolean mDragging, mCanSeek = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_videoplayer);
        ButterKnife.inject(this);
        setSupportActionBar(toolbar);

        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2) {
            toolbar.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, getResources().getDimensionPixelSize(R.dimen.abc_action_bar_default_height_material) + PixelUtils.getStatusBarHeight(this)));
        } else {
            toolbar.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, getResources().getDimensionPixelSize(R.dimen.abc_action_bar_default_height_material)));
        }

        getSupportActionBar().setTitle("Now Playing");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Receive path to play from intent
        Intent intent = getIntent();
        mFilePath = intent.getExtras().getString(LOCATION);

        LogUtils.d(TAG, "Player started: " + mFilePath);

        holder = videoSurface.getHolder();
        holder.addCallback(this);

        controlBar.setOnSeekBarChangeListener(mOnControlBarListener);
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
    }

    public void surfaceCreated(SurfaceHolder holder) {
    }

    public void surfaceChanged(SurfaceHolder surfaceholder, int format,
                               int width, int height) {
        if (mLibVLC != null)
            mLibVLC.attachSurface(holder.getSurface(), this);
    }

    public void surfaceDestroyed(SurfaceHolder surfaceholder) {
    }

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
        holder.setFixedSize(mVideoWidth, mVideoHeight);

        // set display size
        LayoutParams lp = videoSurface.getLayoutParams();
        lp.width = w;
        lp.height = h;
        videoSurface.setLayoutParams(lp);
        videoSurface.invalidate();
    }

    @Override
    public void setSurfaceSize(int width, int height, int visible_width, int visible_height, int sar_num, int sar_den) {
        Message msg = Message.obtain(mHandler, VideoSizeChanged, width, height);
        msg.sendToTarget();
    }

    @Override
    public void eventHardwareAccelerationError() {

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

    public void updatePlayerViews() {

    }

    private int setOverlayProgress() {
        if (mLibVLC == null) {
            return 0;
        }
        int time = (int) mLibVLC.getTime();
        int length = (int) mLibVLC.getLength();

        LogUtils.d("Progress: " + length + "/" + time);

        // Update all view elements
        //boolean isSeekable = mEnableJumpButtons && length > 0;
        //mBackward.setVisibility(isSeekable ? View.VISIBLE : View.GONE);
        //mForward.setVisibility(isSeekable ? View.VISIBLE : View.GONE);
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
            EventHandler.getInstance().addHandler(mHandler);
            holder.setFormat(PixelFormat.RGBX_8888);
            holder.setKeepScreenOn(true);
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
        EventHandler.getInstance().removeHandler(mHandler);
        mLibVLC.stop();
        mLibVLC.detachSurface();
        holder = null;
        mLibVLC.closeAout();
        mLibVLC.destroy();
        mLibVLC = null;

        mVideoWidth = 0;
        mVideoHeight = 0;
    }

    private Handler mHandler = new VideoPlayerHandler(this);

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
                    player.setOverlayProgress();
                    break;
                case EventHandler.MediaPlayerPositionChanged:
                    LogUtils.d("PlayerData: " + b.toString());
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

    SeekBar.OnSeekBarChangeListener mOnControlBarListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            mDragging = true;
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            mDragging = false;
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser && mCanSeek) {
                mLibVLC.setTime(progress);
                setOverlayProgress();
            }

        }
    };
}