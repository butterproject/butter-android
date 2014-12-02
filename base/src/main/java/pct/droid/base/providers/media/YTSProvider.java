package pct.droid.base.providers.media;

import android.accounts.NetworkErrorException;

import com.google.gson.internal.LinkedTreeMap;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import pct.droid.base.providers.media.types.Media;
import pct.droid.base.providers.media.types.Movie;
import pct.droid.base.providers.meta.TraktProvider;
import pct.droid.base.providers.subs.YSubsProvider;

public class YTSProvider extends MediaProvider {

    protected String mApiUrl = "http://yts.re/api/";
    protected String mMirrorApiUrl = "https://yts.wf/api/";
    public static final String NO_MOVIES_ERROR = "No movies found";

    @Override
    public Call getList(final ArrayList<Media> existingList, HashMap<String, String> filters, final Callback callback) {
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

        if (filters.containsKey("page")) {
            params.add(new BasicNameValuePair("set", filters.get("page")));
        }

        Request.Builder requestBuilder = new Request.Builder();
        String query = buildQuery(params);
        requestBuilder.url(mApiUrl + "list.json?" + query);

        return fetchList(currentList, requestBuilder, callback);
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
                if(response.isSuccessful()) {
                    String responseStr = response.body().string();
                    YTSReponse result = mGson.fromJson(responseStr, YTSReponse.class);
                    if(result.status != null && result.status.equals("fail")) {
                        callback.onFailure(new NetworkErrorException(result.error));
                    } else {
                        int previousSize = currentList.size();
                        ArrayList<Media> formattedData = result.formatForPopcorn(currentList);
                        int newDataSize = formattedData.size() - previousSize;

                        // Only get metdata for new items in list
                        String[] imdbIds = new String[newDataSize];
                        for(int i = previousSize, index = 0; i < formattedData.size(); i++, index++) {
                            Media media = formattedData.get(i);
                            imdbIds[index] = media.videoId;
                        }

                        TraktProvider traktProvider = new TraktProvider();
                        TraktProvider.MetaData[] metaDatas = traktProvider.getSummaries(imdbIds, "movie", "normal");

                        if(metaDatas.length == formattedData.size())
                            for(int i = previousSize, index = 0; i < formattedData.size(); i++) {
                                Media media = formattedData.get(i);

                                if(metaDatas.length > index) {
                                    TraktProvider.MetaData meta = metaDatas[index];
                                    if (media.videoId.equals(meta.imdb_id)) {
                                        if (meta.images.containsKey("poster")) {
                                            media.image = meta.images.get("poster").replace(".jpg", "-300.jpg");
                                            media.fullImage = meta.images.get("poster");
                                        }

                                        if (meta.images.containsKey("fanart")) {
                                            media.headerImage = meta.images.get("fanart").replace(".jpg", "-940.jpg");
                                        }

                                        if (meta.title != null) {
                                            media.title = meta.title;
                                        }
                                        formattedData.set(i, media);
                                        index++;
                                    } else {
                                        media.fullImage = media.image;
                                        media.headerImage = media.image;
                                        formattedData.set(i, media);
                                    }
                                } else {
                                    media.fullImage = media.image;
                                    media.headerImage = media.image;
                                    formattedData.set(i, media);
                                }
                            }

                        callback.onSuccess(currentList);
                        return;
                    }
                }
                callback.onFailure(new NetworkErrorException(response.body().string()));
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
                if (response.isSuccessful()) {
                    String responseStr = response.body().string();
                    YTSReponse result = mGson.fromJson(responseStr, YTSReponse.class);
                    if (result.status != null && result.status.equals("fail")) {
                        callback.onFailure(new NetworkErrorException(result.error));
                    } else {
                        ArrayList<Media> formattedData = result.formatForPopcorn();

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
                                movie.runtime = Integer.toString(meta.runtime);
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

    private class YTSReponse {
        public String status;
        public String error;
        public ArrayList<LinkedTreeMap<String, Object>> MovieList;

        private int isInResults(ArrayList<Media> results, String id) {
            int i = 0;
            for(Media item : results) {
                if(item.videoId.equals(id)) return i;
                i++;
            }
            return -1;
        }

        public ArrayList<Media> formatForPopcorn() {
            return formatForPopcorn(new ArrayList<Media>());
        }

        public ArrayList<Media> formatForPopcorn(ArrayList<Media> existingList) {
            for(LinkedTreeMap<String, Object> item : MovieList) {
                Movie movie = new Movie();

                movie.videoId = item.get("ImdbCode").toString();
                String torrentQuality = item.get("Quality").toString();

                if(torrentQuality.equals("3D")) {
                    continue;
                }

                Media.Torrent torrent = new Media.Torrent();
                torrent.url = item.get("TorrentMagnetUrl").toString();
                torrent.seeds = item.get("TorrentSeeds").toString();
                torrent.peers = item.get("TorrentPeers").toString();

                int existingItem = isInResults(existingList, movie.videoId);
                if(existingItem == -1) {
                    movie.image = item.get("CoverImage").toString().replace("_med.", "_large.");
                    movie.title = item.get("MovieTitleClean").toString();//.replaceAll("([^)]*)|1080p|DIRECTORS CUT|EXTENDED|UNRATED|3D|[()]", "");
                    movie.year = item.get("MovieYear").toString();
                    movie.genre = item.get("Genre").toString();
                    movie.rating = item.get("MovieRating").toString();
                    movie.type = "movie";
                } else {
                    movie = (Movie) existingList.get(existingItem);
                }

                if(!movie.torrents.containsKey(torrentQuality)) {
                    movie.torrents.put(torrentQuality, torrent);
                }

                if(existingItem == -1) {
                    existingList.add(movie);
                } else {
                    existingList.set(existingItem, movie);
                }
            }
            return existingList;
        }
    }

}
