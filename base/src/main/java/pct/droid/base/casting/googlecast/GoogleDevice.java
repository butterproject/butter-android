package pct.droid.base.casting.googlecast;


import android.support.v7.media.MediaRouter;

import com.google.android.gms.cast.CastDevice;

import pct.droid.base.casting.CastingDevice;

/**
 * GoogleDevice.java
 * <p/>
 * Wraps a {@link android.support.v7.media.MediaRouter.RouteInfo} in a more general class that represents a Google Cast Device
 */
public class GoogleDevice extends CastingDevice {
    public MediaRouter.RouteInfo routeInfo;
    private CastDevice device;

    public GoogleDevice(MediaRouter.RouteInfo routeInfo) {
        this.routeInfo = routeInfo;
        device = CastDevice.getFromBundle(routeInfo.getExtras());
        this.name = device.getFriendlyName();
        this.model = device.getModelName();
        this.id = device.getDeviceId();
    }

    public CastDevice getCastDevice() {
        return device;
    }
}
