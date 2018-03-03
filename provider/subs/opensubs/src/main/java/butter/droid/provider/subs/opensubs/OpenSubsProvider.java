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
import butter.droid.provider.base.model.Media;
import butter.droid.provider.subs.AbsSubsProvider;
import butter.droid.provider.subs.model.Subtitle;
import butter.droid.provider.subs.opensubs.data.OpenSubsService;
import butter.droid.provider.subs.opensubs.data.model.request.LoginRequest;
import butter.droid.provider.subs.opensubs.data.model.response.LoginResponse;
import butter.droid.provider.subs.opensubs.data.model.response.SearchResponse;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.functions.Function;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class OpenSubsProvider extends AbsSubsProvider {

    private final OpenSubsService service;

    protected OpenSubsProvider(final OpenSubsService service, final Context context) {
        super(context);

        this.service = service;
    }

    @Override protected Maybe<InputStream> provideSubs(@NonNull final Media media, @NonNull final Subtitle subtitle) {
        return null;
    }

    @Override public Single<List<Subtitle>> list(@NonNull final Media media) {
        return service.login(new String[] { "", "", Locale.getDefault().getLanguage(), "Popcorn Time v1" })
                .flatMap(new Function<LoginResponse, SingleSource<SearchResponse>>() {
                    @Override public SingleSource<SearchResponse> apply(final LoginResponse loginResponse) throws Exception {
                        Map<String, String> option = new HashMap<>();
                        option.put("imdbid", media.getId().replace("tt", ""));
                        option.put("sublanguageid", "all");

                        return service.search(new Object[] { loginResponse.getTokem(), option });

                    }
                })
                .map(new Function<SearchResponse, List<Subtitle>>() {
                    @Override public List<Subtitle> apply(final SearchResponse searchResponse) throws Exception {
                        return Collections.emptyList();
                    }
                });
    }
}
