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
import butter.droid.R;
import butter.droid.base.manager.internal.provider.ProviderManager;
import butter.droid.base.manager.internal.provider.ProviderManager.ProviderType;
import butter.droid.provider.MediaProvider;
import butter.droid.ui.main.MainPresenter;
import butter.droid.ui.main.navigation.NavigationDrawerFragment.AbsNavDrawerItem;
import butter.droid.ui.main.navigation.NavigationDrawerFragment.HeaderNavDrawerItem;
import butter.droid.ui.main.navigation.NavigationDrawerFragment.ProviderNavDrawerItem;
import butter.droid.ui.main.navigation.NavigationDrawerFragment.ScreenNavDrawerItem;
import butter.droid.ui.preferences.PreferencesActivity;
import java.util.ArrayList;
import java.util.List;

public class NavigationDrawerPresenterImpl implements NavigationDrawerPresenter {

    private final NavigationDrawerView view;
    private final ProviderManager providerManager;
    private final MainPresenter parentPresenter;

    private final List<NavigationDrawerFragment.AbsNavDrawerItem> items;

    private int selectedPosition;

    public NavigationDrawerPresenterImpl(NavigationDrawerView view, ProviderManager providerManager,
            MainPresenter parentPresenter) {
        this.view = view;
        this.providerManager = providerManager;
        this.parentPresenter = parentPresenter;

        items = getNavigationItems();
    }

    @Override public void onCreate(int savedPosition) {
        selectedPosition = savedPosition;
        items.get(savedPosition).setSelected(true);
    }

    @Override public void onViewCreated() {
        view.populateItems(items);
    }

    @Override public void onSavedInstanceState(Bundle outState) {
        outState.putInt(NavigationDrawerFragment.STATE_SELECTED_POSITION, selectedPosition);
    }

    @Override public void selectItem(int position) {
        if (selectedPosition != position) {
            items.get(selectedPosition).setSelected(false);
            selectedPosition = position;
            items.get(position).setSelected(true);
            view.itemsUpdated();
        }
    }

    @Override public void onMenuItemClicked(int position, AbsNavDrawerItem item) {
        switch (item.getType()) {
            case AbsNavDrawerItem.TYPE_HEADER:
                throw new IllegalStateException("Header item can not be clickable.");
            case AbsNavDrawerItem.TYPE_PROVIDER:
                parentPresenter.selectProvider(((ProviderNavDrawerItem) item).getProviderType());
                selectItem(position);
                break;
            case AbsNavDrawerItem.TYPE_SCREEN:
                parentPresenter.openMenuActivity(((ScreenNavDrawerItem) item).getActivityClass());
                break;
            default:
                throw new IllegalStateException("Unknown item type");
        }
    }

    @Override public void selectProvider(@ProviderType int providerType) {
        for (int i = 0; i < items.size(); i++) {
            AbsNavDrawerItem item = items.get(i);
            if (item.getType() == AbsNavDrawerItem.TYPE_PROVIDER
                    && ((ProviderNavDrawerItem) item).getProviderType() == providerType) {
                selectItem(i);
                return;
            }
        }
    }

    private List<NavigationDrawerFragment.AbsNavDrawerItem> getNavigationItems() {
        List<AbsNavDrawerItem> navItems = new ArrayList<>();

        navItems.add(new HeaderNavDrawerItem());

        for (int i = 0; i < providerManager.getProviders().length; i++) {
            final MediaProvider provider = providerManager.getProvider(i);
            navItems.add(new ProviderNavDrawerItem(provider.getName(), provider.getIcon(), i));
        }

        navItems.add(new ScreenNavDrawerItem(R.string.preferences, R.drawable.ic_nav_settings,
                PreferencesActivity.class));

        navItems.get(selectedPosition).setSelected(true);

        return navItems;
    }


}
