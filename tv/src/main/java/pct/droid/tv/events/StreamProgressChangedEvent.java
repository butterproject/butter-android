package pct.droid.tv.events;

public class StreamProgressChangedEvent {
    private final long bufferedTime;

    public StreamProgressChangedEvent(long bufferedTime) {
        if (bufferedTime < 0) throw new IllegalArgumentException("buffered time must be larger or equal than 0");
        this.bufferedTime = bufferedTime;
    }

    public long getBufferedTime()
    {
        return bufferedTime;
    }
}
