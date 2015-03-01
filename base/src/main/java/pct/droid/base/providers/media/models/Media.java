package pct.droid.base.providers.media.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import pct.droid.base.providers.subs.SubsProvider;

public class Media implements Parcelable {
    public String videoId;
    public String imdbId;
    public String title;
    public String year;
    public String genre;
    public String rating;
    public String type = "media";
    public String image;
    public String fullImage;
    public String headerImage;
    public Map<String, String> subtitles;
    protected SubsProvider mSubsProvider = null;

    public Media() {

    }

    public Media(Parcel in) {
        videoId = in.readString();
        imdbId = in.readString();
        title = in.readString();
        year = in.readString();
        genre = in.readString();
        rating = in.readString();
        type = in.readString();
        image = in.readString();
        fullImage = in.readString();
        headerImage = in.readString();
        String className = in.readString();
        mSubsProvider = null;
        try {
            Class<?> clazz = Class.forName(className);
            mSubsProvider = (SubsProvider) clazz.newInstance();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        int length = in.readInt();
        subtitles = new HashMap<>();
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
        dest.writeString(imdbId);
        dest.writeString(title);
        dest.writeString(year);
        dest.writeString(genre);
        dest.writeString(rating);
        dest.writeString(type);
        dest.writeString(image);
        dest.writeString(fullImage);
        dest.writeString(headerImage);
        dest.writeString(mSubsProvider.getClass().getCanonicalName());
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
        public String hash;

        public Torrent() {

        }

        public Torrent(Parcel in) {
            url = in.readString();
            seeds = in.readString();
            peers = in.readString();
            hash = in.readString();
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
            dest.writeString(hash);
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
        return mSubsProvider;
    }
}
