package pct.droid.base.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.format.Formatter;

import org.apache.http.conn.util.InetAddressUtils;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

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
