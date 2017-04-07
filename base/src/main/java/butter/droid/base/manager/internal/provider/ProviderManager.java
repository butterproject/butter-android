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

import android.support.annotation.IntDef;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import butter.droid.base.providers.media.MediaProvider;
import butter.droid.base.providers.subs.SubsProvider;

public class ProviderManager {

    // region IntDef

    @IntDef({PROVIDER_TYPE_MOVIE, PROVIDER_TYPE_SHOW})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ProviderType {
    }

    public static final int PROVIDER_TYPE_MOVIE = 0;
    public static final int PROVIDER_TYPE_SHOW = 1;

    // endregion IntDef

    @Nullable private final MediaProvider movieProvider;
    @Nullable private final MediaProvider showProvider;

    @ProviderType private int currentProviderType;

    public ProviderManager(@Nullable MediaProvider movieProvider, @Nullable MediaProvider showProvider) {
        this.movieProvider = movieProvider;
        this.showProvider = showProvider;

        if (movieProvider != null) {
            currentProviderType = PROVIDER_TYPE_MOVIE;
        } else if (showProvider != null) {
            currentProviderType = PROVIDER_TYPE_SHOW;
        } else {
            throw new IllegalStateException("No media providers vere provider");
        }
    }

    @ProviderType public int getCurrentMediaProviderType() {
        return currentProviderType;
    }

    @SuppressWarnings("ConstantConditions")
    @NonNull public MediaProvider getCurrentMediaProvider() {
        return getMediaProvider(getCurrentMediaProviderType());
    }

    @MainThread public void setCurrentProviderType(@ProviderType int providerType) {
        if (getMediaProvider(providerType) != null) {
            if (this.currentProviderType != providerType) {
                this.currentProviderType = providerType;
            }
        } else {
            throw new IllegalStateException("Provider for type no provided");
        }
    }

    @Nullable public MediaProvider getMediaProvider(@ProviderType int providerType) {
        if (providerType == PROVIDER_TYPE_MOVIE) {
            return movieProvider;
        } else {
            return showProvider;
        }
    }

    public boolean hasProvider(@ProviderType int providerType) {
        return getMediaProvider(providerType) != null;
    }

    public SubsProvider getCurrentSubsProvider() {
        return getCurrentMediaProvider().getSubsProvider();
    }

    public boolean hasCurrentSubsProvider() {
        return getCurrentMediaProvider().hasSubsProvider();
    }

}
