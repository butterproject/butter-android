/*
 * This file is part of Butter.
 *
 * Butter is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Butter is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Butter. If not, see <http://www.gnu.org/licenses/>.
 */

package butter.droid.provider.base.model;

import android.os.Parcel;

import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import butter.droid.provider.base.filter.Genre;

public class Episode extends Streamable {

    private final int episode;

    public Episode(@NonNull final String id, @NonNull final String title, final int year, @NonNull final Genre[] genres, final float rating,
            @Nullable final String poster, @NonNull final String backdrop, @NonNull final String synopsis,
            @NonNull final Torrent[] torrents, final int episode, @Nullable final Map<String, String> meta) {
        super(id, title, year, genres, rating, poster, backdrop, synopsis, torrents, meta);
        this.episode = episode;
    }

    protected Episode(Parcel in) {
        super(in);
        episode = in.readInt();
    }

    public int getEpisode() {
        return episode;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(episode);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Episode)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        final Episode episode1 = (Episode) o;

        return episode == episode1.episode;
    }

    @Override public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + episode;
        return result;
    }

    public static final Creator<Episode> CREATOR = new Creator<Episode>() {
        @Override
        public Episode createFromParcel(Parcel in) {
            return new Episode(in);
        }

        @Override
        public Episode[] newArray(int size) {
            return new Episode[size];
        }
    };
}
