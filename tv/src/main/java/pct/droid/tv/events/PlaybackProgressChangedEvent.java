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
