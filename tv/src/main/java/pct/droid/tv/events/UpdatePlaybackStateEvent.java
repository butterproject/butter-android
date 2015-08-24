package pct.droid.tv.events;

public class UpdatePlaybackStateEvent {

    private boolean isPlaying;

    public UpdatePlaybackStateEvent(boolean isPlaying) {
        this.isPlaying = isPlaying;
    }

    public boolean isPlaying() {
        return isPlaying;
    }
}
