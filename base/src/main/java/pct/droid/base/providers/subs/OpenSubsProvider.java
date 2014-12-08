package pct.droid.base.providers.subs;

import android.net.Uri;
import android.util.Log;

import org.apache.http.MethodNotSupportedException;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlrpc.android.XMLRPCClient;
import org.xmlrpc.android.XMLRPCException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pct.droid.base.providers.media.types.Movie;
import pct.droid.base.providers.media.types.Show;
import pct.droid.base.utils.LogUtils;

public class OpenSubsProvider extends SubsProvider {

    protected String mApiUrl = "http://api.opensubtitles.org/xml-rpc";
    protected String mUserAgent = "Popcorn Time v1";//"Popcorn Time Android v1";

    @Override
    public Map<String, Map<String, String>> getList(Movie movie) throws MethodNotSupportedException {
        throw new MethodNotSupportedException("Movie subtitles not supported");
    }

    @Override
    public Map<String, Map<String, String>> getList(Show show, Show.Episode episode) {
        String token = login();
        if(!token.isEmpty()) {
            Map<String, Object> subData = search(show, Integer.toString(episode.season), Integer.toString(episode.episode), token);
            Map<String, Map<String, String>> returnMap = new HashMap<String, Map<String, String>>();
            Map<String, Integer[]> scoreMap = new HashMap<String, Integer[]>();
            Map<String, String> episodeMap = new HashMap<String, String>();
            if(subData != null && subData.get("data") != null) {
                Object[] dataList = (Object[]) subData.get("data");
                for(Object dataItem : dataList) {
                    Map<String, String> item = (Map<String, String>) dataItem;
                    if(item.get("SubFormat") != "srt") {
                        continue;
                    }

                    // episode check
                    if(!item.get("SeriesIMDBParent").equals(show.imdbId.replace("tt", ""))) {
                        continue;
                    }
                    if(item.get("SeriesSeason").equals(Integer.toString(episode.season))) {
                        continue;
                    }
                    if(item.get("SeriesEpisode").equals(Integer.toString(episode.episode))) {
                        continue;
                    }

                    String url = item.get("SubDownloadLink").replace(".gz", ".srt");
                    String lang = item.get("ISO639").replace("pb", "pt-BR");
                    int downloads = Integer.parseInt(item.get("SubDownloadsCnt"));
                    int score = 0;

                    if(item.get("MatchedBy") == "tag") {
                        score += 50;
                    }
                    if(item.get("UserRank") == "trusted") {
                        score += 100;
                    }
                    if(!episodeMap.containsKey(lang)) {
                        episodeMap.put(lang, url);
                        scoreMap.put(lang, new Integer[] { score, downloads });
                    } else {
                        // If score is 0 or equal, sort by downloads
                        if(score > scoreMap.get(lang)[0] || (score == scoreMap.get(lang)[0] && downloads > scoreMap.get(lang)[1])) {
                            episodeMap.put(lang, url);
                            scoreMap.put(lang, new Integer[] { score, downloads });
                        }
                    }
                }
                returnMap.put(show.imdbId, episodeMap);
                return returnMap;
            }
        }
        return new HashMap<String, Map<String, String>>();
    }

