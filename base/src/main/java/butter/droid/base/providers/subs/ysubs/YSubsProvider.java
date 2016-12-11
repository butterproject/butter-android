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

package butter.droid.base.providers.subs.ysubs;

import android.content.Context;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butter.droid.base.providers.media.models.Episode;
import butter.droid.base.providers.media.models.Movie;
import butter.droid.base.providers.subs.SubsProvider;
import butter.droid.base.providers.subs.ysubs.response.YSubsResponse;
import butter.droid.base.providers.subs.ysubs.response.models.Id;
import butter.droid.base.providers.subs.ysubs.response.models.Language;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class YSubsProvider extends SubsProvider {

    private static final String API_URL = "http://api.yifysubtitles.com/subs/";
    private static final String MIRROR_URL = "http://api.ysubs.com/subs/";
    private static final String PREFIX = "http://www.yifysubtitles.com/";
    private static final HashMap<String, String> LANGUAGE_MAPPING = new HashMap<>();

    static {
        LANGUAGE_MAPPING.put("albanian", "sq");
        LANGUAGE_MAPPING.put("arabic", "ar");
        LANGUAGE_MAPPING.put("bengali", "bn");
        LANGUAGE_MAPPING.put("brazilian-portuguese", "pt-br");
        LANGUAGE_MAPPING.put("bulgarian", "bg");
        LANGUAGE_MAPPING.put("bosnian", "bs");
        LANGUAGE_MAPPING.put("chinese", "zh");
        LANGUAGE_MAPPING.put("croatian", "hr");
        LANGUAGE_MAPPING.put("czech", "cs");
        LANGUAGE_MAPPING.put("danish", "da");
        LANGUAGE_MAPPING.put("dutch", "nl");
        LANGUAGE_MAPPING.put("english", "en");
        LANGUAGE_MAPPING.put("estonian", "et");
        LANGUAGE_MAPPING.put("farsi-persian", "fa");
        LANGUAGE_MAPPING.put("finnish", "fi");
        LANGUAGE_MAPPING.put("french", "fr");
        LANGUAGE_MAPPING.put("german", "de");
        LANGUAGE_MAPPING.put("greek", "el");
        LANGUAGE_MAPPING.put("hebrew", "he");
        LANGUAGE_MAPPING.put("hungarian", "hu");
        LANGUAGE_MAPPING.put("indonesian", "id");
        LANGUAGE_MAPPING.put("italian", "it");
        LANGUAGE_MAPPING.put("japanese", "ja");
        LANGUAGE_MAPPING.put("korean", "ko");
        LANGUAGE_MAPPING.put("lithuanian", "lt");
        LANGUAGE_MAPPING.put("macedonian", "mk");
        LANGUAGE_MAPPING.put("malay", "ms");
        LANGUAGE_MAPPING.put("norwegian", "no");
        LANGUAGE_MAPPING.put("polish", "pl");
        LANGUAGE_MAPPING.put("portuguese", "pt");
        LANGUAGE_MAPPING.put("romanian", "ro");
        LANGUAGE_MAPPING.put("russian", "ru");
        LANGUAGE_MAPPING.put("serbian", "sr");
        LANGUAGE_MAPPING.put("slovenian", "sl");
        LANGUAGE_MAPPING.put("spanish", "es");
        LANGUAGE_MAPPING.put("swedish", "sv");
        LANGUAGE_MAPPING.put("thai", "th");
        LANGUAGE_MAPPING.put("turkish", "tr");
        LANGUAGE_MAPPING.put("urdu", "ur");
        LANGUAGE_MAPPING.put("ukrainian", "uk");
        LANGUAGE_MAPPING.put("vietnamese", "vi");
    }

    public YSubsProvider(Context context, OkHttpClient client, ObjectMapper mapper) {
        super(context, client, mapper);
    }

    @Override
    public void getList(final Movie media, final Callback callback) {
        final Request.Builder requestBuilder = new Request.Builder()
                .url(API_URL + media.imdbId);

        fetch(requestBuilder, media, new Callback() {
            @Override
            public void onSuccess(Map<String, String> items) {
                callback.onSuccess(items);
            }

            @Override
            public void onFailure(Exception e) {
                requestBuilder.url(MIRROR_URL + media.imdbId);
                fetch(requestBuilder, media, callback);
            }
        });
    }

    @Override
    public void getList(Episode episode, Callback callback) {
        callback.onFailure(new NoSuchMethodException("Show subtitles not supported"));
    }

    private void fetch(Request.Builder requestBuilder, final Movie media, final Callback callback) {
        enqueue(requestBuilder.build(), new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseStr = response.body().string();
                    YSubsResponse result = mapper.readValue(responseStr, YSubsResponse.class);
                    callback.onSuccess(formatForPopcorn(result).get(media.imdbId));
                }
            }
        });
    }

    private Map<String, Map<String, String>> formatForPopcorn(YSubsResponse response) {
        Map<String, Map<String, String>> returnMap = new HashMap<>();
        if (response != null && response.getSubtitles() > 0) {
            for (Map.Entry<String, Id> imDbId : response.getSubs().getIds().entrySet()) {
                HashMap<String, String> imDbMap = new HashMap<>();
                for (Map.Entry<String, List<Language>> langList : imDbId.getValue().getLanguage().entrySet()) {
                    double currentRating = -1;
                    for (Language lang : langList.getValue()) {
                        String currentSub = "";
                        double itemRating = lang.getRating();
                        if (currentRating < itemRating) {
                            currentSub = PREFIX + lang.getUrl();
                            currentRating = itemRating;
                        }
                        imDbMap.put(mapLanguage(langList.getKey()), currentSub);
                    }
                }
                returnMap.put(imDbId.getKey(), imDbMap);
            }
        }
        return returnMap;
    }

    private String mapLanguage(String language) {
        if (LANGUAGE_MAPPING.containsKey(language)) {
            return LANGUAGE_MAPPING.get(language);
        }
        return SUBTITLE_LANGUAGE_NONE;
    }
}
