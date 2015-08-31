package pct.droid.tv.events;

public class PlaybackProgressChangedEvent {

    private final long currentTime;
    private final long duration;

    public PlaybackProgressChangedEvent(long currentTime, long duration) {
        this.currentTime = currentTime;
        this.duration = duration;
    }

    public long getCurrentTime() {
        return currentTime;
    }

    public long getDuration() {
        return duration;
    }
}
