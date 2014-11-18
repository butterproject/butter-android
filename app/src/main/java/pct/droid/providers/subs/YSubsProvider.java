package pct.droid.providers.subs;

import com.squareup.okhttp.Call;
import java.util.ArrayList;
import java.util.HashMap;

public class YSubsProvider extends SubsProvider {

    private String mApiUrl = "http://api.yifysubtitles.com/subs/";
    private String mApiMirror = "http://api.ysubs.com/subs/";
    private String mPrefix = "http://www.yifysubtitles.com/";
    private HashMap<String, String> mLanguageMapping = new HashMap<String, String>();
    
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
    public Call getList(String[] imdbIds, Callback callback) {
        return null;
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
                            currentSub = (String) sub.get("url");
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
