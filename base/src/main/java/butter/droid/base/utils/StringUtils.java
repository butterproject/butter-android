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

/*****************************************************************************
 * Strings.java
 *****************************************************************************
 * Copyright Â© 2011-2014 VLC authors and VideoLAN
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston MA 02110-1301, USA.
 *****************************************************************************/

package butter.droid.base.utils;

import android.support.annotation.Nullable;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

public class StringUtils {

    private static final DecimalFormat formatter = (DecimalFormat) NumberFormat.getInstance(Locale.US);

    static {
        formatter.applyPattern("00");
    }

    /**
     * Convert time to a string
     *
     * @param millis e.g.time/length from file
     * @return Formatted string (hh:)mm:ss
     */
    public static String millisToString(long millis) {
        boolean negative = millis < 0;
        millis = Math.abs(millis);

        millis /= 1000;
        int sec = (int) (millis % 60);
        millis /= 60;
        int min = (int) (millis % 60);
        millis /= 60;
        int hours = (int) millis;

        String time;
        if (millis > 0) {
            time = (negative ? "-" : "") + hours + ":" + formatter.format(min) + ":" + formatter.format(sec);
        } else {
            time = (negative ? "-" : "") + min + ":" + formatter.format(sec);
        }

        return time;
    }

    /**
     * Uppercase first character
     *
     * @param str Input string
     * @return Output string
     */
    public static String uppercaseFirst(String str) {
        return str.substring(0, 1).toUpperCase(Locale.getDefault()) + str.substring(1, str.length());
    }

    public static String capWords(String givenString) {
        String[] arr = givenString.split(" ");
        StringBuffer sb = new StringBuffer();

        for (int i = 0; i < arr.length; i++) {
            sb.append(Character.toUpperCase(arr[i].charAt(0)))
                    .append(arr[i].substring(1)).append(" ");
        }
        return sb.toString().trim();
    }

    public static String colorToString(int color) {
        return String.format("#%06X", 0xFFFFFF & color);
    }

    public static boolean isEmpty(@Nullable CharSequence sequence) {
        return sequence == null || sequence.length() == 0;
    }

}
