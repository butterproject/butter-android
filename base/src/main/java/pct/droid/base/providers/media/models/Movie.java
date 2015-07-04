/*
 * This file is part of Popcorn Time.
 *
 * Popcorn Time is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Popcorn Time is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Popcorn Time. If not, see <http://www.gnu.org/licenses/>.
 */

package pct.droid.base.providers.media.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;
import java.util.Map;

import pct.droid.base.providers.media.MediaProvider;
import pct.droid.base.providers.subs.SubsProvider;

public class Movie extends Media implements Parcelable {
    public String type = "movie";
    public String trailer = "";
    public String runtime = "";
    public String synopsis = "No synopsis available";
    public String certification = "n/a";
    public Map<String, Torrent> torrents = new HashMap<String, Torrent>();

    public Movie(MediaProvider mediaProvider, SubsProvider subsProvider) {
        super(mediaProvider, subsProvider);
        isMovie = true;
    }

    protected Movie(Parcel in) {
        super(in);
        trailer = in.readString();
        runtime = in.readString();
        synopsis = in.readString();
        certification = in.readString();
        int size = in.readInt();
        for (int i = 0; i < size; i++) {
            String key = in.readString();
            Torrent torrent = in.readParcelable(Torrent.class.getClassLoader());
            torrents.put(key, torrent);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(trailer);
        dest.writeString(runtime);
        dest.writeString(synopsis);
        dest.writeString(certification);
        if (torrents != null) {
            dest.writeInt(torrents.size());
            for (String s : torrents.keySet()) {
                dest.writeString(s);
                dest.writeParcelable(torrents.get(s), flags);
            }
        } else {
            dest.writeInt(0);
        }
    }

    @SuppressWarnings("unused")
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
