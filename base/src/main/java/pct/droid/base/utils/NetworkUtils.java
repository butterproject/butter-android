package pct.droid.base.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import pct.droid.base.PopcornApplication;

public class NetworkUtils {

    /**
     * Test if connected to Wifi
     *
     * @return {@code true} if connected
     */
    public static boolean isConnectedToWifi() {
        ConnectivityManager connManager = (ConnectivityManager) PopcornApplication.getAppContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return networkInfo.isConnected();
    }

    /**
     * Test if connected to cellular
     *
     * @return {@code true} if connected
     */
    public static boolean isConnectedToCellular() {
        ConnectivityManager connManager = (ConnectivityManager) PopcornApplication.getAppContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        return networkInfo.isConnected();
    }

}
