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

package butter.droid.base.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import butter.droid.base.ButterApplication;

/**
 * @deprecated Use {@link butter.droid.base.manager.network.NetworkManager} instead
 */
@Deprecated
public class NetworkUtils {

    /* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
     * is wifi connected
     * - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - */

    /**
     * Get whether or not a wifi connection is currently connected.
     */
    public static boolean isWifiConnected(Context context) {
        if (isNetworkConnected(context)) {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            return (cm.getActiveNetworkInfo().getType() == ConnectivityManager.TYPE_WIFI);
        } else {
            return false;
        }
    }

    /* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
     * is ethernet connected
     * - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - */

    /**
     * Get whether or not an ethernet connection is currently connected.
     */
    public static boolean isEthernetConnected(Context context) {
        if (isNetworkConnected(context)) {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            return (cm.getActiveNetworkInfo().getType() == ConnectivityManager.TYPE_ETHERNET);
        } else {
            return false;
        }
    }

    /* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
     * is network connected
     * - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - */

    /**
     * Get whether or not any network connection is present (eg. wifi, 3G, etc.).
     */
    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    /**
     * Get ip address of the Wifi service
     *
     * @return IP
     */
    public static String getWifiIPAddress() {
        WifiManager wifiMgr = (WifiManager) ButterApplication.getAppContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
        int ip = wifiInfo.getIpAddress();
        return String.format("%d.%d.%d.%d", (ip & 0xff), (ip >> 8 & 0xff), (ip >> 16 & 0xff), (ip >> 24 & 0xff));
    }

}
