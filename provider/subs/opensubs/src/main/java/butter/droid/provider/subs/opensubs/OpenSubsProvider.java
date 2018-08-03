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

package butter.droid.provider.subs.opensubs;

import android.content.Context;
import androidx.annotation.NonNull;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import butter.droid.provider.base.model.Media;
import butter.droid.provider.subs.AbsSubsProvider;
import butter.droid.provider.subs.model.Subtitle;
import butter.droid.provider.subs.opensubs.data.OpenSubsService;
import butter.droid.provider.subs.opensubs.data.model.response.OpenSubItem;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.functions.Function;
import io.reactivex.observables.GroupedObservable;
import okhttp3.ResponseBody;

public class OpenSubsProvider extends AbsSubsProvider {

    private static final String USER_AGENT = "Butter v1"; // TODO should be configurable

    private static final String META_DOWNLOAD_LINK = "butter.droid.provider.subs.opensubs.OpenSubsProvider.downloadLink";

    private final OpenSubsService service;

    protected OpenSubsProvider(final OpenSubsService service, final Context context) {
        super(context);

        this.service = service;
    }

    @Override protected Maybe<InputStream> provideSubs(@NonNull final Media media, @NonNull final Subtitle subtitle) {
        //noinspection ConstantConditions
        return service.download(subtitle.getMeta().get(META_DOWNLOAD_LINK))
                .map(ResponseBody::byteStream)
                .toMaybe();
    }

    @Override public Single<List<Subtitle>> list(@NonNull final Media media) {
        return fetchSubtitles(media);
    }

    @Override public Maybe<Subtitle> getSubtitle(@NonNull Media media, @NonNull String languageCode) {
        if (LANGUAGE_CODE_MAP.containsKey(languageCode)) {
            return fetchSubtitles(media, LANGUAGE_CODE_MAP.get(languageCode))
                    .flattenAsObservable(it -> it)
                    .firstElement();
        } else {
            return Maybe.empty();
        }
    }

    private Single<List<Subtitle>> fetchSubtitles(@NonNull final Media media) {
        return fetchSubtitles(media, "all");
    }

    private Single<List<Subtitle>> fetchSubtitles(@NonNull final Media media, String languageCode) {
        String imdbid = media.getId().replaceAll("^tt", "");
        // TODO if episode use episode search
        return service.searchByImdbId(USER_AGENT, imdbid, languageCode)
                .retry(5)
                .flattenAsObservable(m -> m)
                .groupBy(OpenSubItem::getSubLanguageID)
                .concatMap(
                        (Function<GroupedObservable<String, OpenSubItem>, ObservableSource<OpenSubItem>>) observable ->
                                observable.reduce((openSubItem, openSubItem2) -> {
                                    int diff = getItemScore(openSubItem2) - getItemScore(openSubItem);
                                    // TODO downloads count
//                            if (diff > 0 || diff == 0 && openSubItem2.getDownloads() > openSubItem.getDownloads()) {
                                    if (diff >= 0) {
                                        return openSubItem2;
                                    } else {
                                        return openSubItem;
                                    }
                                }).toObservable())
                .map(openSubItem -> {
                    Map<String, String> meta = new HashMap<>(1);
                    meta.put(META_DOWNLOAD_LINK,
                            openSubItem.getSubDownloadLink().replace(".gz", ".srt")); // TODO download gz files
                    return new Subtitle(openSubItem.getSubLanguageID(), openSubItem.getLanguageName(), meta);
                })
                .toSortedList(
                        (o1, o2) -> o1.getName().compareTo(o2.getName()));

    }

    private int getItemScore(OpenSubItem item) {
        int score = 0;

        if (item != null) {
            if ("tag".equals(item.getMatchedBy())) {
                score += 50;
            }
            if ("trusted".equals(item.getUserRank())) {
                score += 100;
            }
        }

        return score;
    }

    private static final Map<String, String> LANGUAGE_CODE_MAP = new HashMap<>();
    static {
        LANGUAGE_CODE_MAP.put("ar", "ara");
        LANGUAGE_CODE_MAP.put("bg", "bul");
        LANGUAGE_CODE_MAP.put("bs", "bos");
        LANGUAGE_CODE_MAP.put("cs", "cze");
        LANGUAGE_CODE_MAP.put("da", "dan");
        LANGUAGE_CODE_MAP.put("de", "ger");
        LANGUAGE_CODE_MAP.put("el", "ell");
        LANGUAGE_CODE_MAP.put("en", "eng");
        LANGUAGE_CODE_MAP.put("es", "spa");
        LANGUAGE_CODE_MAP.put("et", "est");
        LANGUAGE_CODE_MAP.put("eu", "baq");
        LANGUAGE_CODE_MAP.put("fa", "baq");
        LANGUAGE_CODE_MAP.put("fi", "fin");
        LANGUAGE_CODE_MAP.put("fr", "fre");
        LANGUAGE_CODE_MAP.put("he", "heb");
        LANGUAGE_CODE_MAP.put("hr", "hrv");
        LANGUAGE_CODE_MAP.put("hu", "hun");
        LANGUAGE_CODE_MAP.put("it", "ita");
        LANGUAGE_CODE_MAP.put("lt", "lit");
        LANGUAGE_CODE_MAP.put("mk", "mac");
        LANGUAGE_CODE_MAP.put("nl", "dut");
        LANGUAGE_CODE_MAP.put("pl", "pol");
        LANGUAGE_CODE_MAP.put("pt", "por");
        LANGUAGE_CODE_MAP.put("pt-br", "por");
        LANGUAGE_CODE_MAP.put("ro", "rum");
        LANGUAGE_CODE_MAP.put("ru", "rus");
        LANGUAGE_CODE_MAP.put("sl", "slv");
        LANGUAGE_CODE_MAP.put("sr", "scc");
        LANGUAGE_CODE_MAP.put("sv", "swe");
        LANGUAGE_CODE_MAP.put("th", "tha");
        LANGUAGE_CODE_MAP.put("tr", "tur");
        LANGUAGE_CODE_MAP.put("uk", "ukr");
        LANGUAGE_CODE_MAP.put("zh", "chi");
    }
}
