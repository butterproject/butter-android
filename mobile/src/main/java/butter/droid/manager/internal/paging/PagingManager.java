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

package butter.droid.manager.internal.paging;

import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.OnChildAttachStateChangeListener;
import android.view.View;
import butter.droid.base.manager.internal.paging.BasePagingManager;
import butter.droid.base.manager.internal.paging.CursorPagingListener;
import butter.droid.base.manager.internal.paging.PagingAdapter;

public class PagingManager<T> extends BasePagingManager<T> {

    private static final int DEFAULT_PRELOAD_POSITIONS = 5;

    protected LinearLayoutManager layoutManager;

    public void init(@NonNull RecyclerView recyclerView, @NonNull PagingAdapter<T> adapter,
                @NonNull CursorPagingListener listener) {
        initForRecyclerView(recyclerView);
        setPreloadPositionOffset(DEFAULT_PRELOAD_POSITIONS);
        super.init(adapter, listener);
    }

    private void initForRecyclerView(RecyclerView recyclerView) {
        layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();

        recyclerView.addOnChildAttachStateChangeListener(new OnChildAttachStateChangeListener() {

            private final Handler handler = new Handler();

            @Override public void onChildViewAttachedToWindow(View view) {
                final int position = layoutManager.findLastVisibleItemPosition();
                handler.post(() -> onNewPosition(position));
            }

            @Override public void onChildViewDetachedFromWindow(View view) {
            }
        });
    }

}
