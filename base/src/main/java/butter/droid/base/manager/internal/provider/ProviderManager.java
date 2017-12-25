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

package butter.droid.base.manager.internal.provider;

import android.support.annotation.NonNull;
import butter.droid.base.manager.internal.provider.model.ProviderWrapper;
import butter.droid.provider.MediaProvider;
import butter.droid.provider.subs.SubsProvider;

public class ProviderManager {

    @NonNull private final ProviderWrapper[] providers;

    public ProviderManager(@NonNull ProviderWrapper... providers) {
        //noinspection ConstantConditions
        if (providers == null || providers.length == 0) {
            throw new IllegalStateException("No media providers available");
        }

        this.providers = providers;
    }

    @NonNull public MediaProvider getProvider(int providerId) {
        return providers[providerId].getMediaProvider();
    }

    @NonNull public ProviderWrapper[] getProviders() {
        return providers;
    }

    public int getProviderCount() {
        return providers.length;
    }

    public SubsProvider getSubsProvider(int providerId) {
        return providers[providerId].getSubsProvider();
    }

    public boolean hasSubsProvider(int providerId) {
        return getSubsProvider(providerId) != null;
    }

}
