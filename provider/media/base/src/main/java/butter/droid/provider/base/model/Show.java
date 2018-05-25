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

public class Show extends Media {

    @NonNull private final Season[] seasons;

    public Show(@NonNull final String id, @NonNull final String title, final int year, @NonNull final Genre[] genres, final float rating,
            @Nullable final String poster, @NonNull final String backdrop, @NonNull final String synopsis,
            @NonNull final Season[] seasons) {
        super(id, title, year, genres, rating, poster, backdrop, synopsis);
        this.seasons = seasons;
    }

    protected Show(Parcel in) {
        super(in);
        seasons = in.createTypedArray(Season.CREATOR);
    }

    @NonNull public Season[] getSeasons() {
        return seasons;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeTypedArray(seasons, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Show)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        final Show show = (Show) o;

        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        return Arrays.equals(seasons, show.seasons);
    }

    @Override public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + Arrays.hashCode(seasons);
        return result;
    }

    public static final Creator<Show> CREATOR = new Creator<Show>() {
        @Override
        public Show createFromParcel(Parcel in) {
            return new Show(in);
        }

        @Override
        public Show[] newArray(int size) {
            return new Show[size];
        }
    };
}
