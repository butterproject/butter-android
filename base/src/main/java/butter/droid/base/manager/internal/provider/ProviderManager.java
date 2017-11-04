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
import butter.droid.provider.MediaProvider;
import butter.droid.provider.subs.SubsProvider;

public class ProviderManager {

    @NonNull private final MediaProvider[] providers;
    private final SubsProvider subsProvider; // TODO subs - should be per media provider

    public ProviderManager(SubsProvider subsProvider, @NonNull MediaProvider... providers) {
        //noinspection ConstantConditions
        if (providers == null || providers.length == 0) {
            throw new IllegalStateException("No media providers available");
        }

        this.providers = providers;
        this.subsProvider = subsProvider;
    }

    @NonNull public MediaProvider getProvider(int providerId) {
        return providers[providerId];
    }

    @NonNull public MediaProvider[] getProviders() {
        return providers;
    }

    public int getProviderCount() {
        return providers.length;
    }

    public SubsProvider getCurrentSubsProvider() {
        return subsProvider;
    }

    public boolean hasCurrentSubsProvider() {
        return subsProvider != null;
    }

}
