package pct.droid.base.casting.googlecast;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.media.MediaRouteSelector;
import android.support.v7.media.MediaRouter;

import com.google.android.gms.cast.ApplicationMetadata;
import com.google.android.gms.cast.Cast;
import com.google.android.gms.cast.CastMediaControlIntent;
import com.google.android.gms.cast.LaunchOptions;
import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.MediaStatus;
import com.google.android.gms.cast.RemoteMediaPlayer;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import pct.droid.base.Constants;
import pct.droid.base.casting.BaseCastingClient;
import pct.droid.base.casting.CastingDevice;
import pct.droid.base.providers.media.types.Media;
import pct.droid.base.utils.LogUtils;

public class GoogleCastClient extends BaseCastingClient {

    private final Set<MediaRouter.RouteInfo> mDiscoveredDevices = new HashSet<>();

    private Context mContext;
    private Handler mHandler;
    private GoogleCastCallback mCallback;
    private MediaRouter mMediaRouter;
    private GoogleApiClient mGoogleApiClient;
    private GoogleDevice mCurrentDevice;
    private boolean mWaitingForReconnect = false, mApplicationStarted = false;
    private RemoteMediaPlayer mRemoteMediaPlayer;

    public GoogleCastClient(Context context, GoogleCastCallback callback) {
        mContext = context;
        mCallback = callback;
        mHandler = new Handler(context.getApplicationContext().getMainLooper());
        mMediaRouter = MediaRouter.getInstance(context.getApplicationContext());

        MediaRouteSelector mediaRouteSelector = new MediaRouteSelector.Builder().addControlCategory(CastMediaControlIntent.categoryForCast(CastMediaControlIntent.DEFAULT_MEDIA_RECEIVER_APPLICATION_ID)).addControlCategory(CastMediaControlIntent.categoryForCast(Constants.CAST_ID)).build();
        mMediaRouter.addCallback(mediaRouteSelector, mMediaRouterCallback,  MediaRouter.CALLBACK_FLAG_REQUEST_DISCOVERY);

        mRemoteMediaPlayer = new RemoteMediaPlayer();
        mRemoteMediaPlayer.setOnStatusUpdatedListener(mMediaPlayerStatusListener);
    }

    /**
     * Call on close
     */
    public void destroy() {
        disconnect();
        mHandler.removeCallbacksAndMessages(null);
        mMediaRouter.removeCallback(mMediaRouterCallback);
    }

    /**
     * @param context New context for future use
     */
    public void setContext(Context context) {
        mContext = context;
    }

