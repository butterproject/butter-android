package pct.droid.base.casting;

import android.content.Context;
import android.content.Intent;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import pct.droid.base.casting.airplay.AirPlayClient;
import pct.droid.base.casting.airplay.AirPlayDevice;
import pct.droid.base.casting.dlna.DLNAClient;
import pct.droid.base.casting.dlna.DLNADevice;
import pct.droid.base.casting.googlecast.GoogleCastClient;
import pct.droid.base.casting.googlecast.GoogleDevice;
import pct.droid.base.casting.server.CastingServerService;
import pct.droid.base.providers.media.models.Media;

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
    private Set<CastingListener> mListeners = null;
    private CastingDevice mCurrentDevice;
    private Boolean mConnected = false;
    private Set<CastingDevice> mDiscoveredDevices = new HashSet<>();

    private CastingManager(Context context) {
        mContext = context;

        mListeners = new HashSet<>();

        mGoogleCastClient = new GoogleCastClient(context, mInternalListener);
        /*
        Do not init, it does not work stable with torrent videos and needs more work. That can happen in the future.
        mAirPlayClient = new AirPlayClient(context, mInternalListener);
        TODO: Get AirPlay working
        */
        mDLNAClient = new DLNAClient(context, mInternalListener);

        Intent castServerService = new Intent(context, CastingServerService.class);
        context.startService(castServerService);
    }

    public static CastingManager getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new CastingManager(context);
        }
        sInstance.mGoogleCastClient.setContext(context);
        //sInstance.mAirPlayClient.setContext(context);
        sInstance.mDLNAClient.setContext(context);

        return sInstance;
    }

    public boolean addListener(CastingListener listener) {
        return mListeners.add(listener);
    }

    public boolean removeListener(CastingListener listener) {
        return mListeners.remove(listener);
    }

    public void onDestroy() {
        //mAirPlayClient.destroy();
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
            //mAirPlayClient.stop();
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
            //mAirPlayClient.loadMedia(media, location);
        } else if (mCurrentDevice instanceof DLNADevice) {
            mDLNAClient.loadMedia(media, location);
        }

        return false;
    }

    public void setDevice(CastingDevice castingDevice) {
        if(castingDevice == mCurrentDevice) return;

        //mAirPlayClient.disconnect();
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

    private CastingListener mInternalListener = new CastingListener() {
        @Override
        public void onConnected(CastingDevice device) {
            if((!device.equals(mCurrentDevice) && !mConnected) || mConnected) return;

            mConnected = true;
            for(CastingListener listener: mListeners) {
                listener.onConnected(mCurrentDevice);
            }
        }

        @Override
        public void onDisconnected() {
            if(!mConnected) return;

            mConnected = false;
            mCurrentDevice = null;

            for(CastingListener listener: mListeners) {
                listener.onDisconnected();
            }
        }

        @Override
        public void onCommandFailed(String command, String message) {
            for(CastingListener listener: mListeners) {
                listener.onCommandFailed(command, message);
            }
        }

        @Override
        public void onConnectionFailed() {
            for(CastingListener listener: mListeners) {
                listener.onConnectionFailed();
            }
        }

        @Override
        public void onDeviceDetected(CastingDevice device) {
            if(mDiscoveredDevices.add(device)) {
                for(CastingListener listener: mListeners) {
                    listener.onDeviceDetected(device);
                }
            }
        }

        @Override
        public void onDeviceSelected(CastingDevice device) {
            mCurrentDevice = device;
            for(CastingListener listener: mListeners) {
                listener.onDeviceSelected(device);
            }
        }

        @Override
        public void onDeviceRemoved(CastingDevice device) {
            if(mDiscoveredDevices.remove(device)) {
                for (CastingListener listener : mListeners) {
                    listener.onDeviceRemoved(device);
                }
            }
        }

        @Override
        public void onVolumeChanged(double value, boolean isMute) {
            for(CastingListener listener: mListeners) {
                listener.onVolumeChanged(value, isMute);
            }
        }

        @Override
        public void onReady() {
            for(CastingListener listener: mListeners) {
                listener.onReady();
            }
        }

        @Override
        public void onPlayBackChanged(boolean isPlaying, float position) {
            for(CastingListener listener: mListeners) {
                listener.onPlayBackChanged(isPlaying, position);
            }
        }
    };

}
