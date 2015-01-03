package pct.droid.base.casting.googlecast;

import com.google.android.gms.cast.CastDevice;

import pct.droid.base.casting.CastingDevice;

/**
 * GoogleDevice.java
 * <p/>
 * Wraps a {@link com.google.android.gms.cast.CastDevice} in a more general class that represents a Google Cast Device
 */
public class GoogleDevice extends CastingDevice {
    public CastDevice device;

    public GoogleDevice(CastDevice device) {
        this.device = device;
        this.name = device.getFriendlyName();
        this.model = device.getModelName();
        this.id = device.getDeviceId();
    }
}