    /**
     * Login to server and get token
     * @return Token
     */
    private String login() {
        try {
            XMLRPCClient client = new XMLRPCClient(new URI(mApiUrl), "", "", mUserAgent);
            Map<String, Object> response = (Map<String, Object>) client.call("LogIn", new String[]{"", "", "en", mUserAgent});
            return (String) response.get("token");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (XMLRPCException e) {
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * Search for subtitles by imdbId, season and episode
     * @param show Show
     * @param season Season number
     * @param episode Episode number
     * @param token Login token
     * @return SRT URL
     */
    private Map<String, Object> search(Show show, String season, String episode, String token) {
        try {
            XMLRPCClient client = new XMLRPCClient(new URI(mApiUrl), "", "", mUserAgent);
            Map<String, String> option = new HashMap<String, String>();
            option.put("imdbid", show.imdbId.replace("tt", ""));
            option.put("season", season);
            option.put("episode", episode);
            option.put("sublanguageid", "all");
            return (Map<String, Object>) client.call("SearchSubtitles", new Object[] { token, new Object[] { option } });
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (XMLRPCException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return null;
    }

    /*

    protected String mApiUrl = "http://api.yifysubtitles.com/subs/";
    protected String mMirrorApiUrl = "http://api.ysubs.com/subs/";
    protected String mPrefix = "http://www.yifysubtitles.com/";
    protected HashMap<String, String> mLanguageMapping = new HashMap<String, String>();

    public OpenSubsProvider() {
        mLanguageMapping.put("albanian", "sq");
        mLanguageMapping.put("arabic", "ar");
        mLanguageMapping.put("bengali", "bn");
        mLanguageMapping.put("brazilian-portuguese", "pt-br");
        mLanguageMapping.put("bulgarian", "bg");
        mLanguageMapping.put("bosnian", "bs");
        mLanguageMapping.put("chinese", "zh");
        mLanguageMapping.put("croatian", "hr");
        mLanguageMapping.put("czech", "cs");
        mLanguageMapping.put("danish", "da");
        mLanguageMapping.put("dutch", "nl");
        mLanguageMapping.put("english", "en");
        mLanguageMapping.put("estonian", "et");
        mLanguageMapping.put("farsi-persian", "fa");
        mLanguageMapping.put("finnish", "fi");
        mLanguageMapping.put("french", "fr");
        mLanguageMapping.put("german", "de");
        mLanguageMapping.put("greek", "el");
        mLanguageMapping.put("hebrew", "he");
        mLanguageMapping.put("hungarian", "hu");
        mLanguageMapping.put("indonesian", "id");
        mLanguageMapping.put("italian", "it");
        mLanguageMapping.put("japanese", "ja");
        mLanguageMapping.put("korean", "ko");
        mLanguageMapping.put("lithuanian", "lt");
        mLanguageMapping.put("macedonian", "mk");
        mLanguageMapping.put("malay", "ms");
        mLanguageMapping.put("norwegian", "no");
        mLanguageMapping.put("polish", "pl");
        mLanguageMapping.put("portuguese", "pt");
        mLanguageMapping.put("romanian", "ro");
        mLanguageMapping.put("russian", "ru");
        mLanguageMapping.put("serbian", "sr");
        mLanguageMapping.put("slovenian", "sl");
        mLanguageMapping.put("spanish", "es");
        mLanguageMapping.put("swedish", "sv");
        mLanguageMapping.put("thai", "th");
        mLanguageMapping.put("turkish", "tr");
        mLanguageMapping.put("urdu", "ur");
        mLanguageMapping.put("ukrainian", "uk");
        mLanguageMapping.put("vietnamese", "vi");
    }

    @Override
    public HashMap<String, HashMap<String, String>> getList(String[] imdbIds) {
        StringBuilder stringBuilder = new StringBuilder();
        for(String imdbId : imdbIds) {
            stringBuilder.append(imdbId);
            if(!imdbId.equals(imdbIds[imdbIds.length - 1])) {
                stringBuilder.append("-");
            }
        }
        String imdbDbStr = stringBuilder.toString();

        Request.Builder requestBuilder = new Request.Builder();
        requestBuilder.url(mApiUrl + imdbDbStr);

        try {
            return fetch(requestBuilder);
        } catch (IOException e) {
            // eat exception for now TODO
            try {
                requestBuilder.url(mApiUrl + imdbDbStr);
                return fetch(requestBuilder);
            } catch (IOException e1) {
                // eat exception for now TODO
                e.printStackTrace();
            }
        }

        return new HashMap<String, HashMap<String, String>>();
    }

    private HashMap<String, HashMap<String, String>> fetch(Request.Builder requestBuilder) throws IOException {
        Call call = mClient.newCall(requestBuilder.build());
        Response response = call.execute();
        if(response.isSuccessful()) {
            String responseStr = response.body().string();
            YSubsResponse result = mGson.fromJson(responseStr, YSubsResponse.class);
            return result.formatForPopcorn(mPrefix, mLanguageMapping);
        }
        return new HashMap<String, HashMap<String, String>>();
    }

    private class YSubsResponse {
        public boolean success;
        public int subtitles;
        public int lastModified;
        public HashMap<String, HashMap<String, ArrayList<HashMap<String, Object>>>> subs;

        public HashMap<String, HashMap<String, String>> formatForPopcorn(String prefix, HashMap<String, String> mapping) {
            HashMap<String, HashMap<String, String>> returnMap = new HashMap<String, HashMap<String, String>>();
            String[] imdbIds = getKeys(subs);
            for (String imdbId : imdbIds) {
                HashMap<String, String> imdbMap = new HashMap<String, String>();
                HashMap<String, ArrayList<HashMap<String, Object>>> langMap = subs.get(imdbId);
                String[] langs = getKeys(langMap);
                for(String lang : langs) {
                    if(langMap.get(lang).size() <= 0) continue;
                    ArrayList<HashMap<String, Object>> subMap = langMap.get(lang);
                    int currentRating = -1;
                    String currentSub = "";
                    for(HashMap<String, Object> sub : subMap) {
                        int itemRating = ((Double)sub.get("rating")).intValue();
                        if(currentRating < itemRating) {
                            currentSub = prefix + sub.get("url");
                            currentRating = itemRating;
                        }
                    }
                    imdbMap.put(mapLanguage(lang, mapping), currentSub);
                }
                returnMap.put(imdbId, imdbMap);
            }
            return returnMap;
        }

        private String[] getKeys(HashMap<String, ?> map) {
            return map.keySet().toArray(new String[map.size()]);
        }
        
        private String mapLanguage(String input, HashMap<String, String> mapping) {
            if(mapping.containsKey(input)) {
                return mapping.get(input);
            }
            return "no-subs";
        }
    }

    */
}
