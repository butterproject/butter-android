package pct.droid.providers.subs;

import android.accounts.NetworkErrorException;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import pct.droid.providers.meta.MetaProvider;

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
            return result.formatForPopcorn();
        }
        return new HashMap<String, HashMap<String, String>>();
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
