/*
 * This file is part of Popcorn Time.
 *
 * Popcorn Time is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Popcorn Time is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Popcorn Time. If not, see <http://www.gnu.org/licenses/>.
 */

package pct.droid.base.content.preferences;

import android.content.Context;

import java.util.List;

import pct.droid.base.utils.PrefUtils;

public class DefaultQuality {

    public static String get(Context context, List<String> availableQualities) {
        String quality = PrefUtils.get(context, Prefs.QUALITY_DEFAULT, "720p");
        String[] fallbackOrder = new String[] {"720p", "480p", "1080p"};

        if(availableQualities.indexOf(quality) == -1) {
            for (String fallbackQuality : fallbackOrder) {
                if (availableQualities.indexOf(fallbackQuality) != -1) {
                    quality = fallbackQuality;
                    break;
                }
            }
        }

        return quality;
    }

}
