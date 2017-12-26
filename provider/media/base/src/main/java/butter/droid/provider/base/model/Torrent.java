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

public class Torrent implements Parcelable {

    @NonNull private final String url;
    @NonNull private final Format format;
    private final int size;

    // -1 if not set
    private final long fileSize;

    // -1 if not set
    private final int peers;

    // -1 if not set
    private final int seeds;

    public Torrent(@NonNull final String url, @NonNull final Format format, final int size) {
        this(url, format, size, -1, -1, -1);
    }

    public Torrent(@NonNull final String url, @NonNull final Format format, final int size, final long fileSize, final int peers,
            final int seeds) {
        this.url = url;
        this.format = format;
        this.size = size;
        this.fileSize = fileSize;
        this.peers = peers;
        this.seeds = seeds;
    }

    protected Torrent(Parcel in) {
        url = in.readString();
        format = in.readParcelable(Format.class.getClassLoader());
        size = in.readInt();
        fileSize = in.readLong();
        peers = in.readInt();
        seeds = in.readInt();
    }

    @NonNull public String getUrl() {
        return url;
    }

    @NonNull public Format getFormat() {
        return format;
    }

    public int getPeers() {
        return peers;
    }

    public int getSeeds() {
        return seeds;
    }

    @Override public int describeContents() {
        return 0;
    }

    @Override public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeString(url);
        dest.writeParcelable(format, 0);
        dest.writeInt(size);
        dest.writeLong(fileSize);
        dest.writeInt(peers);
        dest.writeInt(seeds);
    }

    @Override public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final Torrent torrent = (Torrent) o;

        return url.equals(torrent.url);
    }

    @Override public int hashCode() {
        return url.hashCode();
    }

    public static final Creator<Torrent> CREATOR = new Creator<Torrent>() {
        @Override
        public Torrent createFromParcel(Parcel in) {
            return new Torrent(in);
        }

        @Override
        public Torrent[] newArray(int size) {
            return new Torrent[size];
        }
    };

}
