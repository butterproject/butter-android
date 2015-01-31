package pct.droid.base.providers.media.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pct.droid.base.providers.subs.SubsProvider;

public class Show extends Media implements Parcelable {
    public enum Status { CONTINUING, ENDED, CANCELED }

    public String type = "show";
    public String imdbId = "";
    public String airDay = "";
    public String airTime = "";
    public Status status = null;
    public String runtime = "";
    public String network = "";
    public String country = "";
    public String tvdbId = "";
    public String synopsis = "No synopsis available";
    public String certification = "n/a";
    public Integer seasons = 0;
    public List<Episode> episodes = new ArrayList<>();

    public Show() {

    }

    protected Show(Parcel in) {
        super(in);
        airDay = in.readString();
        airTime = in.readString();
        runtime = in.readString();

        int statusInt = in.readInt();
        if(statusInt == 0) {
            status = Status.CONTINUING;
        } else if(statusInt == 1) {
            status = Status.ENDED;
        } else {
            status = null;
        }

        network = in.readString();
        country = in.readString();
        tvdbId = in.readString();
        imdbId = in.readString();
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
        dest.writeString(airDay);
        dest.writeString(airTime);
        dest.writeString(runtime);
        dest.writeInt(status.ordinal());
        dest.writeString(network);
        dest.writeString(country);
        dest.writeString(tvdbId);
        dest.writeString(imdbId);
        dest.writeString(synopsis);
        dest.writeString(certification);
        dest.writeInt(seasons == null ? 0 : seasons);
        dest.writeInt(episodes.size());
        for (Episode episode : episodes) {
            dest.writeParcelable(episode, flags);
        }
    }

    @SuppressWarnings("unused")
    public static final Creator<Show> CREATOR = new Creator<Show>() {
        @Override
        public Show createFromParcel(Parcel in) {
            return new Show(in);
        }

        @Override
        public Show[] newArray(int size) {
            return new Show[size];
        }
    };

    public static class Episode extends Media implements Parcelable {
        public int aired;
        public int episode;
        public int season;
        public String overview;
        public String tvdbId;
        public boolean dateBased;
        public Map<String, Torrent> torrents = new HashMap<String, Torrent>();

        public Episode() {

        }

        protected Episode(Parcel in) {
            super(in);
            aired = in.readInt();
            episode = in.readInt();
            season = in.readInt();
            title = in.readString();
            overview = in.readString();
            tvdbId = in.readString();
            dateBased = in.readInt() == 1;
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
            dest.writeInt(aired);
            dest.writeInt(episode);
            dest.writeInt(season);
            dest.writeString(title);
            dest.writeString(overview);
            dest.writeString(tvdbId);
            dest.writeInt(dateBased ? 1 : 0);
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
