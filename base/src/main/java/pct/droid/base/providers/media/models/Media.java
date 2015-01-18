package pct.droid.base.providers.media.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;
import java.util.Map;

import pct.droid.base.providers.subs.SubsProvider;

public class Media implements Parcelable {
    public String videoId;
    public String title;
    public String year;
    public String genre;
    public String rating;
    public String type = "media";
    public String image;
    public String fullImage;
    public String headerImage;
    public Map<String, String> subtitles;

    public Media() {

    }

    public Media(Parcel in) {
        videoId = in.readString();
        title = in.readString();
        year = in.readString();
        genre = in.readString();
        rating = in.readString();
        type = in.readString();
        image = in.readString();
        fullImage = in.readString();
        headerImage = in.readString();
        int length = in.readInt();
        subtitles = new HashMap<String, String>();
        for (int i = 0; i < length; i++) {
            subtitles.put(in.readString(), in.readString());
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(videoId);
        dest.writeString(title);
        dest.writeString(year);
        dest.writeString(genre);
        dest.writeString(rating);
        dest.writeString(type);
        dest.writeString(image);
        dest.writeString(fullImage);
        dest.writeString(headerImage);
        if (subtitles != null) {
            dest.writeInt(subtitles.size());
            for (String key : subtitles.keySet()) {
                dest.writeString(key);
                dest.writeString(subtitles.get(key));
            }
        } else {
            dest.writeInt(0);
        }
    }

    @SuppressWarnings("unused")
    public static final Creator<Media> CREATOR = new Creator<Media>() {
        @Override
        public Media createFromParcel(Parcel in) {
            return new Media(in);
        }

        @Override
        public Media[] newArray(int size) {
            return new Media[size];
        }
    };

    public static class Torrent implements Parcelable {
        public String url;
        public String seeds;
        public String peers;

        public Torrent() {

        }

        public Torrent(Parcel in) {
            url = in.readString();
            seeds = in.readString();
            peers = in.readString();
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(url);
            dest.writeString(seeds);
            dest.writeString(peers);
        }

        @SuppressWarnings("unused")
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

    public SubsProvider getSubsProvider() {
        throw new AbstractMethodError("Not implemented");
    }
}
