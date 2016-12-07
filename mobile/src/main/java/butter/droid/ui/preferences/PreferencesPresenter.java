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

import butter.droid.base.content.preferences.PrefItem;
import butter.droid.base.content.preferences.Prefs.PrefKey;

public interface PreferencesPresenter {
    void onCreate();

    void onDestroy();

    void itemSelected(PrefItem item);

    void onSimpleChaiseItemSelected(@PrefKey String key, int position);

    void onColorSelected(@PrefKey String key, int color);

    void onNumberSelected(@PrefKey String key, int value);

    void clearPreference(@PrefKey String key);

    void onFolderSelected(@PrefKey final String key, String folder);
}
