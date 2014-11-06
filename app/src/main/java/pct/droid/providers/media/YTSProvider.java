package pct.droid.providers.media;

import android.accounts.NetworkErrorException;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.internal.LinkedTreeMap;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class YTSProvider extends MediaProvider {

    private String mApiUrl = "https://yts.wf/api/";
    private ArrayList<MediaProvider.Video> mResults;

    public static class Video extends MediaProvider.Video implements Parcelable {
        public HashMap<String, Torrent> torrents = new HashMap<String, Torrent>();

        protected Video() {

        }

        protected Video(Parcel in) {
            super(in);
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
            dest.writeInt(torrents.size());
            for (String s: torrents.keySet()) {
                dest.writeString(s);
                dest.writeParcelable(torrents.get(s), flags);
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

    public YTSProvider() {
        mResults = new ArrayList<MediaProvider.Video>();
    }

    @Override
    public Call getList(ArrayList<MediaProvider.Video> currentList, HashMap<String, String> filters, final Callback callback) {
        if(currentList != null) {
            mResults = currentList;
        } else {
            mResults = new ArrayList<MediaProvider.Video>();
        }

        ArrayList<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
        params.add(new BasicNameValuePair("limit", "50"));

        if(filters == null) {
            filters = new HashMap<String, String>();
        }

        if (filters.containsKey("keywords")) {
            String keywords = filters.get("keywords");
            keywords = keywords.replaceAll("\\s", "% ");
            params.add(new BasicNameValuePair("keywords", keywords));
        }

        if (filters.containsKey("genre")) {
            params.add(new BasicNameValuePair("genre", filters.get("genre")));
        }

        if (filters.containsKey("order")) {
            params.add(new BasicNameValuePair("order", filters.get("order")));
        } else {
            params.add(new BasicNameValuePair("order", "desc"));
        }

        if (filters.containsKey("sort") && !filters.get("sort").equals("popularity")) {
            params.add(new BasicNameValuePair("sort", filters.get("sort")));
        } else {
            params.add(new BasicNameValuePair("sort", "seeds"));
        }

        if (filters.containsKey("page")) {
            params.add(new BasicNameValuePair("set", filters.get("page")));
        }

        Request.Builder requestBuilder = new Request.Builder();
        String query = buildQuery(params);
        requestBuilder.url(mApiUrl + "list.json?" + query);

        return enqueue(requestBuilder.build(), new com.squareup.okhttp.Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                callback.onFailure(e);
            }

            @Override
            public void onResponse(Response response) throws IOException {
                if(response.isSuccessful()) {
                    String responseStr = response.body().string();
                    YTSReponse result = mGson.fromJson(responseStr, YTSReponse.class);
                    formatForPopcorn(result.MovieList);
                    callback.onSuccess(mResults);
                } else {
                    callback.onFailure(new NetworkErrorException());
                }
            }
        });
    }

    @Override
    public Call getDetail(String torrentId, Callback callback) {
        return null;
    }

    private void formatForPopcorn(ArrayList<LinkedTreeMap<String, Object>> list) {
        for(LinkedTreeMap<String, Object> item : list) {
            Video video = new Video();
            video.imdbId = item.get("ImdbCode").toString();
            String torrentQuality = item.get("Quality").toString();

            if(isInResults(video.imdbId) || torrentQuality.equals("3D")) {
                continue;
            }

            video.image = item.get("CoverImage").toString().replace("_med.", "_large.");
            video.title = item.get("MovieTitleClean").toString();//.replaceAll("([^)]*)|1080p|DIRECTORS CUT|EXTENDED|UNRATED|3D|[()]", "");
            video.year = item.get("MovieYear").toString();
            video.genre = item.get("Genre").toString();
            video.rating = item.get("MovieRating").toString();
            video.type = "movie";

            mResults.add(video);
        }
    }

    private boolean isInResults(String id) {
        for(MediaProvider.Video item : mResults) {
            if(item.imdbId.equals(id)) return true;
        }
        return false;
    }

    private class YTSReponse {
        public ArrayList<LinkedTreeMap<String, Object>> MovieList;
    }

}
