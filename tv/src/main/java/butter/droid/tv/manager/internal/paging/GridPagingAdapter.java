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

import android.support.annotation.Nullable;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.PresenterSelector;

import java.util.List;

import butter.droid.tv.presenters.LoadingCardPresenter.LoadingCardItem;
import butter.droid.tv.presenters.MediaCardPresenter.MediaCardItem;

public class GridPagingAdapter extends ArrayObjectAdapter implements ObjectPagingAdapter<MediaCardItem> {

    private boolean showLoading = false;

    public GridPagingAdapter(final PresenterSelector presenterSelector) {
        super(presenterSelector);
    }

    @Override public int size() {
        int size = super.size();

        if (showLoading()) {
            size++;
        }

        return size;
    }

    @Override public Object get(final int index) {
        if (showLoading() && index == super.size()) {
            return new LoadingCardItem();
        } else {
            return super.get(index);
        }
    }

    @Override public int getItemCount() {
        return size();
    }

    @Override public void addItems(@Nullable List<MediaCardItem> items) {
        if (items != null) {
            addAll(super.size(), items);
        }
    }

    @Override public void showLoading(final boolean show) {
        // TODO hide for now, it causes crashes
        //        if (showLoading != show) {
//            boolean before = showLoading();
//            showLoading = show;
//            boolean after = showLoading();
//
//            if (before != after) {
//                if (after) {
//                    notifyItemRangeInserted(super.size(), 1);
//                } else {
//                    notifyItemRangeRemoved(super.size(), 1);
//                }
//            }
//        }
    }

    private boolean showLoading() {
        return showLoading && super.size() > 0;
    }

}
