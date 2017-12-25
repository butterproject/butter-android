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

package butter.droid.base.providers;

import android.content.Context;
import butter.droid.base.manager.internal.provider.model.ProviderWrapper;
import butter.droid.provider.base.ProviderScope;
import butter.droid.provider.mock.MockMediaProvider;
import butter.droid.provider.subs.mock.MockSubsProvider;
import com.google.gson.Gson;
import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoSet;

@Module
public class BuildTypeProviderModule {

    @Provides @ProviderScope MockMediaProvider provideMockMoviesProvider(Context context, Gson gson) {
        return new MockMediaProvider(context, gson);
    }

    @Provides @ProviderScope MockSubsProvider provideMockSubsProvider(final Context context) {
        return new MockSubsProvider(context);
    }

    @Provides @ProviderScope @IntoSet ProviderWrapper provideMockWrapper(final MockMediaProvider mediaProvider,
            final MockSubsProvider subsProvider) {
        return new ProviderWrapper(mediaProvider, subsProvider, butter.droid.provider.mock.R.string.title_movies,
                butter.droid.provider.mock.R.drawable.ic_nav_movies);
    }

}
