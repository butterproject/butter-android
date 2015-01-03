package pct.droid.base.casting.airplay;

public interface AirPlayCallback {
    public void onConnected();

    public void onDisconnected();

    public void onCommandFailed(String command, String message);

    public void onDeviceDetected(AirPlayDevice device);

    public void onDeviceSelected(AirPlayDevice device);

    public void onDeviceRemoved(AirPlayDevice device);

    public void onPlaybackInfo(boolean isPlaying, float position, float rate, boolean isReady);
}
