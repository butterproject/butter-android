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
import android.support.annotation.NonNull;

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
import butter.droid.provider.subs.opensubs.data.model.request.QuerySearchRequest;
import butter.droid.provider.subs.opensubs.data.model.response.LoginResponse;
import butter.droid.provider.subs.opensubs.data.model.response.OpenSubItem;
import butter.droid.provider.subs.opensubs.data.model.response.SearchResponse;
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
        // TODO cache token
        return service.login(new String[]{"", "", Locale.getDefault().getLanguage(), USER_AGENT}) // TODO add constants
                .flatMap((Function<LoginResponse, SingleSource<SearchResponse>>) loginResponse -> {
                    List<Object> params = new ArrayList<>();
                    params.add(loginResponse.getTokem());
                    // TODO add imdb id search
                    params.add(Collections.singletonList(new QuerySearchRequest(media.getTitle())));

                    return service.search(params);
                })
                .map(SearchResponse::getData)
                .flatMapObservable(Observable::fromIterable)
                .groupBy(OpenSubItem::getLanguageCode)
                .concatMap((Function<GroupedObservable<String, OpenSubItem>, ObservableSource<OpenSubItem>>) observable ->
                        observable.reduce((openSubItem, openSubItem2) -> {
                            int diff = getItemScore(openSubItem2) - getItemScore(openSubItem);
                            // TODO downloads count
//                            if (diff > 0 || diff == 0 && openSubItem2.getDownloads() > openSubItem.getDownloads()) {
                            if (diff > 0) {
                                return openSubItem2;
                            } else {
                                return openSubItem;
                            }
                        }).toObservable())
                .map(openSubItem -> {
                    Map<String, String> meta = new HashMap<>(1);
                    meta.put(META_DOWNLOAD_LINK, openSubItem.getDownalodLink().replace(".gz", ".srt")); // TODO download gz files
                    return new Subtitle(openSubItem.getLanguageCode(), openSubItem.getLanguageName(), meta);
                })
                .toSortedList((o1, o2) -> o1.getName().compareTo(o2.getName())); // TODO shouldn't be dependent on provider
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
}
