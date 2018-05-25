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
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import butter.droid.provider.base.filter.Genre;
import java.util.Arrays;

public abstract class Streamable extends Media {

    @NonNull private final Torrent[] torrents;

    public Streamable(@NonNull final String id, @NonNull final String title, final int year, @NonNull final Genre[] genres,
            final float rating, @Nullable final String poster, @NonNull final String backdrop, @NonNull final String synopsis,
            @NonNull final Torrent[] torrents) {
        super(id, title, year, genres, rating, poster, backdrop, synopsis);
        this.torrents = torrents;
    }

    protected Streamable(Parcel in) {
        super(in);
        torrents = in.createTypedArray(Torrent.CREATOR);
    }

    @NonNull public Torrent[] getTorrents() {
        return torrents;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeTypedArray(torrents, flags);
    }

    @Override public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Streamable)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        final Streamable that = (Streamable) o;

        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        return Arrays.equals(torrents, that.torrents);
    }

    @Override public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + Arrays.hashCode(torrents);
        return result;
    }

    @Override
    public int describeContents() {
        return 0;
    }

}
