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
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import butter.droid.provider.base.filter.Genre;
import java.util.Arrays;

public abstract class Media implements Parcelable {

    @NonNull private final String id;
    @NonNull private final String title;
    private final int year;
    @NonNull private final Genre[] genres;

    // -1 if not set
    private final float rating;
    @Nullable private final String poster;
    @NonNull private final String backdrop;
    @NonNull private final String synopsis;

    public Media(@NonNull final String id, @NonNull final String title, final int year, @NonNull final Genre[] genres, final float rating,
            @Nullable final String poster, @NonNull final String backdrop, @NonNull final String synopsis) {
        this.id = id;
        this.title = title;
        this.year = year;
        this.genres = genres;
        this.rating = rating;
        this.poster = poster;
        this.backdrop = backdrop;
        this.synopsis = synopsis;
    }

    protected Media(Parcel in) {
        id = in.readString();
        title = in.readString();
        year = in.readInt();
        genres = in.createTypedArray(Genre.CREATOR);
        rating = in.readFloat();
        poster = in.readString();
        backdrop = in.readString();
        synopsis = in.readString();
    }

    @NonNull public String getId() {
        return id;
    }

    @NonNull public String getTitle() {
        return title;
    }

    public int getYear() {
        return year;
    }

    @NonNull public Genre[] getGenres() {
        return genres;
    }

    public float getRating() {
        return rating;
    }

    @Nullable public String getPoster() {
        return poster;
    }

    @NonNull public String getBackdrop() {
        return backdrop;
    }

    @NonNull public String getSynopsis() {
        return synopsis;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(title);
        dest.writeInt(year);
        dest.writeTypedArray(genres, flags);
        dest.writeFloat(rating);
        dest.writeString(poster);
        dest.writeString(backdrop);
        dest.writeString(synopsis);
    }

    @Override public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final Media media = (Media) o;

        if (year != media.year) {
            return false;
        }
        if (Float.compare(media.rating, rating) != 0) {
            return false;
        }
        if (!id.equals(media.id)) {
            return false;
        }
        if (!title.equals(media.title)) {
            return false;
        }
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        if (!Arrays.equals(genres, media.genres)) {
            return false;
        }
        if (poster != null ? !poster.equals(media.poster) : media.poster != null) {
            return false;
        }
        if (!backdrop.equals(media.backdrop)) {
            return false;
        }
        return synopsis.equals(media.synopsis);
    }

    @Override public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + title.hashCode();
        result = 31 * result + year;
        result = 31 * result + Arrays.hashCode(genres);
        result = 31 * result + (rating != +0.0f ? Float.floatToIntBits(rating) : 0);
        result = 31 * result + (poster != null ? poster.hashCode() : 0);
        result = 31 * result + backdrop.hashCode();
        result = 31 * result + synopsis.hashCode();
        return result;
    }

    @Override
    public int describeContents() {
        return 0;
    }

}
