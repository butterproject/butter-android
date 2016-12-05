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

import android.content.Intent;
import android.support.annotation.StringRes;

import java.util.Map;

import butter.droid.base.content.preferences.PrefItem;
import butter.droid.base.content.preferences.Prefs.PrefKey;

public interface PreferencesView {
    void displayItems(String[] keys, Map<String, PrefItem> items);

    void openSimpleChoiceSelector(@PrefKey final String key, @StringRes int title, String[] items, int value);

    void openColorSelector(@PrefKey final String key, @StringRes int title, int value);

    void openNumberSelector(@PrefKey final String key, @StringRes int title, int value, int min, int max);

    void openDirectorySelector(@PrefKey final String key, @StringRes int title, String value);

    void openPreciseNumberSelector(@PrefKey final String key, @StringRes int title, int value, int min, int max);

    void openPreciseSmallNumberSelector(@PrefKey final String key, @StringRes int title, int value, int min, int max);

    void openActivity(Intent intent);

    void openChangelog();

    void updateItem(int position, PrefItem preferenceItem);

    void showMessage(@StringRes int message);

    void showAboutScreen();
}
