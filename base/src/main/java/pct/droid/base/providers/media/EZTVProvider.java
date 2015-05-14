/*
 * This file is part of Popcorn Time.
 *
 * Popcorn Time is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Popcorn Time is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Popcorn Time. If not, see <http://www.gnu.org/licenses/>.
 */

package pct.droid.base.providers.media;

import android.accounts.NetworkErrorException;

import com.google.gson.internal.LinkedTreeMap;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import pct.droid.base.PopcornApplication;
import pct.droid.base.R;
import pct.droid.base.providers.media.models.Episode;
import pct.droid.base.providers.media.models.Genre;
import pct.droid.base.providers.media.models.Media;
import pct.droid.base.providers.media.models.Show;
import pct.droid.base.providers.meta.MetaProvider;
import pct.droid.base.providers.meta.TraktProvider;
import pct.droid.base.providers.subs.OpenSubsProvider;
import pct.droid.base.providers.subs.SubsProvider;

public class EZTVProvider extends MediaProvider {

    private static final String API_URL = "http://eztvapi.re/";
    private static final String MIRROR_URL = "http://api.popcorntime.io/";
    private static final SubsProvider sSubsProvider = new OpenSubsProvider();
    private static final MetaProvider sMetaProvider = new TraktProvider();
    private static final MediaProvider sMediaProvider = new EZTVProvider();

    @Override
    public Call getList(final ArrayList<Media> existingList, Filters filters, final Callback callback) {
        final ArrayList<Media> currentList;
        if (existingList == null) {
            currentList = new ArrayList<>();
        } else {
            currentList = (ArrayList<Media>) existingList.clone();
        }

        ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new NameValuePair("limit", "30"));

        if (filters == null) {
            filters = new Filters();
        }

        if (filters.keywords != null) {
            params.add(new NameValuePair("keywords", filters.keywords));
        }

        if (filters.genre != null) {
            params.add(new NameValuePair("genre", filters.genre));
        }

        if (filters.order == Filters.Order.ASC) {
            params.add(new NameValuePair("order", "asc"));
        } else {
            params.add(new NameValuePair("order", "desc"));
        }

        String sort = "";
        switch (filters.sort) {
            default:
            case POPULARITY:
                sort = "popularity";
                break;
            case YEAR:
                sort = "year";
                break;
            case DATE:
                sort = "updated";
                break;
            case RATING:
                sort = "rating";
                break;
            case ALPHABET:
                sort = "name";
                break;
        }

        params.add(new NameValuePair("sort", sort));

        String url = API_URL + "shows/";
        if (filters.page != null) {
            url += filters.page;
        } else {
            url += "1";
        }

        Request.Builder requestBuilder = new Request.Builder();
        String query = buildQuery(params);
        requestBuilder.url(url + "?" + query);
        requestBuilder.tag(MEDIA_CALL);

