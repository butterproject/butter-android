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

package pct.droid.widget;

import android.content.Context;
import android.util.AttributeSet;

import pct.droid.utils.CheatSheet;

public class ImageButton extends android.widget.ImageButton {

    public ImageButton(Context context) {
        super(context);
    }

    public ImageButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ImageButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void setContentDescription(CharSequence contentDesc) {
        super.setContentDescription(contentDesc);
        if (contentDesc.length() > 0)
            CheatSheet.setup(this, contentDesc);
    }

}