    @Override
    public void loadMedia(Media media, String location, float position) {
        if(mCurrentDevice != null && mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            MediaMetadata mediaMetadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE);
            mediaMetadata.putString(MediaMetadata.KEY_TITLE, media.title);
            MediaInfo mediaInfo = new MediaInfo.Builder(
                    location)
                    .setContentType("video/mp4")
                    .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
                    .setMetadata(mediaMetadata)
                    .build();
            try {
                mRemoteMediaPlayer.load(mGoogleApiClient, mediaInfo, true)
                        .setResultCallback(new ResultCallback<RemoteMediaPlayer.MediaChannelResult>() {
                            @Override
                            public void onResult(RemoteMediaPlayer.MediaChannelResult result) {
                                if (result.getStatus().isSuccess()) {
                                    LogUtils.d("GoogleCastClient", "Media loaded successfully");
                                }
                            }
                        });
            } catch (IllegalStateException e) {
                LogUtils.e("GoogleCastClient", "Problem occurred with media during loading", e);
            } catch (Exception e) {
                LogUtils.e("GoogleCastClient", "Problem opening media during loading", e);
            }
        }
    }

    @Override
    public void play() {

    }

    @Override
    public void pause() {

    }

    @Override
    public void seek(float position) {

    }

    @Override
    public void stop() {

    }

    /**
     * @param device Device to connect to
     */
    @Override
    public void connect(CastingDevice device) {
        if (mCurrentDevice != device) {
            disconnect();
        }

        mCurrentDevice = (GoogleDevice) device;
        mMediaRouter.selectRoute(mCurrentDevice.routeInfo);
        LogUtils.d("Connecting to google cast device: " + device.getId() + " - " + mCurrentDevice.getName());



        mCallback.onDeviceSelected(mCurrentDevice);
        // initEvents();
    }

    @Override
    public void disconnect() {
        if (mCurrentDevice != null && mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            try {
                Cast.CastApi.leaveApplication(mGoogleApiClient);
                mApplicationStarted = false;
                mGoogleApiClient.disconnect();
                mGoogleApiClient = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        mCurrentDevice = null;
        mMediaRouter.selectRoute(mMediaRouter.getDefaultRoute());
        mCallback.onDisconnected();
    }

    @Override
    public void setVolume(float volume) {

    }

    @Override
    public boolean canControlVolume() {
        return mCurrentDevice != null && mCurrentDevice.routeInfo.getVolumeHandling() == MediaRouter.RouteInfo.PLAYBACK_VOLUME_VARIABLE;
    }

    private void reconnectChannels() {
        try {
            Cast.CastApi.setMessageReceivedCallbacks(mGoogleApiClient, mRemoteMediaPlayer.getNamespace(), mRemoteMediaPlayer);
            mRemoteMediaPlayer.requestStatus(mGoogleApiClient);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private GoogleApiClient.ConnectionCallbacks mConnectionCallbacks = new GoogleApiClient.ConnectionCallbacks() {
        @Override
        public void onConnected(Bundle bundle) {
            LogUtils.d("Connected " + bundle);
            if (mWaitingForReconnect) {
                mWaitingForReconnect = false;
                reconnectChannels();
            } else {
                mCallback.onConnected();
                try {
                    LaunchOptions launchOptions = new LaunchOptions.Builder().setRelaunchIfRunning(false).build();
                    Cast.CastApi.launchApplication(mGoogleApiClient, Constants.CAST_ID, launchOptions)
                            .setResultCallback(
                                    new ResultCallback<Cast.ApplicationConnectionResult>() {
                                        @Override
                                        public void onResult(Cast.ApplicationConnectionResult result) {
                                            Status status = result.getStatus();
                                            if (status.isSuccess()) {
                                                mApplicationStarted = true;
                                                reconnectChannels();
                                            }
                                        }
                                    });

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onConnectionSuspended(int i) {

        }
    };

    private GoogleApiClient.OnConnectionFailedListener mConnectionFailedListener = new GoogleApiClient.OnConnectionFailedListener() {
        @Override
        public void onConnectionFailed(ConnectionResult connectionResult) {
            mCallback.onConnectionFailed();
        }
    };

    private Cast.Listener mCastListener = new Cast.Listener() {
        @Override
        public void onVolumeChanged() {
            if (mGoogleApiClient != null) {
                LogUtils.d("GoogleCastClient", "onVolumeChanged: " + Cast.CastApi.getVolume(mGoogleApiClient));
            }
        }

        @Override
        public void onApplicationDisconnected(int statusCode) {
            disconnect();
        }

        @Override
        public void onApplicationStatusChanged() {
            if (mGoogleApiClient != null) {
                LogUtils.d("GoogleCastClient", "onApplicationStatusChanged: "
                        + Cast.CastApi.getApplicationStatus(mGoogleApiClient));
            }
        }
    };

    private MediaRouter.Callback mMediaRouterCallback = new MediaRouter.Callback() {
        @Override
        public void onRouteSelected(MediaRouter router, MediaRouter.RouteInfo route) {
            super.onRouteSelected(router, route);
            mCurrentDevice = new GoogleDevice(route);
            Cast.CastOptions.Builder apiOptionsBuilder = Cast.CastOptions
                    .builder(mCurrentDevice.getCastDevice(), mCastListener);

            mGoogleApiClient = new GoogleApiClient.Builder(mContext)
                    .addApi(Cast.API, apiOptionsBuilder.build())
                    .addConnectionCallbacks(mConnectionCallbacks)
                    .addOnConnectionFailedListener(mConnectionFailedListener)
                    .build();

            mGoogleApiClient.connect();
        }

        @Override
        public void onRouteUnselected(MediaRouter router, MediaRouter.RouteInfo route) {
            super.onRouteUnselected(router, route);
            if (mCurrentDevice != null && mCurrentDevice.equals(new GoogleDevice(route))) {
                mCurrentDevice = null;
                mCallback.onDisconnected();
            }
        }

        @Override
        public void onRouteAdded(MediaRouter router, MediaRouter.RouteInfo route) {
            super.onRouteAdded(router, route);
            GoogleDevice device = new GoogleDevice(route);
            if(mDiscoveredDevices.add(route))
                mCallback.onDeviceDetected(device);
        }

        @Override
        public void onRouteRemoved(MediaRouter router, MediaRouter.RouteInfo route) {
            super.onRouteRemoved(router, route);
            GoogleDevice device = new GoogleDevice(route);
            mDiscoveredDevices.remove(route);
            mCallback.onDeviceRemoved(device);
        }

        @Override
        public void onRouteVolumeChanged(MediaRouter router, MediaRouter.RouteInfo route) {
            super.onRouteVolumeChanged(router, route);
            mCallback.onVolumeChanged((double) route.getVolume(), route.getVolume() == 0);
        }
    };

    private RemoteMediaPlayer.OnStatusUpdatedListener mMediaPlayerStatusListener = new RemoteMediaPlayer.OnStatusUpdatedListener() {
        @Override
        public void onStatusUpdated() {
            MediaStatus mediaStatus = mRemoteMediaPlayer.getMediaStatus();
            if(mediaStatus != null) {
                boolean isPlaying = mediaStatus.getPlayerState() == MediaStatus.PLAYER_STATE_PLAYING;
                float position = (float) mRemoteMediaPlayer.getApproximateStreamPosition();
                mCallback.onPlayBackChanged(isPlaying, position);
            }
        }
    };

}