        return fetchList(currentList, requestBuilder, filters, callback);
    }

    /**
     * Fetch the list of movies from EZTV
     *
     * @param currentList    Current shown list to be extended
     * @param requestBuilder Request to be executed
     * @param callback       Network callback
     * @return Call
     */
    private Call fetchList(final ArrayList<Media> currentList, final Request.Builder requestBuilder, final Filters filters, final Callback callback) {
        return enqueue(requestBuilder.build(), new com.squareup.okhttp.Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                String url = requestBuilder.build().urlString();
                if (url.equals(MIRROR_URL)) {
                    callback.onFailure(e);
                } else {
                    url = url.replace(API_URL, MIRROR_URL);
                    requestBuilder.url(url);
                    fetchList(currentList, requestBuilder, filters, callback);
                }
            }

            @Override
            public void onResponse(Response response) throws IOException {
                try {
                    if (response.isSuccessful()) {
                        String responseStr = response.body().string();

                        ArrayList<LinkedTreeMap<String, Object>> list = null;
                        if (responseStr.isEmpty()) {
                            list = new ArrayList<>();
                        } else {
                            list = (ArrayList<LinkedTreeMap<String, Object>>) mGson.fromJson(responseStr, ArrayList.class);
                        }

                        EZTVReponse result = new EZTVReponse(list);
                        if (list == null) {
                            callback.onFailure(new NetworkErrorException("Empty response"));
                        } else {
                            ArrayList<Media> formattedData = result.formatListForPopcorn(currentList);
                            callback.onSuccess(filters, formattedData, list.size() > 0);
                            return;
                        }
                    }
                } catch (Exception e) {
                    callback.onFailure(e);
                }
                callback.onFailure(new NetworkErrorException("Couldn't connect to EZTVAPI"));
            }
        });
    }

    @Override
    public Call getDetail(String videoId, final Callback callback) {
        Request.Builder requestBuilder = new Request.Builder();
        requestBuilder.url(API_URL + "show/" + videoId);
        requestBuilder.tag(MEDIA_CALL);

        return enqueue(requestBuilder.build(), new com.squareup.okhttp.Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                callback.onFailure(e);
            }

            @Override
            public void onResponse(Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseStr = response.body().string();
                    LinkedTreeMap<String, Object> map = mGson.fromJson(responseStr, LinkedTreeMap.class);
                    EZTVReponse result = new EZTVReponse(map);
                    if (map == null) {
                        callback.onFailure(new NetworkErrorException("Empty response"));
                    } else {
                        ArrayList<Media> formattedData = result.formatDetailForPopcorn();

                        if (formattedData.size() > 0) {
                            callback.onSuccess(null, formattedData, true);
                            return;
                        }
                        callback.onFailure(new IllegalStateException("Empty list"));
                        return;
                    }
                }
                callback.onFailure(new NetworkErrorException("Couldn't connect to EZTVAPI"));
            }
        });
    }

    private class EZTVReponse {
        LinkedTreeMap<String, Object> showData;
        ArrayList<LinkedTreeMap<String, Object>> showsList;

        public EZTVReponse(LinkedTreeMap<String, Object> showData) {
            this.showData = showData;
        }

        public ArrayList<Media> formatDetailForPopcorn() {
            ArrayList<Media> list = new ArrayList<>();
            try {
                Show show = new Show(sMediaProvider, sSubsProvider);

                show.title = (String) showData.get("title");
                show.videoId = (String) showData.get("imdb_id");
                show.imdbId = (String) showData.get("imdb_id");
                show.tvdbId = (String) showData.get("tvdb_id");
                show.seasons = ((Double) showData.get("num_seasons")).intValue();
                show.year = (String) showData.get("year");
                LinkedTreeMap<String, String> images = (LinkedTreeMap<String, String>) showData.get("images");
                show.image = images.get("poster").replace("/original/", "/medium/");
                show.fullImage = images.get("poster");
                show.headerImage = images.get("fanart").replace("/original/", "/medium/");

                if (showData.get("status") != null) {
                    String status = (String) showData.get("status");
                    if (status.equalsIgnoreCase("ended")) {
                        show.status = Show.Status.ENDED;
                    } else if (status.equalsIgnoreCase("returning series")) {
                        show.status = Show.Status.CONTINUING;
                    } else if (status.equalsIgnoreCase("in production")) {
                        show.status = Show.Status.CONTINUING;
                    } else if (status.equalsIgnoreCase("canceled")) {
                        show.status = Show.Status.CANCELED;
                    }
                }

                show.country = (String) showData.get("country");
                show.network = (String) showData.get("network");
                show.synopsis = (String) showData.get("synopsis");
                show.runtime = (String) showData.get("runtime");
                show.airDay = (String) showData.get("air_day");
                show.airTime = (String) showData.get("air_time");
                show.genre = ((ArrayList<String>) showData.get("genres")).get(0);
                show.rating = Double.toString(((LinkedTreeMap<String, Double>) showData.get("rating")).get("percentage") / 10);

                ArrayList<LinkedTreeMap<String, Object>> episodes = (ArrayList<LinkedTreeMap<String, Object>>) showData.get("episodes");
                for (LinkedTreeMap<String, Object> episode : episodes) {
                    try {
                        Episode episodeObject = new Episode(sMediaProvider, sSubsProvider, sMetaProvider);
                        LinkedTreeMap<String, LinkedTreeMap<String, Object>> torrents =
                                (LinkedTreeMap<String, LinkedTreeMap<String, Object>>) episode.get("torrents");
                        for (String key : torrents.keySet()) {
                            if (!key.equals("0")) {
                                LinkedTreeMap<String, Object> item = torrents.get(key);
                                Media.Torrent torrent = new Media.Torrent();
                                torrent.url = item.get("url").toString();
                                torrent.seeds = ((Double) item.get("seeds")).intValue();
                                torrent.peers = ((Double) item.get("peers")).intValue();
                                episodeObject.torrents.put(key, torrent);
                            }
                        }

                        episodeObject.showName = show.title;
                        episodeObject.dateBased = (Boolean) episode.get("date_based");
                        episodeObject.aired = ((Double) episode.get("first_aired")).intValue();
                        episodeObject.title = (String) episode.get("title");
                        episodeObject.overview = (String) episode.get("overview");
                        episodeObject.season = ((Double) episode.get("season")).intValue();
                        episodeObject.episode = ((Double) episode.get("episode")).intValue();
                        episodeObject.videoId = show.videoId + episodeObject.season + episodeObject.episode;
                        episodeObject.imdbId = show.imdbId;
                        episodeObject.image = episodeObject.fullImage = episodeObject.headerImage = show.headerImage;

                        show.episodes.add(episodeObject);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                list.add(show);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return list;
        }

        public EZTVReponse(ArrayList<LinkedTreeMap<String, Object>> showsList) {
            this.showsList = showsList;
        }

        public ArrayList<Media> formatListForPopcorn(ArrayList<Media> existingList) {
            for (LinkedTreeMap<String, Object> item : showsList) {
                Show show = new Show(sMediaProvider, sSubsProvider);

                show.title = (String) item.get("title");
                show.videoId = (String) item.get("imdb_id");
                show.seasons = (Integer) item.get("seasons");
                show.tvdbId = (String) item.get("tvdb_id");
                show.year = (String) item.get("year");
                LinkedTreeMap<String, String> images = (LinkedTreeMap<String, String>) item.get("images");
                show.image = images.get("poster").replace("/original/", "/medium/");
                show.headerImage = images.get("fanart").replace("/original/", "/medium/");

                existingList.add(show);
            }
            return existingList;
        }
    }

    @Override
    public int getLoadingMessage() {
        return R.string.loading_shows;
    }

    @Override
    public List<NavInfo> getNavigation() {
        List<NavInfo> tabs = new ArrayList<>();
        tabs.add(new NavInfo(Filters.Sort.POPULARITY, Filters.Order.DESC, PopcornApplication.getAppContext().getString(R.string.popular_now)));
        tabs.add(new NavInfo(Filters.Sort.RATING, Filters.Order.DESC, PopcornApplication.getAppContext().getString(R.string.top_rated)));
        tabs.add(new NavInfo(Filters.Sort.DATE, Filters.Order.DESC, PopcornApplication.getAppContext().getString(R.string.last_updated)));
        tabs.add(new NavInfo(Filters.Sort.YEAR, Filters.Order.DESC, PopcornApplication.getAppContext().getString(R.string.year)));
        tabs.add(new NavInfo(Filters.Sort.ALPHABET, Filters.Order.ASC, PopcornApplication.getAppContext().getString(R.string.a_to_z)));
        return tabs;
    }

    @Override
    public List<Genre> getGenres() {
        List<Genre> returnList = new ArrayList<>();
        returnList.add(new Genre(null, R.string.genre_all));
        returnList.add(new Genre("Action", R.string.genre_action));
        returnList.add(new Genre("Adventure", R.string.genre_adventure));
        returnList.add(new Genre("Animation", R.string.genre_animation));
        returnList.add(new Genre("Children", R.string.genre_children));
        returnList.add(new Genre("Comedy", R.string.genre_comedy));
        returnList.add(new Genre("Crime", R.string.genre_crime));
        returnList.add(new Genre("Documentary", R.string.genre_documentary));
        returnList.add(new Genre("Drama", R.string.genre_drama));
        returnList.add(new Genre("Family", R.string.genre_family));
        returnList.add(new Genre("Fantasy", R.string.genre_fantasy));
        returnList.add(new Genre("Game Show", R.string.genre_game_show));
        returnList.add(new Genre("Home and Garden", R.string.genre_home_garden));
        returnList.add(new Genre("Horror", R.string.genre_horror));
        returnList.add(new Genre("Mini Series", R.string.genre_mini_series));
        returnList.add(new Genre("Mystery", R.string.genre_mystery));
        returnList.add(new Genre("News", R.string.genre_news));
        returnList.add(new Genre("Reality", R.string.genre_reality));
        returnList.add(new Genre("Romance", R.string.genre_romance));
        returnList.add(new Genre("Science Fiction", R.string.genre_sci_fi));
        returnList.add(new Genre("Soap", R.string.genre_soap));
        returnList.add(new Genre("Special Interest", R.string.genre_special_interest));
        returnList.add(new Genre("Sport", R.string.genre_sport));
        returnList.add(new Genre("Suspense", R.string.genre_suspense));
        returnList.add(new Genre("Talk Show", R.string.genre_talk_show));
        returnList.add(new Genre("Thriller", R.string.genre_thriller));
        returnList.add(new Genre("Western", R.string.genre_western));
        return returnList;
    }
}
