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

package pct.droid.base.beaming;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.InputType;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.connectsdk.device.ConnectableDevice;
import com.connectsdk.device.ConnectableDeviceListener;
import com.connectsdk.discovery.CapabilityFilter;
import com.connectsdk.discovery.DiscoveryManager;
import com.connectsdk.discovery.DiscoveryManagerListener;
import com.connectsdk.service.CastService;
import com.connectsdk.service.DeviceService;
import com.connectsdk.service.capability.MediaControl;
import com.connectsdk.service.capability.MediaPlayer;
import com.connectsdk.service.capability.VolumeControl;
import com.connectsdk.service.command.ServiceCommandError;
import com.connectsdk.service.sessions.LaunchSession;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import pct.droid.base.Constants;
import pct.droid.base.PopcornApplication;
import pct.droid.base.R;
import pct.droid.base.beaming.server.BeamServerService;
import pct.droid.base.providers.media.models.Media;
import pct.droid.base.providers.media.models.Show;
import pct.droid.base.torrent.StreamInfo;
import timber.log.Timber;

/**
 * CastingManager.java
 * <p/>
 * Wrapper for ConnectSDK
 */
public class BeamManager implements ConnectableDeviceListener, DiscoveryManagerListener {

    private static BeamManager sInstance;

    private Context mContext;
    private DiscoveryManager mDiscoveryManager;
    private ConnectableDevice mCurrentDevice;
    private LaunchSession mLaunchSession;
    private Boolean mConnected = false;
    private BeamListener mListener = null;
    private InputMethodManager mInputManager;
    private EditText mInput;
    private AlertDialog mPairingAlertDialog;
    private AlertDialog mPairingCodeDialog;

    private BeamManager(Context context) {
        mContext = context;

        mInput = new EditText(context);
        mInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        mInputManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);

        mPairingAlertDialog =
                new AlertDialog.Builder(context)
                        .setTitle(R.string.pairing_tv)
                        .setMessage(R.string.confirm_tv)
                        .setPositiveButton(android.R.string.ok, null)
                        .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .create();

