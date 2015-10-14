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

package pct.droid.tv.utils;

import android.support.v17.leanback.widget.HeaderItem;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.ObjectAdapter;

public class MediaListRow extends ListRow
{
    private int rowIndex;

    //region Constructors

    public MediaListRow(HeaderItem header, ObjectAdapter adapter) {
        super(header, adapter);
    }

    public MediaListRow(long id, HeaderItem header, ObjectAdapter adapter) {
        super(id, header, adapter);
    }

    public MediaListRow(ObjectAdapter adapter) {
        super(adapter);
    }

    //endregion

    //region Setters/Getters

    public int getRowIndex() {
        return rowIndex;
    }

    public void setRowIndex(int rowIndex) {
        this.rowIndex = rowIndex;
    }

    //endregion
}
