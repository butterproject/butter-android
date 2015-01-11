package pct.droid.base.casting.airplay;

import java.net.Inet4Address;

import javax.jmdns.ServiceInfo;

import pct.droid.base.casting.CastingDevice;

/**
 * AirPlayDevice.java
 * <p/>
 * Wraps a {@link javax.jmdns.ServiceInfo} in a more general class that represents an Apple AirPlay device
 */
public class AirPlayDevice extends CastingDevice {

    public ServiceInfo service;

    protected Inet4Address ipAddress;
    protected Integer port;
    private String protovers;
    private String srcvers;
    private Boolean pw = false;

    public AirPlayDevice(ServiceInfo service) {
        this.service = service;
        this.id = service.getPropertyString("deviceid");
        this.model = service.getPropertyString("model");
        this.protovers = service.getPropertyString("protovers");
        this.srcvers = service.getPropertyString("srcvers");
        Inet4Address[] inetAddresses = service.getInet4Addresses();
        if (inetAddresses.length > 0)
            this.ipAddress = inetAddresses[0];
        this.port = service.getPort();
        this.name = service.getName();

        byte[] pwBytes = service.getPropertyBytes("pw");
        byte[] pinBytes = service.getPropertyBytes("pin");
        if (pwBytes != null || pinBytes != null)
            this.pw = true;
    }

    public Inet4Address getIpAddress() {
        return ipAddress;
    }

    public Integer getPort() {
        return port;
    }

    public String getUrl() {
        return "http://" + ipAddress.getHostAddress() + ":" + port + "/";
    }

    public String getSourceVersion() {
        return srcvers;
    }

    public String getProtocolVersion() {
        return protovers;
    }

    public Boolean isPasswordProtected() {
        return pw;
    }
}
