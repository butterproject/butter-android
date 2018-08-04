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

import java.util.Arrays;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import butter.droid.provider.base.filter.Genre;

public class Season extends Media {

    @NonNull private final Episode[] episodes;

    public Season(@NonNull final String id, @NonNull final String title, final int year, @NonNull final Genre[] genres, final float rating,
            @Nullable final String poster, @NonNull final String backdrop, @NonNull final String synopsis,
            @NonNull final Episode[] episodes, @Nullable final Map<String, String> meta) {
        super(id, title, year, genres, rating, poster, backdrop, synopsis, meta);
        this.episodes = episodes;
    }

    protected Season(Parcel in) {
        super(in);
        episodes = in.createTypedArray(Episode.CREATOR);
    }

    @NonNull public Episode[] getEpisodes() {
        return episodes;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeTypedArray(episodes, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Season)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        final Season season = (Season) o;

        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        return Arrays.equals(episodes, season.episodes);
    }

    @Override public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + Arrays.hashCode(episodes);
        return result;
    }

    public static final Creator<Season> CREATOR = new Creator<Season>() {
        @Override
        public Season createFromParcel(Parcel in) {
            return new Season(in);
        }

        @Override
        public Season[] newArray(int size) {
            return new Season[size];
        }
    };
}
