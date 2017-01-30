/*
 * This file is part of Butter.
 *
 * Butter is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Butter is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Butter. If not, see <http://www.gnu.org/licenses/>.
 */

package butter.droid.base.manager.network;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import java.util.Locale;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class NetworkManager {

    private final ConnectivityManager connectivityManager;
    private final WifiManager wifiManager;

    @Inject public NetworkManager(ConnectivityManager connectivityManager, WifiManager wifiManager) {
        this.connectivityManager = connectivityManager;
        this.wifiManager = wifiManager;
    }

    /**
     * Get whether or not a wifi connection is currently connected.
     */
    public boolean isWifiConnected() {
        if (isNetworkConnected()) {
            return connectivityManager.getActiveNetworkInfo().getType() == ConnectivityManager.TYPE_WIFI;
        } else {
            return false;
        }
    }

    /**
     * Get whether or not an ethernet connection is currently connected.
     */
    public boolean isEthernetConnected() {
        if (isNetworkConnected()) {
            return connectivityManager.getActiveNetworkInfo().getType() == ConnectivityManager.TYPE_ETHERNET;
        } else {
            return false;
        }
    }

    /**
     * Get whether or not any network connection is present (eg. wifi, 3G, etc.).
     */
    public boolean isNetworkConnected() {
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    /**
     * Get ip address of the Wifi service
     *
     * @return IP
     */
    public String getWifiIPAddress() {
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ip = wifiInfo.getIpAddress();
        return String.format(Locale.US, "%d.%d.%d.%d", (ip & 0xff), (ip >> 8 & 0xff), (ip >> 16 & 0xff),
                (ip >> 24 & 0xff));
    }


}
