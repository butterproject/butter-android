package pct.droid.base.casting.dlna;

public interface DLNACallback {
    public void onConnected();

    public void onDisconnected();

    public void onCommandFailed(String command, String message);

    public void onDeviceDetected(DLNADevice device);

    public void onDeviceSelected(DLNADevice device);

    public void onDeviceRemoved(DLNADevice device);

    public void onPlaybackInfo(boolean isPlaying, float position, float rate, boolean isReady);
}
