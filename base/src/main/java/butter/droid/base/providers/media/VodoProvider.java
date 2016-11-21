/*
 * This file is part of Butter.
 *
 * Butter is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Butter is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Butter. If not, see <http://www.gnu.org/licenses/>.
 */

package butter.droid.base.providers.media;

import android.accounts.NetworkErrorException;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.internal.LinkedTreeMap;

import java.io.IOException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

import butter.droid.base.ButterApplication;
import butter.droid.base.R;
import butter.droid.base.providers.media.models.Genre;
import butter.droid.base.providers.media.models.Media;
import butter.droid.base.providers.media.models.Movie;
import butter.droid.base.providers.subs.SubsProvider;
import butter.droid.base.utils.LocaleUtils;
import butter.droid.base.utils.StringUtils;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class VodoProvider extends MediaProvider {

    private static Integer CURRENT_API = 0;
    private static final String[] API_URLS = {
            "http://vodo.net/popcorn"
    };
    public static String CURRENT_URL = API_URLS[CURRENT_API];

    private static Filters sFilters = new Filters();

    private final List<Call> ongoingCalls = new ArrayList<>();

    public VodoProvider(OkHttpClient client, Gson gson, @Nullable SubsProvider subsProvider) {
        super(client, gson, subsProvider);
    }

    @Override
    protected Call enqueue(Request request, okhttp3.Callback requestCallback) {
        Context context = ButterApplication.getAppContext();
        PackageInfo pInfo;
        String versionName = "0.0.0";
        try {
            pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            versionName = pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        request = request
                .newBuilder().removeHeader("User-Agent")
                .addHeader("User-Agent", String.format(
                        "Mozilla/5.0 (Linux; U; Android %s; %s; %s Build/%s) AppleWebkit/534.30 (KHTML, like Gecko) PT/%s",
                        Build.VERSION.RELEASE, LocaleUtils.getCurrentAsString(), Build.MODEL, Build.DISPLAY,
                        versionName)).build();
        Call call = super.enqueue(request, requestCallback);

        synchronized (ongoingCalls) {
            ongoingCalls.add(call);
        }

        return call;
    }

    @Override public void cancel() {
        synchronized (ongoingCalls) {
            for (Call ongoingCall : ongoingCalls) {
                ongoingCall.cancel();
            }
            ongoingCalls.clear();
        }
    }

    @Override
    public Call getList(final ArrayList<Media> existingList, Filters filters, final Callback callback) {
        sFilters = filters;

        final ArrayList<Media> currentList;
        if (existingList == null) {
            currentList = new ArrayList<>();
        } else {
            currentList = (ArrayList<Media>) existingList.clone();
        }

        ArrayList<NameValuePair> params = new ArrayList<>();
        params.add(new NameValuePair("limit", "30"));

        if (filters == null) {
            filters = new Filters();
        }

        if (filters.keywords != null) {
            params.add(new NameValuePair("query_term", filters.keywords));
        }

        if (filters.genre != null) {
            params.add(new NameValuePair("genre", filters.genre));
        }

        if (filters.order == Filters.Order.ASC) {
            params.add(new NameValuePair("order_by", "asc"));
        } else {
            params.add(new NameValuePair("order_by", "desc"));
        }

        if (filters.langCode != null) {
            params.add(new NameValuePair("lang", filters.langCode));
        }

        String sort;
        switch (filters.sort) {
            default:
            case POPULARITY:
                sort = "seeds";
                break;
            case YEAR:
                sort = "year";
                break;
            case DATE:
                sort = "date_added";
                break;
            case RATING:
                sort = "rating";
                break;
            case ALPHABET:
                sort = "title";
                break;
            case TRENDING:
                sort = "trending_score";
                break;
        }

        params.add(new NameValuePair("sort_by", sort));

        if (filters.page != null) {
            params.add(new NameValuePair("page", Integer.toString(filters.page)));
        }

        Request.Builder requestBuilder = new Request.Builder()
                .url(CURRENT_URL);

        // query not used, but still here as example
//        String query = "?" + buildQuery(params);

        return fetchList(currentList, requestBuilder, filters, callback);
    }

    /**
     * Fetch the list of movies from YTS
     *
     * @param currentList Current shown list to be extended
     * @param requestBuilder Request to be executed
     * @param callback Network callback
     * @return Call
     */
    private Call fetchList(final ArrayList<Media> currentList, final Request.Builder requestBuilder,
            final Filters filters, final Callback callback) {
        return enqueue(requestBuilder.build(), new okhttp3.Callback() {
            @Override public void onFailure(Call call, IOException e) {
                String url = requestBuilder.build().url().toString();
                if (CURRENT_API >= API_URLS.length - 1) {
                    callback.onFailure(e);
                } else {
                    if (url.contains(API_URLS[CURRENT_API])) {
                        url = url.replace(API_URLS[CURRENT_API], API_URLS[CURRENT_API + 1]);
                        url = url.replace(API_URLS[CURRENT_API], API_URLS[CURRENT_API + 1]);
                        CURRENT_API++;
                    } else {
                        url = url.replace(API_URLS[CURRENT_API - 1], API_URLS[CURRENT_API]);
                        url = url.replace(API_URLS[CURRENT_API - 1], API_URLS[CURRENT_API]);
                    }
                    requestBuilder.url(url);
                    fetchList(currentList, requestBuilder, filters, callback);
                }
                finishCall(call);
            }

            @Override public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseStr;
                    try {
                        responseStr = response.body().string();
                    } catch (SocketException e) {
                        onFailure(call, new IOException("Socket failed"));
                        return;
                    }

                    VodoResponse result;
                    try {
                        result = mGson.fromJson(responseStr, VodoResponse.class);
                    } catch (IllegalStateException e) {
                        onFailure(call, new IOException("JSON Failed"));
                        return;
                    } catch (JsonSyntaxException e) {
                        onFailure(call, new IOException("JSON Failed"));
                        return;
                    }

                    if (result == null) {
                        callback.onFailure(new NetworkErrorException("No response"));
                    } else if (result.downloads == null || result.downloads.size() <= 0) {
                        callback.onFailure(new NetworkErrorException("No movies found"));
                    } else {
                        ArrayList<Media> formattedData = result.formatForApp(currentList);
                        callback.onSuccess(filters, formattedData, true);
                        return;
                    }
                }
                onFailure(call, new IOException("Couldn't connect to Vodo"));
                finishCall(call);
            }
        });
    }

    @Override
    public Call getDetail(ArrayList<Media> currentList, Integer index, Callback callback) {
        ArrayList<Media> returnList = new ArrayList<>();
        returnList.add(currentList.get(index));
        callback.onSuccess(null, returnList, true);
        return null;
    }

    private class VodoResponse {
        public ArrayList<LinkedTreeMap<String, Object>> downloads;

        /**
         * Test if there is an item that already exists
         *
         * @param results List with items
         * @param id Id of item to check for
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
         * @param existingList List to be extended
         * @return List with items
         */
        public ArrayList<Media> formatForApp(ArrayList<Media> existingList) {
            ArrayList<LinkedTreeMap<String, Object>> movies = new ArrayList<>();
            if (downloads != null) {
                movies = downloads;
            }

            for (LinkedTreeMap<String, Object> item : movies) {
                Movie movie = new Movie();

                movie.imdbId = (String) item.get("ImdbCode");
                movie.videoId = movie.imdbId.substring(2);

                int existingItem = isInResults(existingList, movie.videoId);
                if (existingItem == -1) {
                    movie.title = (String) item.get("MovieTitleClean");
                    String yearStr = item.get("MovieYear").toString();
                    Double year = Double.parseDouble(yearStr);
                    movie.year = Integer.toString(year.intValue());
                    movie.rating = item.get("MovieRating").toString();
                    movie.genre = StringUtils.uppercaseFirst(item.get("Genre").toString().split(",")[0]);
                    movie.image = (String) item.get("CoverImage");
                    movie.headerImage = (String) item.get("CoverImage");
                    movie.trailer = null;
                    String runtimeStr = item.get("Runtime").toString();
                    Double runtime = 0d;
                    if (!runtimeStr.isEmpty())
                        runtime = Double.parseDouble(runtimeStr);
                    movie.runtime = Integer.toString(runtime.intValue());
                    movie.synopsis = (String) item.get("Synopsis");
                    movie.certification = null;
                    movie.fullImage = movie.image;

                    Media.Torrent torrent = new Media.Torrent();
                    torrent.seeds = 0;
                    torrent.peers = 0;
                    torrent.hash = null;
                    torrent.url = (String) item.get("TorrentUrl");
                    movie.torrents.put(item.get("Quality").toString(), torrent);

                    existingList.add(movie);
                }
            }
            return existingList;
        }
    }

    @Override
    public int getLoadingMessage() {
        return R.string.loading_movies;
    }

    @Override
    public List<NavInfo> getNavigation() {
        List<NavInfo> tabs = new ArrayList<>();
        tabs.add(new NavInfo(R.id.yts_filter_a_to_z, Filters.Sort.ALPHABET, Filters.Order.ASC,
                ButterApplication.getAppContext().getString(R.string.a_to_z), R.drawable.yts_filter_a_to_z));
        return tabs;
    }

    @Override
    public List<Genre> getGenres() {
        return null;
    }

    private void finishCall(Call call) {
        synchronized (ongoingCalls) {
            ongoingCalls.remove(call);
        }
    }

}
