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

package pct.droid.base.providers.subs;

import org.apache.http.MethodNotSupportedException;
import org.xmlrpc.android.XMLRPCCallback;
import org.xmlrpc.android.XMLRPCClient;
import org.xmlrpc.android.XMLRPCException;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import pct.droid.base.providers.media.models.Episode;
import pct.droid.base.providers.media.models.Movie;
import pct.droid.base.providers.media.models.Show;

public class OpenSubsProvider extends SubsProvider {

    protected String mApiUrl = "http://api.opensubtitles.org/xml-rpc";
    protected String mUserAgent = "Popcorn Time v1";//"Popcorn Time Android v1";

    @Override
    public void getList(Movie movie, Callback callback) {
        // Movie subtitles not supported
        callback.onFailure(new MethodNotSupportedException("Movie subtitles not supported"));
    }

    @Override
    public void getList(final Show show, final Episode episode, final Callback callback) {
        login(new XMLRPCCallback() {
            @Override
            public void onSuccess(long id, Object result) {
                Map<String, Object> response = (Map<String, Object>) result;
                String token = (String) response.get("token");

                final String episodeStr = Integer.toString(episode.episode);
                final String seasonStr = Integer.toString(episode.season);

                if (!token.isEmpty()) {
                    search(show, seasonStr, episodeStr, token, new XMLRPCCallback() {
                        @Override
                        public void onSuccess(long id, Object result) {
                            Map<String, Map<String, String>> returnMap = new HashMap<>();
                            Map<String, Integer[]> scoreMap = new HashMap<>();
                            Map<String, String> episodeMap = new HashMap<>();
                            Map<String, Object> subData = (Map<String, Object>) result;
                            if (subData != null && subData.get("data") != null && subData.get("data") instanceof Object[]) {
                                Object[] dataList = (Object[]) subData.get("data");
                                for (Object dataItem : dataList) {
                                    Map<String, String> item = (Map<String, String>) dataItem;
                                    if (!item.get("SubFormat").equals("srt")) {
                                        continue;
                                    }

                                    // episode check
                                    if (Integer.parseInt(item.get("SeriesIMDBParent")) != Integer.parseInt(show.imdbId.replace("tt", ""))) {
                                        continue;
                                    }
                                    if (!item.get("SeriesSeason").equals(seasonStr)) {
                                        continue;
                                    }
                                    if (!item.get("SeriesEpisode").equals(episodeStr)) {
                                        continue;
                                    }

                                    String url = item.get("SubDownloadLink").replace(".gz", ".srt");
                                    String lang = item.get("ISO639").replace("pb", "pt-br");
                                    int downloads = Integer.parseInt(item.get("SubDownloadsCnt"));
                                    int score = 0;

                                    if (item.get("MatchedBy").equals("tag")) {
                                        score += 50;
                                    }
                                    if (item.get("UserRank").equals("trusted")) {
                                        score += 100;
                                    }
                                    if (!episodeMap.containsKey(lang)) {
                                        episodeMap.put(lang, url);
                                        scoreMap.put(lang, new Integer[]{score, downloads});
                                    } else {
                                        // If score is 0 or equal, sort by downloads
                                        if (score > scoreMap.get(lang)[0] || (score == scoreMap.get(lang)[0] && downloads > scoreMap.get(lang)[1])) {
                                            episodeMap.put(lang, url);
                                            scoreMap.put(lang, new Integer[]{score, downloads});
                                        }
                                    }
                                }
                                returnMap.put(show.videoId, episodeMap);
                                callback.onSuccess(returnMap.get(show.videoId));
                            } else {
                                callback.onFailure(new XMLRPCException("No subs found"));
                            }
                        }

                        @Override
                        public void onFailure(long id, Exception error) {
                            callback.onFailure(error);
                        }
                    });
                } else {
                    callback.onFailure(new XMLRPCException("Token not correct"));
                }
            }

            @Override
            public void onFailure(long id, Exception error) {
                callback.onFailure(error);
            }
        });
    }

    /**
     * Login to server and get token
     *
     * @return Token
     */
    private void login(XMLRPCCallback callback) {
        try {
            XMLRPCClient client = new XMLRPCClient(new URI(mApiUrl), "", "", mUserAgent);
            client.callAsync(callback, "LogIn", new String[]{"", "", "en", mUserAgent});
        } catch (URISyntaxException e) {
            e.printStackTrace();
            // Just catch and fail
        }
    }

    /**
     * Search for subtitles by imdbId, season and episode
     *
     * @param show    Show
     * @param season  Season number
     * @param episode Episode number
     * @param token   Login token
     * @return SRT URL
     */
    private void search(Show show, String season, String episode, String token, XMLRPCCallback callback) {
        try {
            XMLRPCClient client = new XMLRPCClient(new URI(mApiUrl), "", "", mUserAgent);
            Map<String, String> option = new HashMap<String, String>();
            option.put("imdbid", show.imdbId.replace("tt", ""));
            option.put("season", season);
            option.put("episode", episode);
            option.put("sublanguageid", "all");
            client.callAsync(callback, "SearchSubtitles", new Object[]{token, new Object[]{option}});
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

}
