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

package butter.droid.tv.manager.internal.paging;

import android.support.annotation.NonNull;
import butter.droid.base.manager.internal.paging.BasePagingManager;
import butter.droid.base.manager.internal.paging.CursorPagingListener;

public class GridPagingManager<T> extends BasePagingManager<T> {

    private static final int DEFAULT_PRELOAD_POSITIONS = 3;

    private ObjectPagingAdapter<T> adapter;

    public void init(int numColumns, @NonNull ObjectPagingAdapter<T> adapter, @NonNull CursorPagingListener listener) {
        if (numColumns <= 0) {
            throw new IllegalStateException("Grid has to have at least 1 column");
        }

        this.adapter = adapter;
        setPreloadPositionOffset(DEFAULT_PRELOAD_POSITIONS * numColumns);
        super.init(adapter, listener);
    }

    public void onItemSelected(Object item) {
        int itemPosition = adapter.indexOf(item);
        onNewPosition(itemPosition);
    }
}
