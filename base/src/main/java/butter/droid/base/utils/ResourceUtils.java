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
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.util.TypedValue;

public class ResourceUtils {

    public static TypedValue resolveThemeAttribute(Context context, int attrId) {
        TypedValue value = new TypedValue();
        context.getTheme().resolveAttribute(attrId, value, true);
        return value;
    }

    public static float getAttributeDimension(final Context context, final Resources.Theme theme, final int resId) {
        final TypedValue typedValue = new TypedValue(); // create a new typed value to received the resolved attribute
        // value
        final DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        if (!theme.resolveAttribute(resId, typedValue, true)) { // if we can't resolve the value
            throw new Resources.NotFoundException("Resource ID #0x" + Integer.toHexString(resId));
        }

        if (typedValue.type != TypedValue.TYPE_DIMENSION) { // if the value isn't of the correct type
            throw new Resources.NotFoundException("Resource ID #0x" + Integer.toHexString(resId) + " type #0x"
                    + Integer.toHexString(typedValue.type) + " is not valid");
        }
        return typedValue.getDimension(displayMetrics); // return the value of the attribute in terms of the display
    }
}
