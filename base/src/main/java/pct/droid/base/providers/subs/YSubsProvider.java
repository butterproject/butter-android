package pct.droid.base.providers.subs;

import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.apache.http.MethodNotSupportedException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import pct.droid.base.providers.media.models.Movie;
import pct.droid.base.providers.media.models.Show;

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
    public void getList(final Movie media, final Callback callback) {
        final Request.Builder requestBuilder = new Request.Builder();
        requestBuilder.url(mApiUrl + media.videoId);
        requestBuilder.tag(SUBS_CALL);

        fetch(requestBuilder, media, new Callback() {
            @Override
            public void onSuccess(Map<String, String> items) {
                callback.onSuccess(items);
            }

            @Override
            public void onFailure(Exception e) {
                requestBuilder.url(mMirrorApiUrl + media.videoId);
                fetch(requestBuilder, media, callback);
            }
        });
    }

    @Override
    public void getList(Show media, Show.Episode episode, Callback callback) {
        // Show subtitles not supported
        callback.onFailure(new MethodNotSupportedException("Show subtitles not supported"));
    }

    private void fetch(Request.Builder requestBuilder, final Movie media, final Callback callback) {
        enqueue(requestBuilder.build(), new com.squareup.okhttp.Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                callback.onFailure(e);
            }

            @Override
            public void onResponse(Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseStr = response.body().string();
                    YSubsResponse result = mGson.fromJson(responseStr, YSubsResponse.class);
                    callback.onSuccess(result.formatForPopcorn(mPrefix, mLanguageMapping).get(media.videoId));
                }
            }
        });
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
            if(map.size() > 0 && map != null) {
                return map.keySet().toArray(new String[map.size()]);
            }
            return new String[0];
        }

        private String mapLanguage(String input, HashMap<String, String> mapping) {
            if (mapping.containsKey(input)) {
                return mapping.get(input);
            }
            return "no-subs";
        }
    }

}
