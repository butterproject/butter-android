package pct.droid.base.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

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

}
