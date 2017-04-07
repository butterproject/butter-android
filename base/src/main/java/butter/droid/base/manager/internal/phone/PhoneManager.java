package butter.droid.base.manager.internal.phone;

import android.telephony.TelephonyManager;
import butter.droid.base.Internal;
import javax.inject.Inject;

@Internal
public class PhoneManager {

    private final TelephonyManager telephonyManager;

    @Inject
    public PhoneManager(TelephonyManager telephonyManager) {
        this.telephonyManager = telephonyManager;
    }

    public boolean isPhone() {
        return telephonyManager.getPhoneType() != TelephonyManager.PHONE_TYPE_NONE;
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
