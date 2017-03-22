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
    return telephonyManager.getNetworkType() == TelephonyManager.NETWORK_TYPE_UMTS ||
        telephonyManager.getNetworkType() == TelephonyManager.NETWORK_TYPE_HSUPA ||
        telephonyManager.getNetworkType() == TelephonyManager.NETWORK_TYPE_HSPA ||
        telephonyManager.getNetworkType() == TelephonyManager.NETWORK_TYPE_HSDPA ||
        telephonyManager.getNetworkType() == TelephonyManager.NETWORK_TYPE_EVDO_0 ||
        telephonyManager.getNetworkType() == TelephonyManager.NETWORK_TYPE_EVDO_A;
  }

}
