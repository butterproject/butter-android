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

import butter.droid.base.R;
import butter.droid.base.manager.internal.provider.model.ProviderWrapper;
import butter.droid.provider.base.ProviderScope;
import butter.droid.provider.mock.MockMediaProvider;
import butter.droid.provider.mock.MockProviderModule;
import butter.droid.provider.subs.mock.MockSubsProvider;
import butter.droid.provider.vodo.VodoModule;
import butter.droid.provider.vodo.VodoProvider;
import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoSet;

@Module(includes = {
        ProviderBindModule.class,
        VodoModule.class,
        MockProviderModule.class}
)
public class ProviderModule {

    @Provides @ProviderScope @IntoSet ProviderWrapper provideVodoWrapper(final VodoProvider mediaProvider) {
        return new ProviderWrapper(mediaProvider, null, R.string.vodo_label, R.drawable.ic_nav_movies);
    }

    @Provides @ProviderScope @IntoSet ProviderWrapper provideMockWrapper(final MockMediaProvider mediaProvider,
            final MockSubsProvider subsProvider) {
        return new ProviderWrapper(mediaProvider, subsProvider, butter.droid.provider.mock.R.string.title_movies,
                butter.droid.provider.mock.R.drawable.ic_nav_movies);
    }

}
