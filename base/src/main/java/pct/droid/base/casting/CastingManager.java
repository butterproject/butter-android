package pct.droid.base.casting;

import android.content.Context;
import android.content.Intent;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import pct.droid.base.casting.airplay.AirPlayCallback;
import pct.droid.base.casting.airplay.AirPlayClient;
import pct.droid.base.casting.airplay.AirPlayDevice;
import pct.droid.base.casting.dlna.DLNACallback;
import pct.droid.base.casting.dlna.DLNAClient;
import pct.droid.base.casting.dlna.DLNADevice;
import pct.droid.base.casting.googlecast.GoogleCastCallback;
import pct.droid.base.casting.googlecast.GoogleCastClient;
import pct.droid.base.casting.googlecast.GoogleDevice;
import pct.droid.base.casting.server.CastingServerService;
import pct.droid.base.providers.media.types.Media;

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
    private GoogleCastClient mGoogleCastClient;
    private AirPlayClient mAirPlayClient;
    private DLNAClient mDLNAClient;
    private CastingListener mCallback = null;
    private CastingDevice mCurrentDevice;
    private Boolean mConnected = false;
    private Set<CastingDevice> mDiscoveredDevices = new HashSet<>();

    private CastingManager(Context context) {
        mContext = context;

        mGoogleCastClient = new GoogleCastClient(context, googleCastCallback);
        mAirPlayClient = new AirPlayClient(context, airPlayCallback);
        mDLNAClient = new DLNAClient(context, dlnaCallback);

        Intent castServerService = new Intent(context, CastingServerService.class);
        context.startService(castServerService);
    }

    public static CastingManager getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new CastingManager(context);
        }
        sInstance.mGoogleCastClient.setContext(context);
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
        mGoogleCastClient.destroy();

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
            mGoogleCastClient.stop();
        } else if (mCurrentDevice instanceof AirPlayDevice) {
            mAirPlayClient.stop();
        } else if (mCurrentDevice instanceof DLNADevice) {
            mDLNAClient.stop();
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
            mGoogleCastClient.loadMedia(media, location);
        } else if (mCurrentDevice instanceof AirPlayDevice) {
            mAirPlayClient.loadMedia(media, location);
        } else if (mCurrentDevice instanceof DLNADevice) {
            mDLNAClient.loadMedia(media, location);
        }

        return false;
    }

    public void setDevice(CastingDevice castingDevice) {
        if(castingDevice == mCurrentDevice) return;

        mAirPlayClient.disconnect();
        mDLNAClient.disconnect();
        mGoogleCastClient.disconnect();

        mCurrentDevice = castingDevice;

        if (castingDevice != null) {
            if (castingDevice instanceof GoogleDevice) {
                mGoogleCastClient.connect(castingDevice);
            } else if (castingDevice instanceof AirPlayDevice) {
                mAirPlayClient.connect(castingDevice);
            } else if (castingDevice instanceof DLNADevice) {
                mDLNAClient.connect(castingDevice);
            }
        }
    }

    GoogleCastCallback googleCastCallback = new GoogleCastCallback() {
        @Override
        public void onConnected() {

        }

        @Override
        public void onDisconnected() {

        }

        @Override
        public void onConnectionFailed() {

        }

        @Override
        public void onDeviceDetected(GoogleDevice device) {
            if(!mDiscoveredDevices.contains(device)) {
                mDiscoveredDevices.add(device);
                if (mCallback != null) {
                    mCallback.onDeviceDetected(device);
                }
            }
        }

        @Override
        public void onDeviceSelected(GoogleDevice device) {

        }

        @Override
        public void onDeviceRemoved(GoogleDevice device) {
            mDiscoveredDevices.remove(device);
            if (mCallback != null) {
                mCallback.onDeviceRemoved(device);
            }
        }

        @Override
        public void onVolumeChanged(double value, boolean isMute) {
            if(mCallback != null) {
                mCallback.onVolumeChanged(value, isMute);
            }
        }

        @Override
        public void onPlayBackChanged(boolean isPlaying, float position) {

        }
    };

    AirPlayCallback airPlayCallback = new AirPlayCallback() {
        @Override
        public void onConnected() {
            if (mCurrentDevice instanceof AirPlayDevice) {
                if (!mConnected && mCallback != null) {
                    mCallback.onConnected(mCurrentDevice);
                }

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
            if (mCurrentDevice instanceof DLNADevice) {
                if (!mConnected && mCallback != null) {
                    mCallback.onConnected(mCurrentDevice);
                }

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
