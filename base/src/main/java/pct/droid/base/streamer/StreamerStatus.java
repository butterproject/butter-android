package pct.droid.base.streamer;

public class StreamerStatus {
    public float progress;
    public int bufferProgress;
    public int seeds;
    public float downloadSpeed;

    StreamerStatus(float progess, int bufferProgress, int seeds, int downloadSpeed) {
        this.progress = progess;
        this.bufferProgress = bufferProgress;
        this.seeds = seeds;
        this.downloadSpeed = downloadSpeed;
    }
}
