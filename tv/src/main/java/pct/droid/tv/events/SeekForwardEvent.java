package pct.droid.tv.events;

public class SeekForwardEvent {
    public static final int MINIMUM_SEEK_SPEED = 10000;
    private int seek = MINIMUM_SEEK_SPEED;

    public SeekForwardEvent() {
        setSeek(MINIMUM_SEEK_SPEED);
    }

    public void setSeek(int seek) {
        if (seek < MINIMUM_SEEK_SPEED) throw new IllegalArgumentException("Seek speed must be larger than SeekBackwardEvent.MINIMUM_SEEK_SPEED");
        if (seek % MINIMUM_SEEK_SPEED != 0) throw new IllegalArgumentException("Seek speed must be multiplication of SeekBackwardEvent.MINIMUM_SEEK_SPEED");
        this.seek = seek;
    }

    public int getSeek() {
        return seek;
    }
}
