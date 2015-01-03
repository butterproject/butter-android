package pct.droid.base.casting;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.media.MediaRouter;

import com.google.android.gms.cast.ApplicationMetadata;
import com.google.android.gms.cast.CastDevice;
import com.google.android.gms.cast.CastMediaControlIntent;
import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.images.WebImage;
import com.google.sample.castcompanionlibrary.cast.VideoCastManager;
import com.google.sample.castcompanionlibrary.cast.callbacks.VideoCastConsumerImpl;
import com.google.sample.castcompanionlibrary.cast.exceptions.CastException;
import com.google.sample.castcompanionlibrary.cast.exceptions.NoConnectionException;
import com.google.sample.castcompanionlibrary.cast.exceptions.TransientNetworkDisconnectionException;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import pct.droid.base.casting.airplay.AirPlayCallback;
import pct.droid.base.casting.airplay.AirPlayClient;
import pct.droid.base.casting.airplay.AirPlayDevice;
import pct.droid.base.casting.dlna.DLNACallback;
import pct.droid.base.casting.dlna.DLNAClient;
import pct.droid.base.casting.dlna.DLNADevice;
import pct.droid.base.casting.googlecast.GoogleCastClient;
import pct.droid.base.casting.googlecast.GoogleDevice;
import pct.droid.base.casting.server.CastingServerService;
import pct.droid.base.providers.media.types.Media;
import pct.droid.base.providers.media.types.Show;

/**
 * CastingManager.java
 * <p/>
 * This class is the god over all casting clients, those are:
 * {@link AirPlayClient}, {@link pct.droid.base.casting.dlna.DLNAClient}, {@link GoogleCastClient}
 * It takes note when a device has been detected or removed, controls when a device is connected and chooses which client should be used to cast for that specific {@link CastingDevice}
 */
public class CastingManager {

    private static CastingManager sInstance;

    private Context mContext;
    private VideoCastManager mCastMgr;
    private AirPlayClient mAirPlayClient;
    private DLNAClient mDLNAClient;
    private CastingListener mCallback = null;
    private CastingDevice mCurrentDevice;
    private Boolean mConnected = false;
    private List<CastingDevice> mDiscoveredDevices = new ArrayList<>();

    private CastingManager(Context context) {
        mContext = context;
        if (null == mCastMgr) {
            mCastMgr = VideoCastManager.initialize(context, CastMediaControlIntent.DEFAULT_MEDIA_RECEIVER_APPLICATION_ID, null, null);
            mCastMgr.enableFeatures(VideoCastManager.FEATURE_NOTIFICATION | VideoCastManager.FEATURE_LOCKSCREEN | VideoCastManager.FEATURE_WIFI_RECONNECT | VideoCastManager.FEATURE_DEBUGGING);
        }
        mCastMgr.setContext(context);

        mCastMgr.addVideoCastConsumer(googleCastCallback);
        mAirPlayClient = new AirPlayClient(context, airPlayCallback);
        mDLNAClient = new DLNAClient(context, dlnaCallback);

        Intent castServerService = new Intent(context, CastingServerService.class);
        context.startService(castServerService);
    }

