package pct.droid.base.casting.googlecast;

public interface GoogleCastCallback {
    public void onConnected();

    public void onDisconnected();

    public void onDeviceDetected(GoogleDevice device);

    public void onDeviceSelected(GoogleDevice device);

    public void onDeviceRemoved(GoogleDevice device);

    public void onVolumeChanged(double value, boolean isMute);
}
