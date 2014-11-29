package pct.droid.providers.media.types;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;

public class Show extends Media implements Parcelable {
    public String trailer = "";
    public Integer runtime = -1;
    public String tagline = "";
    public String synopsis = "No synopsis available";
    public String certification = "n/a";
    public HashMap<String, Torrent> torrents = new HashMap<String, Torrent>();

    public Show() {

    }

    protected Show(Parcel in) {
        super(in);
        trailer = in.readString();
        runtime = in.readInt();
        tagline = in.readString();
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
        dest.writeInt(runtime);
        dest.writeString(tagline);
        dest.writeString(synopsis);
        dest.writeString(certification);
        dest.writeInt(torrents.size());
        for (String s: torrents.keySet()) {
            dest.writeString(s);
            dest.writeParcelable(torrents.get(s), flags);
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
