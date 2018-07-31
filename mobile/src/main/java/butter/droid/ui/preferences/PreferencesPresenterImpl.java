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

package butter.droid.ui.preferences;

import android.content.res.Resources;

import java.util.Map;

import butter.droid.base.content.preferences.PrefItem;
import butter.droid.base.content.preferences.PreferencesHandler;
import butter.droid.base.manager.internal.vlc.PlayerManager;
import butter.droid.base.manager.prefs.PrefManager;
import butter.droid.base.ui.preferences.BasePreferencesPresenterImpl;

public class PreferencesPresenterImpl extends BasePreferencesPresenterImpl implements PreferencesPresenter {

    private final PreferencesView view;
    private final PreferencesHandler preferencesHandler;

    public PreferencesPresenterImpl(PreferencesView view, PrefManager prefManager,
            PreferencesHandler preferencesHandler, Resources resources, PlayerManager playerManager) {
        super(view, prefManager, playerManager, preferencesHandler, resources, false);
        this.view = view;
        this.preferencesHandler = preferencesHandler;
    }

    @Override public void onCreate() {
        super.onCreate();

        Map<String, PrefItem> items = preferencesHandler.getPreferenceItems(keys);
        view.displayItems(keys, items);
    }

    @Override protected void updateDisplayItem(int position, PrefItem prefItem) {
        view.updateItem(position, prefItem);
    }

    @Override public void itemSelected(PrefItem item) {
        updateItem(item);
    }

}
