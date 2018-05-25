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

package butter.droid.provider.base.nav;

import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import butter.droid.provider.base.filter.Sorter;

public final class NavItem {

    @DrawableRes private final int icon;
    @StringRes private final int label;
    @Nullable private final Sorter sorter;

    public NavItem(final int icon, final int label, @Nullable final Sorter sorter) {
        this.icon = icon;
        this.label = label;
        this.sorter = sorter;
    }

    @DrawableRes public int getIcon() {
        return icon;
    }

    @StringRes public int getLabel() {
        return label;
    }

    @Nullable public Sorter getSorter() {
        return sorter;
    }

    @Override public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof NavItem)) {
            return false;
        }

        final NavItem navItem = (NavItem) o;

        if (icon != navItem.icon) {
            return false;
        }
        if (label != navItem.label) {
            return false;
        }
        return sorter != null ? sorter.equals(navItem.sorter) : navItem.sorter == null;
    }

    @Override public int hashCode() {
        int result = icon;
        result = 31 * result + label;
        result = 31 * result + (sorter != null ? sorter.hashCode() : 0);
        return result;
    }
}
