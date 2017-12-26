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

package butter.droid.provider.base.filter;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;

public final class Filter implements Parcelable {

    @Nullable private final Genre genre;
    @Nullable private final Sorter sorter;
    @Nullable private String query;

    public Filter(@Nullable final Genre genre, @Nullable final Sorter sorter, @Nullable final String query) {
        this.genre = genre;
        this.sorter = sorter;
        this.query = query;
    }

    public Filter(@Nullable final Genre genre, @Nullable final Sorter sorter) {
        this(genre, sorter, null);
    }

    private Filter(Parcel in) {
        genre = in.readParcelable(Genre.class.getClassLoader());
        sorter = in.readParcelable(Sorter.class.getClassLoader());
        query = in.readString();
    }

    @Nullable public Genre getGenre() {
        return genre;
    }

    @Nullable public Sorter getSorter() {
        return sorter;
    }

    @Nullable public String getQuery() {
        return query;
    }

    public void setQuery(@Nullable final String query) {
        this.query = query;
    }

    @Override public int describeContents() {
        return 0;
    }

    @Override public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeParcelable(genre, flags);
        dest.writeParcelable(sorter, flags);
        dest.writeString(query);
    }

    @Override public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final Filter filter = (Filter) o;

        if (genre != null ? !genre.equals(filter.genre) : filter.genre != null) {
            return false;
        }
        if (sorter != null ? !sorter.equals(filter.sorter) : filter.sorter != null) {
            return false;
        }
        return query != null ? query.equals(filter.query) : filter.query == null;
    }

    @Override public int hashCode() {
        int result = genre != null ? genre.hashCode() : 0;
        result = 31 * result + (sorter != null ? sorter.hashCode() : 0);
        result = 31 * result + (query != null ? query.hashCode() : 0);
        return result;
    }

    public static final Creator<Filter> CREATOR = new Creator<Filter>() {
        @Override
        public Filter createFromParcel(Parcel in) {
            return new Filter(in);
        }

        @Override
        public Filter[] newArray(int size) {
            return new Filter[size];
        }
    };

}
