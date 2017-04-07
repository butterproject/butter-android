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

package butter.droid.base.manager.prefs;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import butter.droid.base.content.ObscuredSharedPreferences;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class PrefManager {

    private final SharedPreferences preferences;

    @Inject
    public PrefManager(SharedPreferences preferences) {
        this.preferences = preferences;
    }

    /**
     * Clear the central {@link ObscuredSharedPreferences}
     */
    public void clear() {
        preferences.edit().clear().apply();
    }

    /**
     * Save a string to the central {@link ObscuredSharedPreferences}
     *
     * @param key     Key of the preference
     * @param value   Value of the preference
     */
    public void save(String key, String value) {
        preferences.edit().putString(key, value).apply();
    }

    /**
     * Get a saved string from the central {@link ObscuredSharedPreferences}
     *
     * @param key          Key of the preference
     * @param defaultValue Default
     * @return The saved String
     */
    public String get(String key, String defaultValue) {
        return preferences.getString(key, defaultValue);
    }

    /**
     * Save a boolean to the central {@link ObscuredSharedPreferences}
     *
     * @param key     Key of the preference
     * @param value   Value of the preference
     */
    public void save(String key, boolean value) {
        preferences.edit().putBoolean(key, value).apply();
    }

    /**
     * Get a saved boolean from the central {@link ObscuredSharedPreferences}
     *
     * @param key          Key of the preference
     * @param defaultValue Default
     * @return The saved bool
     */
    public Boolean get(String key, boolean defaultValue) {
        return preferences.getBoolean(key, defaultValue);
    }

    /**
     * Save a long to the central {@link ObscuredSharedPreferences}
     *
     * @param key     Key of the preference
     * @param value   Value of the preference
     */
    public void save(String key, long value) {
        preferences.edit().putLong(key, value).apply();
    }

    /**
     * Get a saved long from the central {@link ObscuredSharedPreferences}
     *
     * @param key          Key of the preference
     * @param defaultValue Default
     * @return The saved long
     */
    public long get(String key, long defaultValue) {
        return preferences.getLong(key, defaultValue);
    }

    /**
     * Save a int to the central {@link ObscuredSharedPreferences}
     *
     * @param key     Key of the preference
     * @param value   Value of the preference
     */
    public void save(String key, int value) {
        preferences.edit().putInt(key, value).apply();
    }

    /**
     * Get a saved integer from the central {@link ObscuredSharedPreferences}
     *
     * @param key          Key of the preference
     * @param defaultValue Default
     * @return The saved integer
     */
    public int get(String key, int defaultValue) {
        return preferences.getInt(key, defaultValue);
    }

    /**
     * Check if the central {@link ObscuredSharedPreferences} contains a preference that uses that key
     *
     * @param key     Key
     * @return {@code true} if there exists a preference
     */
    public boolean contains(String key) {
        return preferences.contains(key);
    }

    /**
     * Remove item from the central {@link ObscuredSharedPreferences} if it exists
     *
     * @param key     Key
     */
    public void remove(String key) {
        preferences.edit().remove(key).apply();
    }

    public void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
        preferences.registerOnSharedPreferenceChangeListener(listener);
    }

    public void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
        preferences.unregisterOnSharedPreferenceChangeListener(listener);
    }

    public SharedPreferences getPrefs() {
        return preferences;
    }
}
