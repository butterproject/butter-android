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

public class Format implements Parcelable {

    public static final int FORMAT_NORMAL = 0;
    public static final int FORMAT_3D = 1;

    public static final int QUALITY_HD = 720;
    public static final int QUALITY_FULL_HD = 1080;
    public static final int QUALITY_4K = 2160;

    private final int quality;
    private final int type;

    public Format(final int quality, final int type) {
        this.quality = quality;
        this.type = type;
    }

    protected Format(Parcel in) {
        quality = in.readInt();
        type = in.readInt();
    }

    public int getQuality() {
        return quality;
    }

    public int getType() {
        return type;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(quality);
        dest.writeInt(type);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final Format format = (Format) o;

        if (quality != format.quality) {
            return false;
        }
        return type == format.type;
    }

    @Override public int hashCode() {
        int result = quality;
        result = 31 * result + type;
        return result;
    }

    public static final Creator<Format> CREATOR = new Creator<Format>() {
        @Override
        public Format createFromParcel(Parcel in) {
            return new Format(in);
        }

        @Override
        public Format[] newArray(int size) {
            return new Format[size];
        }
    };
}
