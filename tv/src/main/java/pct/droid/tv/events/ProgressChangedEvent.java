package pct.droid.tv.events;

public class ProgressChangedEvent {

    private final long currentTime;
    private final long duration;
    private final long bufferedTime;

    public ProgressChangedEvent(long currentTime, long bufferedTime, long duration) {
        this.currentTime = currentTime;
        this.bufferedTime = bufferedTime;
        this.duration = duration;
    }

    public long getBufferedTime()
    {
        return bufferedTime;
    }

    public long getCurrentTime() {
        return currentTime;
    }

    public long getDuration() {
        return duration;
    }
}
