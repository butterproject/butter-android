/*
 * This file is part of Popcorn Time.
 *
 * Popcorn Time is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Popcorn Time is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Popcorn Time. If not, see <http://www.gnu.org/licenses/>.
 */

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