        mPairingCodeDialog =
                new AlertDialog.Builder(context)
                        .setTitle(R.string.enter_pairing_code)
                        .setView(mInput)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                if (mCurrentDevice != null) {
                                    String value = mInput.getText().toString().trim();
                                    mCurrentDevice.sendPairingKey(value);
                                    mInputManager.hideSoftInputFromWindow(mInput.getWindowToken(), 0);
                                }
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int whichButton) {
                                dialog.dismiss();
                                mInputManager.hideSoftInputFromWindow(mInput.getWindowToken(), 0);
                            }
                        })
                        .create();

        CastService.setApplicationID(Constants.CAST_ID);
        DiscoveryManager.init(PopcornApplication.getAppContext());
        mDiscoveryManager = DiscoveryManager.getInstance();
        mDiscoveryManager.setPairingLevel(DiscoveryManager.PairingLevel.ON);
        mDiscoveryManager.setCapabilityFilters(new CapabilityFilter(
                MediaPlayer.Play_Video,
                MediaControl.Any
        ));
        mDiscoveryManager.start();
        mDiscoveryManager.addListener(this);

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
        mDiscoveryManager.removeListener(this);
        mDiscoveryManager.stop();
        mDiscoveryManager.onDestroy();
        Intent castServerService = new Intent(mContext, BeamServerService.class);
        mContext.stopService(castServerService);
    }

    public Map<String, ConnectableDevice> getDevices() {
        Map<String, ConnectableDevice> devices = mDiscoveryManager.getCompatibleDevices();
        for (String key : devices.keySet()) {
            ConnectableDevice device = devices.get(key);
            if (device.getServices().size() <= 0) {
                devices.remove(key);
            }
        }
        return devices;
    }

    public ConnectableDevice getConnectedDevice() {
        return mCurrentDevice;
    }

    public boolean hasCastDevices() {
        return mDiscoveryManager.getCompatibleDevices().size() > 0;
    }

    public boolean isConnected() {
        return mConnected;
    }

    public MediaControl getMediaControl() {
        return mCurrentDevice.getCapability(MediaControl.class);
    }

    public VolumeControl getVolumeControl() {
        if (mCurrentDevice != null && mCurrentDevice.hasCapability(VolumeControl.Volume_Get) && mCurrentDevice.hasCapability(VolumeControl.Volume_Get) && mCurrentDevice.hasCapability(VolumeControl.Volume_Subscribe)) {
            return mCurrentDevice.getCapability(VolumeControl.class);
        }
        return null;
    }

    public boolean stopVideo() {
        if (!mConnected || mLaunchSession == null) return false;

        mLaunchSession.close(null);
        mLaunchSession = null;

        return true;
    }

    public void playVideo(StreamInfo info) {
        playVideo(info, false, null);
    }

    public void playVideo(StreamInfo info, Boolean subs) {
        playVideo(info, subs, null);
    }

    public void playVideo(StreamInfo info, Boolean subs, final MediaPlayer.LaunchListener listener) {
        if (!mConnected) listener.onError(ServiceCommandError.getError(503));

        String location = info.getVideoLocation();
        try {
            URL url = new URL(location);
            URI uri = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(), url.getQuery(), url.getRef());
            location = uri.toString();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        String title = "";
        String imageUrl = "";
        Media media = info.getMedia();
        if (media != null) {
            if (info.isShow()) {
                Show show = info.getShow();
                title = show.title == null ? "" : show.title;
                title += media.title == null ? "" : ": " + media.title;
                imageUrl = show.image;
            } else {
                title = media.title == null ? "" : media.title;
                imageUrl = media.image;
            }
        }
        imageUrl = imageUrl == null ? "https://popcorntime.io/images/header-logo.png" : imageUrl;

        //String url, String mimeType, String title, String description, String iconSrc, boolean shouldLoop, LaunchListener listener
        if (mCurrentDevice != null)
            mCurrentDevice.getCapability(MediaPlayer.class).playMedia(location, "video/mp4", title, "", imageUrl, false, new MediaPlayer.LaunchListener() {
                @Override
                public void onSuccess(MediaPlayer.MediaLaunchObject object) {
                    mLaunchSession = object.launchSession;
                    if (listener != null)
                        listener.onSuccess(object);
                }

                @Override
                public void onError(ServiceCommandError error) {
                    Timber.e(error.getMessage());
                    if (listener != null)
                        listener.onError(error);
                }
            });
    }

    public void connect(ConnectableDevice castingDevice) {
        if (castingDevice == mCurrentDevice) return;

        if (mCurrentDevice != null) {
            mCurrentDevice.removeListener(this);
            mCurrentDevice.disconnect();
            mConnected = false;
        }

        mCurrentDevice = castingDevice;

        if (castingDevice != null) {
            mCurrentDevice.addListener(this);
            mCurrentDevice.connect();
        }
    }

    public void disconnect() {
        if (mCurrentDevice != null) {
            mConnected = false;

            mCurrentDevice.disconnect();
            mCurrentDevice.removeListener(this);
        }

        mCurrentDevice = null;

        if (mListener != null) {
            mListener.updateBeamIcon();
        }
    }

    public void addDiscoveryListener(DiscoveryManagerListener listener) {
        mDiscoveryManager.addListener(listener);
    }

    public void removeDiscoveryListener(DiscoveryManagerListener listener) {
        mDiscoveryManager.removeListener(listener);
    }

    public void setListener(BeamListener listener) {
        mListener = listener;
    }

    @Override
    public void onDeviceReady(ConnectableDevice device) {
        if (mPairingAlertDialog.isShowing()) {
            mPairingAlertDialog.dismiss();
        }

        mConnected = true;
        if (mListener != null)
            mListener.updateBeamIcon();
    }

    @Override
    public void onDeviceDisconnected(ConnectableDevice device) {
        mConnected = false;
        if (mListener != null)
            mListener.updateBeamIcon();
    }

    @Override
    public void onPairingRequired(ConnectableDevice device, DeviceService service, DeviceService.PairingType pairingType) {
        switch (pairingType) {
            case FIRST_SCREEN:
                mPairingAlertDialog.show();
                break;
            case PIN_CODE:
                mPairingCodeDialog.show();
                break;
            case NONE:
            default:
                break;
        }
    }

    @Override
    public void onCapabilityUpdated(ConnectableDevice device, List<String> added, List<String> removed) {
    }

    @Override
    public void onConnectionFailed(ConnectableDevice device, ServiceCommandError error) {
        Toast.makeText(mContext, R.string.unknown_error, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDeviceAdded(DiscoveryManager manager, ConnectableDevice device) {
        if (mListener != null)
            mListener.updateBeamIcon();
    }

    @Override
    public void onDeviceUpdated(DiscoveryManager manager, ConnectableDevice device) {

    }

    @Override
    public void onDeviceRemoved(DiscoveryManager manager, ConnectableDevice device) {
        if (device == mCurrentDevice) {
            mConnected = false;
            mCurrentDevice = null;
        }

        if (mListener != null)
            mListener.updateBeamIcon();
    }

    @Override
    public void onDiscoveryFailed(DiscoveryManager manager, ServiceCommandError error) {

    }

    public interface BeamListener {
        public void updateBeamIcon();
    }

}
