package pct.droid.base.providers.media;

import android.accounts.NetworkErrorException;

import com.google.gson.internal.LinkedTreeMap;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.apache.http.message.BasicNameValuePair;

import java.io.EOFException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import pct.droid.base.providers.media.types.Media;
import pct.droid.base.providers.media.types.Movie;
import pct.droid.base.providers.media.types.Show;
import pct.droid.base.providers.meta.TraktProvider;
import pct.droid.base.providers.subs.YSubsProvider;

public class EZTVProvider extends MediaProvider {

    protected String mApiUrl = "http://eztvapi.re/";
    protected String mMirrorApiUrl = "http://api.popcorntime.io/";
    public static final String NO_MOVIES_ERROR = "No movies found";

    @Override
    public void getList(final ArrayList<Media> existingList, HashMap<String, String> filters, final Callback callback) {
        final ArrayList<Media> currentList;
        if(existingList == null) {
            currentList = new ArrayList<Media>();
        } else {
            currentList = (ArrayList<Media>) existingList.clone();
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

        String url = mApiUrl + "shows/";
        if (filters.containsKey("page")) {
            url += filters.get("page");
        } else {
            url += "1";
        }

        Request.Builder requestBuilder = new Request.Builder();
        String query = buildQuery(params);
        requestBuilder.url(url + "?" + query);

        fetchList(currentList, requestBuilder, callback);
    }

    private Call fetchList(final ArrayList<Media> currentList, final Request.Builder requestBuilder, final Callback callback) {
        return enqueue(requestBuilder.build(), new com.squareup.okhttp.Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                String url = requestBuilder.build().urlString();
                if(url.equals(mMirrorApiUrl)) {
                    callback.onFailure(e);
                } else {
                    url = url.replace(mApiUrl, mMirrorApiUrl);
                    requestBuilder.url(url);
                    fetchList(currentList, requestBuilder, callback);
                }
            }

            @Override
            public void onResponse(Response response) throws IOException {
                try {
                    if (response.isSuccessful()) {
                        String responseStr = response.body().string();
                        ArrayList<LinkedTreeMap<String, Object>> list = (ArrayList<LinkedTreeMap<String, Object>>) mGson.fromJson(responseStr, ArrayList.class);
                        EZTVReponse result = new EZTVReponse(list);
                        if (list == null) {
                            callback.onFailure(new NetworkErrorException("Empty response"));
                        } else {
                            ArrayList<Media> formattedData = result.formatListForPopcorn(currentList);
                            callback.onSuccess(formattedData);
                            return;
                        }
                    }
                } catch (Exception e) {
                    callback.onFailure(e);
                }
                callback.onFailure(new NetworkErrorException(response.body().string()));
            }
        });
    }

    @Override
    public void getDetail(String imdbId, final Callback callback) {
        Request.Builder requestBuilder = new Request.Builder();
        requestBuilder.url(mApiUrl + "listimdb.json?imdb_id=" + imdbId);

        enqueue(requestBuilder.build(), new com.squareup.okhttp.Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                callback.onFailure(e);
            }

            @Override
            public void onResponse(Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseStr = response.body().string();
                    EZTVReponse result = mGson.fromJson(responseStr, EZTVReponse.class);
                    if (result != null) {
                        callback.onFailure(new NetworkErrorException("Empty response"));
                    } else {
                        ArrayList<Media> formattedData = result.formatListForPopcorn();

                        if(formattedData.size() > 0) {
                            TraktProvider traktProvider = new TraktProvider();
                            Movie movie = (Movie) formattedData.get(0);

                            TraktProvider.MetaData meta = traktProvider.getSummary(movie.videoId, "movie");
                            if (meta.images != null && meta.images.containsKey("poster")) {
                                movie.image = meta.images.get("poster").replace(".jpg", "-300.jpg");
                                movie.fullImage = meta.images.get("poster");
                            } else {
                                movie.fullImage = movie.image;
                            }

                            if (meta.images != null && meta.images.containsKey("fanart")) {
                                movie.headerImage = meta.images.get("fanart").replace(".jpg", "-940.jpg");
                            } else {
                                movie.headerImage = movie.image;
                            }

                            if (meta.title != null) {
                                movie.title = meta.title;
                            }

                            if (meta.overview != null) {
                                movie.synopsis = meta.overview;
                            }

                            if (meta.tagline != null) {
                                movie.tagline = meta.tagline;
                            }

                            if (meta.trailer != null) {
                                movie.trailer = meta.trailer;
                            }

                            if (meta.runtime != null) {
                                movie.runtime = meta.runtime;
                            }

                            if (meta.certification != null) {
                                movie.certification = meta.certification;
                            }

                            YSubsProvider subsProvider = new YSubsProvider();
                            movie.subtitles = subsProvider.getList(movie.videoId).get(movie.videoId);

                            formattedData.set(0, movie);

                            callback.onSuccess(formattedData);
                            return;
                        }
                        callback.onFailure(new IllegalStateException("Empty list"));
                    }
                }
                callback.onFailure(new NetworkErrorException(response.body().string()));
            }
        });
    }

    private class EZTVReponse {
        public String _id;
        public String air_day;
        public String air_time;

        ArrayList<LinkedTreeMap<String, Object>> showsList;

        public EZTVReponse(ArrayList<LinkedTreeMap<String, Object>> showsList) {
            this.showsList = showsList;
        }

        public ArrayList<Media> formatListForPopcorn() {
            return formatListForPopcorn(new ArrayList<Media>());
        }

        public ArrayList<Media> formatListForPopcorn(ArrayList<Media> existingList) {
            for(LinkedTreeMap<String, Object> item : showsList) {
                Show show = new Show();

                show.title = item.get("title").toString();
                show.videoId = item.get("_id").toString();
                show.seasons = (Integer) item.get("seasons");
                show.tvdbId = item.get("tvdb_id").toString();
                show.year = item.get("year").toString();
                LinkedTreeMap<String, String> images = (LinkedTreeMap<String, String>) item.get("images");
                show.image = images.get("poster").replace(".jpg", "-300.jpg");
                show.headerImage = images.get("fanart").replace(".jpg", "-940.jpg");

                existingList.add(show);
            }
            return existingList;
        }
    }

}
