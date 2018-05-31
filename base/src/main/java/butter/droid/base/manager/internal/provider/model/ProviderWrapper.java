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

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import butter.droid.provider.MediaProvider;
import butter.droid.provider.subs.SubsProvider;

public class ProviderWrapper {

    @NonNull private final MediaProvider mediaProvider;
    @Nullable private final SubsProvider subsProvider;

    @StringRes private final int displayName;
    @DrawableRes private final int icon;

    private final int position;

    public ProviderWrapper(@NonNull final MediaProvider mediaProvider, @Nullable final SubsProvider subsProvider, final int displayName,
            final int icon, final int position) {
        this.mediaProvider = mediaProvider;
        this.subsProvider = subsProvider;
        this.displayName = displayName;
        this.icon = icon;
        this.position = position;
    }

    @NonNull public MediaProvider getMediaProvider() {
        return mediaProvider;
    }

    @Nullable public SubsProvider getSubsProvider() {
        return subsProvider;
    }

    @StringRes public final int getDisplayName() {
        return displayName;
    }

    @DrawableRes public final int getIcon() {
        return icon;
    }

    public int getPosition() {
        return position;
    }
}
