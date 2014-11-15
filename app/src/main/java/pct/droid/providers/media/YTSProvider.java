package pct.droid.providers.media;

import android.accounts.NetworkErrorException;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.google.gson.internal.LinkedTreeMap;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

import pct.droid.providers.meta.TraktProvider;
import pct.droid.utils.LogUtils;

public class YTSProvider extends MediaProvider {

    public static class Video extends MediaProvider.Video implements Parcelable {
        public String trailer = "";
        public Integer runtime = -1;
        public String tagline = "";
        public String synopsis = "No synopsis available";
        public String certification = "n/a";
        public String fullImage;
        public String headerImage;
        public HashMap<String, Torrent> torrents = new HashMap<String, Torrent>();

        protected Video() {

        }

        protected Video(Parcel in) {
            super(in);
            trailer = in.readString();
            runtime = in.readInt();
            tagline = in.readString();
            synopsis = in.readString();
            certification = in.readString();
            fullImage = in.readString();
            headerImage = in.readString();
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
            dest.writeString(fullImage);
            dest.writeString(headerImage);
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

    protected String mApiUrl = "http://yts.re/api/";
    protected String mMirrorApiUrl = "https://yts.wf/api/";
    public static final String NO_MOVIES_ERROR = "No movies found";

    @Override
    public Call getList(final ArrayList<MediaProvider.Video> existingList, HashMap<String, String> filters, final Callback callback) {
        final ArrayList<MediaProvider.Video> currentList;
        if(existingList == null) {
            currentList = new ArrayList<MediaProvider.Video>();
        } else {
            currentList = (ArrayList<MediaProvider.Video>) existingList.clone();
        }

        ArrayList<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
        params.add(new BasicNameValuePair("limit", "30"));

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
        LogUtils.d("YTS query: " + query);
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
                    LogUtils.d(responseStr);
                    YTSReponse result = mGson.fromJson(responseStr, YTSReponse.class);
                    if(result.status != null && result.status.equals("fail")) {
                        callback.onFailure(new NetworkErrorException(result.error));
                    } else {
                        int previousSize = currentList.size();
                        ArrayList<MediaProvider.Video> formattedData = result.formatForPopcorn(currentList);
                        int newDataSize = formattedData.size() - previousSize;

                        // Only get metdata for new items in list
                        String[] imdbIds = new String[newDataSize];
                        for(int i = previousSize, index = 0; i < formattedData.size(); i++, index++) {
                            Video video = (Video) formattedData.get(i);
                            imdbIds[index] = video.imdbId;
                        }

                        TraktProvider traktProvider = new TraktProvider();
                        TraktProvider.MetaData[] metaDatas = traktProvider.getSummaries(imdbIds, "movie", "normal");

                        for(int i = previousSize, index = 0; i < formattedData.size(); i++) {
                            Video video = (Video) formattedData.get(i);
                            TraktProvider.MetaData meta = metaDatas[index];
                            if(video.imdbId.equals(meta.imdb_id)) {
                                if (meta.images.containsKey("poster")) {
                                    video.image = meta.images.get("poster").replace(".jpg", "-300.jpg");
                                    video.fullImage = meta.images.get("poster");
                                }

                                if (meta.images.containsKey("fanart")) {
                                    video.headerImage = meta.images.get("fanart").replace(".jpg", "-940.jpg");
                                }

                                if (meta.title != null) {
                                    video.title = meta.title;
                                }
                                formattedData.set(i, video);
                                index++;
                            } else {
                                video.fullImage = video.image;
                                video.headerImage = video.image;
                                formattedData.set(i, video);
                            }
                        }

                        callback.onSuccess(currentList);
                    }
                } else {
                    callback.onFailure(new NetworkErrorException(response.body().string()));
                }
            }
        });
    }

    @Override
    public Call getDetail(String imdbId, final Callback callback) {
        Request.Builder requestBuilder = new Request.Builder();
        requestBuilder.url(mApiUrl + "listimdb.json?imdb_id=" + imdbId);

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
                    if(result.status != null && result.status.equals("fail")) {
                        callback.onFailure(new NetworkErrorException(result.error));
                    } else {
                        ArrayList<MediaProvider.Video> formattedData = result.formatForPopcorn();

                        TraktProvider traktProvider = new TraktProvider();
                        for (MediaProvider.Video item : formattedData) {
                            Video video = (Video) item;
                            int index = formattedData.indexOf(item);

                            TraktProvider.MetaData meta = traktProvider.getSummary(video.imdbId, "movie");
                            if (meta.images != null && meta.images.containsKey("poster")) {
                                video.image = meta.images.get("poster").replace(".jpg", "-300.jpg");
                                video.fullImage = meta.images.get("poster");
                            } else {
                                video.fullImage = video.image;
                            }

                            if (meta.images != null && meta.images.containsKey("fanart")) {
                                video.headerImage = meta.images.get("fanart").replace(".jpg", "-940.jpg");
                            } else {
                                video.headerImage = video.image;
                            }

                            if (meta.title != null) {
                                video.title = meta.title;
                            }

                            if (meta.overview != null) {
                                video.synopsis = meta.overview;
                            }

                            if (meta.tagline != null) {
                                video.tagline = meta.tagline;
                            }

                            if (meta.trailer != null) {
                                video.trailer = meta.trailer;
                            }

                            if (meta.runtime != null) {
                                video.runtime = meta.runtime;
                            }

                            if (meta.certification != null) {
                                video.certification = meta.certification;
                            }
                            formattedData.set(index, video);
                        }

                        callback.onSuccess(formattedData);
                    }
                } else {
                    callback.onFailure(new NetworkErrorException(response.body().string()));
                }
            }
        });
    }

    private class YTSReponse {
        public String status;
        public String error;
        public ArrayList<LinkedTreeMap<String, Object>> MovieList;

        private int isInResults(ArrayList<MediaProvider.Video> results, String id) {
            int i = 0;
            for(MediaProvider.Video item : results) {
                if(item.imdbId.equals(id)) return i;
                i++;
            }
            return -1;
        }

        public ArrayList<MediaProvider.Video> formatForPopcorn() {
            return formatForPopcorn(new ArrayList<MediaProvider.Video>());
        }

        public ArrayList<MediaProvider.Video> formatForPopcorn(ArrayList<MediaProvider.Video> existingList) {
            for(LinkedTreeMap<String, Object> item : MovieList) {
                Video video = new Video();

                video.imdbId = item.get("ImdbCode").toString();
                String torrentQuality = item.get("Quality").toString();

                if(torrentQuality.equals("3D")) {
                    continue;
                }

                Torrent torrent = new Torrent();
                torrent.url = item.get("TorrentUrl").toString();
                torrent.magnet = item.get("TorrentMagnetUrl").toString();
                torrent.size = item.get("SizeByte").toString();
                torrent.fileSize = item.get("Size").toString();
                torrent.seeds = item.get("TorrentSeeds").toString();
                torrent.peers = item.get("TorrentPeers").toString();

                int existingItem = isInResults(existingList, video.imdbId);
                if(existingItem == -1) {
                    video.image = item.get("CoverImage").toString().replace("_med.", "_large.");
                    video.title = item.get("MovieTitleClean").toString();//.replaceAll("([^)]*)|1080p|DIRECTORS CUT|EXTENDED|UNRATED|3D|[()]", "");
                    video.year = item.get("MovieYear").toString();
                    video.genre = item.get("Genre").toString();
                    video.rating = item.get("MovieRating").toString();
                    video.type = "movie";
                } else {
                    video = (Video) existingList.get(existingItem);
                }

                if(!video.torrents.containsKey(torrentQuality)) {
                    video.torrents.put(torrentQuality, torrent);
                }

                if(existingItem == -1) {
                    existingList.add(video);
                } else {
                    existingList.set(existingItem, video);
                }
            }
            return existingList;
        }
    }

}
