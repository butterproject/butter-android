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

package butter.droid.ui.main.pager;

import android.support.annotation.DrawableRes;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import butter.droid.base.R;
import butter.droid.provider.base.filter.Sorter;
import butter.droid.provider.base.nav.NavItem;

public class NavInfo {

    @IdRes private final int id;
    @DrawableRes private final int icon;
    @StringRes private final int label;
    @Nullable private final Sorter sorter;
    private final int providerId;

    public NavInfo(@NonNull NavItem item, final int providerId) {
        this.id = R.id.nav_item_filter;
        this.icon = item.getIcon();
        this.label = item.getLabel();
        this.providerId = providerId;
        this.sorter = item.getSorter();
    }

    public NavInfo(@IdRes final int id, @DrawableRes final int icon, @StringRes final int label, final int providerId) {
        if (id == R.id.nav_item_filter) {
            throw new IllegalStateException("Filter items have to have filter parameter set");
        }

        this.id = id;
        this.icon = icon;
        this.label = label;
        this.providerId = providerId;
        this.sorter = null;
    }

    public int getId() {
        return id;
    }

    @DrawableRes
    public int getIcon() {
        return icon;
    }

    @StringRes
    public int getLabel() {
        return label;
    }

    public int getProviderId() {
        return providerId;
    }

    @Nullable public Sorter getSorter() {
        return sorter;
    }
}
