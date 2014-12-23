package pct.droid.base.providers.subs;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.apache.http.MethodNotSupportedException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import pct.droid.base.providers.media.types.Movie;
import pct.droid.base.providers.media.types.Show;

public class YSubsProvider extends SubsProvider {

    protected String mApiUrl = "http://api.yifysubtitles.com/subs/";
    protected String mMirrorApiUrl = "http://api.ysubs.com/subs/";
    protected String mPrefix = "http://www.yifysubtitles.com/";
    protected HashMap<String, String> mLanguageMapping = new HashMap<String, String>();

    public YSubsProvider() {
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
    public Map<String, Map<String, String>> getList(Movie media) {
        Request.Builder requestBuilder = new Request.Builder();
        requestBuilder.url(mApiUrl + media.videoId);

        try {
            return fetch(requestBuilder);
        } catch (IOException e) {
            // eat exception for now TODO
            try {
                requestBuilder.url(mApiUrl + media.videoId);
                return fetch(requestBuilder);
            } catch (IOException e1) {
                // eat exception for now TODO
                e.printStackTrace();
            }
        }

        return new HashMap<>();
    }

    @Override
    public Map<String, Map<String, String>> getList(Show media, Show.Episode episode) throws MethodNotSupportedException {
        throw new MethodNotSupportedException("Show subtitles not supported");
    }

    private Map<String, Map<String, String>> fetch(Request.Builder requestBuilder) throws IOException {
        Call call = mClient.newCall(requestBuilder.build());
        Response response = call.execute();
        if (response.isSuccessful()) {
            String responseStr = response.body().string();
            YSubsResponse result = mGson.fromJson(responseStr, YSubsResponse.class);
            return result.formatForPopcorn(mPrefix, mLanguageMapping);
        }
        return new HashMap<>();
    }

    private class YSubsResponse {
        public boolean success;
        public int subtitles;
        public HashMap<String, HashMap<String, ArrayList<HashMap<String, Object>>>> subs;

        public Map<String, Map<String, String>> formatForPopcorn(String prefix, HashMap<String, String> mapping) {
            Map<String, Map<String, String>> returnMap = new HashMap<>();
            if (success) {
                String[] imdbIds = getKeys(subs);
                for (String imdbId : imdbIds) {
                    HashMap<String, String> imdbMap = new HashMap<>();
                    HashMap<String, ArrayList<HashMap<String, Object>>> langMap = subs.get(imdbId);
                    String[] langs = getKeys(langMap);
                    for (String lang : langs) {
                        if (langMap.get(lang).size() <= 0) continue;
                        ArrayList<HashMap<String, Object>> subMap = langMap.get(lang);
                        int currentRating = -1;
                        String currentSub = "";
                        for (HashMap<String, Object> sub : subMap) {
                            int itemRating = ((Double) sub.get("rating")).intValue();
                            if (currentRating < itemRating) {
                                currentSub = prefix + sub.get("url");
                                currentRating = itemRating;
                            }
                        }
                        imdbMap.put(mapLanguage(lang, mapping), currentSub);
                    }
                    returnMap.put(imdbId, imdbMap);
                }
            }
            return returnMap;
        }

        private String[] getKeys(HashMap<String, ?> map) {
            return map.keySet().toArray(new String[map.size()]);
        }

        private String mapLanguage(String input, HashMap<String, String> mapping) {
            if (mapping.containsKey(input)) {
                return mapping.get(input);
            }
            return "no-subs";
        }
    }

}
