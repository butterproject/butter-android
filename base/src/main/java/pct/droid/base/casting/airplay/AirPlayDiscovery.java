package pct.droid.base.casting.airplay;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Looper;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceListener;

import pct.droid.base.utils.LogUtils;

/**
 * AirPlayDiscovery.java
 *
 * mDNS discovery of AirPlay devices. Gives callback when devices are found/removed/resolved.
 */
public class AirPlayDiscovery implements ServiceListener {

    private static final String SERVICE_TYPE = "_airplay._tcp.local.";

    private static AirPlayDiscovery sInstance;

    private JmDNS mJmDNS;
    private Thread mDNSThread;
    private WifiManager.MulticastLock mLock;
    private ServiceListener mServiceListener;
    private InetAddress mDeviceAddress;
    private Handler mHandler;

    private AirPlayDiscovery(Context context, ServiceListener serviceListener) {
        mServiceListener = serviceListener;

        mHandler = new Handler(Looper.getMainLooper());

        WifiManager wifi = (WifiManager) context.getSystemService(android.content.Context.WIFI_SERVICE);
        mLock = wifi.createMulticastLock("AirplayDNSLock");
        mLock.setReferenceCounted(true);
        mLock.acquire();

        mDNSThread = new Thread() {
            @Override
            public void run() {
                try {
                    mDeviceAddress = getWifiInetAddress();
                    if (mDeviceAddress == null) {
                        LogUtils.d("Unable to get local ip address");
                        return;
                    }

                    mJmDNS = JmDNS.create(mDeviceAddress);
                    mJmDNS.addServiceListener(SERVICE_TYPE, AirPlayDiscovery.this);
                    LogUtils.d("Using local address " + mDeviceAddress.getHostAddress());
                } catch (Exception e) {
                    LogUtils.d("Error: " + e.getMessage() == null ? "Unable to initialize discovery service" : e.getMessage());
                }
            }
        };
    }

    /**
     * Get existing instance of AirplayDiscovery or create new
     *
     * @param context Context
     * @param serviceListener Listener
     * @return AirPlayDiscovery
     */
    public static AirPlayDiscovery getInstance(Context context, ServiceListener serviceListener) {
        if (sInstance == null) {
            sInstance = new AirPlayDiscovery(context, serviceListener);
        }
        sInstance.mServiceListener = serviceListener;
        return sInstance;
    }

    /**
     * Start the jmDNS service and try to find AirPlay devices on the network
     */
    public void start() {
        mLock.acquire();
        if (!mDNSThread.isAlive()) mDNSThread.start();
    }

    /**
     * Stop the jmDNS service
     */
    public void stop() {
        if (mJmDNS != null) {
            try {
                mJmDNS.removeServiceListener(SERVICE_TYPE, this);
                mJmDNS.close();
            } catch (Exception e) {
                LogUtils.d("Error: " + e.getMessage());
            }
        }

        if (mLock != null) {
            mLock.release();
        }
    }

    @Override
    public void serviceAdded(final ServiceEvent event) {
        mServiceListener.serviceAdded(event);
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mJmDNS.requestServiceInfo(event.getType(), event.getName(), 30000);
            }
        });
    }

    @Override
    public void serviceRemoved(ServiceEvent event) {
        mServiceListener.serviceRemoved(event);
    }

    @Override
    public void serviceResolved(ServiceEvent event) {
        mServiceListener.serviceResolved(event);
    }

    /**
     * Get the ip address used by the wifi interface
     *
     * @return IP4v InetAddress
     */
    private InetAddress getWifiInetAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                        return (inetAddress);
                    }
                }
            }
        } catch (Exception e) {
            return (null);
        }
        return (null);
    }
}
