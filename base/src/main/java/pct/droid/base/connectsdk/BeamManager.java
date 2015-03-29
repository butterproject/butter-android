/*
 * This file is part of Popcorn Time.
 *
 * Popcorn Time is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Popcorn Time is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Popcorn Time. If not, see <http://www.gnu.org/licenses/>.
 */

package pct.droid.base.connectsdk;

import android.content.Context;
import android.content.Intent;

import com.connectsdk.device.ConnectableDevice;
import com.connectsdk.device.ConnectableDeviceListener;
import com.connectsdk.discovery.CapabilityFilter;
import com.connectsdk.discovery.DiscoveryManager;
import com.connectsdk.discovery.DiscoveryManagerListener;
import com.connectsdk.service.DeviceService;
import com.connectsdk.service.capability.MediaControl;
import com.connectsdk.service.capability.MediaPlayer;
import com.connectsdk.service.command.ServiceCommandError;
import com.connectsdk.service.sessions.LaunchSession;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import pct.droid.base.connectsdk.server.BeamServerService;
import pct.droid.base.providers.media.models.Media;
import timber.log.Timber;

/**
 * CastingManager.java
 * <p/>
 * Wrapper for ConnectSDK
 */
public class BeamManager implements ConnectableDeviceListener {

    private static BeamManager sInstance;

    private Context mContext;
    private DiscoveryManager mDiscoveryManager;
    private ConnectableDevice mCurrentDevice;
    private LaunchSession mLaunchSession;
    private MediaControl mMediaControl;
    private Boolean mConnected = false;

    private BeamManager(Context context) {
        mContext = context;

        DiscoveryManager.init(context);
        mDiscoveryManager = DiscoveryManager.getInstance();
        mDiscoveryManager.setPairingLevel(DiscoveryManager.PairingLevel.ON);
        mDiscoveryManager.setCapabilityFilters(new CapabilityFilter(
                MediaPlayer.Play_Video,
                MediaControl.Any
        ));
        mDiscoveryManager.start();

        Intent castServerService = new Intent(context, BeamServerService.class);
        context.startService(castServerService);
    }

    public static BeamManager getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new BeamManager(context);
        }

        return sInstance;
    }

    public void onDestroy() {
        mDiscoveryManager.stop();
        mDiscoveryManager.onDestroy();
        Intent castServerService = new Intent(mContext, BeamServerService.class);
        mContext.stopService(castServerService);
    }

    public Map<String, ConnectableDevice> getDevices() {
        return mDiscoveryManager.getCompatibleDevices();
    }

    public boolean hasCastDevices() {
        return mDiscoveryManager.getCompatibleDevices().size() > 0;
    }

    public boolean isConnected() {
        return mConnected;
    }

    public MediaControl getMediaControl() {
        return mMediaControl;
    }

    public boolean stop() {
        if (!mConnected || mMediaControl == null) return false;

        mLaunchSession.close(null);
        mLaunchSession = null;
        mMediaControl = null;

        return true;
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

        //String url, String mimeType, String title, String description, String iconSrc, boolean shouldLoop, LaunchListener listener
        mCurrentDevice.getCapability(MediaPlayer.class).playMedia(location, "video/mp4", media.title, "", media.image, false, new MediaPlayer.LaunchListener() {
            @Override
            public void onSuccess(MediaPlayer.MediaLaunchObject object) {
                mLaunchSession = object.launchSession;
                mMediaControl = object.mediaControl;
            }

            @Override
            public void onError(ServiceCommandError error) {
                Timber.e(error.getMessage());
            }
        });

        return false;
    }

    public void setDevice(ConnectableDevice castingDevice) {
        if(castingDevice == mCurrentDevice) return;

        if(mCurrentDevice != null) {
            mCurrentDevice.removeListener(this);
            mCurrentDevice.disconnect();
        }

        mCurrentDevice = castingDevice;

        if(castingDevice != null) {
            mCurrentDevice.addListener(this);
            mCurrentDevice.connect();
        }
    }

    public void addListener(DiscoveryManagerListener listener) {
        mDiscoveryManager.addListener(listener);
    }

    public void removeListener(DiscoveryManagerListener listener) {
        mDiscoveryManager.removeListener(listener);
    }

    @Override
    public void onDeviceReady(ConnectableDevice device) {
        mConnected = true;
    }

    @Override
    public void onDeviceDisconnected(ConnectableDevice device) {
        mConnected = false;
    }

    @Override
    public void onPairingRequired(ConnectableDevice device, DeviceService service, DeviceService.PairingType pairingType) {

    }

    @Override
    public void onCapabilityUpdated(ConnectableDevice device, List<String> added, List<String> removed) {

    }

    @Override
    public void onConnectionFailed(ConnectableDevice device, ServiceCommandError error) {

    }

}
