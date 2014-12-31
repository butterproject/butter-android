package pct.droid.base.providers.media;

import android.accounts.NetworkErrorException;

import com.google.gson.internal.LinkedTreeMap;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Map;

import pct.droid.base.providers.media.types.Media;
import pct.droid.base.providers.media.types.Movie;
import pct.droid.base.providers.meta.TraktProvider;
import pct.droid.base.providers.subs.SubsProvider;
import pct.droid.base.providers.subs.YSubsProvider;

public class YTSProvider extends MediaProvider {

    protected String mApiUrl = "https://yts.pm/api/";
    protected String mMirrorApiUrl = "https://yts.wf/api/";
    public static final String NO_MOVIES_ERROR = "No movies found";

    @Override
    public Call getList(final ArrayList<Media> existingList, Filters filters, final Callback callback) {
        final ArrayList<Media> currentList;
        if (existingList == null) {
            currentList = new ArrayList<>();
        } else {
            currentList = (ArrayList<Media>) existingList.clone();
        }

        ArrayList<BasicNameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("limit", "30"));

        if (filters == null) {
            filters = new Filters();
        }

        if (filters.keywords != null) {
            String keywords = filters.keywords.replaceAll("\\s", "% ");
            params.add(new BasicNameValuePair("keywords", keywords));
        }

        if (filters.genre != null) {
            params.add(new BasicNameValuePair("genre", filters.genre));
        }

        if (filters.order == Filters.Order.ASC) {
            params.add(new BasicNameValuePair("order", "asc"));
        } else {
            params.add(new BasicNameValuePair("order", "desc"));
        }

        String sort;
        switch (filters.sort) {
            default:
            case POPULARITY:
                sort = "seeds";
                break;
        }

        params.add(new BasicNameValuePair("sort", sort));

        if (filters.page != null) {
            params.add(new BasicNameValuePair("set", Integer.toString(filters.page)));
        }

        Request.Builder requestBuilder = new Request.Builder();
        String query = buildQuery(params);
        requestBuilder.url(mApiUrl + "list.json?" + query);
        requestBuilder.tag(MEDIA_CALL);

