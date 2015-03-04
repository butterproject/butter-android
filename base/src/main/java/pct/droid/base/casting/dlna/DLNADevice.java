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

package pct.droid.base.casting.dlna;

import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.model.types.UDAServiceType;

import pct.droid.base.casting.CastingDevice;

/**
 * DLNADevice.java
 * <p/>
 * Wraps a {@link org.fourthline.cling.model.meta.Device} in a more general class that represents a DLNA Device
 */
public class DLNADevice extends CastingDevice {

    public static UDAServiceType AV_TRANSPORT = new UDAServiceType("AVTransport");
    public static UDAServiceType RENDERING_CONTROL = new UDAServiceType("RenderingControl");

    private Device device;

    public DLNADevice(Device device) {
        this.id = device.getIdentity().getUdn().getIdentifierString();
        this.name = device.getDetails().getFriendlyName();
        this.model = device.getDetails().getModelDetails().getModelName();
        this.device = device;
    }

    public Service getAVTransportService() {
        return device.findService(AV_TRANSPORT);
    }

    public Service getRenderingControlService() {
        return device.findService(RENDERING_CONTROL);
    }

}
