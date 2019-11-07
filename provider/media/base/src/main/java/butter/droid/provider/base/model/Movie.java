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

public class Movie extends Streamable {

    @Nullable private final String trailer;

    public Movie(@NonNull final String id, @NonNull final String title, final int year, @NonNull final Genre[] genres, final float rating,
            @Nullable final String poster, @NonNull final String backdrop, @NonNull final String synopsis,
            @NonNull final Torrent[] torrents, @Nullable final String trailer, @Nullable final Map<String, String> meta) {
        super(id, title, year, genres, rating, poster, backdrop, synopsis, torrents, meta);
        this.trailer = trailer;
    }


    protected Movie(Parcel in) {
        super(in);
        trailer = in.readString();
    }

    @Nullable public String getTrailer() {
        return trailer;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(trailer);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Movie)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        final Movie movie = (Movie) o;

        return trailer != null ? trailer.equals(movie.trailer) : movie.trailer == null;
    }

    @Override public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (trailer != null ? trailer.hashCode() : 0);
        return result;
    }

    public static final Creator<Movie> CREATOR = new Creator<Movie>() {
        @Override
        public Movie createFromParcel(Parcel in) {
            return new Movie(in);
        }

        @Override
        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };
}
