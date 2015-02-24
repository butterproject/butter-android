package pct.droid.base.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import pct.droid.base.PopcornApplication;

public class NetworkUtils {

	/* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	 * is wifi connected
	 * - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - */

	/** Get whether or not a wifi connection is currently connected. */
	public static boolean isWifiConnected(Context context) {
		ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectivityManager == null) return false;
		return connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnected();
	}

	/* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	 * is network connected
	 * - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - */

	/** Get whether or not any network connection is present (eg. wifi, 3G, etc.). */
	public static boolean isNetworkConnected(Context context) {
		ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectivityManager == null) return false;
		NetworkInfo[] info = connectivityManager.getAllNetworkInfo();
		if (info == null) return false;
		for (int i = 0; i < info.length; i++)
			if (info[i].getState() == NetworkInfo.State.CONNECTED) return true;
		return false;
	}

    /**
     * Get ip address of the Wifi service
     *
     * @return IP
     */
    public static String getWifiIPAddress() {
        WifiManager wifiMgr = (WifiManager) PopcornApplication.getAppContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
        int ip = wifiInfo.getIpAddress();
        return String.format("%d.%d.%d.%d", (ip & 0xff), (ip >> 8 & 0xff), (ip >> 16 & 0xff), (ip >> 24 & 0xff));
    }


}
