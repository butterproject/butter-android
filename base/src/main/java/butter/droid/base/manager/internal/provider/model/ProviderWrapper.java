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

package butter.droid.base.manager.internal.provider.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import butter.droid.provider.MediaProvider;
import butter.droid.provider.subs.SubsProvider;

public class ProviderWrapper {

    @NonNull private final MediaProvider mediaProvider;
    @Nullable private final SubsProvider subsProvider;

    public ProviderWrapper(@NonNull final MediaProvider mediaProvider, final SubsProvider subsProvider) {
        this.mediaProvider = mediaProvider;
        this.subsProvider = subsProvider;
    }

    @NonNull public MediaProvider getMediaProvider() {
        return mediaProvider;
    }

    @Nullable public SubsProvider getSubsProvider() {
        return subsProvider;
    }
}
