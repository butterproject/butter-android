package pct.droid.base.casting;

import java.net.Inet4Address;

/**
 * CastingDevice.java
 * <p/>
 * This class represents the base of a device the application can cast to.
 */
public abstract class CastingDevice {
    protected String id;
    protected String name;
    protected String model;

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getModel() {
        return model;
    }

    @Override
    public boolean equals(Object o) {
        if(o instanceof CastingDevice) {
            CastingDevice device = (CastingDevice) o;
            return device.id.equals(this.id);
        }
        return false;
    }
}