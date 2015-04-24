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

package pct.droid.base.vpn;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import ht.vpn.android.api.IOpenVPNAPIService;
import ht.vpn.android.api.IOpenVPNStatusCallback;
import pct.droid.base.R;
import pct.droid.base.utils.ThreadUtils;

public class VPNManager {

    private Activity mActivity;
    private IOpenVPNAPIService mService = null;
    private Listener mListener;

    private static final int START_PROFILE_EMBEDDED = 1;
    private static final int OPENVPN_PERMISSION = 2;

    public static VPNManager start(Activity activity) {
        if(activity instanceof Listener) {
            VPNManager manager = new VPNManager(activity);
            manager.init();
            return manager;
        }
        throw new UnsupportedOperationException("Activity does not implement VPNManager.Listener");
    }

    public VPNManager(Activity activity) {
        mActivity = activity;
        mListener = (Listener) mActivity;
    }

    private void init() {
        Intent serviceIntent = new Intent(IOpenVPNAPIService.class.getName());
        serviceIntent.setPackage("ht.vpn.android");
        mActivity.bindService(serviceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    public void stop() {
        mListener = null;
        if(mService != null) {
            try {
                mService.unregisterStatusCallback(mCallback);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            mActivity.unbindService(mServiceConnection);
        }
    }

    public void startVPN() throws RemoteException {
        Intent intent = mService.prepareVPNService();
        if(intent == null) {
            onActivityResult(START_PROFILE_EMBEDDED, Activity.RESULT_OK, null);
        } else {
            mActivity.startActivityForResult(intent, START_PROFILE_EMBEDDED);
        }
    }

    private void startEmbeddedProfile()
    {
        try {
            InputStream conf = mActivity.getAssets().open("vpnht.conf");
            InputStreamReader isr = new InputStreamReader(conf);
            BufferedReader br = new BufferedReader(isr);
            String config="";
            String line;
            while(true) {
                line = br.readLine();
                if(line == null)
                    break;
                config += line + "\n";
            }
            br.readLine();

            mService.startVPN("Popcorn Time", config);
        } catch (IOException | RemoteException e) {
            e.printStackTrace();
        }
    }

    public boolean isConnected() {
        try {
            return mService.isConnectedOrConnecting();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void onActivityResult(int requestCode, int resultCode, Bundle data) {
        if (resultCode == Activity.RESULT_OK) {
            if(requestCode == START_PROFILE_EMBEDDED) {
                startEmbeddedProfile();
            } else if (requestCode == OPENVPN_PERMISSION) {
                try {
                    mService.registerStatusCallback(mCallback);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }

            }
        }
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder binder) {
            mService = IOpenVPNAPIService.Stub.asInterface(binder);
            try {
                Intent intent = mService.prepare(mActivity.getPackageName());
                if (intent != null) {
                    mActivity.startActivityForResult(intent, OPENVPN_PERMISSION);
                } else {
                    onActivityResult(OPENVPN_PERMISSION, Activity.RESULT_OK, null);
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }

            if(mListener != null)
                mListener.onVPNServiceReady();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mService = null;
        }
    };

    private IOpenVPNStatusCallback mCallback = new IOpenVPNStatusCallback.Stub() {
        @Override
        public void newStatus(String uuid, final String state, final String message, String level) throws RemoteException {
            ThreadUtils.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(mListener != null)
                        mListener.onStatusUpdate(state, message);
                }
            });
        }
    };

    public interface Listener {
        public void onVPNServiceReady();
        public void onStatusUpdate(String state, String message);
    }

}