        return fetchList(currentList, requestBuilder, callback);
    }

    /**
     * Fetch the list of movies from YTS
     *
     * @param currentList    Current shown list to be extended
     * @param requestBuilder Request to be executed
     * @param callback       Network callback
     * @return Call
     */
    private Call fetchList(final ArrayList<Media> currentList, final Request.Builder requestBuilder, final Callback callback) {
        return enqueue(requestBuilder.build(), new com.squareup.okhttp.Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                String url = requestBuilder.build().urlString();
                if (url.equals(mMirrorApiUrl)) {
                    callback.onFailure(e);
                } else {
                    url = url.replace(mApiUrl, mMirrorApiUrl);
                    requestBuilder.url(url);
                    fetchList(currentList, requestBuilder, callback);
                }
            }

            @Override
            public void onResponse(Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseStr;
                    try {
                        responseStr = response.body().string();
                    } catch (SocketException e) {
                        onFailure(response.request(), new IOException("Socket failed"));
                        return;
                    }

                    YTSReponse result;
                    try {
                        result = mGson.fromJson(responseStr, YTSReponse.class);
                    } catch (IllegalStateException e) {
                        onFailure(response.request(), new IOException("JSON Failed"));
                        return;
                    }

                    if (result.status != null && result.status.equals("fail")) {
                        callback.onFailure(new NetworkErrorException(result.error));
                    } else {
                        int previousSize = currentList.size();
                        ArrayList<Media> formattedData = result.formatForPopcorn(currentList);
                        int newDataSize = formattedData.size() - previousSize;

                        // Only get metdata for new items in list
                        String[] imdbIds = new String[newDataSize];
                        for (int i = previousSize, index = 0; i < formattedData.size(); i++, index++) {
                            Media media = formattedData.get(i);
                            imdbIds[index] = media.videoId;
                        }

                        TraktProvider traktProvider = new TraktProvider();
                        TraktProvider.MetaData[] metaDatas = traktProvider.getSummaries(imdbIds, "movie", "normal");

                        for (int i = previousSize, index = 0; i < formattedData.size(); i++) {
                            Media media = formattedData.get(i);

                            if (metaDatas.length > index) {
                                TraktProvider.MetaData meta = metaDatas[index];
                                if (media.videoId.equals(meta.imdb_id)) {
                                    if (meta.images.containsKey("poster")) {
                                        media.image = meta.images.get("poster").replace("/original/", "/medium/");
                                        media.fullImage = meta.images.get("poster");
                                    }

                                    if (meta.images.containsKey("fanart")) {
                                        media.headerImage = meta.images.get("fanart").replace("/original/", "/medium/");
                                    }

                                    if (meta.title != null) {
                                        media.title = meta.title;
                                    }
                                    formattedData.set(i, media);
                                    index++;
                                }
                            }
                        }

                        callback.onSuccess(formattedData);
                        return;
                    }
                }
                callback.onFailure(new NetworkErrorException("Couldn't connect to YTS"));
            }
        });
    }

    @Override
    public Call getDetail(String imdbId, final Callback callback) {
        Request.Builder requestBuilder = new Request.Builder();
        requestBuilder.url(mApiUrl + "listimdb.json?imdb_id=" + imdbId);
        requestBuilder.tag(MEDIA_CALL);

        return enqueue(requestBuilder.build(), new com.squareup.okhttp.Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                callback.onFailure(e);
            }

            @Override
            public void onResponse(Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseStr;
                    try {
                        responseStr = response.body().string();
                    } catch (SocketException e) {
                        onFailure(response.request(), new IOException("Socket failed"));
                        return;
                    }

                    YTSReponse result;
                    try {
                        result = mGson.fromJson(responseStr, YTSReponse.class);
                    } catch (IllegalStateException e) {
                        onFailure(response.request(), new IOException("JSON Failed"));
                        return;
                    }

                    if (result.status != null && result.status.equals("fail")) {
                        callback.onFailure(new NetworkErrorException(result.error));
                    } else {
                        final ArrayList<Media> formattedData = result.formatForPopcorn();

                        if (formattedData.size() > 0) {
                            TraktProvider traktProvider = new TraktProvider();
                            final Movie movie = (Movie) formattedData.get(0);

                            TraktProvider.MetaData meta = traktProvider.getSummary(movie.videoId, "movie");
                            if (meta.images != null && meta.images.containsKey("poster")) {
                                movie.image = meta.images.get("poster").replace("/original/", "/medium/");
                                movie.fullImage = meta.images.get("poster");
                            } else {
                                movie.fullImage = movie.image;
                            }

                            if (meta.images != null && meta.images.containsKey("fanart")) {
                                movie.headerImage = meta.images.get("fanart").replace("/original/", "/medium/");
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
                            subsProvider.getList(movie, new SubsProvider.Callback() {
                                @Override
                                public void onSuccess(Map<String, String> items) {
                                    movie.subtitles = items;
                                    formattedData.set(0, movie);
                                    callback.onSuccess(formattedData);
                                }

                                @Override
                                public void onFailure(Exception e) {
                                    callback.onSuccess(formattedData);
                                }
                            });

                            return;
                        }
                        callback.onFailure(new IllegalStateException("Empty list"));
                    }
                }
                callback.onFailure(new NetworkErrorException("Couldn't connect to YTS"));
            }
        });
    }

    private class YTSReponse {
        public String status;
        public String error;
        public ArrayList<LinkedTreeMap<String, Object>> MovieList;

        /**
         * Test if there is an item that already exists
         *
         * @param results List with items
         * @param id      Id of item to check for
         * @return Return the index of the item in the results
         */
        private int isInResults(ArrayList<Media> results, String id) {
            int i = 0;
            for (Media item : results) {
                if (item.videoId.equals(id)) return i;
                i++;
            }
            return -1;
        }

        /**
         * Format data for the application
         *
         * @return List with items
         */
        public ArrayList<Media> formatForPopcorn() {
            return formatForPopcorn(new ArrayList<Media>());
        }

        /**
         * Format data for the application
         *
         * @param existingList List to be extended
         * @return List with items
         */
        public ArrayList<Media> formatForPopcorn(ArrayList<Media> existingList) {
            for (LinkedTreeMap<String, Object> item : MovieList) {
                Movie movie = new Movie();

                movie.videoId = item.get("ImdbCode").toString();
                String torrentQuality = item.get("Quality").toString();

                if (torrentQuality.equals("3D")) {
                    continue;
                }

                Media.Torrent torrent = new Media.Torrent();
                torrent.url = item.get("TorrentMagnetUrl").toString();
                torrent.seeds = item.get("TorrentSeeds").toString();
                torrent.peers = item.get("TorrentPeers").toString();

                int existingItem = isInResults(existingList, movie.videoId);
                if (existingItem == -1) {
                    movie.title = item.get("MovieTitleClean").toString();//.replaceAll("([^)]*)|1080p|DIRECTORS CUT|EXTENDED|UNRATED|3D|[()]", "");
                    movie.year = item.get("MovieYear").toString();
                    movie.genre = item.get("Genre").toString();
                    movie.rating = item.get("MovieRating").toString();
                    movie.image = movie.fullImage = movie.headerImage = item.get("CoverImage").toString();
                } else {
                    movie = (Movie) existingList.get(existingItem);
                }

                if (!movie.torrents.containsKey(torrentQuality)) {
                    movie.torrents.put(torrentQuality, torrent);
                }

                if (existingItem == -1) {
                    existingList.add(movie);
                } else {
                    existingList.set(existingItem, movie);
                }
            }
            return existingList;
        }
    }

}
