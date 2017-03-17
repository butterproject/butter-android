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

package butter.droid.ui.main.navigation;

import android.os.Bundle;

import butter.droid.base.manager.provider.ProviderManager.ProviderType;
import butter.droid.ui.main.navigation.NavigationDrawerFragment.AbsNavDrawerItem;

public interface NavigationDrawerPresenter {
    void onCreate(int savedPosition);

    void onViewCreated();

    void onSavedInstanceState(Bundle outState);

    void selectItem(int position);

    void onMenuItemClicked(int position, AbsNavDrawerItem item);

    void selectProvider(@ProviderType int providerType);
}
