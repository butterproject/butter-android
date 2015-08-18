package pct.droid.tv.events;

public class ProgressChangedEvent {

    private final long currentTime;
    private final long duration;

    public ProgressChangedEvent(long currentTime, long duration) {
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
