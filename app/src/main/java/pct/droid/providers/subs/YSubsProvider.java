package pct.droid.providers.subs;

import android.accounts.NetworkErrorException;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class YSubsProvider extends SubsProvider {

    private String mApiUrl = "http://api.yifysubtitles.com/subs/";
    private String mMirrorApiUrl = "http://api.ysubs.com/subs/";
    private String mPrefix = "http://www.yifysubtitles.com/";
    private HashMap<String, String> mLanguageMapping = new HashMap<String, String>();
    private Call mCurrentCall;
    
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
    public void getList(String[] imdbIds, Callback callback) {

        StringBuilder stringBuilder = new StringBuilder();
        for(String imdbId : imdbIds) {
            stringBuilder.append(imdbId);
            if(!imdbId.equals(imdbIds[imdbIds.length - 1])) {
                stringBuilder.append("-");
            }
        }

        Request.Builder requestBuilder = new Request.Builder();
        requestBuilder.url(mApiUrl + stringBuilder.toString());

        fetch(requestBuilder, callback);
    }

    private void fetch(final Request.Builder requestBuilder, final SubsProvider.Callback callback) {
        enqueue(requestBuilder.build(), new com.squareup.okhttp.Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                String url = requestBuilder.build().urlString();
                if(url.equals(mMirrorApiUrl)) {
                    callback.onFailure(e);
                } else {
                    url = url.replace(mApiUrl, mMirrorApiUrl);
                    requestBuilder.url(url);
                    fetch(requestBuilder, callback);
                }
            }

            @Override
            public void onResponse(Response response) throws IOException {
                if(response.isSuccessful()) {
                    String responseStr = response.body().string();
                    YSubsResponse result = mGson.fromJson(responseStr, YSubsResponse.class);
                    if(result.success) {
                        callback.onSuccess(result.formatForPopcorn());
                        return;
                    }
                }
                callback.onFailure(new NetworkErrorException(response.body().string()));
            }
        });
    }

    private class YSubsResponse {
        public boolean success;
        public int subtitles;
        public int lastModified;
        public HashMap<String, HashMap<String, ArrayList<HashMap<String, Object>>>> subs;

        public HashMap<String, HashMap<String, String>> formatForPopcorn() {
            HashMap<String, HashMap<String, String>> returnMap = new HashMap<String, HashMap<String, String>>();
            String[] imdbIds = getKeys(subs);
            for (String imdbId : imdbIds) {
                HashMap<String, String> imdbMap = new HashMap<String, String>();
                HashMap<String, ArrayList<HashMap<String, Object>>> langMap = subs.get(imdbId);
                String[] langs = getKeys(langMap);
                for(String lang : langs) {
                    if(langMap.get(lang).size() <= 0) continue;
                    ArrayList<HashMap<String, Object>> subMap = langMap.get(lang);
                    int currentRating = 0;
                    String currentSub = "";
                    for(HashMap<String, Object> sub : subMap) {
                        int itemRating = (Integer)sub.get("rating");
                        if(currentRating < itemRating) {
                            currentSub = mPrefix + sub.get("url");
                            currentRating = itemRating;
                        }
                    }
                    imdbMap.put(mapLanguage(lang), currentSub);
                }
                returnMap.put(imdbId, imdbMap);
            }
            return returnMap;
        }

        private String[] getKeys(HashMap<String, ?> map) {
            return map.keySet().toArray(new String[map.size()]);
        }
        
        private String mapLanguage(String input) {
            if(mLanguageMapping.containsKey(input)) {
                return mLanguageMapping.get(input);
            }
            return "?";
        }
    }

}
