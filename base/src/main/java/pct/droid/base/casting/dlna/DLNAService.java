package pct.droid.base.casting.dlna;

import org.fourthline.cling.UpnpServiceConfiguration;
import org.fourthline.cling.android.AndroidUpnpServiceConfiguration;
import org.fourthline.cling.android.AndroidUpnpServiceImpl;
import org.fourthline.cling.model.types.ServiceType;

public class DLNAService extends AndroidUpnpServiceImpl {

    @Override
    protected UpnpServiceConfiguration createConfiguration() {
        return new AndroidUpnpServiceConfiguration() {
            @Override
            public ServiceType[] getExclusiveServiceTypes() {
                return new ServiceType[]{
                        DLNADevice.AV_TRANSPORT
                };
            }

            @Override
            public int getRegistryMaintenanceIntervalMillis()
            {
                return 7000;
            }
        };
    }

}
