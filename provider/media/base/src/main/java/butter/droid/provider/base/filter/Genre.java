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
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import butter.droid.provider.base.R;

public final class Genre implements Parcelable {

    // TODO should be defined per provider
    public static final Genre ACTION = new Genre("action", R.string.genre_action);
    public static final Genre ADVENTURE = new Genre("adventure", R.string.genre_adventure);
    public static final Genre ANIMATION = new Genre("animation", R.string.genre_animation);
    public static final Genre BIOGRAPHY = new Genre("biography", R.string.genre_biography);
    public static final Genre COMEDY = new Genre("comedy", R.string.genre_comedy);
    public static final Genre CRIME = new Genre("crime", R.string.genre_crime);
    public static final Genre DOCUMENTARY = new Genre("documentary", R.string.genre_documentary);
    public static final Genre DRAMA = new Genre("drama", R.string.genre_drama);
    public static final Genre FAMILY = new Genre("family", R.string.genre_family);
    public static final Genre FANTASY = new Genre("fantasy", R.string.genre_fantasy);
    public static final Genre FILM_NOIR = new Genre("film-noir", R.string.genre_film_noir);
    public static final Genre HISTORY = new Genre("history", R.string.genre_history);
    public static final Genre HORROR = new Genre("horror", R.string.genre_horror);
    public static final Genre MUSIC = new Genre("music", R.string.genre_music);
    public static final Genre MUSICAL = new Genre("musical", R.string.genre_musical);
    public static final Genre MYSTERY = new Genre("mystery", R.string.genre_mystery);
    public static final Genre ROMANCE = new Genre("romance", R.string.genre_romance);
    public static final Genre SCI_FI = new Genre("sci-fi", R.string.genre_sci_fi);
    public static final Genre SPORT = new Genre("sport", R.string.genre_sport);
    public static final Genre THRILLER = new Genre("thriller", R.string.genre_thriller);
    public static final Genre WAR = new Genre("war", R.string.genre_war);
    public static final Genre WESTERN = new Genre("western", R.string.genre_western);

    @NonNull private final String key;
    @StringRes private final int name;

    public Genre(@NonNull final String key, @StringRes final int name) {
        this.key = key;
        this.name = name;
    }

    private Genre(Parcel in) {
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

        final Genre genre = (Genre) o;

        return key.equals(genre.key);
    }

    @Override public int hashCode() {
        return key.hashCode();
    }

    public static final Creator<Genre> CREATOR = new Creator<Genre>() {
        @Override
        public Genre createFromParcel(Parcel in) {
            return new Genre(in);
        }

        @Override
        public Genre[] newArray(int size) {
            return new Genre[size];
        }
    };
}
