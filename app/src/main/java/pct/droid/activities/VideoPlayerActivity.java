/*****************************************************************************
 * VideoPlayerActivity.java
 *****************************************************************************
 * Copyright Â© 2011-2014 VLC authors and VideoLAN
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston MA 02110-1301, USA.
 *****************************************************************************/

package pct.droid.activities;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.KeyguardManager;
import android.app.Presentation;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.MediaRouter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.format.DateFormat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnSystemUiVisibilityChangeListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import org.videolan.libvlc.EventHandler;
import org.videolan.libvlc.IVideoPlayer;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.LibVlcException;
import org.videolan.libvlc.LibVlcUtil;
import org.videolan.libvlc.Media;
import org.videolan.vlc.util.VLCInstance;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.StreamCorruptedException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import pct.droid.R;

public class VideoPlayerActivity extends ActionBarActivity implements IVideoPlayer {

    public final static String TAG = "VLC/VideoPlayerActivity";

    // Internal intent identifier to distinguish between internal launch and
    // external intent.
    public final static String PLAY_FROM_VIDEOGRID = "org.videolan.vlc.gui.video.PLAY_FROM_VIDEOGRID";

    private SurfaceView mSurfaceView;
    private SurfaceView mSubtitlesSurfaceView;
    private SurfaceHolder mSurfaceHolder;
    private SurfaceHolder mSubtitlesSurfaceHolder;
    private Surface mSurface = null;
    private Surface mSubtitleSurface = null;
    private FrameLayout mSurfaceFrame;
    private MediaRouter mMediaRouter;
    private MediaRouter.SimpleCallback mMediaRouterCallback;
    private SecondaryDisplay mPresentation;
    private LibVLC mLibVLC;
    private String mLocation;

    private static final int SURFACE_BEST_FIT = 0;
    private static final int SURFACE_FIT_HORIZONTAL = 1;
    private static final int SURFACE_FIT_VERTICAL = 2;
    private static final int SURFACE_FILL = 3;
    private static final int SURFACE_16_9 = 4;
    private static final int SURFACE_4_3 = 5;
    private static final int SURFACE_ORIGINAL = 6;
    private int mCurrentSize = SURFACE_BEST_FIT;

    private SharedPreferences mSettings;

    /** Overlay */
    private ActionBar mActionBar;
    private boolean mOverlayUseStatusBar;
    private View mOverlayHeader;
    private View mOverlayOption;
    private View mOverlayProgress;
    private View mOverlayBackground;
    private static final int OVERLAY_TIMEOUT = 4000;
    private static final int OVERLAY_INFINITE = -1;
    private static final int FADE_OUT = 1;
    private static final int SHOW_PROGRESS = 2;
    private static final int SURFACE_LAYOUT = 3;
    private static final int AUDIO_SERVICE_CONNECTION_SUCCESS = 5;
    private static final int AUDIO_SERVICE_CONNECTION_FAILED = 6;
    private static final int FADE_OUT_INFO = 4;
    private boolean mDragging;
    private boolean mShowing;
    private int mUiVisibility = -1;
    private SeekBar mSeekbar;
    private TextView mTitle;
    private TextView mSysTime;
    private TextView mBattery;
    private TextView mTime;
    private TextView mLength;
    private TextView mInfo;
    private ImageView mLoading;
    private TextView mLoadingText;
    private ImageButton mPlayPause;
    private ImageButton mBackward;
    private ImageButton mForward;
    private boolean mEnableJumpButtons;
    private boolean mEnableBrightnessGesture;
    private boolean mEnableCloneMode;
    private boolean mDisplayRemainingTime = false;
    private int mScreenOrientation;
    private int mScreenOrientationLock;
    private ImageButton mAudioTrack;
    private ImageButton mSubtitle;
    private ImageButton mLock;
    private ImageButton mSize;
    private ImageButton mMenu;
    private boolean mIsLocked = false;
    private int mLastAudioTrack = -1;
    private int mLastSpuTrack = -2;
    private int mOverlayTimeout = 0;

    /**
     * For uninterrupted switching between audio and video mode
     */
    private boolean mSwitchingView;
    private boolean mHardwareAccelerationError;
    private boolean mEndReached;
    private boolean mCanSeek;

    // Playlist
    private int savedIndexPosition = -1;

    // size of the video
    private int mVideoHeight;
    private int mVideoWidth;
    private int mVideoVisibleHeight;
    private int mVideoVisibleWidth;
    private int mSarNum;
    private int mSarDen;

    //Volume
    private AudioManager mAudioManager;
    private int mAudioMax;
    private OnAudioFocusChangeListener mAudioFocusListener;
    private boolean mMute = false;
    private int mVolSave;
    private float mVol;

    //Touch Events
    private static final int TOUCH_NONE = 0;
    private static final int TOUCH_VOLUME = 1;
    private static final int TOUCH_BRIGHTNESS = 2;
    private static final int TOUCH_SEEK = 3;
    private int mTouchAction;
    private int mSurfaceYDisplayRange;
    private float mTouchY, mTouchX;

    //stick event
    private static final int JOYSTICK_INPUT_DELAY = 300;
    private long mLastMove;

    // Brightness
    private boolean mIsFirstBrightnessGesture = true;
    private float mRestoreAutoBrightness = -1f;

    // Tracks & Subtitles
    private Map<Integer,String> mAudioTracksList;
    private Map<Integer,String> mSubtitleTracksList;
    /**
     * Used to store a selected subtitle; see onActivityResult.
     * It is possible to have multiple custom subs in one session
     * (just like desktop VLC allows you as well.)
     */
    private final ArrayList<String> mSubtitleSelectedFiles = new ArrayList<String>();

    // Whether fallback from HW acceleration to SW decoding was done.
    private boolean mDisabledHardwareAcceleration = false;
    private int mPreviousHardwareAccelerationMode;

    // Navigation handling (DVD, Blu-Ray...)
    private ImageButton mNavMenu;
    private boolean mHasMenu = false;
    private boolean mIsNavMenu = false;

    @Override
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (LibVlcUtil.isJellyBeanMR1OrLater()) {
            // Get the media router service (Miracast)
            mMediaRouter = (MediaRouter) getSystemService(Context.MEDIA_ROUTER_SERVICE);
            mMediaRouterCallback = new MediaRouter.SimpleCallback() {
                @Override
                public void onRoutePresentationDisplayChanged(MediaRouter router, MediaRouter.RouteInfo info) {
                    Log.d(TAG, "onRoutePresentationDisplayChanged: info=" + info);
                    removePresentation();
                }
            };
            Log.d(TAG, "MediaRouter information : " + mMediaRouter  .toString());
            mOverlayUseStatusBar = true;
        } else {
            mOverlayUseStatusBar = false;
        }

        mSettings = PreferenceManager.getDefaultSharedPreferences(this);

        /* Services and miscellaneous */
        mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        mAudioMax = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

        mEnableCloneMode = mSettings.getBoolean("enable_clone_mode", false);
        createPresentation();
        setContentView(mPresentation == null ? R.layout.activity_player : R.layout.activity_player_remote_control);

        if (LibVlcUtil.isICSOrLater())
            getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(
                    new OnSystemUiVisibilityChangeListener() {
                        @Override
                        public void onSystemUiVisibilityChange(int visibility) {
                            if (visibility == mUiVisibility)
                                return;
                            setSurfaceLayout(mVideoWidth, mVideoHeight, mVideoVisibleWidth, mVideoVisibleHeight, mSarNum, mSarDen);
                            if (visibility == View.SYSTEM_UI_FLAG_VISIBLE && !mShowing && !isFinishing()) {
                                showOverlay();
                            }
                            mUiVisibility = visibility;
                        }
                    }
            );

