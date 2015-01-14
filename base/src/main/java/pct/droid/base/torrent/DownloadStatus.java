package pct.droid.base.torrent;

public class DownloadStatus {
    public float progress;
    public int bufferProgress;
    public int seeds;
    public float downloadSpeed;

    DownloadStatus(float progess, int bufferProgress, int seeds, int downloadSpeed) {
        this.progress = progess;
        this.bufferProgress = bufferProgress;
        this.seeds = seeds;
        this.downloadSpeed = downloadSpeed;
    }
}
