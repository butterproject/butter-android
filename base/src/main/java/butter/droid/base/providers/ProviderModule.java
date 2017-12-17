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
import butter.droid.provider.base.ProviderScope;
import butter.droid.provider.mock.MockMediaProvider;
import butter.droid.provider.subs.SubsProvider;
import butter.droid.provider.subs.mock.MockSubsProvider;
import butter.droid.provider.vodo.VodoModule;
import com.google.gson.Gson;
import dagger.Module;
import dagger.Provides;

@Module(includes = VodoModule.class)
public class ProviderModule {

    @Provides @ProviderScope
    public SubsProvider provideSubsProvider(final Context context) {
        return new MockSubsProvider(context);
    }

    @Provides @ProviderScope public MockMediaProvider provideMockMoviesProvider(Context context, Gson gson) {
        return new MockMediaProvider(context, gson);
    }

    @Provides @ProviderScope MockSubsProvider provideMockSubsProvider(final Context context) {
        return new MockSubsProvider(context);
    }

}
