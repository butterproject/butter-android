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
import butter.droid.provider.subs.opensubs.OpenSubsModule;
import butter.droid.provider.subs.opensubs.OpenSubsProvider;
import butter.droid.provider.vodo.VodoModule;
import butter.droid.provider.vodo.VodoProvider;
import dagger.Module;
import dagger.Provides;
import dagger.multibindings.ElementsIntoSet;
import java.util.Set;
import java.util.TreeSet;

@Module(includes = {
        ProviderBindModule.class,
        VodoModule.class,
        MockProviderModule.class,
        OpenSubsModule.class}
)
public class ProviderModule {

    @Provides @ProviderScope @ElementsIntoSet Set<ProviderWrapper> provideVodoWrapper(final VodoProvider vodoProvider,
            final OpenSubsProvider openSubsProvider, final MockMediaProvider mockProvider, final MockSubsProvider mockSubsProvider) {
        Set<ProviderWrapper> set = new TreeSet<>((o1, o2) -> o2.getPosition() - o1.getPosition());
        set.add(new ProviderWrapper(vodoProvider, openSubsProvider, R.string.vodo_label, R.drawable.ic_nav_movies, 0));
        set.add(new ProviderWrapper(mockProvider, mockSubsProvider, butter.droid.provider.mock.R.string.title_movies,
                butter.droid.provider.mock.R.drawable.ic_nav_movies, 1));
        return set;
    }

}