    public static CastingManager getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new CastingManager(context);
        }
        sInstance.mCastMgr.setContext(context);
        sInstance.mAirPlayClient.setContext(context);
        sInstance.mDLNAClient.setContext(context);

        return sInstance;
    }

    public void setListener(CastingListener callback) {
        mCallback = callback;
    }

    public void onDestroy() {
        mAirPlayClient.destroy();
        mDLNAClient.destroy();

        Intent castServerService = new Intent(mContext, CastingServerService.class);
        mContext.stopService(castServerService);
    }

    public CastingDevice[] getDevices() {
        return mDiscoveredDevices.toArray(new CastingDevice[mDiscoveredDevices.size()]);
    }

    public boolean isConnected() {
        return mConnected;
    }

    public boolean stop() {
        if (!mConnected) return false;

        if (mCurrentDevice instanceof GoogleDevice) {
            try {
                mCastMgr.stop();
            } catch (CastException e) {
                e.printStackTrace();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            } catch (NoConnectionException e) {
                e.printStackTrace();
            } catch (TransientNetworkDisconnectionException e) {
                e.printStackTrace();
            }
        } else if (mCurrentDevice instanceof AirPlayDevice) {
            mAirPlayClient.stop();
        }

        return false;
    }

    public boolean loadMedia(Media media, String location) {
        return loadMedia(media, location, false);
    }

    public boolean loadMedia(Media media, String location, Boolean subs) {
        if (!mConnected) return false;

        try {
            URL url = new URL(location);
            URI uri = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(), url.getQuery(), url.getRef());
            location = uri.toString();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        if (mCurrentDevice instanceof GoogleDevice) {
            MediaMetadata metaData = new MediaMetadata();
            metaData.addImage(new WebImage(Uri.parse(media.image)));
            metaData.addImage(new WebImage(Uri.parse(media.fullImage)));
            metaData.putString(MediaMetadata.KEY_TITLE, media.title);
            metaData.putString(MediaMetadata.KEY_SUBTITLE, "Popcorn Time for Android");

            if (media instanceof Show.Episode) {
                Show.Episode episode = (Show.Episode) media;
                metaData.putString(MediaMetadata.KEY_EPISODE_NUMBER, Integer.toString(episode.episode));
                metaData.putString(MediaMetadata.KEY_SEASON_NUMBER, Integer.toString(episode.season));
            }

            MediaInfo.Builder mediaInfoBuilder = new MediaInfo.Builder(location);
            mediaInfoBuilder.setStreamType(MediaInfo.STREAM_TYPE_BUFFERED);
            mediaInfoBuilder.setMetadata(metaData);
            mediaInfoBuilder.setContentType("video/mp4");

            mCastMgr.startCastControllerActivity(mContext, mediaInfoBuilder.build(), 0, true);

            /*
            mCurrentDevice.loadMedia(mediaInfoBuilder.build(), 0, new CastCallback() {
                @Override
                public void onConnected() {

                }

                @Override
                public void onDisconnected() {

                }

                @Override
                public void onDeviceDetected(CastingDevice device) {

                }

                @Override
                public void onDeviceSelected(CastingDevice device) {

                }

                @Override
                public void onDeviceRemoved(CastingDevice device) {

                }

                @Override
                public void onVolumeChanged(double value, boolean isMute) {

                }
            });
            */
        } else if (mCurrentDevice instanceof AirPlayDevice) {
            mAirPlayClient.loadMedia(media, location);
        } else if (mCurrentDevice instanceof DLNADevice) {
            mDLNAClient.loadMedia(media, location);
        }

        return false;
    }

    public void setDevice(CastingDevice castingDevice) {
        mCastMgr.disconnect();
        mAirPlayClient.disconnect();

        mCurrentDevice = castingDevice;

        if (castingDevice != null) {
            if (castingDevice instanceof GoogleDevice) {
                mCastMgr.setDevice(((GoogleDevice) castingDevice).device);
            } else if (castingDevice instanceof AirPlayDevice) {
                mAirPlayClient.connect(castingDevice);
            } else if (castingDevice instanceof DLNADevice) {
                mDLNAClient.connect(castingDevice);
            }
        }
    }

    VideoCastConsumerImpl googleCastCallback = new VideoCastConsumerImpl() {
        @Override
        public void onVolumeChanged(double value, boolean isMute) {
            if (mCallback != null) {
                mCallback.onVolumeChanged(value, isMute);
            }
        }

        @Override
        public void onConnected() {
            if (mCurrentDevice instanceof GoogleDevice) {
                mConnected = true;
            }

            if (mCallback != null) {
                mCallback.onConnected();
            }
        }

        @Override
        public void onDisconnected() {
            if (mCurrentDevice instanceof GoogleDevice) {
                mConnected = false;
                mCurrentDevice = null;

                if (mCallback != null) {
                    mCallback.onDisconnected();
                }
            }
        }

        @Override
        public boolean onConnectionFailed(ConnectionResult result) {
            return false;
        }

        @Override
        public void onCastDeviceDetected(MediaRouter.RouteInfo info) {
            CastingDevice device = new GoogleDevice(CastDevice.getFromBundle(info.getExtras()));
            if(!mDiscoveredDevices.contains(device)) {
                mDiscoveredDevices.add(device);
                if (mCallback != null) {
                    mCallback.onDeviceDetected(device);
                }
            }
        }

        @Override
        public boolean onApplicationConnectionFailed(int errorCode) {
            return super.onApplicationConnectionFailed(errorCode);
        }

        @Override
        public void onApplicationConnected(ApplicationMetadata appMetadata, String sessionId, boolean wasLaunched) {
            super.onApplicationConnected(appMetadata, sessionId, wasLaunched);
        }

        @Override
        public void onApplicationStatusChanged(String appStatus) {
            super.onApplicationStatusChanged(appStatus);
        }

        @Override
        public void onConnectionSuspended(int cause) {
            super.onConnectionSuspended(cause);
        }

        @Override
        public void onFailed(int resourceId, int statusCode) {
            super.onFailed(resourceId, statusCode);
        }
    };

    AirPlayCallback airPlayCallback = new AirPlayCallback() {
        @Override
        public void onConnected() {
            if (!mConnected && mCallback != null) {
                mCallback.onConnected();
            }

            if (mCurrentDevice instanceof AirPlayDevice) {
                mConnected = true;
            }
        }

        @Override
        public void onDisconnected() {
            if (mCurrentDevice instanceof AirPlayDevice) {
                mConnected = false;
                mCurrentDevice = null;

                if (mCallback != null) {
                    mCallback.onDisconnected();
                }
            }
        }

        @Override
        public void onCommandFailed(String command, String message) {

        }

        @Override
        public void onDeviceDetected(AirPlayDevice device) {
            if(!mDiscoveredDevices.contains(device)) {
                mDiscoveredDevices.add(device);
                if (mCallback != null) {
                    mCallback.onDeviceDetected(device);
                }
            }
        }

        @Override
        public void onDeviceSelected(AirPlayDevice device) {
            if (mCallback != null) {
                mCallback.onDeviceSelected(device);
            }
        }

        @Override
        public void onDeviceRemoved(AirPlayDevice device) {
            mDiscoveredDevices.remove(device);
            if (mCallback != null) {
                mCallback.onDeviceRemoved(device);
            }
        }

        @Override
        public void onPlaybackInfo(boolean isPlaying, float position, float rate, boolean isReady) {

        }
    };

    DLNACallback dlnaCallback = new DLNACallback() {
        @Override
        public void onConnected() {
            if (!mConnected && mCallback != null) {
                mCallback.onConnected();
            }

            if (mCurrentDevice instanceof DLNADevice) {
                mConnected = true;
            }
        }

        @Override
        public void onDisconnected() {
            if (mCurrentDevice instanceof DLNADevice) {
                mConnected = false;
                mCurrentDevice = null;

                if (mCallback != null) {
                    mCallback.onDisconnected();
                }
            }
        }

        @Override
        public void onCommandFailed(String command, String message) {

        }

        @Override
        public void onDeviceDetected(DLNADevice device) {
            if(!mDiscoveredDevices.contains(device)) {
                mDiscoveredDevices.add(device);
                if (mCallback != null) {
                    mCallback.onDeviceDetected(device);
                }
            }
        }

        @Override
        public void onDeviceSelected(DLNADevice device) {
            if (mCallback != null) {
                mCallback.onDeviceSelected(device);
            }
        }

        @Override
        public void onDeviceRemoved(DLNADevice device) {
            mDiscoveredDevices.remove(device);
            if (mCallback != null) {
                mCallback.onDeviceRemoved(device);
            }
        }

        @Override
        public void onPlaybackInfo(boolean isPlaying, float position, float rate, boolean isReady) {

        }
    };

}
