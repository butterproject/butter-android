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
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;

public final class Sorter implements Parcelable {

    @NonNull private final String key;
    @StringRes private final int name;

    public Sorter(@NonNull final String key, @StringRes final int name) {
        this.key = key;
        this.name = name;
    }

    private Sorter(Parcel in) {
        key = in.readString();
        name = in.readInt();
    }

    @NonNull public String getKey() {
        return key;
    }

    @StringRes public int getName() {
        return name;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(key);
        dest.writeInt(name);
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

        final Sorter sorter = (Sorter) o;

        return key.equals(sorter.key);
    }

    @Override public int hashCode() {
        return key.hashCode();
    }

    public static final Creator<Sorter> CREATOR = new Creator<Sorter>() {
        @Override
        public Sorter createFromParcel(Parcel in) {
            return new Sorter(in);
        }

        @Override
        public Sorter[] newArray(int size) {
            return new Sorter[size];
        }
    };
}
