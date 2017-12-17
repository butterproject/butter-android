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

package butter.droid.provider.subs.mock;

import android.content.Context;
import android.support.annotation.NonNull;
import butter.droid.provider.base.model.Media;
import butter.droid.provider.subs.AbsSubsProvider;
import butter.droid.provider.subs.model.Subtitle;
import io.reactivex.Maybe;
import io.reactivex.Single;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

public class MockSubsProvider extends AbsSubsProvider {

    private final Context context;

    public MockSubsProvider(final Context context) {
        super(context);
        this.context = context;
    }

    @Override public Single<List<Subtitle>> list(@NonNull final Media media) {
        return Single.fromCallable(() -> Arrays.asList(
                new Subtitle("en", "English"),
                new Subtitle("pl", "Polish")
        ));
    }

    @Override protected Maybe<InputStream> provideSubs(@NonNull final Media media, @NonNull final Subtitle subtitle) {
        return Maybe.fromCallable(() -> context.getAssets().open("big_buck_bunny.pt.srt"));
    }

}
