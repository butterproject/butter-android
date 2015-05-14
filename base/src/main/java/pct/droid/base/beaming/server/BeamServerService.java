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

package pct.droid.base.beaming.server;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import pct.droid.base.Constants;
import pct.droid.base.utils.NetworkUtils;
import timber.log.Timber;

public class BeamServerService extends Service {

    private static BeamServer sServer;

    /**
     * Start service and server
     *
     * @param intent  Intent used for start
     * @param flags   Flags
     * @param startId Id
     * @return Starting sticky or not?
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Timber.i("Starting CastingServer");
        sServer = new BeamServer(NetworkUtils.getWifiIPAddress(), Constants.SERVER_PORT);

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
        Timber.i("Destroying CastingServer");
        super.onDestroy();
    }

    public static BeamServer getServer() {
        return sServer;
    }
}
