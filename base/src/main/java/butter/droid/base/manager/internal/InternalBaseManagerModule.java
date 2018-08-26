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

package butter.droid.base.manager.internal;

import java.util.Arrays;
import java.util.Set;

import butter.droid.base.Internal;
import butter.droid.base.manager.internal.provider.ProviderManager;
import butter.droid.base.manager.internal.provider.model.ProviderWrapper;
import dagger.Module;
import dagger.Provides;
import timber.log.Timber;

@Module
public class InternalBaseManagerModule {

    @Provides @Internal ProviderManager provideProviderManager(final Set<ProviderWrapper> providers) {
        ProviderWrapper[] array = new ProviderWrapper[providers.size()];
        providers.toArray(array);

        for (ProviderWrapper providerWrapper : array) {
            Timber.d("Test 2 %s", providerWrapper.getMediaProvider().getClass().getName());
        }

        Arrays.sort(array, (o1, o2) -> o1.getPosition() - o2.getPosition());

        for (ProviderWrapper providerWrapper : array) {
            Timber.d("Test 3 %s", providerWrapper.getMediaProvider().getClass().getName());
        }

        return new ProviderManager(array);
    }

}
