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

package butter.droid.base.utils;

import android.content.Context;

import butter.droid.base.Constants;
import butter.droid.base.content.ObscuredSharedPreferences;

public class PrefUtils {

    /**
     * Clear the central {@link ObscuredSharedPreferences}
     *
     * @param context Context
     */
    public static void clear(Context context) {
        getPrefs(context).edit().clear().apply();
    }

    /**
     * Save a string to the central {@link ObscuredSharedPreferences}
     *
     * @param context Context
     * @param key     Key of the preference
     * @param value   Value of the preference
     */
    public static void save(Context context, String key, String value) {
        getPrefs(context).edit().putString(key, value).apply();
    }

    /**
     * Get a saved string from the central {@link ObscuredSharedPreferences}
     *
     * @param context      Context
     * @param key          Key of the preference
     * @param defaultValue Default
     * @return The saved String
     */
    public static String get(Context context, String key, String defaultValue) {
        return getPrefs(context).getString(key, defaultValue);
    }

    /**
     * Save a boolean to the central {@link ObscuredSharedPreferences}
     *
     * @param context Context
     * @param key     Key of the preference
     * @param value   Value of the preference
     */
    public static void save(Context context, String key, boolean value) {
        getPrefs(context).edit().putBoolean(key, value).apply();
    }

    /**
     * Get a saved boolean from the central {@link ObscuredSharedPreferences}
     *
     * @param context      Context
     * @param key          Key of the preference
     * @param defaultValue Default
     * @return The saved bool
     */
    public static Boolean get(Context context, String key, boolean defaultValue) {
        return getPrefs(context).getBoolean(key, defaultValue);
    }

    /**
     * Save a long to the central {@link ObscuredSharedPreferences}
     *
     * @param context Context
     * @param key     Key of the preference
     * @param value   Value of the preference
     */
    public static void save(Context context, String key, long value) {
        getPrefs(context).edit().putLong(key, value).apply();
    }

    /**
     * Get a saved long from the central {@link ObscuredSharedPreferences}
     *
     * @param context      Context
     * @param key          Key of the preference
     * @param defaultValue Default
     * @return The saved long
     */
    public static long get(Context context, String key, long defaultValue) {
        return getPrefs(context).getLong(key, defaultValue);
    }

    /**
     * Save a int to the central {@link ObscuredSharedPreferences}
     *
     * @param context Context
     * @param key     Key of the preference
     * @param value   Value of the preference
     */
    public static void save(Context context, String key, int value) {
        getPrefs(context).edit().putInt(key, value).apply();
    }

    /**
     * Get a saved integer from the central {@link ObscuredSharedPreferences}
     *
     * @param context      Context
     * @param key          Key of the preference
     * @param defaultValue Default
     * @return The saved integer
     */
    public static int get(Context context, String key, int defaultValue) {
        return getPrefs(context).getInt(key, defaultValue);
    }

    /**
     * Check if the central {@link ObscuredSharedPreferences} contains a preference that uses that key
     *
     * @param context Context
     * @param key     Key
     * @return {@code true} if there exists a preference
     */
    public static Boolean contains(Context context, String key) {
        return getPrefs(context).contains(key);
    }

    /**
     * Remove item from the central {@link ObscuredSharedPreferences} if it exists
     *
     * @param context Context
     * @param key     Key
     */
    public static void remove(Context context, String key) {
        getPrefs(context).edit().remove(key).apply();
    }

    /**
     * Get the central {@link ObscuredSharedPreferences}
     *
     * @param context Context
     * @return {@link ObscuredSharedPreferences}
     */
    public static ObscuredSharedPreferences getPrefs(Context context) {
        return new ObscuredSharedPreferences(context, context.getSharedPreferences(Constants.PREFS_FILE, Context.MODE_PRIVATE));
    }

}
