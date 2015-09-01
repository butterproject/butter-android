package pct.droid.tv.events;

public class ToggleSubtitleEvent {
    private boolean enabled;

    public ToggleSubtitleEvent(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return this.enabled;
    }
}