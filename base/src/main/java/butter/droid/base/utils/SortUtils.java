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

import java.util.Arrays;
import java.util.Comparator;

public class SortUtils {

    public static String[] sortQualities(String[] qualities) {
        Arrays.sort(qualities, new Comparator<String>() {
            @Override
            public int compare(String lhs, String rhs) {
                if (lhs.contains("p") && rhs.contains("p")) {
                    int q1 = Integer.parseInt(lhs.substring(lhs.contains("-") ? lhs.indexOf("- ") + 2 : 0, lhs.indexOf('p')));
                    int q2 = Integer.parseInt(rhs.substring(rhs.contains("-") ? rhs.indexOf("- ") + 2 : 0, rhs.indexOf('p')));
                    if (q1 < q2) {
                        return 1;
                    } else if (q1 < q2) {
                        return -1;
                    } else {
                        return 0;
                    }
                }

                if(lhs.equals("3D"))
                    return 1;

                return 0;
            }
        });
        return qualities;
    }

}
