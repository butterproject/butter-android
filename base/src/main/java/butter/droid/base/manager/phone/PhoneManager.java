package butter.droid.base.manager.phone;

import android.telephony.TelephonyManager;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class PhoneManager {

    private final TelephonyManager telephonyManager;

    @Inject
    public PhoneManager(TelephonyManager telephonyManager) {
        this.telephonyManager = telephonyManager;
    }

    public boolean isConnected() {
        return telephonyManager.getDataState() == TelephonyManager.DATA_CONNECTED;
    }

    public boolean isHighSpeedConnection() {
        final int networkType = telephonyManager.getNetworkType();
        return networkType == TelephonyManager.NETWORK_TYPE_UMTS ||
                networkType == TelephonyManager.NETWORK_TYPE_HSUPA ||
                networkType == TelephonyManager.NETWORK_TYPE_HSPA ||
                networkType == TelephonyManager.NETWORK_TYPE_HSDPA ||
                networkType == TelephonyManager.NETWORK_TYPE_EVDO_0 ||
                networkType == TelephonyManager.NETWORK_TYPE_EVDO_A;
    }

}
