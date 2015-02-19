package pct.droid.base.casting.server;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import java.io.File;
import java.io.IOException;

import pct.droid.base.Constants;
import pct.droid.base.PopcornApplication;
import pct.droid.base.preferences.Prefs;
import pct.droid.base.utils.LogUtils;
import pct.droid.base.utils.NetworkUtils;
import pct.droid.base.utils.PrefUtils;

public class CastingServerService extends Service {

    private CastingServer mServer;

    /**
     * Start service and server
     * @param intent Intent used for start
     * @param flags Flags
     * @param startId Id
     * @return Starting sticky or not?
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogUtils.i("CastingServer", "Starting CastingServer");
        mServer = new CastingServer(NetworkUtils.getWifiIPAddress(), Constants.SERVER_PORT);
        mServer.start();

        return START_STICKY;
    }

    @Override
    public IBinder onBind(final Intent intent) {
        return null;
    }

    /**
     * Destroy service and server running inside
     */
    @Override
    public void onDestroy() {
        LogUtils.i("CastingServer", "Destroying CastingServer");
        mServer.stop();
        super.onDestroy();
    }
}
