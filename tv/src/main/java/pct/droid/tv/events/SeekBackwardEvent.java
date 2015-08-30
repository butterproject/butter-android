package pct.droid.tv.events;

public class SeekBackwardEvent {
    public static final int SEEK_SPEED = 10000;
    private int seek = SEEK_SPEED;

    public void setSeek(int seek) {
        if (seek < SEEK_SPEED) throw new IllegalArgumentException("Seek speed must be larger than SeekBackwardEvent.SEEK_SPEED");
        if (seek % SEEK_SPEED != 0) throw new IllegalArgumentException("Seek speed must be multiplication of SeekBackwardEvent.SEEK_SPEED");
        this.seek = seek;
    }

    public int getSeek() {
        return seek;
    }
}
