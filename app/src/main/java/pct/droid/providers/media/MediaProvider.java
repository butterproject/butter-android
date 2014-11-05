package pct.droid.providers.media;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.Gson;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public abstract class MediaProvider {

    protected OkHttpClient mClient = new OkHttpClient();
    protected Gson mGson = new Gson();
    private String mApiUrl;
    public static final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json");
    public static final MediaType MEDIA_TYPE_XML = MediaType.parse("application/xml");

    public static class Video implements Parcelable {
        public String imdbId;
        public String title;
        public String year;
        public String genre;
        public String rating;
        public String image;
        public String type;

        protected Video() {

        }

        protected Video(Parcel in) {
            imdbId = in.readString();
            title = in.readString();
            year = in.readString();
            genre = in.readString();
            rating = in.readString();
            image = in.readString();
            type = in.readString();
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
            dest.writeString(image);
            dest.writeString(type);
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
        public String size;
        public String fileSize;
        public String seeds;
        public String peers;

        protected Torrent() {

        }

        protected Torrent(Parcel in) {
            url = in.readString();
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

    protected Call enqueue(Request request, com.squareup.okhttp.Callback requestCallback) {
        Call call = mClient.newCall(request);
        call.enqueue(requestCallback);
        return call;
    }

    protected String buildQuery(List<BasicNameValuePair> valuePairs) {
        StringBuilder stringBuilder = new StringBuilder();

        for(int i = 0; i < valuePairs.size(); i++) {
            NameValuePair pair = valuePairs.get(i);
            stringBuilder.append(pair.getName());
            stringBuilder.append("=");
            stringBuilder.append(pair.getValue());
            if(i + 1 != valuePairs.size()) stringBuilder.append("&");
        }

        return stringBuilder.toString();
    }

    public abstract Call getList(HashMap<String, String> filters, Callback callback);
    public abstract Call getDetail(String torrentId, Callback callback);

    public interface Callback {
        public void onSuccess(TreeMap<String, Video> items);
        public void onFailure(Exception exception);
    }

}
