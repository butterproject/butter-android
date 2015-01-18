package pct.droid.base.casting;

import pct.droid.base.providers.media.models.Media;

public abstract class BaseCastingClient {

    public abstract void loadMedia(Media media, String location, float position);

    public abstract void play();

    public abstract void pause();

    public abstract void seek(float position);

    public abstract void stop();

    public abstract void connect(CastingDevice device);

    public abstract void disconnect();

    public abstract void setVolume(float volume);

    public abstract boolean canControlVolume();

    public void loadMedia(Media media, String location) {
        loadMedia(media, location, 0);
    }

}
