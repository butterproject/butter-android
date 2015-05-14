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
import android.text.Html;

import com.google.gson.internal.LinkedTreeMap;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pct.droid.base.PopcornApplication;
import pct.droid.base.R;
import pct.droid.base.providers.media.models.Episode;
import pct.droid.base.providers.media.models.Genre;
import pct.droid.base.providers.media.models.Media;
import pct.droid.base.providers.media.models.Movie;
import pct.droid.base.providers.media.models.Show;

public class HaruProvider extends MediaProvider {

    private static String API_URL = "http://ptp.haruhichan.com/";
    private static final MediaProvider sMediaProvider = new HaruProvider();

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
            params.add(new NameValuePair("search", filters.keywords));
        }

        if (filters.genre != null) {
            params.add(new NameValuePair("genres", filters.genre));
        }

        if (filters.order == Filters.Order.DESC) {
            params.add(new NameValuePair("order", "desc"));
        } else {
            params.add(new NameValuePair("order", "asc"));
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

        String url = API_URL + "list.php";
        if (filters.page != null) {
            params.add(new NameValuePair("page", Integer.toString(filters.page - 1)));
        } else {
            params.add(new NameValuePair("page", "0"));
        }

        Request.Builder requestBuilder = new Request.Builder();
        String query = buildQuery(params);
        requestBuilder.url(url + "?" + query);
        requestBuilder.tag(MEDIA_CALL);

        return fetchList(currentList, requestBuilder, filters, callback);
    }

    /**
     * Fetch the list of movies from Haruhichan
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
                callback.onFailure(e);
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

                        HaruResponse result = new HaruResponse(list);
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
                callback.onFailure(new NetworkErrorException("Couldn't connect to Haruhichan"));
            }
        });
    }

    @Override
    public Call getDetail(String videoId, final Callback callback) {
        Request.Builder requestBuilder = new Request.Builder();
        requestBuilder.url(API_URL + "anime.php?id=" + videoId);
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
                    HaruResponse result = new HaruResponse(map);
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
                callback.onFailure(new NetworkErrorException("Couldn't connect to Haruhichan API"));
            }
        });
    }

    @Override
    public int getLoadingMessage() {
        return R.string.loading_data;
    }

    @Override
    public List<NavInfo> getNavigation() {
        List<NavInfo> tabs = new ArrayList<>();
        tabs.add(new NavInfo(Filters.Sort.POPULARITY, Filters.Order.DESC, PopcornApplication.getAppContext().getString(R.string.popular_now)));
        tabs.add(new NavInfo(Filters.Sort.ALPHABET, Filters.Order.ASC, PopcornApplication.getAppContext().getString(R.string.a_to_z)));
        return tabs;
    }

    @Override
    public List<Genre> getGenres() {
        List<Genre> returnList = new ArrayList<>();
        returnList.add(new Genre("All", R.string.genre_all));
        returnList.add(new Genre("Action", R.string.genre_action));
        returnList.add(new Genre("Adventure", R.string.genre_adventure));
        returnList.add(new Genre("Cars", R.string.genre_cars));
        returnList.add(new Genre("Comedy", R.string.genre_comedy));
        returnList.add(new Genre("Dementia", R.string.genre_dementia));
        returnList.add(new Genre("Demons", R.string.genre_demons));
        returnList.add(new Genre("Drama", R.string.genre_drama));
        returnList.add(new Genre("Ecchi", R.string.genre_ecchi));
        returnList.add(new Genre("Fantasy", R.string.genre_fantasy));
        returnList.add(new Genre("Game", R.string.genre_game));
        returnList.add(new Genre("Harem", R.string.genre_harem));
        returnList.add(new Genre("Historical", R.string.genre_history));
        returnList.add(new Genre("Horror", R.string.genre_horror));
        returnList.add(new Genre("Josei", R.string.genre_josei));
        returnList.add(new Genre("Kids", R.string.genre_kids));
        returnList.add(new Genre("Magic", R.string.genre_magic));
        returnList.add(new Genre("Martial Arts", R.string.genre_martial_arts));
        returnList.add(new Genre("Mecha", R.string.genre_mecha));
        returnList.add(new Genre("Military", R.string.genre_military));
        returnList.add(new Genre("Music", R.string.genre_music));
        returnList.add(new Genre("Mystery", R.string.genre_mystery));
        returnList.add(new Genre("Parody", R.string.genre_parody));
        returnList.add(new Genre("Police", R.string.genre_police));
        returnList.add(new Genre("Psychological", R.string.genre_psychological));
        returnList.add(new Genre("Romance", R.string.genre_romance));
        returnList.add(new Genre("Samurai", R.string.genre_samurai));
        returnList.add(new Genre("School", R.string.genre_school));
        returnList.add(new Genre("Sci-Fi", R.string.genre_sci_fi));
        returnList.add(new Genre("Seinen", R.string.genre_seinen));
        returnList.add(new Genre("Shoujo", R.string.genre_shoujo));
        returnList.add(new Genre("Shoujo Ai", R.string.genre_shoujo_ai));
        returnList.add(new Genre("Shounen", R.string.genre_shounen));
        returnList.add(new Genre("Shounen Ai", R.string.genre_shounen_ai));
        returnList.add(new Genre("Slice of Life", R.string.genre_slice_of_life));
        returnList.add(new Genre("Space", R.string.genre_space));
        returnList.add(new Genre("Sports", R.string.genre_sport));
        returnList.add(new Genre("Super Power", R.string.genre_super_power));
        returnList.add(new Genre("Supernatural", R.string.genre_supernatural));
        returnList.add(new Genre("Thriller", R.string.genre_thriller));
        returnList.add(new Genre("Vampire", R.string.genre_vampire));
        return returnList;
    }

    private class HaruResponse {
        LinkedTreeMap<String, Object> detailData;
        ArrayList<LinkedTreeMap<String, Object>> showsList;

        Show.Status[] statusArray = new Show.Status[]{Show.Status.NOT_AIRED_YET, Show.Status.CONTINUING, Show.Status.ENDED};

        public HaruResponse(LinkedTreeMap<String, Object> detailData) {
            this.detailData = detailData;
        }

        public ArrayList<Media> formatDetailForPopcorn() {
            ArrayList<Media> list = new ArrayList<>();

            String image = detailData.get("malimg").toString();
            String id = detailData.get("id").toString();
            String synopsis = Html.fromHtml(detailData.get("synopsis").toString()).toString();
            List<LinkedTreeMap<String, Object>> episodes = (List<LinkedTreeMap<String, Object>>) detailData.get("episodes");

            Media media;
            if (detailData.get("type").toString().equalsIgnoreCase("movie")) {
                Movie movie = new Movie(sMediaProvider, null);

                String duration = detailData.get("duration").toString();
                if (duration.contains("to")) duration = duration.split("to ")[1];
                movie.runtime = duration.split(" min")[0];
                movie.synopsis = synopsis;

                for (LinkedTreeMap<String, Object> item : episodes) {
                    Pattern pattern = Pattern.compile("([0-9]+p)", Pattern.CASE_INSENSITIVE);
                    Matcher matcher = pattern.matcher(item.get("quality").toString());
                    if (matcher.find()) {
                        String quality = matcher.group(1);
                        if (!movie.torrents.containsKey(quality)) {
                            movie.torrents.put(quality, new Media.Torrent(item.get("magnet").toString(), 0, 0, null));
                        }
                    }
                }

                media = movie;
            } else {
                Show show = new Show(sMediaProvider, null);

                show.seasons = 1;

                String duration = detailData.get("duration").toString();
                if (duration.contains("to")) duration = duration.split("to ")[1];
                show.runtime = duration;
                show.synopsis = synopsis;
                show.status = statusArray[Integer.parseInt(detailData.get("status").toString())];

                Map<Integer, Episode> episodeMap = new HashMap<>();

                for (LinkedTreeMap<String, Object> item : episodes) {
                    try {
                        Episode episode = new Episode(sMediaProvider, null, null);

                        Pattern pattern = Pattern.compile("([0-9]+p)", Pattern.CASE_INSENSITIVE);
                        Pattern match = Pattern.compile("[\\s_]([0-9]+(-[0-9]+)?|CM|OVA)[\\s_]", Pattern.CASE_INSENSITIVE);
                        Matcher name = match.matcher(item.get("name").toString());
                        Matcher matcher = pattern.matcher(item.get("quality").toString());

                        if (name.find() && matcher.find()) {
                            String quality = matcher.group(1);

                            episode.dateBased = false;
                            episode.season = 1;
                            episode.episode = Integer.parseInt(name.group(1));

                            if (!episodeMap.containsKey(episode.episode)) {
                                episode.aired = -1;
                                episode.title = PopcornApplication.getAppContext().getString(R.string.episode) + " " + episode.episode;
                                episode.overview = PopcornApplication.getAppContext().getString(R.string.overview_not_available);
                                episode.videoId = show.videoId + episode.season + episode.episode;
                                episode.imdbId = show.imdbId;
                                episode.image = episode.fullImage = episode.headerImage = image;
                            } else {
                                episode = episodeMap.get(episode.episode);
                            }

                            if (!episode.torrents.containsKey(quality)) {
                                episode.torrents.put(quality, new Media.Torrent(item.get("magnet").toString(), 0, 0, null));
                            }

                            episodeMap.put(episode.episode, episode);
                        }
                    } catch (Exception e) {
                        // Do nothing
                    }
                }

                show.episodes = new ArrayList<>(episodeMap.values());

                media = show;
            }

            media.title = (String) detailData.get("name");
            media.videoId = id;
            media.imdbId = "mal-" + media.videoId;
            String year = detailData.get("aired").toString().split(", ")[1];
            if (year.contains(" ")) {
                year = year.split(" ")[0];
            }
            media.year = year;
            media.image = media.headerImage = image;
            String genres = detailData.get("genres").toString();
            if (genres.contains(", ")) {
                genres = genres.split(", ")[0];
            }
            media.genre = genres;
            media.rating = "-1";

            list.add(media);

            return list;
        }

        public HaruResponse(ArrayList<LinkedTreeMap<String, Object>> showsList) {
            this.showsList = showsList;
        }

        public ArrayList<Media> formatListForPopcorn(ArrayList<Media> existingList) {
            for (LinkedTreeMap<String, Object> item : showsList) {
                Media media;
                if (item.get("type").toString().equalsIgnoreCase("movie")) {
                    media = new Movie(sMediaProvider, null);
                } else {
                    media = new Show(sMediaProvider, null);
                }

                media.title = (String) item.get("name");
                media.videoId = item.get("id").toString();
                media.imdbId = "mal-" + media.videoId;
                String year = item.get("aired").toString();
                if (year.contains(", ")) {
                    year = year.split(", ")[1];
                }
                if (year.contains(" ")) {
                    year = year.split(" ")[0];
                }
                media.year = year;
                media.image = media.headerImage = item.get("malimg").toString();

                existingList.add(media);
            }
            return existingList;
        }
    }

}
