package pct.droid.base.providers.media.types;

import android.os.Parcel;
import android.os.Parcelable;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

public class Show extends Media implements Parcelable {
    public String type = "show";
    public String trailer = "";
    public Integer airDay = -1;
    public String airTime = "";
    public String status = "";
    public String runtime = "";
    public String network = "";
    public String country = "";
    public String tvdbId = "";
    public String synopsis = "No synopsis available";
    public String certification = "n/a";
    public Integer seasons = 0;
    public ArrayList<Episode> episodes = new ArrayList<Episode>();

    public Show() {

    }

    protected Show(Parcel in) {
        super(in);
        trailer = in.readString();
        airDay = in.readInt();
        airTime = in.readString();
        runtime = in.readString();
        status = in.readString();
        network = in.readString();
        country = in.readString();
        tvdbId = in.readString();
        synopsis = in.readString();
        certification = in.readString();
        seasons = in.readInt();
        int size = in.readInt();
        for (int i = 0; i < size; i++) {
            Episode episode = in.readParcelable(Episode.class.getClassLoader());
            episodes.add(episode);
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
        dest.writeInt(airDay);
        dest.writeString(airTime);
        dest.writeString(runtime);
        dest.writeString(status);
        dest.writeString(network);
        dest.writeString(country);
        dest.writeString(tvdbId);
        dest.writeString(synopsis);
        dest.writeString(certification);
        dest.writeInt(seasons);
        dest.writeInt(episodes.size());
        for (Episode e: episodes) {
            dest.writeParcelable(e, flags);
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

    public static class Episode implements Parcelable {
        public int aired;
        public int episode;
        public int season;
        public String tvdbId;
        public HashMap<String, Torrent> torrents;

        protected Episode(Parcel in) {
            aired = in.readInt();
            episode = in.readInt();
            season = in.readInt();
            tvdbId = in.readString();
            torrents = (HashMap) in.readValue(HashMap.class.getClassLoader());
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(aired);
            dest.writeInt(episode);
            dest.writeInt(season);
            dest.writeString(tvdbId);
            dest.writeValue(torrents);
        }

        @SuppressWarnings("unused")
        public static final Parcelable.Creator<Episode> CREATOR = new Parcelable.Creator<Episode>() {
            @Override
            public Episode createFromParcel(Parcel in) {
                return new Episode(in);
            }

            @Override
            public Episode[] newArray(int size) {
                return new Episode[size];
            }
        };
    }
}
