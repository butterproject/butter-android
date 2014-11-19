package pct.droid.providers.media;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import pct.droid.providers.BaseProvider;

public abstract class MediaProvider extends BaseProvider {

    public static class Video implements Parcelable {
        public String imdbId;
        public String title;
        public String year;
        public String genre;
        public String rating;
        public String type;
        public String image;
        public String fullImage;
        public String headerImage;
        public Map<String, String> subtitles;

        public Video() {

        }

        public Video(Parcel in) {
            imdbId = in.readString();
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
            for(int i = 0; i < length; i++) {
                subtitles.put(in.readString(), in.readString());
            }
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(imdbId);
            dest.writeString(title);
            dest.writeString(year);
            dest.writeString(genre);
            dest.writeString(rating);
            dest.writeString(type);
            dest.writeString(image);
            dest.writeString(fullImage);
            dest.writeString(headerImage);
            dest.writeInt(subtitles.size());
            for(String key : subtitles.keySet()) {
                dest.writeString(key);
                dest.writeString(subtitles.get(key));
            }
        }

        @SuppressWarnings("unused")
        public static final Parcelable.Creator<Video> CREATOR = new Parcelable.Creator<Video>() {
            @Override
            public Video createFromParcel(Parcel in) {
                return new Video(in);
            }

            @Override
            public Video[] newArray(int size) {
                return new Video[size];
            }
        };
    }

    public static class Torrent implements Parcelable {
        public String url;
        public String magnet;
        public String size;
        public String fileSize;
        public String seeds;
        public String peers;

        public Torrent() {

        }

        public Torrent(Parcel in) {
            url = in.readString();
            magnet = in.readString();
            size = in.readString();
            fileSize = in.readString();
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
            dest.writeString(magnet);
            dest.writeString(size);
            dest.writeString(fileSize);
            dest.writeString(seeds);
            dest.writeString(peers);
        }

        @SuppressWarnings("unused")
        public static final Parcelable.Creator<Torrent> CREATOR = new Parcelable.Creator<Torrent>() {
            @Override
            public Torrent createFromParcel(Parcel in) {
                return new MediaProvider.Torrent(in);
            }

            @Override
            public Torrent[] newArray(int size) {
                return new Torrent[size];
            }
        };
    }

    public void getList(HashMap<String, String> filters, Callback callback) {
        getList(null, filters, callback);
    }

    public abstract void getList(ArrayList<MediaProvider.Video> currentList, HashMap<String, String> filters, Callback callback);
    public abstract void getDetail(String torrentId, Callback callback);

    public interface Callback {
        public void onSuccess(ArrayList<Video> items);
        public void onFailure(Exception e);
    }

}
