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

package pct.droid.base.casting.dlna;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import org.fourthline.cling.android.AndroidUpnpService;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.meta.RemoteDevice;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.registry.DefaultRegistryListener;
import org.fourthline.cling.registry.Registry;
import org.fourthline.cling.support.avtransport.callback.Pause;
import org.fourthline.cling.support.avtransport.callback.Play;
import org.fourthline.cling.support.avtransport.callback.Seek;
import org.fourthline.cling.support.avtransport.callback.SetAVTransportURI;
import org.fourthline.cling.support.avtransport.callback.Stop;

import java.util.ArrayList;
import java.util.List;

import pct.droid.base.casting.BaseCastingClient;
import pct.droid.base.casting.CastingDevice;
import pct.droid.base.casting.CastingListener;
import pct.droid.base.providers.media.models.Media;
import pct.droid.base.utils.LogUtils;

public class DLNAClient extends BaseCastingClient {

    private Context mContext;
    private CastingListener mListener;
    private AndroidUpnpService mUpnpService;
    private DlnaRegistryListener mRegistryListener = new DlnaRegistryListener();
    private List<DLNADevice> mDiscoveredDevices;
    private DLNADevice mCurrentDevice;

    public DLNAClient(Context context, CastingListener listener) {
        mContext = context;
        mDiscoveredDevices = new ArrayList<>();
        mListener = listener;

        Intent serviceIntent = new Intent(context, DLNAService.class);
        context.getApplicationContext().bindService(serviceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    /**
     * Call on close
     */
    public void destroy() {
        if(mUpnpService != null) {
            mUpnpService.getRegistry().removeListener(mRegistryListener);
            mContext.getApplicationContext().unbindService(mServiceConnection);
        }
    }

    /**
     * @param context New context for future use
     */
    public void setContext(Context context) {
        mContext = context;
    }

    @Override
    public void loadMedia(Media media, String location, float position) {
        if (getAVTransportService() == null)
            return;

        stop();

        DLNAMetaData trackMetaData = new DLNAMetaData(media.videoId, media.title, "Popcorn Time for Android", "", media.fullImage, location, "object.item.videoItem");

        mUpnpService.getControlPoint().execute(new SetAVTransportURI(getAVTransportService(), location, trackMetaData.getXML()) {
            @Override
            public void success(ActionInvocation invocation) {
                super.success(invocation);
                LogUtils.d("DLNA: URI succesfully set!");
                play();
            }

            @Override
            public void failure(ActionInvocation invocation, UpnpResponse response, String message) {
                LogUtils.d("DLNA: Failed to set uri: " + message);
            }
        });
    }

    @Override
    public void play() {
        if (getAVTransportService() == null)
            return;

        mUpnpService.getControlPoint().execute(new Play(getAVTransportService()) {
            @Override
            public void success(ActionInvocation invocation) {
                LogUtils.d("DLNA: Success playing!");
                // TODO update player state
            }

            @Override
            public void failure(ActionInvocation invocation, UpnpResponse response, String message) {
                LogUtils.d("DLNA: Failed to play: " + message);
            }
        });
    }

    @Override
    public void pause() {
        if (getAVTransportService() == null)
            return;

        mUpnpService.getControlPoint().execute(new Pause(getAVTransportService()) {
            @Override
            public void success(ActionInvocation invocation) {
                LogUtils.d("DLNA: Successfully paused!");
                // TODO update player state
            }

            @Override
            public void failure(ActionInvocation invocation, UpnpResponse response, String message) {
                LogUtils.d("DLNA: Failed to pause: " + message);
            }
        });
    }

    @Override
    public void seek(float position) {
        if (getAVTransportService() == null)
            return;

        mUpnpService.getControlPoint().execute(new Seek(getAVTransportService(), Float.toString(position)) {
            @Override
            public void success(ActionInvocation invocation) {
                LogUtils.d("DLNA: Successfully sought!");
                // TODO update player state
            }

            @Override
            public void failure(ActionInvocation invocation, UpnpResponse response, String message) {
                LogUtils.d("DLNA: Failed to seek: " + message);
            }
        });
    }

    @Override
    public void stop() {
        if (getAVTransportService() == null)
            return;

        mUpnpService.getControlPoint().execute(new Stop(getAVTransportService()) {
            @Override
            public void success(ActionInvocation invocation) {
                LogUtils.d("DLNA: Successfully stopped!");
                // TODO update player state
            }

            @Override
            public void failure(ActionInvocation invocation, UpnpResponse response, String message) {
                LogUtils.d("DLNA: Failed to stop: " + message);
            }
        });
    }

    @Override
    public void connect(CastingDevice device) {
        if(device != mCurrentDevice && mCurrentDevice != null) {
            disconnect();
        }
        mCurrentDevice = (DLNADevice) device;
        mListener.onConnected(mCurrentDevice);
    }

    @Override
    public void disconnect() {
        stop();
        mCurrentDevice = null;
        mListener.onDisconnected();
    }

    @Override
    public void setVolume(float volume) {
        // Can't control volume (yet), so do nothing
    }

    @Override
    public boolean canControlVolume() {
        return false;
    }

    private Service getAVTransportService() {
        if(mCurrentDevice == null) return null;
        return mCurrentDevice.getAVTransportService();
    }

    private Service getRenderingControlService() {
        if(mCurrentDevice == null) return null;
        return mCurrentDevice.getRenderingControlService();
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, IBinder service) {
            mUpnpService = (AndroidUpnpService) service;

            mDiscoveredDevices.clear();
            mUpnpService.getRegistry().addListener(mRegistryListener);

            for (Device device : mUpnpService.getRegistry().getDevices()) {
                mRegistryListener.deviceAdded(device);
            }

            mUpnpService.getControlPoint().search();
        }

        public void onServiceDisconnected(ComponentName className) {
            mUpnpService = null;
        }
    };

    public class DlnaRegistryListener extends DefaultRegistryListener {
        @Override
        public void remoteDeviceAdded(Registry registry, RemoteDevice device) {
            deviceAdded(device);
        }

        @Override
        public void remoteDeviceRemoved(Registry registry, RemoteDevice device) {
            deviceRemoved(device);
        }

        @Override
        public void localDeviceAdded(Registry registry, LocalDevice device) {
            deviceAdded(device);
        }

        @Override
        public void localDeviceRemoved(Registry registry, LocalDevice device) {
            deviceRemoved(device);
        }

        public void deviceAdded(final Device device) {
            DLNADevice dlnaDevice = new DLNADevice(device);
            int index = mDiscoveredDevices.indexOf(dlnaDevice);
            if(index > -1) {
                mDiscoveredDevices.set(index, dlnaDevice);
            } else {
                mDiscoveredDevices.add(dlnaDevice);
            }
            mListener.onDeviceDetected(dlnaDevice);
        }

        public void deviceRemoved(final Device device) {
            DLNADevice dlnaDevice = new DLNADevice(device);
            mDiscoveredDevices.remove(dlnaDevice);
            mListener.onDeviceRemoved(dlnaDevice);
        }
    }
}
