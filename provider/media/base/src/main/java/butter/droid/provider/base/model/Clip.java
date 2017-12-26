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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import butter.droid.provider.base.filter.Genre;

public class Clip extends Media {

    @NonNull private final String videoUrl;

    public Clip(@NonNull final String id, @NonNull final String title, final int year, @NonNull final Genre[] genres, final float rating,
            @Nullable final String poster, @NonNull final String backdrop, @NonNull final String synopsis, @NonNull final String videoUrl) {
        super(id, title, year, genres, rating, poster, backdrop, synopsis);
        this.videoUrl = videoUrl;
    }

    protected Clip(Parcel in) {
        super(in);
        videoUrl = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(videoUrl);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Clip)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        final Clip clip = (Clip) o;

        return videoUrl.equals(clip.videoUrl);
    }

    @Override public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + videoUrl.hashCode();
        return result;
    }

    public static final Creator<Clip> CREATOR = new Creator<Clip>() {
        @Override
        public Clip createFromParcel(Parcel in) {
            return new Clip(in);
        }

        @Override
        public Clip[] newArray(int size) {
            return new Clip[size];
        }
    };
}