        /** initialize Views an their Events */
        if (mOverlayUseStatusBar) {
            mActionBar = getSupportActionBar();
            mActionBar.setDisplayShowHomeEnabled(false);
            mActionBar.setDisplayShowTitleEnabled(false);
            mActionBar.setBackgroundDrawable(null);
            mActionBar.setDisplayShowCustomEnabled(true);
            mActionBar.setCustomView(R.layout.activity_player_action_bar);

            ViewGroup view = (ViewGroup) mActionBar.getCustomView();
            /* Dispatch ActionBar touch events to the Activity */
            view.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    onTouchEvent(event);
                    return true;
                }
            });
            mTitle = (TextView) view.findViewById(R.id.player_overlay_title);
            mOverlayHeader = mSysTime = mBattery = null;
        } else {
            mOverlayHeader = findViewById(R.id.player_overlay_header);
            /* header */
            mTitle = (TextView) findViewById(R.id.player_overlay_title);
            mSysTime = (TextView) findViewById(R.id.player_overlay_systime);
            mBattery = (TextView) findViewById(R.id.player_overlay_battery);
        }
        mOverlayOption = findViewById(R.id.option_overlay);
        mOverlayProgress = findViewById(R.id.progress_overlay);
        mOverlayBackground = findViewById(R.id.player_overlay_background);

        // Position and remaining time
        mTime = (TextView) findViewById(R.id.player_overlay_time);
        mTime.setOnClickListener(mRemainingTimeListener);
        mLength = (TextView) findViewById(R.id.player_overlay_length);
        mLength.setOnClickListener(mRemainingTimeListener);

        // the info textView is not on the overlay
        mInfo = (TextView) findViewById(R.id.player_overlay_info);

        mEnableBrightnessGesture = mSettings.getBoolean("enable_brightness_gesture", true);
        mScreenOrientation = Integer.valueOf(
                mSettings.getString("screen_orientation_value", "4" /*SCREEN_ORIENTATION_SENSOR*/));

        mEnableJumpButtons = mSettings.getBoolean("enable_jump_buttons", false);
        mPlayPause = (ImageButton) findViewById(R.id.player_overlay_play);
        mPlayPause.setOnClickListener(mPlayPauseListener);
        mBackward = (ImageButton) findViewById(R.id.player_overlay_backward);
        mBackward.setOnClickListener(mBackwardListener);
        mForward = (ImageButton) findViewById(R.id.player_overlay_forward);
        mForward.setOnClickListener(mForwardListener);

        mAudioTrack = (ImageButton) findViewById(R.id.player_overlay_audio);
        mAudioTrack.setVisibility(View.GONE);
        mSubtitle = (ImageButton) findViewById(R.id.player_overlay_subtitle);
        mSubtitle.setVisibility(View.GONE);
        mNavMenu = (ImageButton) findViewById(R.id.player_overlay_navmenu);
        mNavMenu.setVisibility(View.GONE);

        mLock = (ImageButton) findViewById(R.id.lock_overlay_button);
        mLock.setOnClickListener(mLockListener);

        mSize = (ImageButton) findViewById(R.id.player_overlay_size);
        mSize.setOnClickListener(mSizeListener);

        mMenu = (ImageButton) findViewById(R.id.player_overlay_adv_function);

        try {
            mLibVLC = VLCInstance.getLibVlcInstance();
        } catch (LibVlcException e) {
            Log.d(TAG, "LibVLC initialisation failed");
            return;
        }

        mSurfaceView = (SurfaceView) findViewById(R.id.player_surface);
        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceFrame = (FrameLayout) findViewById(R.id.player_surface_frame);

        mSubtitlesSurfaceView = (SurfaceView) findViewById(R.id.subtitles_surface);
        mSubtitlesSurfaceHolder = mSubtitlesSurfaceView.getHolder();
        mSubtitlesSurfaceView.setZOrderMediaOverlay(true);
        mSubtitlesSurfaceHolder.setFormat(PixelFormat.TRANSLUCENT);

        if (mLibVLC.useCompatSurface())
            mSubtitlesSurfaceView.setVisibility(View.GONE);
        if (mPresentation == null) {
            mSurfaceHolder.addCallback(mSurfaceCallback);
            mSubtitlesSurfaceHolder.addCallback(mSubtitlesSurfaceCallback);
        }

        mSeekbar = (SeekBar) findViewById(R.id.player_overlay_seekbar);
        mSeekbar.setOnSeekBarChangeListener(mSeekListener);

        /* Loading view */
        mLoading = (ImageView) findViewById(R.id.player_overlay_loading);
        mLoadingText = (TextView) findViewById(R.id.player_overlay_loading_text);
        startLoadingAnimation();

        mSwitchingView = false;
        mHardwareAccelerationError = false;
        mEndReached = false;

        IntentFilter filter = new IntentFilter();
        if (!mOverlayUseStatusBar)
            filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(mReceiver, filter);

        Log.d(TAG,
                "Hardware acceleration mode: "
                        + Integer.toString(mLibVLC.getHardwareAcceleration()));

        // Signal to LibVLC that the videoPlayerActivity was created, thus the
        // SurfaceView is now available for MediaCodec direct rendering.
        mLibVLC.eventVideoPlayerActivityCreated(true);

        EventHandler em = EventHandler.getInstance();
        em.addHandler(eventHandler);

        this.setVolumeControlStream(AudioManager.STREAM_MUSIC);

        // Extra initialization when no secondary display is detected
        if (mPresentation == null) {
            // Orientation
            // 100 is the value for screen_orientation_start_lock
            setRequestedOrientation(mScreenOrientation != 100 ? mScreenOrientation : getScreenOrientation());
        } else
            setRequestedOrientation(getScreenOrientation());

        updateNavStatus();
    }

    @Override
    protected void onPause() {/*****************************************************************************
     * AudioService.java
     *****************************************************************************
     * Copyright Â© 2011-2013 VLC authors and VideoLAN
     *
     * This program is free software; you can redistribute it and/or modify
     * it under the terms of the GNU General Public License as published by
     * the Free Software Foundation; either version 2 of the License, or
     * (at your option) any later version.
     *
     * This program is distributed in the hope that it will be useful,
     * but WITHOUT ANY WARRANTY; without even the implied warranty of
     * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     * GNU General Public License for more details.
     *
     * You should have received a copy of the GNU General Public License
     * along with this program; if not, write to the Free Software
     * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston MA 02110-1301, USA.
     *****************************************************************************/

        package org.videolan.vlc.audio;

        import java.io.BufferedReader;
        import java.io.BufferedWriter;
        import java.io.File;
        import java.io.FileInputStream;
        import java.io.FileOutputStream;
        import java.io.IOException;
        import java.io.InputStreamReader;
        import java.io.OutputStreamWriter;
        import java.net.URI;
        import java.net.URISyntaxException;
        import java.util.ArrayList;
        import java.util.Calendar;
        import java.util.HashMap;
        import java.util.List;
        import java.util.Locale;
        import java.util.Random;
        import java.util.Stack;

        import org.videolan.libvlc.EventHandler;
        import org.videolan.libvlc.LibVLC;
        import org.videolan.libvlc.LibVlcException;
        import org.videolan.libvlc.LibVlcUtil;
        import org.videolan.libvlc.Media;
        import org.videolan.libvlc.MediaList;
        import org.videolan.vlc.MediaDatabase;
        import org.videolan.vlc.R;
        import org.videolan.vlc.RemoteControlClientReceiver;
        import org.videolan.vlc.VLCApplication;
        import org.videolan.vlc.gui.MainActivity;
        import org.videolan.vlc.gui.audio.AudioUtil;
        import org.videolan.vlc.gui.video.VideoPlayerActivity;
        import org.videolan.vlc.interfaces.IAudioService;
        import org.videolan.vlc.interfaces.IAudioServiceCallback;
        import org.videolan.vlc.util.AndroidDevices;
        import org.videolan.vlc.util.VLCInstance;
        import org.videolan.vlc.util.WeakHandler;

        import android.annotation.TargetApi;
        import android.app.Notification;
        import android.app.PendingIntent;
        import android.app.Service;
        import android.content.BroadcastReceiver;
        import android.content.ComponentName;
        import android.content.Context;
        import android.content.Intent;
        import android.content.IntentFilter;
        import android.content.SharedPreferences;
        import android.graphics.Bitmap;
        import android.media.AudioManager;
        import android.media.AudioManager.OnAudioFocusChangeListener;
        import android.media.MediaMetadataRetriever;
        import android.media.RemoteControlClient;
        import android.media.RemoteControlClient.MetadataEditor;
        import android.os.Build;
        import android.os.Bundle;
        import android.os.Handler;
        import android.os.IBinder;
        import android.os.Message;
        import android.os.PowerManager;
        import android.os.RemoteException;
        import android.preference.PreferenceManager;
        import android.support.v4.app.NotificationCompat;
        import android.telephony.TelephonyManager;
        import android.util.Log;
        import android.widget.RemoteViews;
        import android.widget.Toast;

        public class AudioService extends Service {

            private static final String TAG = "VLC/AudioService";

            private static final int SHOW_PROGRESS = 0;
            private static final int SHOW_TOAST = 1;
            public static final String START_FROM_NOTIFICATION = "from_notification";
            public static final String ACTION_REMOTE_GENERIC = "org.videolan.vlc.remote.";
            public static final String ACTION_REMOTE_BACKWARD = "org.videolan.vlc.remote.Backward";
            public static final String ACTION_REMOTE_PLAY = "org.videolan.vlc.remote.Play";
            public static final String ACTION_REMOTE_PLAYPAUSE = "org.videolan.vlc.remote.PlayPause";
            public static final String ACTION_REMOTE_PAUSE = "org.videolan.vlc.remote.Pause";
            public static final String ACTION_REMOTE_STOP = "org.videolan.vlc.remote.Stop";
            public static final String ACTION_REMOTE_FORWARD = "org.videolan.vlc.remote.Forward";
            public static final String ACTION_REMOTE_LAST_PLAYLIST = "org.videolan.vlc.remote.LastPlaylist";
            public static final String ACTION_WIDGET_INIT = "org.videolan.vlc.widget.INIT";
            public static final String ACTION_WIDGET_UPDATE = "org.videolan.vlc.widget.UPDATE";
            public static final String ACTION_WIDGET_UPDATE_COVER = "org.videolan.vlc.widget.UPDATE_COVER";
            public static final String ACTION_WIDGET_UPDATE_POSITION = "org.videolan.vlc.widget.UPDATE_POSITION";

            public static final String WIDGET_PACKAGE = "org.videolan.vlc";
            public static final String WIDGET_CLASS = "org.videolan.vlc.widget.VLCAppWidgetProvider";

            public static final int CURRENT_ITEM = 1;
            public static final int PREVIOUS_ITEM = 2;
            public static final int NEXT_ITEM = 3;

            private LibVLC mLibVLC;
            private HashMap<IAudioServiceCallback, Integer> mCallback;
            private EventHandler mEventHandler;
            private OnAudioFocusChangeListener audioFocusListener;
            private boolean mDetectHeadset = true;
            private PowerManager.WakeLock mWakeLock;

            private static boolean mWasPlayingAudio = false;

            // Index management
            /**
             * Stack of previously played indexes, used in shuffle mode
             */
            private Stack<Integer> mPrevious;
            private int mCurrentIndex; // Set to -1 if no media is currently loaded
            private int mPrevIndex; // Set to -1 if no previous media
            private int mNextIndex; // Set to -1 if no next media

            // Playback management
            private boolean mShuffling = false;
            private RepeatType mRepeating = RepeatType.None;
            private Random mRandom = null; // Used in shuffling process

            // RemoteControlClient-related
            /**
             * RemoteControlClient is for lock screen playback control.
             */
            private RemoteControlClient mRemoteControlClient = null;
            private RemoteControlClientReceiver mRemoteControlClientReceiver = null;
            /**
             * Last widget position update timestamp
             */
            private long mWidgetPositionTimestamp = Calendar.getInstance().getTimeInMillis();
            private ComponentName mRemoteControlClientReceiverComponent;

            @Override
            public void onCreate() {
                super.onCreate();

                // Get libVLC instance
                try {
                    mLibVLC = VLCInstance.getLibVlcInstance();
                } catch (LibVlcException e) {
                    e.printStackTrace();
                }

                mCallback = new HashMap<IAudioServiceCallback, Integer>();
                mCurrentIndex = -1;
                mPrevIndex = -1;
                mNextIndex = -1;
                mPrevious = new Stack<Integer>();
                mEventHandler = EventHandler.getInstance();
                mRemoteControlClientReceiverComponent = new ComponentName(getPackageName(),
                        RemoteControlClientReceiver.class.getName());

                // Make sure the audio player will acquire a wake-lock while playing. If we don't do
                // that, the CPU might go to sleep while the song is playing, causing playback to stop.
                PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
                mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);

                IntentFilter filter = new IntentFilter();
                filter.setPriority(Integer.MAX_VALUE);
                filter.addAction(ACTION_REMOTE_BACKWARD);
                filter.addAction(ACTION_REMOTE_PLAYPAUSE);
                filter.addAction(ACTION_REMOTE_PLAY);
                filter.addAction(ACTION_REMOTE_PAUSE);
                filter.addAction(ACTION_REMOTE_STOP);
                filter.addAction(ACTION_REMOTE_FORWARD);
                filter.addAction(ACTION_REMOTE_LAST_PLAYLIST);
                filter.addAction(ACTION_WIDGET_INIT);
                filter.addAction(Intent.ACTION_HEADSET_PLUG);
                filter.addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
                filter.addAction(VLCApplication.SLEEP_INTENT);
                filter.addAction(VLCApplication.INCOMING_CALL_INTENT);
                filter.addAction(VLCApplication.CALL_ENDED_INTENT);
                registerReceiver(serviceReceiver, filter);

                final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
                boolean stealRemoteControl = pref.getBoolean("enable_steal_remote_control", false);

                if (!LibVlcUtil.isFroyoOrLater() || stealRemoteControl) {
            /* Backward compatibility for API 7 */
                    filter = new IntentFilter();
                    if (stealRemoteControl)
                        filter.setPriority(Integer.MAX_VALUE);
                    filter.addAction(Intent.ACTION_MEDIA_BUTTON);
                    mRemoteControlClientReceiver = new RemoteControlClientReceiver();
                    registerReceiver(mRemoteControlClientReceiver, filter);
                }
            }

            /**
             * Set up the remote control and tell the system we want to be the default receiver for the MEDIA buttons
             * @see http://android-developers.blogspot.fr/2010/06/allowing-applications-to-play-nicer.html
             */
            @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
            public void setUpRemoteControlClient() {
                Context context = VLCApplication.getAppContext();
                AudioManager audioManager = (AudioManager)context.getSystemService(AUDIO_SERVICE);

                if (LibVlcUtil.isICSOrLater()) {
                    audioManager.registerMediaButtonEventReceiver(mRemoteControlClientReceiverComponent);

                    if (mRemoteControlClient == null) {
                        Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
                        mediaButtonIntent.setComponent(mRemoteControlClientReceiverComponent);
                        PendingIntent mediaPendingIntent = PendingIntent.getBroadcast(context, 0, mediaButtonIntent, 0);

                        // create and register the remote control client
                        mRemoteControlClient = new RemoteControlClient(mediaPendingIntent);
                        audioManager.registerRemoteControlClient(mRemoteControlClient);
                    }

                    mRemoteControlClient.setTransportControlFlags(
                            RemoteControlClient.FLAG_KEY_MEDIA_PLAY |
                                    RemoteControlClient.FLAG_KEY_MEDIA_PAUSE |
                                    RemoteControlClient.FLAG_KEY_MEDIA_PREVIOUS |
                                    RemoteControlClient.FLAG_KEY_MEDIA_NEXT |
                                    RemoteControlClient.FLAG_KEY_MEDIA_STOP);
                } else if (LibVlcUtil.isFroyoOrLater()) {
                    audioManager.registerMediaButtonEventReceiver(mRemoteControlClientReceiverComponent);
                }
            }

            /**
             * A function to control the Remote Control Client. It is needed for
             * compatibility with devices below Ice Cream Sandwich (4.0).
             *
             * @param p Playback state
             */
            @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
            private void setRemoteControlClientPlaybackState(int state) {
                if (!LibVlcUtil.isICSOrLater() || mRemoteControlClient == null)
                    return;

                switch (state) {
                    case EventHandler.MediaPlayerPlaying:
                        mRemoteControlClient.setPlaybackState(RemoteControlClient.PLAYSTATE_PLAYING);
                        break;
                    case EventHandler.MediaPlayerPaused:
                        mRemoteControlClient.setPlaybackState(RemoteControlClient.PLAYSTATE_PAUSED);
                        break;
                    case EventHandler.MediaPlayerStopped:
                        mRemoteControlClient.setPlaybackState(RemoteControlClient.PLAYSTATE_STOPPED);
                        break;
                }
            }

            @Override
            public int onStartCommand(Intent intent, int flags, int startId) {
                if (intent == null)
                    return START_STICKY;
                if(ACTION_REMOTE_PLAYPAUSE.equals(intent.getAction())){
                    if (hasCurrentMedia())
                        return START_STICKY;
                    else loadLastPlaylist();
                }
                updateWidget(this);
                return super.onStartCommand(intent, flags, startId);
            }

            @Override
            public void onDestroy() {
                super.onDestroy();
                stop();
                if (mWakeLock.isHeld())
                    mWakeLock.release();
                unregisterReceiver(serviceReceiver);
                if (mRemoteControlClientReceiver != null) {
                    unregisterReceiver(mRemoteControlClientReceiver);
                    mRemoteControlClientReceiver = null;
                }
            }

            @Override
            public IBinder onBind(Intent intent) {
                return mInterface;
            }

            @TargetApi(Build.VERSION_CODES.FROYO)
            private void changeAudioFocus(boolean gain) {
                if (!LibVlcUtil.isFroyoOrLater()) // NOP if not supported
                    return;

                if (audioFocusListener == null) {
                    audioFocusListener = new OnAudioFocusChangeListener() {
                        @Override
                        public void onAudioFocusChange(int focusChange) {
                            LibVLC libVLC = LibVLC.getExistingInstance();
                            switch (focusChange)
                            {
                                case AudioManager.AUDIOFOCUS_LOSS:
                                    if (libVLC.isPlaying())
                                        libVLC.pause();
                                    break;
                                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                            /*
                             * Lower the volume to 36% to "duck" when an alert or something
                             * needs to be played.
                             */
                                    libVLC.setVolume(36);
                                    break;
                                case AudioManager.AUDIOFOCUS_GAIN:
                                case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT:
                                case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK:
                                    libVLC.setVolume(100);
                                    break;
                            }
                        }
                    };
                }

                AudioManager am = (AudioManager)getSystemService(AUDIO_SERVICE);
                if(gain)
                    am.requestAudioFocus(audioFocusListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
                else
                    am.abandonAudioFocus(audioFocusListener);

            }

            private final BroadcastReceiver serviceReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    String action = intent.getAction();
                    int state = intent.getIntExtra("state", 0);
                    if( mLibVLC == null ) {
                        Log.w(TAG, "Intent received, but VLC is not loaded, skipping.");
                        return;
                    }

            /*
             * Incoming Call : Pause if VLC is playing audio or video.
             */
                    if (action.equalsIgnoreCase(VLCApplication.INCOMING_CALL_INTENT)) {
                        mWasPlayingAudio = mLibVLC.isPlaying() && mLibVLC.getVideoTracksCount() < 1;
                        if (mLibVLC.isPlaying())
                            pause();
                    }

            /*
             * Call ended : Play only if VLC was playing audio.
             */
                    if (action.equalsIgnoreCase(VLCApplication.CALL_ENDED_INTENT)
                            && mWasPlayingAudio) {
                        play();
                    }

                    // skip all headsets events if there is a call
                    TelephonyManager telManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                    if (telManager != null && telManager.getCallState() != TelephonyManager.CALL_STATE_IDLE)
                        return;

            /*
             * Launch the activity if needed
             */
                    if (action.startsWith(ACTION_REMOTE_GENERIC) && !mLibVLC.isPlaying() && !hasCurrentMedia()) {
                        Intent iVlc = new Intent(context, MainActivity.class);
                        iVlc.putExtra(START_FROM_NOTIFICATION, true);
                        iVlc.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        context.startActivity(iVlc);
                    }

            /*
             * Remote / headset control events
             */
                    if (action.equalsIgnoreCase(ACTION_REMOTE_PLAYPAUSE)) {
                        if (mLibVLC.isPlaying() && hasCurrentMedia())
                            pause();
                        else if (!mLibVLC.isPlaying() && hasCurrentMedia())
                            play();
                    } else if (action.equalsIgnoreCase(ACTION_REMOTE_PLAY)) {
                        if (!mLibVLC.isPlaying() && hasCurrentMedia())
                            play();
                    } else if (action.equalsIgnoreCase(ACTION_REMOTE_PAUSE)) {
                        if (mLibVLC.isPlaying() && hasCurrentMedia())
                            pause();
                    } else if (action.equalsIgnoreCase(ACTION_REMOTE_BACKWARD)) {
                        previous();
                    } else if (action.equalsIgnoreCase(ACTION_REMOTE_STOP)) {
                        stop();
                    } else if (action.equalsIgnoreCase(ACTION_REMOTE_FORWARD)) {
                        next();
                    } else if (action.equalsIgnoreCase(ACTION_REMOTE_LAST_PLAYLIST)) {
                        loadLastPlaylist();
                    } else if (action.equalsIgnoreCase(ACTION_WIDGET_INIT)) {
                        updateWidget(context);
                    }

            /*
             * headset plug events
             */
                    if (mDetectHeadset) {
                        if (action.equalsIgnoreCase(AudioManager.ACTION_AUDIO_BECOMING_NOISY)) {
                            Log.i(TAG, "Headset Removed.");
                            if (mLibVLC.isPlaying() && hasCurrentMedia())
                                pause();
                        }
                        else if (action.equalsIgnoreCase(Intent.ACTION_HEADSET_PLUG) && state != 0) {
                            Log.i(TAG, "Headset Inserted.");
                            if (!mLibVLC.isPlaying() && hasCurrentMedia())
                                play();
                        }
                    }

            /*
             * Sleep
             */
                    if (action.equalsIgnoreCase(VLCApplication.SLEEP_INTENT)) {
                        stop();
                    }
                }
            };

            /**
             * Handle libvlc asynchronous events
             */
            private final Handler mVlcEventHandler = new AudioServiceEventHandler(this);

            private static class AudioServiceEventHandler extends WeakHandler<AudioService> {
                public AudioServiceEventHandler(AudioService fragment) {
                    super(fragment);
                }

                @Override
                public void handleMessage(Message msg) {
                    AudioService service = getOwner();
                    if(service == null) return;

                    switch (msg.getData().getInt("event")) {
                        case EventHandler.MediaParsedChanged:
                            Log.i(TAG, "MediaParsedChanged");
                            break;
                        case EventHandler.MediaPlayerPlaying:
                            Log.i(TAG, "MediaPlayerPlaying");
                            service.executeUpdate();
                            service.executeUpdateProgress();

                            String location = service.mLibVLC.getMediaList().getMRL(service.mCurrentIndex);
                            long length = service.mLibVLC.getLength();
                            MediaDatabase dbManager = MediaDatabase.getInstance();
                            Media m = dbManager.getMedia(location);
                            /**
                             * 1) There is a media to update
                             * 2) It has a length of 0
                             * (dynamic track loading - most notably the OGG container)
                             * 3) We were able to get a length even after parsing
                             * (don't want to replace a 0 with a 0)
                             */
                            if(m != null && m.getLength() == 0 && length > 0) {
                                Log.d(TAG, "Updating audio file length");
                                dbManager.updateMedia(location,
                                        MediaDatabase.mediaColumn.MEDIA_LENGTH, length);
                            }

                            service.changeAudioFocus(true);
                            service.setRemoteControlClientPlaybackState(EventHandler.MediaPlayerPlaying);
                            service.showNotification();
                            if (!service.mWakeLock.isHeld())
                                service.mWakeLock.acquire();
                            break;
                        case EventHandler.MediaPlayerPaused:
                            Log.i(TAG, "MediaPlayerPaused");
                            service.executeUpdate();
                            service.executeUpdateProgress();
                            service.showNotification();
                            service.setRemoteControlClientPlaybackState(EventHandler.MediaPlayerPaused);
                            if (service.mWakeLock.isHeld())
                                service.mWakeLock.release();
                            break;
                        case EventHandler.MediaPlayerStopped:
                            Log.i(TAG, "MediaPlayerStopped");
                            service.executeUpdate();
                            service.executeUpdateProgress();
                            service.setRemoteControlClientPlaybackState(EventHandler.MediaPlayerStopped);
                            if (service.mWakeLock.isHeld())
                                service.mWakeLock.release();
                            break;
                        case EventHandler.MediaPlayerEndReached:
                            Log.i(TAG, "MediaPlayerEndReached");
                            service.executeUpdate();
                            service.executeUpdateProgress();
                            service.determinePrevAndNextIndices(true);
                            service.next();
                            if (service.mWakeLock.isHeld())
                                service.mWakeLock.release();
                            break;
                        case EventHandler.MediaPlayerVout:
                            if(msg.getData().getInt("data") > 0) {
                                service.handleVout();
                            }
                            break;
                        case EventHandler.MediaPlayerPositionChanged:
                            float pos = msg.getData().getFloat("data");
                            service.updateWidgetPosition(service, pos);
                            break;
                        case EventHandler.MediaPlayerEncounteredError:
                            service.showToast(service.getString(
                                    R.string.invalid_location,
                                    service.mLibVLC.getMediaList().getMRL(
                                            service.mCurrentIndex)), Toast.LENGTH_SHORT);
                            service.executeUpdate();
                            service.executeUpdateProgress();
                            service.next();
                            if (service.mWakeLock.isHeld())
                                service.mWakeLock.release();
                            break;
                        case EventHandler.MediaPlayerTimeChanged:
                            // avoid useless error logs
                            break;
                        default:
                            Log.e(TAG, String.format("Event not handled (0x%x)", msg.getData().getInt("event")));
                            break;
                    }
                }
            };

            private final Handler mListEventHandler = new MediaListEventHandler(this);

            private static class MediaListEventHandler extends WeakHandler<AudioService> {
                // Don't clobber mCurrentIndex when MediaList is expanding itself.
                boolean expanding = false;

                public MediaListEventHandler(AudioService audioService) {
                    super(audioService);
                }

                @Override
                public void handleMessage(Message msg) {
                    AudioService service = getOwner();
                    if(service == null) return;

                    int index;
                    switch (msg.getData().getInt("event")) {
                        case EventHandler.CustomMediaListItemAdded:
                            Log.i(TAG, "CustomMediaListItemAdded");
                            index = msg.getData().getInt("item_index");
                            if(service.mCurrentIndex >= index && !expanding)
                                service.mCurrentIndex++;

                            service.determinePrevAndNextIndices();
                            service.executeUpdate();
                            break;
                        case EventHandler.CustomMediaListItemDeleted:
                            Log.i(TAG, "CustomMediaListItemDeleted");
                            index = msg.getData().getInt("item_index");
                            if (service.mCurrentIndex == index && !expanding) {
                                // The current item has been deleted
                                service.mCurrentIndex--;
                                service.determinePrevAndNextIndices();
                                if (service.mNextIndex != -1)
                                    service.next();
                                else if (service.mCurrentIndex != -1)
                                    service.mLibVLC.playIndex(service.mCurrentIndex);
                                else
                                    service.stop();
                                break;
                            }

                            if(service.mCurrentIndex > index && !expanding)
                                service.mCurrentIndex--;
                            service.determinePrevAndNextIndices();
                            service.executeUpdate();
                            break;
                        case EventHandler.CustomMediaListItemMoved:
                            Log.i(TAG, "CustomMediaListItemMoved");
                            int positionStart = msg.getData().getInt("index_before");
                            int positionEnd = msg.getData().getInt("index_after");
                            if (service.mCurrentIndex == positionStart) {
                                service.mCurrentIndex = positionEnd;
                                if (positionEnd > positionStart)
                                    service.mCurrentIndex--;
                            } else if (positionStart > service.mCurrentIndex
                                    && positionEnd <= service.mCurrentIndex)
                                service.mCurrentIndex++;
                            else if (positionStart < service.mCurrentIndex
                                    && positionEnd > service.mCurrentIndex)
                                service.mCurrentIndex--;

                            // If we are in random mode, we completely reset the stored previous track
                            // as their indices changed.
                            service.mPrevious.clear();

                            service.determinePrevAndNextIndices();
                            service.executeUpdate();
                            break;
                        case EventHandler.CustomMediaListExpanding:
                            expanding = true;
                            break;
                        case EventHandler.CustomMediaListExpandingEnd:
                            expanding = false;
                            break;
                    }
                }
            };

            private void handleVout() {
                if (!hasCurrentMedia())
                    return;
                Log.i(TAG, "Obtained video track");
                String title = getCurrentMedia().getTitle();
                String MRL = mLibVLC.getMediaList().getMRL(mCurrentIndex);
                int index = mCurrentIndex;
                mCurrentIndex = -1;
                mEventHandler.removeHandler(mVlcEventHandler);
                // Preserve playback when switching to video
                hideNotification(false);

                // Switch to the video player & don't lose the currently playing stream
                VideoPlayerActivity.start(VLCApplication.getAppContext(), MRL, title, index, true);
            }

            private void executeUpdate() {
                executeUpdate(true);
            }

            private void executeUpdate(Boolean updateWidget) {
                for (IAudioServiceCallback callback : mCallback.keySet()) {
                    try {
                        callback.update();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                if (updateWidget)
                    updateWidget(this);
            }

            private void executeUpdateProgress() {
                for (IAudioServiceCallback callback : mCallback.keySet()) {
                    try {
                        callback.updateProgress();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }

            /**
             * Return the current media.
             *
             * @return The current media or null if there is not any.
             */
            private Media getCurrentMedia() {
                return mLibVLC.getMediaList().getMedia(mCurrentIndex);
            }

            /**
             * Alias for mCurrentIndex >= 0
             *
             * @return True if a media is currently loaded, false otherwise
             */
            private boolean hasCurrentMedia() {
                return mCurrentIndex >= 0 && mCurrentIndex < mLibVLC.getMediaList().size();
            }

            private final Handler mHandler = new AudioServiceHandler(this);

            private static class AudioServiceHandler extends WeakHandler<AudioService> {
                public AudioServiceHandler(AudioService fragment) {
                    super(fragment);
                }

                @Override
                public void handleMessage(Message msg) {
                    AudioService service = getOwner();
                    if(service == null) return;

                    switch (msg.what) {
                        case SHOW_PROGRESS:
                            if (service.mCallback.size() > 0) {
                                removeMessages(SHOW_PROGRESS);
                                service.executeUpdateProgress();
                                sendEmptyMessageDelayed(SHOW_PROGRESS, 1000);
                            }
                            break;
                        case SHOW_TOAST:
                            final Bundle bundle = msg.getData();
                            final String text = bundle.getString("text");
                            final int duration = bundle.getInt("duration");
                            Toast.makeText(VLCApplication.getAppContext(), text, duration).show();
                            break;
                    }
                }
            };

            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
            private void showNotification() {
                try {
                    Media media = getCurrentMedia();
                    if (media == null)
                        return;
                    Bitmap cover = AudioUtil.getCover(this, media, 64);
                    String title = media.getTitle();
                    String artist = media.getArtist();
                    String album = media.getAlbum();
                    Notification notification;

                    // add notification to status bar
                    NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                            .setSmallIcon(R.drawable.ic_stat_vlc)
                            .setTicker(title + " - " + artist)
                            .setAutoCancel(false)
                            .setOngoing(true);

                    Intent notificationIntent = new Intent(this, MainActivity.class);
                    notificationIntent.setAction(MainActivity.ACTION_SHOW_PLAYER);
                    notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);
                    notificationIntent.putExtra(START_FROM_NOTIFICATION, true);
                    PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                    if (LibVlcUtil.isJellyBeanOrLater()) {
                        Intent iBackward = new Intent(ACTION_REMOTE_BACKWARD);
                        Intent iPlay = new Intent(ACTION_REMOTE_PLAYPAUSE);
                        Intent iForward = new Intent(ACTION_REMOTE_FORWARD);
                        Intent iStop = new Intent(ACTION_REMOTE_STOP);
                        PendingIntent piBackward = PendingIntent.getBroadcast(this, 0, iBackward, PendingIntent.FLAG_UPDATE_CURRENT);
                        PendingIntent piPlay = PendingIntent.getBroadcast(this, 0, iPlay, PendingIntent.FLAG_UPDATE_CURRENT);
                        PendingIntent piForward = PendingIntent.getBroadcast(this, 0, iForward, PendingIntent.FLAG_UPDATE_CURRENT);
                        PendingIntent piStop = PendingIntent.getBroadcast(this, 0, iStop, PendingIntent.FLAG_UPDATE_CURRENT);

                        RemoteViews view = new RemoteViews(getPackageName(), R.layout.notification);
                        if (cover != null)
                            view.setImageViewBitmap(R.id.cover, cover);
                        view.setTextViewText(R.id.songName, title);
                        view.setTextViewText(R.id.artist, artist);
                        view.setImageViewResource(R.id.play_pause, mLibVLC.isPlaying() ? R.drawable.ic_pause_w : R.drawable.ic_play_w);
                        view.setOnClickPendingIntent(R.id.play_pause, piPlay);
                        view.setOnClickPendingIntent(R.id.forward, piForward);
                        view.setOnClickPendingIntent(R.id.stop, piStop);
                        view.setOnClickPendingIntent(R.id.content, pendingIntent);

                        RemoteViews view_expanded = new RemoteViews(getPackageName(), R.layout.notification_expanded);
                        if (cover != null)
                            view_expanded.setImageViewBitmap(R.id.cover, cover);
                        view_expanded.setTextViewText(R.id.songName, title);
                        view_expanded.setTextViewText(R.id.artist, artist);
                        view_expanded.setTextViewText(R.id.album, album);
                        view_expanded.setImageViewResource(R.id.play_pause, mLibVLC.isPlaying() ? R.drawable.ic_pause_w : R.drawable.ic_play_w);
                        view_expanded.setOnClickPendingIntent(R.id.backward, piBackward);
                        view_expanded.setOnClickPendingIntent(R.id.play_pause, piPlay);
                        view_expanded.setOnClickPendingIntent(R.id.forward, piForward);
                        view_expanded.setOnClickPendingIntent(R.id.stop, piStop);
                        view_expanded.setOnClickPendingIntent(R.id.content, pendingIntent);

                        notification = builder.build();
                        notification.contentView = view;
                        notification.bigContentView = view_expanded;
                    }
                    else {
                        builder.setLargeIcon(cover)
                                .setContentTitle(title)
                                .setContentText(LibVlcUtil.isJellyBeanOrLater() ? artist
                                        : media.getSubtitle())
                                .setContentInfo(album)
                                .setContentIntent(pendingIntent);
                        notification = builder.build();
                    }

                    startService(new Intent(this, AudioService.class));
                    startForeground(3, notification);
                }
                catch (NoSuchMethodError e){
                    // Compat library is wrong on 3.2
                    // http://code.google.com/p/android/issues/detail?id=36359
                    // http://code.google.com/p/android/issues/detail?id=36502
                }
            }

            private void hideNotification() {
                hideNotification(true);
            }

            /**
             * Hides the VLC notification and stops the service.
             *
             * @param stopPlayback True to also stop playback at the same time. Set to false to preserve playback (e.g. for vout events)
             */
            private void hideNotification(boolean stopPlayback) {
                stopForeground(true);
                if(stopPlayback)
                    stopSelf();
            }

            private void pause() {
                setUpRemoteControlClient();
                mHandler.removeMessages(SHOW_PROGRESS);
                // hideNotification(); <-- see event handler
                mLibVLC.pause();
            }

            private void play() {
                if(hasCurrentMedia()) {
                    setUpRemoteControlClient();
                    mLibVLC.play();
                    mHandler.sendEmptyMessage(SHOW_PROGRESS);
                    showNotification();
                    updateWidget(this);
                }
            }

            private void stop() {
                mLibVLC.stop();
                mEventHandler.removeHandler(mVlcEventHandler);
                mLibVLC.getMediaList().getEventHandler().removeHandler(mListEventHandler);
                setRemoteControlClientPlaybackState(EventHandler.MediaPlayerStopped);
                mCurrentIndex = -1;
                mPrevious.clear();
                mHandler.removeMessages(SHOW_PROGRESS);
                hideNotification();
                executeUpdate();
                executeUpdateProgress();
                changeAudioFocus(false);
            }

            private void determinePrevAndNextIndices() {
                determinePrevAndNextIndices(false);
            }

            private void determinePrevAndNextIndices(boolean expand) {
                mNextIndex = expand ? mLibVLC.expand() : -1;
                mPrevIndex = -1;

                if (mNextIndex == -1) {
                    // No subitems; play the next item.
                    int size = mLibVLC.getMediaList().size();

                    // Repeating once doesn't change the index
                    if (mRepeating == RepeatType.Once) {
                        mPrevIndex = mNextIndex = mCurrentIndex;
                    } else {

                        if(mShuffling) {
                            if(mPrevious.size() > 0)
                                mPrevIndex = mPrevious.peek();
                            // If we've played all songs already in shuffle, then either
                            // reshuffle or stop (depending on RepeatType).
                            if(mPrevious.size() + 1 == size) {
                                if(mRepeating == RepeatType.None) {
                                    mNextIndex = -1;
                                    return;
                                } else {
                                    mPrevious.clear();
                                }
                            }
                            if(mRandom == null) mRandom = new Random();
                            // Find a new index not in mPrevious.
                            do
                            {
                                mNextIndex = mRandom.nextInt(size);
                            }
                            while(mNextIndex == mCurrentIndex || mPrevious.contains(mNextIndex));

                        } else {
                            // normal playback
                            if(mCurrentIndex > 0)
                                mPrevIndex = mCurrentIndex - 1;
                            if(mCurrentIndex + 1 < size)
                                mNextIndex = mCurrentIndex + 1;
                            else {
                                if(mRepeating == RepeatType.None) {
                                    mNextIndex = -1;
                                } else {
                                    mNextIndex = 0;
                                }
                            }
                        }
                    }
                }
            }

            private void next() {
                mPrevious.push(mCurrentIndex);
                mCurrentIndex = mNextIndex;

                int size = mLibVLC.getMediaList().size();
                if (size == 0 || mCurrentIndex < 0 || mCurrentIndex >= size) {
                    Log.w(TAG, "Warning: invalid next index, aborted !");
                    stop();
                    return;
                }

                mLibVLC.playIndex(mCurrentIndex);

                mHandler.sendEmptyMessage(SHOW_PROGRESS);
                setUpRemoteControlClient();
                showNotification();
                updateWidget(this);
                updateRemoteControlClientMetadata();
                saveCurrentMedia();

                determinePrevAndNextIndices();
            }

            @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
            private void updateRemoteControlClientMetadata() {
                if (!LibVlcUtil.isICSOrLater()) // NOP check
                    return;

                Media media = getCurrentMedia();
                if (mRemoteControlClient != null && media != null) {
                    MetadataEditor editor = mRemoteControlClient.editMetadata(true);
                    editor.putString(MediaMetadataRetriever.METADATA_KEY_ALBUM, media.getAlbum());
                    editor.putString(MediaMetadataRetriever.METADATA_KEY_ARTIST, media.getArtist());
                    editor.putString(MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST, media.getArtist());
                    editor.putString(MediaMetadataRetriever.METADATA_KEY_GENRE, media.getGenre());
                    editor.putString(MediaMetadataRetriever.METADATA_KEY_TITLE, media.getTitle());
                    editor.putLong(MediaMetadataRetriever.METADATA_KEY_DURATION, media.getLength());
                    // Copy the cover bitmap because the RemonteControlClient can recycle its artwork bitmap.
                    Bitmap cover = getCover();
                    editor.putBitmap(MetadataEditor.BITMAP_KEY_ARTWORK, ((cover != null) ? cover.copy(cover.getConfig(), false) : null));
                    editor.apply();
                }
            }

            private void previous() {
                mCurrentIndex = mPrevIndex;
                if (mPrevious.size() > 0)
                    mPrevious.pop();

                int size = mLibVLC.getMediaList().size();
                if (size == 0 || mPrevIndex < 0 || mCurrentIndex >= size) {
                    Log.w(TAG, "Warning: invalid previous index, aborted !");
                    stop();
                    return;
                }

                mLibVLC.playIndex(mCurrentIndex);
                mHandler.sendEmptyMessage(SHOW_PROGRESS);
                setUpRemoteControlClient();
                showNotification();
                updateWidget(this);
                updateRemoteControlClientMetadata();
                saveCurrentMedia();

                determinePrevAndNextIndices();
            }

            private void shuffle() {
                if (mShuffling)
                    mPrevious.clear();
                mShuffling = !mShuffling;
                saveCurrentMedia();
                determinePrevAndNextIndices();
            }

            private void setRepeatType(int t) {
                mRepeating = RepeatType.values()[t];
                determinePrevAndNextIndices();
            }

            private Bitmap getCover() {
                Media media = getCurrentMedia();
                return media != null ? AudioUtil.getCover(this, media, 512) : null;
            }

            private final IAudioService.Stub mInterface = new IAudioService.Stub() {

                @Override
                public void pause() throws RemoteException {
                    AudioService.this.pause();
                }

                @Override
                public void play() throws RemoteException {
                    AudioService.this.play();
                }

                @Override
                public void stop() throws RemoteException {
                    AudioService.this.stop();
                }

                @Override
                public boolean isPlaying() throws RemoteException {
                    return mLibVLC.isPlaying();
                }

                @Override
                public boolean isShuffling() {
                    return mShuffling;
                }

                @Override
                public int getRepeatType() {
                    return mRepeating.ordinal();
                }

                @Override
                public boolean hasMedia() throws RemoteException {
                    return hasCurrentMedia();
                }

                @Override
                public String getAlbum() throws RemoteException {
                    if (hasCurrentMedia())
                        return getCurrentMedia().getAlbum();
                    else
                        return null;
                }

                @Override
                public String getArtist() throws RemoteException {
                    if (hasCurrentMedia())
                        return getCurrentMedia().getArtist();
                    else
                        return null;
                }

                @Override
                public String getArtistPrev() throws RemoteException {
                    if (mPrevIndex != -1)
                        return mLibVLC.getMediaList().getMedia(mPrevIndex).getArtist();
                    else
                        return null;
                }

                @Override
                public String getArtistNext() throws RemoteException {
                    if (mNextIndex != -1)
                        return mLibVLC.getMediaList().getMedia(mNextIndex).getArtist();
                    else
                        return null;
                }

                @Override
                public String getTitle() throws RemoteException {
                    if (hasCurrentMedia())
                        return getCurrentMedia().getTitle();
                    else
                        return null;
                }

                @Override
                public String getTitlePrev() throws RemoteException {
                    if (mPrevIndex != -1)
                        return mLibVLC.getMediaList().getMedia(mPrevIndex).getTitle();
                    else
                        return null;
                }

                @Override
                public String getTitleNext() throws RemoteException {
                    if (mNextIndex != -1)
                        return mLibVLC.getMediaList().getMedia(mNextIndex).getTitle();
                    else
                        return null;
                }

                @Override
                public Bitmap getCover() {
                    if (hasCurrentMedia()) {
                        return AudioService.this.getCover();
                    }
                    return null;
                }

                @Override
                public Bitmap getCoverPrev() throws RemoteException {
                    if (mPrevIndex != -1)
                        return AudioUtil.getCover(AudioService.this, mLibVLC.getMediaList().getMedia(mPrevIndex), 64);
                    else
                        return null;
                }

                @Override
                public Bitmap getCoverNext() throws RemoteException {
                    if (mNextIndex != -1)
                        return AudioUtil.getCover(AudioService.this, mLibVLC.getMediaList().getMedia(mNextIndex), 64);
                    else
                        return null;
                }

                @Override
                public synchronized void addAudioCallback(IAudioServiceCallback cb)
                        throws RemoteException {
                    Integer count = mCallback.get(cb);
                    if (count == null)
                        count = 0;
                    mCallback.put(cb, count + 1);
                    if (hasCurrentMedia())
                        mHandler.sendEmptyMessage(SHOW_PROGRESS);
                }

                @Override
                public synchronized void removeAudioCallback(IAudioServiceCallback cb)
                        throws RemoteException {
                    Integer count = mCallback.get(cb);
                    if (count == null)
                        count = 0;
                    if (count > 1)
                        mCallback.put(cb, count - 1);
                    else
                        mCallback.remove(cb);
                }

                @Override
                public int getTime() throws RemoteException {
                    return (int) mLibVLC.getTime();
                }

                @Override
                public int getLength() throws RemoteException {
                    return (int) mLibVLC.getLength();
                }

                /**
                 * Loads a selection of files (a non-user-supplied collection of media)
                 * into the primary or "currently playing" playlist.
                 *
                 * @param mediaPathList A list of locations to load
                 * @param position The position to start playing at
                 * @param noVideo True to disable video, false otherwise
                 * @throws RemoteException
                 */
                @Override
                public void load(List<String> mediaPathList, int position, boolean noVideo)
                        throws RemoteException {

                    Log.v(TAG, "Loading position " + ((Integer)position).toString() + " in " + mediaPathList.toString());
                    mEventHandler.addHandler(mVlcEventHandler);

                    mLibVLC.getMediaList().getEventHandler().removeHandler(mListEventHandler);
                    mLibVLC.setMediaList();
                    mLibVLC.getPrimaryMediaList().clear();
                    MediaList mediaList = mLibVLC.getMediaList();

                    mPrevious.clear();

                    MediaDatabase db = MediaDatabase.getInstance();
                    for (int i = 0; i < mediaPathList.size(); i++) {
                        String location = mediaPathList.get(i);
                        Media media = db.getMedia(location);
                        if(media == null) {
                            if(!validateLocation(location)) {
                                Log.w(TAG, "Invalid location " + location);
                                showToast(getResources().getString(R.string.invalid_location, location), Toast.LENGTH_SHORT);
                                continue;
                            }
                            Log.v(TAG, "Creating on-the-fly Media object for " + location);
                            media = new Media(mLibVLC, location);
                        }
                        mediaList.add(media, noVideo);
                    }

                    if (mLibVLC.getMediaList().size() == 0) {
                        Log.w(TAG, "Warning: empty media list, nothing to play !");
                        return;
                    }
                    if (mLibVLC.getMediaList().size() > position && position >= 0) {
                        mCurrentIndex = position;
                    } else {
                        Log.w(TAG, "Warning: positon " + position + " out of bounds");
                        mCurrentIndex = 0;
                    }

                    // Add handler after loading the list
                    mLibVLC.getMediaList().getEventHandler().addHandler(mListEventHandler);

                    mLibVLC.playIndex(mCurrentIndex);
                    mHandler.sendEmptyMessage(SHOW_PROGRESS);
                    setUpRemoteControlClient();
                    showNotification();
                    updateWidget(AudioService.this);
                    updateRemoteControlClientMetadata();
                    AudioService.this.saveMediaList();
                    AudioService.this.saveCurrentMedia();
                    determinePrevAndNextIndices();
                }

                /**
                 * Use this function to play a media inside whatever MediaList LibVLC is following.
                 *
                 * Unlike load(), it does not import anything into the primary list.
                 */
                @Override
                public void playIndex(int index) {
                    if (mLibVLC.getMediaList().size() == 0) {
                        Log.w(TAG, "Warning: empty media list, nothing to play !");
                        return;
                    }
                    if (index >= 0 && index < mLibVLC.getMediaList().size()) {
                        mCurrentIndex = index;
                    } else {
                        Log.w(TAG, "Warning: index " + index + " out of bounds");
                        mCurrentIndex = 0;
                    }

                    mEventHandler.addHandler(mVlcEventHandler);
                    mLibVLC.playIndex(mCurrentIndex);
                    mHandler.sendEmptyMessage(SHOW_PROGRESS);
                    setUpRemoteControlClient();
                    showNotification();
                    updateWidget(AudioService.this);
                    updateRemoteControlClientMetadata();
                    determinePrevAndNextIndices();
                }

                /**
                 * Use this function to show an URI in the audio interface WITHOUT
                 * interrupting the stream.
                 *
                 * Mainly used by VideoPlayerActivity in response to loss of video track.
                 */
                @Override
                public void showWithoutParse(int index) throws RemoteException {
                    String URI = mLibVLC.getMediaList().getMRL(index);
                    Log.v(TAG, "Showing index " + index + " with playing URI " + URI);
                    // Show an URI without interrupting/losing the current stream

                    if(URI == null || !mLibVLC.isPlaying())
                        return;
                    mEventHandler.addHandler(mVlcEventHandler);
                    mCurrentIndex = index;

                    // Notify everyone
                    mHandler.sendEmptyMessage(SHOW_PROGRESS);
                    showNotification();
                    determinePrevAndNextIndices();
                    executeUpdate();
                    executeUpdateProgress();
                }

                /**
                 * Append to the current existing playlist
                 */
                @Override
                public void append(List<String> mediaLocationList) throws RemoteException {
                    if (!hasCurrentMedia())
                    {
                        load(mediaLocationList, 0, false);
                        return;
                    }

                    MediaDatabase db = MediaDatabase.getInstance();
                    for (int i = 0; i < mediaLocationList.size(); i++) {
                        String location = mediaLocationList.get(i);
                        Media media = db.getMedia(location);
                        if(media == null) {
                            if (!validateLocation(location)) {
                                showToast(getResources().getString(R.string.invalid_location, location), Toast.LENGTH_SHORT);
                                continue;
                            }
                            Log.v(TAG, "Creating on-the-fly Media object for " + location);
                            media = new Media(mLibVLC, location);
                        }
                        mLibVLC.getMediaList().add(media);
                    }
                    AudioService.this.saveMediaList();
                    determinePrevAndNextIndices();
                    executeUpdate();
                }

                /**
                 * Move an item inside the playlist.
                 */
                @Override
                public void moveItem(int positionStart, int positionEnd) throws RemoteException {
                    mLibVLC.getMediaList().move(positionStart, positionEnd);
                }

                @Override
                public void remove(int position) {
                    mLibVLC.getMediaList().remove(position);
                }

                @Override
                public void removeLocation(String location) {
                    mLibVLC.getMediaList().remove(location);
                }

                @Override
                public List<String> getMediaLocations() {
                    ArrayList<String> medias = new ArrayList<String>();
                    for (int i = 0; i < mLibVLC.getMediaList().size(); i++) {
                        medias.add(mLibVLC.getMediaList().getMRL(i));
                    }
                    return medias;
                }

                @Override
                public String getCurrentMediaLocation() throws RemoteException {
                    return mLibVLC.getMediaList().getMRL(mCurrentIndex);
                }

                @Override
                public void next() throws RemoteException {
                    AudioService.this.next();
                }

                @Override
                public void previous() throws RemoteException {
                    AudioService.this.previous();
                }

                @Override
                public void shuffle() throws RemoteException {
                    AudioService.this.shuffle();
                }

                @Override
                public void setRepeatType(int t) throws RemoteException {
                    AudioService.this.setRepeatType(t);
                }

                @Override
                public void setTime(long time) throws RemoteException {
                    mLibVLC.setTime(time);
                }

                @Override
                public boolean hasNext() throws RemoteException {
                    if (mNextIndex != -1)
                        return true;
                    else
                        return false;
                }

                @Override
                public boolean hasPrevious() throws RemoteException {
                    if (mPrevIndex != -1)
                        return true;
                    else
                        return false;
                }

                @Override
                public void detectHeadset(boolean enable) throws RemoteException {
                    mDetectHeadset = enable;
                }

                @Override
                public float getRate() throws RemoteException {
                    return mLibVLC.getRate();
                }
            };

            private void updateWidget(Context context) {
                Log.d(TAG, "Updating widget");
                updateWidgetState(context);
                updateWidgetCover(context);
            }

            private void updateWidgetState(Context context) {
                Intent i = new Intent();
                i.setClassName(WIDGET_PACKAGE, WIDGET_CLASS);
                i.setAction(ACTION_WIDGET_UPDATE);

                if (hasCurrentMedia()) {
                    i.putExtra("title", getCurrentMedia().getTitle());
                    i.putExtra("artist", getCurrentMedia().getArtist());
                }
                else {
                    i.putExtra("title", context.getString(R.string.widget_name));
                    i.putExtra("artist", "");
                }
                i.putExtra("isplaying", mLibVLC.isPlaying());

                sendBroadcast(i);
            }

            private void updateWidgetCover(Context context)
            {
                Intent i = new Intent();
                i.setClassName(WIDGET_PACKAGE, WIDGET_CLASS);
                i.setAction(ACTION_WIDGET_UPDATE_COVER);

                Bitmap cover = hasCurrentMedia() ? AudioUtil.getCover(this, getCurrentMedia(), 64) : null;
                i.putExtra("cover", cover);

                sendBroadcast(i);
            }

            private void updateWidgetPosition(Context context, float pos)
            {
                // no more than one widget update for each 1/50 of the song
                long timestamp = Calendar.getInstance().getTimeInMillis();
                if (!hasCurrentMedia()
                        || timestamp - mWidgetPositionTimestamp < getCurrentMedia()
                        .getLength() / 50)
                    return;

                updateWidgetState(context);

                mWidgetPositionTimestamp = timestamp;
                Intent i = new Intent();
                i.setClassName(WIDGET_PACKAGE, WIDGET_CLASS);
                i.setAction(ACTION_WIDGET_UPDATE_POSITION);
                i.putExtra("position", pos);
                sendBroadcast(i);
            }

            private synchronized void loadLastPlaylist() {
                if (!AndroidDevices.hasExternalStorage())
                    return;

                String line;
                FileInputStream input = null;
                BufferedReader br = null;
                int rowCount = 0;

                int position = 0;
                String currentMedia;
                List<String> mediaPathList = new ArrayList<String>();

                try {
                    // read CurrentMedia
                    input = new FileInputStream(AudioUtil.CACHE_DIR + "/" + "CurrentMedia.txt");
                    br = new BufferedReader(new InputStreamReader(input));
                    currentMedia = br.readLine();
                    mShuffling = "1".equals(br.readLine());
                    br.close(); br = null;
                    input.close();

                    // read MediaList
                    input = new FileInputStream(AudioUtil.CACHE_DIR + "/" + "MediaList.txt");
                    br = new BufferedReader(new InputStreamReader(input));
                    while ((line = br.readLine()) != null) {
                        mediaPathList.add(line);
                        if (line.equals(currentMedia))
                            position = rowCount;
                        rowCount++;
                    }

                    // load playlist
                    mInterface.load(mediaPathList, position, false);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                finally {
                    try {
                        if (br!= null) br.close();
                        if (input != null) input.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }

            private synchronized void saveCurrentMedia() {
                if (!AndroidDevices.hasExternalStorage())
                    return;

                FileOutputStream output;
                BufferedWriter bw;

                try {
                    output = new FileOutputStream(AudioUtil.CACHE_DIR + "/" + "CurrentMedia.txt");
                    bw = new BufferedWriter(new OutputStreamWriter(output));
                    bw.write(mLibVLC.getMediaList().getMRL(mCurrentIndex));
                    bw.write('\n');
                    bw.write(mShuffling ? "1" : "0");
                    bw.write('\n');
                    bw.close();
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            private synchronized void saveMediaList() {
                if (!AndroidDevices.hasExternalStorage())
                    return;

                FileOutputStream output;
                BufferedWriter bw;

                try {
                    output = new FileOutputStream(AudioUtil.CACHE_DIR + "/" + "MediaList.txt");
                    bw = new BufferedWriter(new OutputStreamWriter(output));
                    for (int i = 0; i < mLibVLC.getMediaList().size(); i++) {
                        bw.write(mLibVLC.getMediaList().getMRL(i));
                        bw.write('\n');
                    }
                    bw.close();
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            private boolean validateLocation(String location)
            {
        /* Check if the MRL contains a scheme */
                if (!location.matches("\\w+://.+"))
                    location = "file://".concat(location);
                if (location.toLowerCase(Locale.ENGLISH).startsWith("file://")) {
            /* Ensure the file exists */
                    File f;
                    try {
                        f = new File(new URI(location));
                    } catch (URISyntaxException e) {
                        return false;
                    }
                    if (!f.isFile())
                        return false;
                }
                return true;
            }

            private void showToast(String text, int duration) {
                Message msg = new Message();
                Bundle bundle = new Bundle();
                bundle.putString("text", text);
                bundle.putInt("duration", duration);
                msg.setData(bundle);
                msg.what = SHOW_TOAST;
                mHandler.sendMessage(msg);
            }
        }
        super.onPause();

        if (mMediaRouter != null) {
            // Stop listening for changes to media routes.
            mediaRouterAddCallback(false);
        }

        if(mSwitchingView) {
            Log.d(TAG, "mLocation = \"" + mLocation + "\"");
            AudioServiceController.getInstance().showWithoutParse(savedIndexPosition);
            AudioServiceController.getInstance().unbindAudioService(this);
            return;
        }

        long time = mLibVLC.getTime();
        long length = mLibVLC.getLength();
        //remove saved position if in the last 5 seconds
        if (length - time < 5000)
            time = 0;
        else
            time -= 5000; // go back 5 seconds, to compensate loading time

        /*
         * Pausing here generates errors because the vout is constantly
         * trying to refresh itself every 80ms while the surface is not
         * accessible anymore.
         * To workaround that, we keep the last known position in the playlist
         * in savedIndexPosition to be able to restore it during onResume().
         */
        mLibVLC.stop();

        mSurfaceView.setKeepScreenOn(false);

        SharedPreferences.Editor editor = mSettings.edit();
        // Save position
        if (time >= 0 && mCanSeek) {
            if(MediaDatabase.getInstance().mediaItemExists(mLocation)) {
                MediaDatabase.getInstance().updateMedia(
                        mLocation,
                        MediaDatabase.mediaColumn.MEDIA_TIME,
                        time);
            } else {
                // Video file not in media library, store time just for onResume()
                editor.putLong(PreferencesActivity.VIDEO_RESUME_TIME, time);
            }
        }
        // Save selected subtitles
        String subtitleList_serialized = null;
        if(mSubtitleSelectedFiles.size() > 0) {
            Log.d(TAG, "Saving selected subtitle files");
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            try {
                ObjectOutputStream oos = new ObjectOutputStream(bos);
                oos.writeObject(mSubtitleSelectedFiles);
                subtitleList_serialized = bos.toString();
            } catch(IOException e) {}
        }
        editor.putString(PreferencesActivity.VIDEO_SUBTITLE_FILES, subtitleList_serialized);

        editor.commit();
        AudioServiceController.getInstance().unbindAudioService(this);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    protected void onStop() {
        super.onStop();

        // Dismiss the presentation when the activity is not visible.
        if (mPresentation != null) {
            Log.i(TAG, "Dismissing presentation because the activity is no longer visible.");
            mPresentation.dismiss();
            mPresentation = null;
        }
        restoreBrightness();
    }

    @TargetApi(android.os.Build.VERSION_CODES.FROYO)
    private void restoreBrightness() {
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
        unregisterReceiver(mReceiver);

        EventHandler em = EventHandler.getInstance();
        em.removeHandler(eventHandler);

        // MediaCodec opaque direct rendering should not be used anymore since there is no surface to attach.
        mLibVLC.eventVideoPlayerActivityCreated(false);
        // HW acceleration was temporarily disabled because of an error, restore the previous value.
        if (mDisabledHardwareAcceleration)
            mLibVLC.setHardwareAcceleration(mPreviousHardwareAccelerationMode);

        mAudioManager = null;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSwitchingView = false;
        AudioServiceController.getInstance().bindAudioService(this,
                new AudioServiceController.AudioServiceConnectionListener() {
                    @Override
                    public void onConnectionSuccess() {
                        mHandler.sendEmptyMessage(AUDIO_SERVICE_CONNECTION_SUCCESS);
                    }

                    @Override
                    public void onConnectionFailed() {
                        mHandler.sendEmptyMessage(AUDIO_SERVICE_CONNECTION_FAILED);
                    }
                });

        if (mMediaRouter != null) {
            // Listen for changes to media routes.
            mediaRouterAddCallback(true);
        }

        if (mIsLocked && mScreenOrientation == ActivityInfo.SCREEN_ORIENTATION_SENSOR)
            setRequestedOrientation(mScreenOrientationLock);
    }

    /**
     * Add or remove MediaRouter callbacks. This is provided for version targeting.
     *
     * @param add true to add, false to remove
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void mediaRouterAddCallback(boolean add) {
        if(!LibVlcUtil.isJellyBeanMR1OrLater() || mMediaRouter == null) return;

        if(add)
            mMediaRouter.addCallback(MediaRouter.ROUTE_TYPE_LIVE_VIDEO, mMediaRouterCallback);
        else
            mMediaRouter.removeCallback(mMediaRouterCallback);
    }

    private void startPlayback() {
        loadMedia();

        /*
         * if the activity has been paused by pressing the power button,
         * pressing it again will show the lock screen.
         * But onResume will also be called, even if vlc-android is still in the background.
         * To workaround that, pause playback if the lockscreen is displayed
         */
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mLibVLC != null && mLibVLC.isPlaying()) {
                    KeyguardManager km = (KeyguardManager)getSystemService(KEYGUARD_SERVICE);
                    if (km.inKeyguardRestrictedInputMode())
                        mLibVLC.pause();
                }
            }}, 500);

        // Add any selected subtitle file from the file picker
        if(mSubtitleSelectedFiles.size() > 0) {
            for(String file : mSubtitleSelectedFiles) {
                Log.i(TAG, "Adding user-selected subtitle " + file);
                mLibVLC.addSubtitleTrack(file);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(data == null) return;

        if(data.getDataString() == null) {
            Log.d(TAG, "Subtitle selection dialog was cancelled");
        }
        if(data.getData() == null) return;

        String subtitlePath = data.getData().getPath();
        if(requestCode == CommonDialogs.INTENT_SPECIFIC) {
            Log.d(TAG, "Specific subtitle file: " + subtitlePath);
        } else if(requestCode == CommonDialogs.INTENT_GENERIC) {
            Log.d(TAG, "Generic subtitle file: " + subtitlePath);
        }
        mSubtitleSelectedFiles.add(subtitlePath);
    }

    public static void start(Context context, String location) {
        start(context, location, null, -1, false, false);
    }

    public static void start(Context context, String location, Boolean fromStart) {
        start(context, location, null, -1, false, fromStart);
    }

    public static void start(Context context, String location, String title, Boolean dontParse) {
        start(context, location, title, -1, dontParse, false);
    }

    public static void start(Context context, String location, String title, int position, Boolean dontParse) {
        start(context, location, title, position, dontParse, false);
    }

    public static void start(Context context, String location, String title, int position, Boolean dontParse, Boolean fromStart) {
        Intent intent = new Intent(context, VideoPlayerActivity.class);
        intent.setAction(VideoPlayerActivity.PLAY_FROM_VIDEOGRID);
        intent.putExtra("itemLocation", location);
        intent.putExtra("itemTitle", title);
        intent.putExtra("dontParse", dontParse);
        intent.putExtra("fromStart", fromStart);
        intent.putExtra("itemPosition", position);

        if (dontParse)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);

        context.startActivity(intent);
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();
            if (action.equalsIgnoreCase(Intent.ACTION_BATTERY_CHANGED)) {
                if (mBattery == null)
                    return;
                int batteryLevel = intent.getIntExtra("level", 0);
                if (batteryLevel >= 50)
                    mBattery.setTextColor(Color.GREEN);
                else if (batteryLevel >= 30)
                    mBattery.setTextColor(Color.YELLOW);
                else
                    mBattery.setTextColor(Color.RED);
                mBattery.setText(String.format("%d%%", batteryLevel));
            }
            else if (action.equalsIgnoreCase(VLCApplication.SLEEP_INTENT)) {
                finish();
            }
        }
    };

    @Override
    public boolean onTrackballEvent(MotionEvent event) {
        showOverlay();
        return true;
    }

    @TargetApi(12) //only active for Android 3.1+
    public boolean dispatchGenericMotionEvent(MotionEvent event){

        InputDevice mInputDevice = event.getDevice();

        float x = AndroidDevices.getCenteredAxis(event, mInputDevice,
                MotionEvent.AXIS_X);
        float y = AndroidDevices.getCenteredAxis(event, mInputDevice,
                MotionEvent.AXIS_Y);
        float z = AndroidDevices.getCenteredAxis(event, mInputDevice,
                MotionEvent.AXIS_Z);
        float rz = AndroidDevices.getCenteredAxis(event, mInputDevice,
                MotionEvent.AXIS_RZ);

        if (System.currentTimeMillis() - mLastMove > JOYSTICK_INPUT_DELAY){
            if (Math.abs(x) > 0.3){
                if (AndroidDevices.hasTsp()) {
                    seek(x > 0.0f ? 10000 : -10000);
                } else
                    navigateDvdMenu(x > 0.0f ? KeyEvent.KEYCODE_DPAD_RIGHT : KeyEvent.KEYCODE_DPAD_LEFT);
            } else if (Math.abs(y) > 0.3){
                if (AndroidDevices.hasTsp()) {
                    if (mIsFirstBrightnessGesture)
                        initBrightnessTouch();
                    changeBrightness(-y / 10f);
                } else
                    navigateDvdMenu(x > 0.0f ? KeyEvent.KEYCODE_DPAD_UP : KeyEvent.KEYCODE_DPAD_DOWN);
            } else if (Math.abs(rz) > 0.3){
                mVol = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                int delta = -(int) ((rz / 7) * mAudioMax);
                int vol = (int) Math.min(Math.max(mVol + delta, 0), mAudioMax);
                setAudioVolume(vol);
            }
            mLastMove = System.currentTimeMillis();
        }
        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        showOverlayTimeout(OVERLAY_TIMEOUT);
        switch (keyCode) {
            case KeyEvent.KEYCODE_F:
            case KeyEvent.KEYCODE_MEDIA_FAST_FORWARD:
            case KeyEvent.KEYCODE_BUTTON_R1:
                seek(10000);
                return true;
            case KeyEvent.KEYCODE_R:
            case KeyEvent.KEYCODE_MEDIA_REWIND:
            case KeyEvent.KEYCODE_BUTTON_L1:
                seek(-10000);
                return true;
            case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
            case KeyEvent.KEYCODE_MEDIA_PLAY:
            case KeyEvent.KEYCODE_MEDIA_PAUSE:
            case KeyEvent.KEYCODE_SPACE:
            case KeyEvent.KEYCODE_BUTTON_A:
                if (mIsNavMenu)
                    return navigateDvdMenu(keyCode);
                else
                    doPlayPause();
                return true;
            case KeyEvent.KEYCODE_V:
            case KeyEvent.KEYCODE_BUTTON_Y:
                selectSubtitles();
                return true;
            case KeyEvent.KEYCODE_B:
            case KeyEvent.KEYCODE_MEDIA_AUDIO_TRACK:
            case KeyEvent.KEYCODE_BUTTON_B:
                selectAudioTrack();
                return true;
            case KeyEvent.KEYCODE_M:
            case KeyEvent.KEYCODE_MENU:
                showNavMenu();
                return true;
            case KeyEvent.KEYCODE_O:
                showAdvancedOptions(mMenu);
                return true;
            case KeyEvent.KEYCODE_A:
                resizeVideo();
                return true;
            case KeyEvent.KEYCODE_VOLUME_MUTE:
            case KeyEvent.KEYCODE_BUTTON_X:
                if (mIsNavMenu)
                    return navigateDvdMenu(keyCode);
                else
                    updateMute();
                return true;
            case KeyEvent.KEYCODE_S:
            case KeyEvent.KEYCODE_MEDIA_STOP:
                finish();
                return true;
            case KeyEvent.KEYCODE_DPAD_UP:
            case KeyEvent.KEYCODE_DPAD_DOWN:
            case KeyEvent.KEYCODE_DPAD_LEFT:
            case KeyEvent.KEYCODE_DPAD_RIGHT:
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_ENTER:
                if (mIsNavMenu)
                    return navigateDvdMenu(keyCode);
                else
                    return super.onKeyDown(keyCode, event);
            default:
                return super.onKeyDown(keyCode, event);
        }
    }

    private boolean navigateDvdMenu(int keyCode) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_UP:
                mLibVLC.playerNavigate(LibVLC.INPUT_NAV_UP);
                return true;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                mLibVLC.playerNavigate(LibVLC.INPUT_NAV_DOWN);
                return true;
            case KeyEvent.KEYCODE_DPAD_LEFT:
                mLibVLC.playerNavigate(LibVLC.INPUT_NAV_LEFT);
                return true;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                mLibVLC.playerNavigate(LibVLC.INPUT_NAV_RIGHT);
                return true;
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_ENTER:
            case KeyEvent.KEYCODE_BUTTON_X:
            case KeyEvent.KEYCODE_BUTTON_A:
                mLibVLC.playerNavigate(LibVLC.INPUT_NAV_ACTIVATE);
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        setSurfaceLayout(mVideoWidth, mVideoHeight, mVideoVisibleWidth, mVideoVisibleHeight, mSarNum, mSarDen);
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void setSurfaceLayout(int width, int height, int visible_width, int visible_height, int sar_num, int sar_den) {
        if (width * height == 0)
            return;

        // store video size
        mVideoHeight = height;
        mVideoWidth = width;
        mVideoVisibleHeight = visible_height;
        mVideoVisibleWidth  = visible_width;
        mSarNum = sar_num;
        mSarDen = sar_den;
        Message msg = mHandler.obtainMessage(SURFACE_LAYOUT);
        mHandler.sendMessage(msg);
    }

    @Override
    public int configureSurface(final Surface surface, final int width, final int height, final int hal) {
        if (LibVlcUtil.isICSOrLater() || surface == null)
            return -1;
        if (width * height == 0)
            return 0;
        Log.d(TAG, "configureSurface: " + width +"x"+height);

        final Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (mSurface == surface && mSurfaceHolder != null) {
                    if (hal != 0)
                        mSurfaceHolder.setFormat(hal);
                    mSurfaceHolder.setFixedSize(width, height);
                } else if (mSubtitleSurface == surface && mSubtitlesSurfaceHolder != null) {
                    if (hal != 0)
                        mSubtitlesSurfaceHolder.setFormat(hal);
                    mSubtitlesSurfaceHolder.setFixedSize(width, height);
                }

                synchronized (surface) {
                    surface.notifyAll();
                }
            }
        });

        try {
            synchronized (surface) {
                surface.wait();
            }
        } catch (InterruptedException e) {
            return 0;
        }
        return 1;
    }

    /**
     * Lock screen rotation
     */
    private void lockScreen() {
        if(mScreenOrientation == ActivityInfo.SCREEN_ORIENTATION_SENSOR) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2)
                setRequestedOrientation(14 /* SCREEN_ORIENTATION_LOCKED */);
            else
                setRequestedOrientation(getScreenOrientation());
            mScreenOrientationLock = getScreenOrientation();
        }
        showInfo(R.string.locked, 1000);
        mLock.setBackgroundResource(R.drawable.ic_locked);
        mTime.setEnabled(false);
        mSeekbar.setEnabled(false);
        mLength.setEnabled(false);
        hideOverlay(true);
    }

    /**
     * Remove screen lock
     */
    private void unlockScreen() {
        if(mScreenOrientation == ActivityInfo.SCREEN_ORIENTATION_SENSOR)
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        showInfo(R.string.unlocked, 1000);
        mLock.setBackgroundResource(R.drawable.ic_lock);
        mTime.setEnabled(true);
        mSeekbar.setEnabled(true);
        mLength.setEnabled(true);
        mShowing = false;
        showOverlay();
    }

    /**
     * Show text in the info view for "duration" milliseconds
     * @param text
     * @param duration
     */
    private void showInfo(String text, int duration) {
        mInfo.setVisibility(View.VISIBLE);
        mInfo.setText(text);
        mHandler.removeMessages(FADE_OUT_INFO);
        mHandler.sendEmptyMessageDelayed(FADE_OUT_INFO, duration);
    }

    private void showInfo(int textid, int duration) {
        mInfo.setVisibility(View.VISIBLE);
        mInfo.setText(textid);
        mHandler.removeMessages(FADE_OUT_INFO);
        mHandler.sendEmptyMessageDelayed(FADE_OUT_INFO, duration);
    }

    /**
     * Show text in the info view
     * @param text
     */
    private void showInfo(String text) {
        mHandler.removeMessages(FADE_OUT_INFO);
        mInfo.setVisibility(View.VISIBLE);
        mInfo.setText(text);
        hideInfo();
    }

    /**
     * hide the info view with "delay" milliseconds delay
     * @param delay
     */
    private void hideInfo(int delay) {
        mHandler.sendEmptyMessageDelayed(FADE_OUT_INFO, delay);
    }

    /**
     * hide the info view
     */
    private void hideInfo() {
        hideInfo(0);
    }

    private void fadeOutInfo() {
        if (mInfo.getVisibility() == View.VISIBLE)
            mInfo.startAnimation(AnimationUtils.loadAnimation(
                    VideoPlayerActivity.this, android.R.anim.fade_out));
        mInfo.setVisibility(View.INVISIBLE);
    }

    @TargetApi(Build.VERSION_CODES.FROYO)
    private int changeAudioFocus(boolean acquire) {
        if(!LibVlcUtil.isFroyoOrLater()) // NOP if not supported
            return AudioManager.AUDIOFOCUS_REQUEST_GRANTED;

        if (mAudioFocusListener == null) {
            mAudioFocusListener = new OnAudioFocusChangeListener() {
                @Override
                public void onAudioFocusChange(int focusChange) {
                    /*
                     * Pause playback during alerts and notifications
                     */
                    switch (focusChange)
                    {
                        case AudioManager.AUDIOFOCUS_LOSS:
                        case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                        case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                            if (mLibVLC.isPlaying())
                                mLibVLC.pause();
                            break;
                        case AudioManager.AUDIOFOCUS_GAIN:
                        case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT:
                        case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK:
                            if (!mLibVLC.isPlaying())
                                mLibVLC.play();
                            break;
                    }
                }
            };
        }

        int result;
        if(acquire) {
            result = mAudioManager.requestAudioFocus(mAudioFocusListener,
                    AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
            mAudioManager.setParameters("bgm_state=true");
        }
        else {
            if (mAudioManager != null) {
                result = mAudioManager.abandonAudioFocus(mAudioFocusListener);
                mAudioManager.setParameters("bgm_state=false");
            }
            else
                result = AudioManager.AUDIOFOCUS_REQUEST_FAILED;
        }

        return result;
    }

    /**
     *  Handle libvlc asynchronous events
     */
    private final Handler eventHandler = new VideoPlayerEventHandler(this);

    private static class VideoPlayerEventHandler extends WeakHandler<VideoPlayerActivity> {
        public VideoPlayerEventHandler(VideoPlayerActivity owner) {
            super(owner);
        }

        @Override
        public void handleMessage(Message msg) {
            VideoPlayerActivity activity = getOwner();
            if(activity == null) return;
            // Do not handle events if we are leaving the VideoPlayerActivity
            if (activity.mSwitchingView) return;

            switch (msg.getData().getInt("event")) {
                case EventHandler.MediaParsedChanged:
                    Log.i(TAG, "MediaParsedChanged");
                    activity.updateNavStatus();
                    if (!activity.mHasMenu && activity.mLibVLC.getVideoTracksCount() < 1) {
                        Log.i(TAG, "No video track, open in audio mode");
                        activity.switchToAudioMode();
                    }
                    break;
                case EventHandler.MediaPlayerPlaying:
                    Log.i(TAG, "MediaPlayerPlaying");
                    activity.stopLoadingAnimation();
                    activity.showOverlay();
                    /** FIXME: update the track list when it changes during the
                     *  playback. (#7540) */
                    activity.setESTrackLists(true);
                    activity.setESTracks();
                    activity.changeAudioFocus(true);
                    activity.updateNavStatus();
                    break;
                case EventHandler.MediaPlayerPaused:
                    Log.i(TAG, "MediaPlayerPaused");
                    break;
                case EventHandler.MediaPlayerStopped:
                    Log.i(TAG, "MediaPlayerStopped");
                    activity.changeAudioFocus(false);
                    break;
                case EventHandler.MediaPlayerEndReached:
                    Log.i(TAG, "MediaPlayerEndReached");
                    activity.changeAudioFocus(false);
                    activity.endReached();
                    break;
                case EventHandler.MediaPlayerVout:
                    activity.updateNavStatus();
                    if (!activity.mHasMenu)
                        activity.handleVout(msg);
                    break;
                case EventHandler.MediaPlayerPositionChanged:
                    if (!activity.mCanSeek)
                        activity.mCanSeek = true;
                    //don't spam the logs
                    break;
                case EventHandler.MediaPlayerEncounteredError:
                    Log.i(TAG, "MediaPlayerEncounteredError");
                    activity.encounteredError();
                    break;
                case EventHandler.HardwareAccelerationError:
                    Log.i(TAG, "HardwareAccelerationError");
                    activity.handleHardwareAccelerationError();
                    break;
                case EventHandler.MediaPlayerTimeChanged:
                    // avoid useless error logs
                    break;
                default:
                    Log.e(TAG, String.format("Event not handled (0x%x)", msg.getData().getInt("event")));
                    break;
            }
            activity.updateOverlayPausePlay();
        }
    };

    /**
     * Handle resize of the surface and the overlay
     */
    private final Handler mHandler = new VideoPlayerHandler(this);

    private static class VideoPlayerHandler extends WeakHandler<VideoPlayerActivity> {
        public VideoPlayerHandler(VideoPlayerActivity owner) {
            super(owner);
        }

        @Override
        public void handleMessage(Message msg) {
            VideoPlayerActivity activity = getOwner();
            if(activity == null) // WeakReference could be GC'ed early
                return;

            switch (msg.what) {
                case FADE_OUT:
                    activity.hideOverlay(false);
                    break;
                case SHOW_PROGRESS:
                    int pos = activity.setOverlayProgress();
                    if (activity.canShowProgress()) {
                        msg = obtainMessage(SHOW_PROGRESS);
                        sendMessageDelayed(msg, 1000 - (pos % 1000));
                    }
                    break;
                case SURFACE_LAYOUT:
                    activity.changeSurfaceLayout();
                    break;
                case FADE_OUT_INFO:
                    activity.fadeOutInfo();
                    break;
                case AUDIO_SERVICE_CONNECTION_SUCCESS:
                    activity.startPlayback();
                    break;
                case AUDIO_SERVICE_CONNECTION_FAILED:
                    activity.finish();
                    break;
            }
        }
    };

    private boolean canShowProgress() {
        return !mDragging && mShowing && mLibVLC.isPlaying();
    }

    private void endReached() {
        if(mLibVLC.getMediaList().expandMedia(savedIndexPosition) == 0) {
            Log.d(TAG, "Found a video playlist, expanding it");
            eventHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    loadMedia();
                }
            }, 1000);
        } else {
            /* Exit player when reaching the end */
            mEndReached = true;
            finish();
        }
    }

    private void encounteredError() {
        /* Encountered Error, exit player with a message */
        AlertDialog dialog = new AlertDialog.Builder(VideoPlayerActivity.this)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        finish();
                    }
                })
                .setTitle(R.string.encountered_error_title)
                .setMessage(R.string.encountered_error_message)
                .create();
        dialog.show();
    }

    public void eventHardwareAccelerationError() {
        EventHandler em = EventHandler.getInstance();
        em.callback(EventHandler.HardwareAccelerationError, new Bundle());
    }

    private void handleHardwareAccelerationError() {
        mHardwareAccelerationError = true;
        if (mSwitchingView)
            return;
        mLibVLC.stop();
        AlertDialog dialog = new AlertDialog.Builder(VideoPlayerActivity.this)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        mDisabledHardwareAcceleration = true;
                        mPreviousHardwareAccelerationMode = mLibVLC.getHardwareAcceleration();
                        mLibVLC.setHardwareAcceleration(LibVLC.HW_ACCELERATION_DISABLED);
                        loadMedia();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        finish();
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        finish();
                    }
                })
                .setTitle(R.string.hardware_acceleration_error_title)
                .setMessage(R.string.hardware_acceleration_error_message)
                .create();
        if(!isFinishing())
            dialog.show();
    }

    private void handleVout(Message msg) {
        if (msg.getData().getInt("data") == 0 && !mEndReached) {
            /* Video track lost, open in audio mode */
            Log.i(TAG, "Video track lost, switching to audio");
            mSwitchingView = true;
            finish();
        }
    }

    private void switchToAudioMode() {
        if (mHardwareAccelerationError)
            return;
        mSwitchingView = true;
        // Show the MainActivity if it is not in background.
        if (getIntent().getAction() != null
                && getIntent().getAction().equals(Intent.ACTION_VIEW)) {
            Intent i = new Intent(this, MainActivity.class);
            startActivity(i);
        }
        finish();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void changeSurfaceLayout() {
        int sw;
        int sh;

        // get screen size
        if (mPresentation == null) {
            sw = getWindow().getDecorView().getWidth();
            sh = getWindow().getDecorView().getHeight();
        } else {
            sw = mPresentation.getWindow().getDecorView().getWidth();
            sh = mPresentation.getWindow().getDecorView().getHeight();
        }

        double dw = sw, dh = sh;
        boolean isPortrait;

        if (mPresentation == null) {
            // getWindow().getDecorView() doesn't always take orientation into account, we have to correct the values
            isPortrait = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
        } else {
            isPortrait = false;
        }

        if (sw > sh && isPortrait || sw < sh && !isPortrait) {
            dw = sh;
            dh = sw;
        }

        // sanity check
        if (dw * dh == 0 || mVideoWidth * mVideoHeight == 0) {
            Log.e(TAG, "Invalid surface size");
            return;
        }

        // compute the aspect ratio
        double ar, vw;
        if (mSarDen == mSarNum) {
            /* No indication about the density, assuming 1:1 */
            vw = mVideoVisibleWidth;
            ar = (double)mVideoVisibleWidth / (double)mVideoVisibleHeight;
        } else {
            /* Use the specified aspect ratio */
            vw = mVideoVisibleWidth * (double)mSarNum / mSarDen;
            ar = vw / mVideoVisibleHeight;
        }

        // compute the display aspect ratio
        double dar = dw / dh;

        switch (mCurrentSize) {
            case SURFACE_BEST_FIT:
                if (dar < ar)
                    dh = dw / ar;
                else
                    dw = dh * ar;
                break;
            case SURFACE_FIT_HORIZONTAL:
                dh = dw / ar;
                break;
            case SURFACE_FIT_VERTICAL:
                dw = dh * ar;
                break;
            case SURFACE_FILL:
                break;
            case SURFACE_16_9:
                ar = 16.0 / 9.0;
                if (dar < ar)
                    dh = dw / ar;
                else
                    dw = dh * ar;
                break;
            case SURFACE_4_3:
                ar = 4.0 / 3.0;
                if (dar < ar)
                    dh = dw / ar;
                else
                    dw = dh * ar;
                break;
            case SURFACE_ORIGINAL:
                dh = mVideoVisibleHeight;
                dw = vw;
                break;
        }

        SurfaceView surface;
        SurfaceView subtitlesSurface;
        FrameLayout surfaceFrame;

        if (mPresentation == null) {
            surface = mSurfaceView;
            subtitlesSurface = mSubtitlesSurfaceView;
            surfaceFrame = mSurfaceFrame;
        } else {
            surface = mPresentation.mSurfaceView;
            subtitlesSurface = mPresentation.mSubtitlesSurfaceView;
            surfaceFrame = mPresentation.mSurfaceFrame;
        }

        // set display size
        LayoutParams lp = surface.getLayoutParams();
        lp.width  = (int) Math.ceil(dw * mVideoWidth / mVideoVisibleWidth);
        lp.height = (int) Math.ceil(dh * mVideoHeight / mVideoVisibleHeight);
        surface.setLayoutParams(lp);
        subtitlesSurface.setLayoutParams(lp);

        // set frame size (crop if necessary)
        lp = surfaceFrame.getLayoutParams();
        lp.width = (int) Math.floor(dw);
        lp.height = (int) Math.floor(dh);
        surfaceFrame.setLayoutParams(lp);

        surface.invalidate();
        subtitlesSurface.invalidate();
    }

    /**
     * show/hide the overlay
     */

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mIsLocked) {
            // locked, only handle show/hide & ignore all actions
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (!mShowing) {
                    showOverlay();
                } else {
                    hideOverlay(true);
                }
            }
            return false;
        }

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
        mSurfaceView.getLocationOnScreen(offset);
        int xTouch = Math.round((event.getRawX() - offset[0]) * mVideoWidth / mSurfaceView.getWidth());
        int yTouch = Math.round((event.getRawY() - offset[1]) * mVideoHeight / mSurfaceView.getHeight());

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
                // Mouse events for the core
                LibVLC.sendMouseEvent(MotionEvent.ACTION_MOVE, 0, xTouch, yTouch);

                // No volume/brightness action if coef < 2 or a secondary display is connected
                //TODO : Volume action when a secondary display is connected
                if (coef > 2 && mPresentation == null) {
                    mTouchY = event.getRawY();
                    mTouchX = event.getRawX();
                    // Volume (Up or Down - Right side)
                    if (!mEnableBrightnessGesture || (int)mTouchX > (screen.widthPixels / 2)){
                        doVolumeTouch(y_changed);
                    }
                    // Brightness (Up or Down - Left side)
                    if (mEnableBrightnessGesture && (int)mTouchX < (screen.widthPixels / 2)){
                        doBrightnessTouch(y_changed);
                    }
                } else {
                    // Seek (Right or Left move)
                    doSeekTouch(coef, xgesturesize, false);
                }
                if (mTouchAction != TOUCH_NONE && mOverlayTimeout != OVERLAY_INFINITE)
                    showOverlayTimeout(OVERLAY_INFINITE);
                break;

            case MotionEvent.ACTION_UP:
                // Mouse events for the core
                LibVLC.sendMouseEvent(MotionEvent.ACTION_UP, 0, xTouch, yTouch);

                if (mTouchAction == TOUCH_NONE) {
                    if (!mShowing) {
                        showOverlay();
                    } else {
                        hideOverlay(true);
                    }
                } else {
                    // We were in gesture mode, re-init the overlay timeout
                    showOverlay(true);
                }
                // Seek
                doSeekTouch(coef, xgesturesize, true);
                break;
        }
        return mTouchAction != TOUCH_NONE;
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

        if (length > 0)
            //Show the jump's size
            showInfo(String.format("%s%s (%s)",
                    jump >= 0 ? "+" : "",
                    Strings.millisToString(jump),
                    Strings.millisToString(time + jump)), 1000);
        else
            showInfo(R.string.unseekable_stream, 1000);
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
        showInfo(getString(R.string.volume) + '\u00A0' + Integer.toString(vol),1000);
    }

    private void updateMute () {
        if (!mMute) {
            mVolSave = Float.floatToIntBits(mVol);
            mMute = true;
            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
            showInfo(R.string.sound_off,1000);
        } else {
            mVol = mVolSave;
            mMute = false;
            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, Float.floatToIntBits(mVol), 0);
            showInfo(R.string.sound_on,1000);
        }
    }

    @TargetApi(android.os.Build.VERSION_CODES.FROYO)
    private void initBrightnessTouch() {
        float brightnesstemp = 0.6f;
        // Initialize the layoutParams screen brightness
        try {
            if (LibVlcUtil.isFroyoOrLater() &&
                    Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE) == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
                Settings.System.putInt(getContentResolver(),
                        Settings.System.SCREEN_BRIGHTNESS_MODE,
                        Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
                mRestoreAutoBrightness = android.provider.Settings.System.getInt(getContentResolver(),
                        android.provider.Settings.System.SCREEN_BRIGHTNESS) / 255.0f;
            } else {
                brightnesstemp = android.provider.Settings.System.getInt(getContentResolver(),
                        android.provider.Settings.System.SCREEN_BRIGHTNESS) / 255.0f;
            }
        } catch (SettingNotFoundException e) {
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
        showInfo(getString(R.string.brightness) + '\u00A0' + Math.round(lp.screenBrightness*15),1000);
    }

    /**
     * handle changes of the seekbar (slicer)
     */
    private final OnSeekBarChangeListener mSeekListener = new OnSeekBarChangeListener() {

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            mDragging = true;
            showOverlayTimeout(OVERLAY_INFINITE);
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            mDragging = false;
            showOverlay(true);
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser && mCanSeek) {
                mLibVLC.setTime(progress);
                setOverlayProgress();
                mTime.setText(Strings.millisToString(progress));
                showInfo(Strings.millisToString(progress));
            }

        }
    };

    /**
     *
     */
    private final OnClickListener mAudioTrackListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            selectAudioTrack();
        }
    };

    private void selectAudioTrack() {
        if (mAudioTracksList == null) return;

        final String[] arrList = new String[mAudioTracksList.size()];
        int i = 0;
        int listPosition = 0;
        for(Map.Entry<Integer,String> entry : mAudioTracksList.entrySet()) {
            arrList[i] = entry.getValue();
            // map the track position to the list position
            if(entry.getKey() == mLibVLC.getAudioTrack())
                listPosition = i;
            i++;
        }
        AlertDialog dialog = new AlertDialog.Builder(VideoPlayerActivity.this)
                .setTitle(R.string.track_audio)
                .setSingleChoiceItems(arrList, listPosition, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int listPosition) {
                        int trackID = -1;
                        // Reverse map search...
                        for(Map.Entry<Integer, String> entry : mAudioTracksList.entrySet()) {
                            if(arrList[listPosition].equals(entry.getValue())) {
                                trackID = entry.getKey();
                                break;
                            }
                        }
                        if(trackID < 0) return;

                        MediaDatabase.getInstance().updateMedia(
                                mLocation,
                                MediaDatabase.mediaColumn.MEDIA_AUDIOTRACK,
                                trackID);
                        mLibVLC.setAudioTrack(trackID);
                        dialog.dismiss();
                    }
                })
                .create();
        dialog.setCanceledOnTouchOutside(true);
        dialog.setOwnerActivity(VideoPlayerActivity.this);
        dialog.show();
    }

    /**
     *
     */
    private final OnClickListener mSubtitlesListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            selectSubtitles();
        }
    };

    private void selectSubtitles() {
        final String[] arrList = new String[mSubtitleTracksList.size()];
        int i = 0;
        int listPosition = 0;
        for(Map.Entry<Integer,String> entry : mSubtitleTracksList.entrySet()) {
            arrList[i] = entry.getValue();
            // map the track position to the list position
            if(entry.getKey() == mLibVLC.getSpuTrack())
                listPosition = i;
            i++;
        }

        AlertDialog dialog = new AlertDialog.Builder(VideoPlayerActivity.this)
                .setTitle(R.string.track_text)
                .setSingleChoiceItems(arrList, listPosition, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int listPosition) {
                        int trackID = -2;
                        // Reverse map search...
                        for(Map.Entry<Integer, String> entry : mSubtitleTracksList.entrySet()) {
                            if(arrList[listPosition].equals(entry.getValue())) {
                                trackID = entry.getKey();
                                break;
                            }
                        }
                        if(trackID < -1) return;

                        MediaDatabase.getInstance().updateMedia(
                                mLocation,
                                MediaDatabase.mediaColumn.MEDIA_SPUTRACK,
                                trackID);
                        mLibVLC.setSpuTrack(trackID);
                        dialog.dismiss();
                    }
                })
                .create();
        dialog.setCanceledOnTouchOutside(true);
        dialog.setOwnerActivity(VideoPlayerActivity.this);
        dialog.show();
    }

    private final OnClickListener mNavMenuListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            showNavMenu();
        }
    };

    private void showNavMenu() {
        /* Try to return to the menu. */
        /* FIXME: not working correctly in all cases */
        mLibVLC.setTitle(0);
    }

    /**
     *
     */
    private final OnClickListener mPlayPauseListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            doPlayPause();
        }
    };

    private final void doPlayPause() {
        if (mLibVLC.isPlaying()) {
            pause();
            showOverlayTimeout(OVERLAY_INFINITE);
        } else {
            play();
            showOverlayTimeout(OVERLAY_TIMEOUT);
        }
    }

    /**
     *
     */
    private final OnClickListener mBackwardListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            seek(-10000);
        }
    };

    /**
     *
     */
    private final OnClickListener mForwardListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            seek(10000);
        }
    };

    public void seek(int delta) {
        // unseekable stream
        if(mLibVLC.getLength() <= 0 || !mCanSeek) return;

        long position = mLibVLC.getTime() + delta;
        if (position < 0) position = 0;
        mLibVLC.setTime(position);
        showOverlay();
    }

    /**
     *
     */
    private final OnClickListener mLockListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            if (mIsLocked) {
                mIsLocked = false;
                unlockScreen();
            } else {
                mIsLocked = true;
                lockScreen();
            }
        }
    };

    /**
     *
     */
    private final OnClickListener mSizeListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            resizeVideo();
        }
    };

    private void resizeVideo() {
        if (mCurrentSize < SURFACE_ORIGINAL) {
            mCurrentSize++;
        } else {
            mCurrentSize = 0;
        }
        changeSurfaceLayout();
        switch (mCurrentSize) {
            case SURFACE_BEST_FIT:
                showInfo(R.string.surface_best_fit, 1000);
                break;
            case SURFACE_FIT_HORIZONTAL:
                showInfo(R.string.surface_fit_horizontal, 1000);
                break;
            case SURFACE_FIT_VERTICAL:
                showInfo(R.string.surface_fit_vertical, 1000);
                break;
            case SURFACE_FILL:
                showInfo(R.string.surface_fill, 1000);
                break;
            case SURFACE_16_9:
                showInfo("16:9", 1000);
                break;
            case SURFACE_4_3:
                showInfo("4:3", 1000);
                break;
            case SURFACE_ORIGINAL:
                showInfo(R.string.surface_original, 1000);
                break;
        }
        showOverlay();
    }

    private final OnClickListener mRemainingTimeListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            mDisplayRemainingTime = !mDisplayRemainingTime;
            showOverlay();
        }
    };

    /**
     * attach and disattach surface to the lib
     */
    private final SurfaceHolder.Callback mSurfaceCallback = new Callback() {
        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            if(mLibVLC != null) {
                final Surface newSurface = holder.getSurface();
                if (mSurface != newSurface) {
                    if (mSurface != null) {
                        synchronized (mSurface) {
                            mSurface.notifyAll();
                        }
                    }
                    mSurface = newSurface;
                    Log.d(TAG, "surfaceChanged: " + mSurface);
                    mLibVLC.attachSurface(mSurface, VideoPlayerActivity.this);
                }
            }
        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            Log.d(TAG, "surfaceDestroyed");
            if(mLibVLC != null) {
                synchronized (mSurface) {
                    mSurface.notifyAll();
                }
                mSurface = null;
                mLibVLC.detachSurface();
            }
        }
    };

    private final SurfaceHolder.Callback mSubtitlesSurfaceCallback = new Callback() {
        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            if(mLibVLC != null) {
                final Surface newSurface = holder.getSurface();
                if (mSubtitleSurface != newSurface) {
                    if (mSubtitleSurface != null) {
                        synchronized (mSubtitleSurface) {
                            mSubtitleSurface.notifyAll();
                        }
                    }
                    mSubtitleSurface = newSurface;
                    mLibVLC.attachSubtitlesSurface(mSubtitleSurface);
                }
            }
        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            if(mLibVLC != null) {
                synchronized (mSubtitleSurface) {
                    mSubtitleSurface.notifyAll();
                }
                mSubtitleSurface = null;
                mLibVLC.detachSubtitlesSurface();
            }
        }
    };

    /**
     * show overlay
     * @param forceCheck: adjust the timeout in function of playing state
     */
    private void showOverlay(boolean forceCheck) {
        if (forceCheck)
            mOverlayTimeout = 0;
        showOverlayTimeout(0);
    }

    /**
     * show overlay with the previous timeout value
     */
    private void showOverlay() {
        showOverlay(false);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void setActionBarVisibility(boolean show) {
        if (show)
            mActionBar.show();
        else
            mActionBar.hide();
    }

    /**
     * show overlay
     */
    private void showOverlayTimeout(int timeout) {
        if (timeout != 0)
            mOverlayTimeout = timeout;
        if (mOverlayTimeout == 0)
            mOverlayTimeout = mLibVLC.isPlaying() ? OVERLAY_TIMEOUT : OVERLAY_INFINITE;
        if (mIsNavMenu){
            mShowing = true;
            return;
        }
        mHandler.sendEmptyMessage(SHOW_PROGRESS);
        if (!mShowing) {
            mShowing = true;
            if (!mIsLocked) {
                if (mOverlayUseStatusBar)
                    setActionBarVisibility(true);
                else if (mOverlayHeader != null)
                    mOverlayHeader.setVisibility(View.VISIBLE);
                mOverlayOption.setVisibility(View.VISIBLE);
                mPlayPause.setVisibility(View.VISIBLE);
                mMenu.setVisibility(View.VISIBLE);
                dimStatusBar(false);
            }
            mOverlayProgress.setVisibility(View.VISIBLE);
            if (mPresentation != null) mOverlayBackground.setVisibility(View.VISIBLE);
        }
        mHandler.removeMessages(FADE_OUT);
        if (mOverlayTimeout != OVERLAY_INFINITE)
            mHandler.sendMessageDelayed(mHandler.obtainMessage(FADE_OUT), mOverlayTimeout);
        updateOverlayPausePlay();
    }


    /**
     * hider overlay
     */
    private void hideOverlay(boolean fromUser) {
        if (mShowing) {
            mHandler.removeMessages(FADE_OUT);
            mHandler.removeMessages(SHOW_PROGRESS);
            Log.i(TAG, "remove View!");
            if (mOverlayTips != null) mOverlayTips.setVisibility(View.INVISIBLE);
            if (!fromUser && !mIsLocked) {
                if (mOverlayHeader != null)
                    mOverlayHeader.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_out));
                mOverlayOption.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_out));
                mOverlayProgress.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_out));
                mPlayPause.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_out));
                mMenu.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_out));
            }
            if (mPresentation != null) {
                mOverlayBackground.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_out));
                mOverlayBackground.setVisibility(View.INVISIBLE);
            }
            if (mOverlayUseStatusBar)
                setActionBarVisibility(false);
            else if (mOverlayHeader != null)
                mOverlayHeader.setVisibility(View.INVISIBLE);
            mOverlayOption.setVisibility(View.INVISIBLE);
            mOverlayProgress.setVisibility(View.INVISIBLE);
            mPlayPause.setVisibility(View.INVISIBLE);
            mMenu.setVisibility(View.INVISIBLE);
            mShowing = false;
            dimStatusBar(true);
        } else if (!fromUser) {
            /*
             * Try to hide the Nav Bar again.
             * It seems that you can't hide the Nav Bar if you previously
             * showed it in the last 1-2 seconds.
             */
            dimStatusBar(true);
        }
    }

    /**
     * Dim the status bar and/or navigation icons when needed on Android 3.x.
     * Hide it on Android 4.0 and later
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void dimStatusBar(boolean dim) {
        if (!LibVlcUtil.isHoneycombOrLater() || !AndroidDevices.hasNavBar() || mIsNavMenu)
            return;
        int layout = 0;
        if (!AndroidDevices.hasCombBar() && LibVlcUtil.isJellyBeanOrLater())
            layout = View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
        if (mOverlayUseStatusBar)
            layout |= View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;

        int visibility = layout;
        if (dim) {
            visibility |= View.SYSTEM_UI_FLAG_LOW_PROFILE;
            if (!AndroidDevices.hasCombBar()) {
                visibility |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
                if (LibVlcUtil.isKitKatOrLater())
                    visibility |= View.SYSTEM_UI_FLAG_IMMERSIVE;
                if (mOverlayUseStatusBar)
                    visibility |= View.SYSTEM_UI_FLAG_FULLSCREEN;
            }
        } else {
            visibility |= View.SYSTEM_UI_FLAG_VISIBLE;
        }
        getWindow().getDecorView().setSystemUiVisibility(visibility);
    }

    private void updateOverlayPausePlay() {
        if (mLibVLC == null)
            return;

        if (mPresentation == null)
            mPlayPause.setBackgroundResource(mLibVLC.isPlaying() ? R.drawable.ic_pause_circle
                    : R.drawable.ic_play_circle);
        else
            mPlayPause.setBackgroundResource(mLibVLC.isPlaying() ? R.drawable.ic_pause_circle_big_o
                    : R.drawable.ic_play_circle_big_o);
    }

    /**
     * update the overlay
     */
    private int setOverlayProgress() {
        if (mLibVLC == null) {
            return 0;
        }
        int time = (int) mLibVLC.getTime();
        int length = (int) mLibVLC.getLength();
        if (length == 0) {
            Media media = MediaDatabase.getInstance().getMedia(mLocation);
            if (media != null)
                length = (int) media.getLength();
        }

        // Update all view elements
        boolean isSeekable = mEnableJumpButtons && length > 0;
        mBackward.setVisibility(isSeekable ? View.VISIBLE : View.GONE);
        mForward.setVisibility(isSeekable ? View.VISIBLE : View.GONE);
        mSeekbar.setMax(length);
        mSeekbar.setProgress(time);
        if (mSysTime != null)
            mSysTime.setText(DateFormat.getTimeFormat(this).format(new Date(System.currentTimeMillis())));
        if (time >= 0) mTime.setText(Strings.millisToString(time));
        if (length >= 0) mLength.setText(mDisplayRemainingTime && length > 0
                ? "- " + Strings.millisToString(length - time)
                : Strings.millisToString(length));

        return time;
    }

    private void setESTracks() {
        if (mLastAudioTrack >= 0) {
            mLibVLC.setAudioTrack(mLastAudioTrack);
            mLastAudioTrack = -1;
        }
        if (mLastSpuTrack >= -1) {
            mLibVLC.setSpuTrack(mLastSpuTrack);
            mLastSpuTrack = -2;
        }
    }

    private void setESTrackLists(boolean force) {
        if(mAudioTracksList == null || force) {
            if (mLibVLC.getAudioTracksCount() > 2) {
                mAudioTracksList = mLibVLC.getAudioTrackDescription();
                mAudioTrack.setOnClickListener(mAudioTrackListener);
                mAudioTrack.setVisibility(View.VISIBLE);
            }
            else {
                mAudioTrack.setVisibility(View.GONE);
                mAudioTrack.setOnClickListener(null);
            }
        }
        if (mSubtitleTracksList == null || force) {
            if (mLibVLC.getSpuTracksCount() > 0) {
                mSubtitleTracksList = mLibVLC.getSpuTrackDescription();
                mSubtitle.setOnClickListener(mSubtitlesListener);
                mSubtitle.setVisibility(View.VISIBLE);
            }
            else {
                mSubtitle.setVisibility(View.GONE);
                mSubtitle.setOnClickListener(null);
            }
        }
    }


    /**
     *
     */
    private void play() {
        mLibVLC.play();
        mSurfaceView.setKeepScreenOn(true);
    }

    /**
     *
     */
    private void pause() {
        mLibVLC.pause();
        mSurfaceView.setKeepScreenOn(false);
    }

    /**
     * External extras:
     * - position (long) - position of the video to start with (in ms)
     */
    @SuppressWarnings({ "unchecked" })
    private void loadMedia() {
        mLocation = null;
        String title = getResources().getString(R.string.title);
        boolean dontParse = false;
        boolean fromStart = false;
        Uri data;
        String itemTitle = null;
        int itemPosition = -1; // Index in the media list as passed by AudioServer (used only for vout transition internally)
        long intentPosition = -1; // position passed in by intent (ms)

        if (getIntent().getAction() != null
                && getIntent().getAction().equals(Intent.ACTION_VIEW)) {
            /* Started from external application 'content' */
            data = getIntent().getData();
            if (data != null
                    && data.getScheme() != null
                    && data.getScheme().equals("content")) {


                // Mail-based apps - download the stream to a temporary file and play it
                if(data.getHost().equals("com.fsck.k9.attachmentprovider")
                        || data.getHost().equals("gmail-ls")) {
                    try {
                        Cursor cursor = getContentResolver().query(data,
                                new String[]{MediaStore.MediaColumns.DISPLAY_NAME}, null, null, null);
                        if (cursor != null) {
                            cursor.moveToFirst();
                            String filename = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME));
                            cursor.close();
                            Log.i(TAG, "Getting file " + filename + " from content:// URI");

                            InputStream is = getContentResolver().openInputStream(data);
                            OutputStream os = new FileOutputStream(Environment.getExternalStorageDirectory().getPath() + "/Download/" + filename);
                            byte[] buffer = new byte[1024];
                            int bytesRead = 0;
                            while((bytesRead = is.read(buffer)) >= 0) {
                                os.write(buffer, 0, bytesRead);
                            }
                            os.close();
                            is.close();
                            mLocation = LibVLC.PathToURI(Environment.getExternalStorageDirectory().getPath() + "/Download/" + filename);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Couldn't download file from mail URI");
                        encounteredError();
                    }
                }
                // Media or MMS URI
                else {
                    try {
                        Cursor cursor = getContentResolver().query(data,
                                new String[]{ MediaStore.Video.Media.DATA }, null, null, null);
                        if (cursor != null) {
                            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
                            if (cursor.moveToFirst())
                                mLocation = LibVLC.PathToURI(cursor.getString(column_index));
                            cursor.close();
                        }
                        // other content-based URI (probably file pickers)
                        else {
                            mLocation = data.getPath();
                        }
                    } catch (Exception e) {
                        mLocation = data.getPath();
                        if (!mLocation.startsWith("file://"))
                            mLocation = "file://"+mLocation;
                        Log.e(TAG, "Couldn't read the file from media or MMS");
                    }
                }
            } /* External application */
            else if (getIntent().getDataString() != null) {
                // Plain URI
                mLocation = getIntent().getDataString();
                // Remove VLC prefix if needed
                if (mLocation.startsWith("vlc://")) {
                    mLocation = mLocation.substring(6);
                }
                // Decode URI
                if (!mLocation.contains("/")){
                    try {
                        mLocation = URLDecoder.decode(mLocation,"UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        Log.w(TAG, "UnsupportedEncodingException while decoding MRL " + mLocation);
                    }
                }
            } else {
                Log.e(TAG, "Couldn't understand the intent");
                encounteredError();
            }

            // Try to get the position
            if(getIntent().getExtras() != null)
                intentPosition = getIntent().getExtras().getLong("position", -1);
        } /* ACTION_VIEW */
        /* Started from VideoListActivity */
        else if(getIntent().getAction() != null
                && getIntent().getAction().equals(PLAY_FROM_VIDEOGRID)
                && getIntent().getExtras() != null) {
            mLocation = getIntent().getExtras().getString("itemLocation");
            itemTitle = getIntent().getExtras().getString("itemTitle");
            dontParse = getIntent().getExtras().getBoolean("dontParse");
            fromStart = getIntent().getExtras().getBoolean("fromStart");
            itemPosition = getIntent().getExtras().getInt("itemPosition", -1);
        }

        mSurfaceView.setKeepScreenOn(true);

        if(mLibVLC == null)
            return;

        /* WARNING: hack to avoid a crash in mediacodec on KitKat.
         * Disable hardware acceleration if the media has a ts extension. */
        if (mLocation != null && LibVlcUtil.isKitKatOrLater()) {
            String locationLC = mLocation.toLowerCase(Locale.ENGLISH);
            if (locationLC.endsWith(".ts")
                    || locationLC.endsWith(".tts")
                    || locationLC.endsWith(".m2t")
                    || locationLC.endsWith(".mts")
                    || locationLC.endsWith(".m2ts")) {
                mDisabledHardwareAcceleration = true;
                mPreviousHardwareAccelerationMode = mLibVLC.getHardwareAcceleration();
                mLibVLC.setHardwareAcceleration(LibVLC.HW_ACCELERATION_DISABLED);
            }
        }

        /* Start / resume playback */
        if(dontParse && itemPosition >= 0) {
            // Provided externally from AudioService
            Log.d(TAG, "Continuing playback from AudioService at index " + itemPosition);
            savedIndexPosition = itemPosition;
            if(!mLibVLC.isPlaying()) {
                // AudioService-transitioned playback for item after sleep and resume
                mLibVLC.playIndex(savedIndexPosition);
                dontParse = false;
            }
            else {
                stopLoadingAnimation();
                showOverlay();
            }
            updateNavStatus();
        } else if (savedIndexPosition > -1) {
            AudioServiceController.getInstance().stop(); // Stop the previous playback.
            mLibVLC.setMediaList();
            mLibVLC.playIndex(savedIndexPosition);
        } else if (mLocation != null && mLocation.length() > 0 && !dontParse) {
            AudioServiceController.getInstance().stop(); // Stop the previous playback.
            mLibVLC.setMediaList();
            mLibVLC.getMediaList().add(new Media(mLibVLC, mLocation));
            savedIndexPosition = mLibVLC.getMediaList().size() - 1;
            mLibVLC.playIndex(savedIndexPosition);
        }
        mCanSeek = false;

        if (mLocation != null && mLocation.length() > 0 && !dontParse) {
            // restore last position
            Media media = MediaDatabase.getInstance().getMedia(mLocation);
            if(media != null) {
                // in media library
                if(media.getTime() > 0 && !fromStart)
                    mLibVLC.setTime(media.getTime());
                // Consume fromStart option after first use to prevent
                // restarting again when playback is paused.
                getIntent().putExtra("fromStart", false);

                mLastAudioTrack = media.getAudioTrack();
                mLastSpuTrack = media.getSpuTrack();
            } else {
                // not in media library
                long rTime = mSettings.getLong(PreferencesActivity.VIDEO_RESUME_TIME, -1);
                Editor editor = mSettings.edit();
                editor.putLong(PreferencesActivity.VIDEO_RESUME_TIME, -1);
                editor.commit();
                if(rTime > 0)
                    mLibVLC.setTime(rTime);

                if(intentPosition > 0)
                    mLibVLC.setTime(intentPosition);
            }

            // Get possible subtitles
            String subtitleList_serialized = mSettings.getString(PreferencesActivity.VIDEO_SUBTITLE_FILES, null);
            ArrayList<String> prefsList = new ArrayList<String>();
            if(subtitleList_serialized != null) {
                ByteArrayInputStream bis = new ByteArrayInputStream(subtitleList_serialized.getBytes());
                try {
                    ObjectInputStream ois = new ObjectInputStream(bis);
                    prefsList = (ArrayList<String>)ois.readObject();
                } catch(ClassNotFoundException e) {}
                catch (StreamCorruptedException e) {}
                catch (IOException e) {}
            }
            for(String x : prefsList){
                if(!mSubtitleSelectedFiles.contains(x))
                    mSubtitleSelectedFiles.add(x);
            }

            // Get the title
            try {
                title = URLDecoder.decode(mLocation, "UTF-8");
            } catch (UnsupportedEncodingException e) {
            } catch (IllegalArgumentException e) {
            }
            if (title.startsWith("file:")) {
                title = new File(title).getName();
                int dotIndex = title.lastIndexOf('.');
                if (dotIndex != -1)
                    title = title.substring(0, dotIndex);
            }
        } else if(itemTitle != null) {
            title = itemTitle;
        }
        mTitle.setText(title);
    }

    @SuppressWarnings("deprecation")
    private int getScreenRotation(){
        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO /* Android 2.2 has getRotation */) {
            try {
                Method m = display.getClass().getDeclaredMethod("getRotation");
                return (Integer) m.invoke(display);
            } catch (Exception e) {
                return Surface.ROTATION_0;
            }
        } else {
            return display.getOrientation();
        }
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    private int getScreenOrientation(){
        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        int rot = getScreenRotation();
        /*
         * Since getRotation() returns the screen's "natural" orientation,
         * which is not guaranteed to be SCREEN_ORIENTATION_PORTRAIT,
         * we have to invert the SCREEN_ORIENTATION value if it is "naturally"
         * landscape.
         */
        @SuppressWarnings("deprecation")
        boolean defaultWide = display.getWidth() > display.getHeight();
        if(rot == Surface.ROTATION_90 || rot == Surface.ROTATION_270)
            defaultWide = !defaultWide;
        if(defaultWide) {
            switch (rot) {
                case Surface.ROTATION_0:
                    return ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                case Surface.ROTATION_90:
                    return ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                case Surface.ROTATION_180:
                    // SCREEN_ORIENTATION_REVERSE_PORTRAIT only available since API
                    // Level 9+
                    return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO ? ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
                            : ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                case Surface.ROTATION_270:
                    // SCREEN_ORIENTATION_REVERSE_LANDSCAPE only available since API
                    // Level 9+
                    return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO ? ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT
                            : ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                default:
                    return 0;
            }
        } else {
            switch (rot) {
                case Surface.ROTATION_0:
                    return ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                case Surface.ROTATION_90:
                    return ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                case Surface.ROTATION_180:
                    // SCREEN_ORIENTATION_REVERSE_PORTRAIT only available since API
                    // Level 9+
                    return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO ? ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT
                            : ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                case Surface.ROTATION_270:
                    // SCREEN_ORIENTATION_REVERSE_LANDSCAPE only available since API
                    // Level 9+
                    return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO ? ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
                            : ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                default:
                    return 0;
            }
        }
    }

    public void showAdvancedOptions(View v) {
        CommonDialogs.advancedOptions(this, v, MenuType.Video);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void createPresentation() {
        if (mMediaRouter == null || mEnableCloneMode)
            return;

        // Get the current route and its presentation display.
        MediaRouter.RouteInfo route = mMediaRouter.getSelectedRoute(
                MediaRouter.ROUTE_TYPE_LIVE_VIDEO);

        Display presentationDisplay = route != null ? route.getPresentationDisplay() : null;

        if (presentationDisplay != null) {
            // Show a new presentation if possible.
            Log.i(TAG, "Showing presentation on display: " + presentationDisplay);
            mPresentation = new SecondaryDisplay(this, presentationDisplay);
            mPresentation.setOnDismissListener(mOnDismissListener);
            try {
                mPresentation.show();
            } catch (WindowManager.InvalidDisplayException ex) {
                Log.w(TAG, "Couldn't show presentation!  Display was removed in "
                        + "the meantime.", ex);
                mPresentation = null;
            }
        } else
            Log.i(TAG, "No secondary display detected");
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void removePresentation() {
        if (mMediaRouter == null)
            return;

        // Dismiss the current presentation if the display has changed.
        Log.i(TAG, "Dismissing presentation because the current route no longer "
                + "has a presentation display.");
        mLibVLC.pause(); // Stop sending frames to avoid a crash.
        finish(); //TODO restore the video on the new display instead of closing
        if (mPresentation != null) mPresentation.dismiss();
        mPresentation = null;
    }

    /**
     * Listens for when presentations are dismissed.
     */
    private final DialogInterface.OnDismissListener mOnDismissListener = new DialogInterface.OnDismissListener() {
        @Override
        public void onDismiss(DialogInterface dialog) {
            if (dialog == mPresentation) {
                Log.i(TAG, "Presentation was dismissed.");
                mPresentation = null;
            }
        }
    };

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private final class SecondaryDisplay extends Presentation {
        public final static String TAG = "VLC/SecondaryDisplay";

        private SurfaceView mSurfaceView;
        private SurfaceView mSubtitlesSurfaceView;
        private SurfaceHolder mSurfaceHolder;
        private SurfaceHolder mSubtitlesSurfaceHolder;
        private FrameLayout mSurfaceFrame;
        private LibVLC mLibVLC;

        public SecondaryDisplay(Context context, Display display) {
            super(context, display);
            if (context instanceof Activity) {
                setOwnerActivity((Activity) context);
            }
            try {
                mLibVLC = VLCInstance.getLibVlcInstance();
            } catch (LibVlcException e) {
                Log.d(TAG, "LibVLC initialisation failed");
                return;
            }
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.player_remote);

            mSurfaceView = (SurfaceView) findViewById(R.id.remote_player_surface);
            mSurfaceHolder = mSurfaceView.getHolder();
            mSurfaceFrame = (FrameLayout) findViewById(R.id.remote_player_surface_frame);

            VideoPlayerActivity activity = (VideoPlayerActivity)getOwnerActivity();
            if (activity == null) {
                Log.e(TAG, "Failed to get the VideoPlayerActivity instance, secondary display won't work");
                return;
            }

            mSurfaceHolder.addCallback(activity.mSurfaceCallback);

            mSubtitlesSurfaceView = (SurfaceView) findViewById(R.id.remote_subtitles_surface);
            mSubtitlesSurfaceHolder = mSubtitlesSurfaceView.getHolder();
            mSubtitlesSurfaceView.setZOrderMediaOverlay(true);
            mSubtitlesSurfaceHolder.setFormat(PixelFormat.TRANSLUCENT);
            mSubtitlesSurfaceHolder.addCallback(activity.mSubtitlesSurfaceCallback);

            if (mLibVLC.useCompatSurface())
                mSubtitlesSurfaceView.setVisibility(View.GONE);
            Log.i(TAG, "Secondary display created");
        }
    }

    /**
     * Start the video loading animation.
     */
    private void startLoadingAnimation() {
        AnimationSet anim = new AnimationSet(true);
        RotateAnimation rotate = new RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotate.setDuration(800);
        rotate.setInterpolator(new DecelerateInterpolator());
        rotate.setRepeatCount(RotateAnimation.INFINITE);
        anim.addAnimation(rotate);
        mLoading.startAnimation(anim);
        mLoadingText.setVisibility(View.VISIBLE);
    }

    /**
     * Stop the video loading animation.
     */
    private void stopLoadingAnimation() {
        mLoading.setVisibility(View.INVISIBLE);
        mLoading.clearAnimation();
        mLoadingText.setVisibility(View.GONE);
    }

    public void onClickOverlayTips(View v) {
        mOverlayTips.setVisibility(View.GONE);
    }

    public void onClickDismissTips(View v) {
        mOverlayTips.setVisibility(View.GONE);
        Editor editor = mSettings.edit();
        editor.putBoolean(PREF_TIPS_SHOWN, true);
        editor.commit();
    }

    private void updateNavStatus() {
        mHasMenu = mLibVLC.getChapterCountForTitle(0) > 1 && mLibVLC.getTitleCount() > 1;
        mIsNavMenu = mHasMenu && mLibVLC.getTitle() == 0;
        /***
         * HACK ALERT: assume that any media with >1 titles = DVD with menus
         * Should be replaced with a more robust title/chapter selection popup
         */

        Log.d(TAG,
                "updateNavStatus: getChapterCountForTitle(0) = "
                        + mLibVLC.getChapterCountForTitle(0)
                        + ", getTitleCount() = " + mLibVLC.getTitleCount());
        if (mIsNavMenu) {
            /*
             * Keep the overlay hidden in order to have touch events directly
             * transmitted to navigation handling.
             */
            hideOverlay(false);
        }
        else if (mHasMenu) {
            setESTrackLists(true);
            setESTracks();

            /* Show the return to menu button. */
            mNavMenu.setVisibility(View.VISIBLE);
            mNavMenu.setOnClickListener(mNavMenuListener);
        }
        else
            mNavMenu.setVisibility(View.GONE);

    }
}