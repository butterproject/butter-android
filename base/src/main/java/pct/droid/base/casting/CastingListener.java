package pct.droid.base.casting;


public interface CastingListener {
    public void onConnected(CastingDevice device);

    public void onDisconnected();

    public void onConnectionFailed();

    public void onDeviceDetected(CastingDevice device);

    public void onDeviceSelected(CastingDevice device);

    public void onDeviceRemoved(CastingDevice device);

    public void onVolumeChanged(double value, boolean isMute);

    public void onReady();

    public void onPlayBackChanged(boolean isPlaying, float position);
}
