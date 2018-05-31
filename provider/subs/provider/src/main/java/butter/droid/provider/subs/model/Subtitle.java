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

package butter.droid.provider.subs.model;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class Subtitle implements Parcelable {

    @NonNull private final String language; // ISO language code
    @NonNull private final String name; // Usually full language name

    /**
     * Additional meta data that can be used when downloading subtitles;
     */
    @Nullable private final Map<String, String> meta;

    public Subtitle(@NonNull final String language, @NonNull final String name) {
        this(language, name, null);
    }

    public Subtitle(@NonNull final String language, @NonNull final String name, @Nullable final Map<String, String> meta) {
        this.language = language;
        this.name = name;
        this.meta = meta;
    }

    protected Subtitle(Parcel in) {
        language = in.readString();
        name = in.readString();
        meta = new HashMap<>();
        in.readMap(meta, Map.class.getClassLoader());
    }

    public String getLanguage() {
        return language;
    }

    public String getName() {
        return name;
    }

    @Nullable public Map<String, String> getMeta() {
        return meta;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(language);
        dest.writeString(name);
        dest.writeMap(meta);
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

        final Subtitle subtitle = (Subtitle) o;

        if (!language.equals(subtitle.language)) {
            return false;
        }
        return name.equals(subtitle.name);
    }

    @Override public int hashCode() {
        int result = language.hashCode();
        result = 31 * result + name.hashCode();
        return result;
    }

    public static final Creator<Subtitle> CREATOR = new Creator<Subtitle>() {
        @Override
        public Subtitle createFromParcel(Parcel in) {
            return new Subtitle(in);
        }

        @Override
        public Subtitle[] newArray(int size) {
            return new Subtitle[size];
        }
    };

}
