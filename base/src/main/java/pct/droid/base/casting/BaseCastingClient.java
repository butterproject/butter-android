/*
 * This file is part of Popcorn Time.
 *
 * Popcorn Time is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Popcorn Time is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Popcorn Time. If not, see <http://www.gnu.org/licenses/>.
 */

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
