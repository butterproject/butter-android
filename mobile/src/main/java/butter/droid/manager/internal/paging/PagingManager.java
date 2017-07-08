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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.OnChildAttachStateChangeListener;
import android.view.View;
import java.util.List;
import timber.log.Timber;

public class PagingManager<T> {

    private static final int DEFAULT_PRELOAD_POSITIONS = 5;

    private int preloadPositionOffset = DEFAULT_PRELOAD_POSITIONS;
    private int lastVisibleItem;
    private int lastVisibleItemInProgress;
    private boolean completed;
    private boolean loading;
    private boolean refreshing;

    protected LinearLayoutManager layoutManager;
    private PagingAdapter<T> adapter;

    private IndexPagingListener listener;
    @Nullable private String endCursor;

    public void setPreloadPositionOffset(int preloadPositionOffset) {
        this.preloadPositionOffset = preloadPositionOffset;
    }

    public void addItems(@Nullable List<T> items, boolean completed, String endCursor) {
        if (refreshing) {
            adapter.clear();
            lastVisibleItem = 0;
            lastVisibleItemInProgress = 0;
            refreshing = false;
        }

        lastVisibleItem = lastVisibleItemInProgress;

        adapter.addItems(items);

        this.completed = completed;
        if (!completed) {
            this.endCursor = endCursor;
        }

        setLoading(false);
    }

    private boolean pagingIsCompleted(int itemsCount) {
        return itemsCount == 0;
    }

    public boolean isCompleted() {
        return completed;
    }

    public boolean isLoading() {
        return loading;
    }

    public boolean isRefreshing() {
        return refreshing;
    }

    @Nullable public String getEndCursor() {
        return endCursor;
    }

    public void init(@NonNull RecyclerView recyclerView, @NonNull PagingAdapter<T> adapter,
            @NonNull IndexPagingListener listener) {
        this.adapter = adapter;
        this.listener = listener;

        initForRecyclerView(recyclerView);
        reset();
    }

    public void reset() {
        adapter.clear();
        completed = false;
        refreshing = false;
        lastVisibleItem = 0;
        lastVisibleItemInProgress = 0;
        endCursor = null;
        setLoading(false);
    }

    public void getNextPage() {
        if (refreshing) {
            Timber.d("getNextPage: refreshing");
            return;
        }

        if (completed) {
            Timber.d("getNextPage: completed");
            return;
        }

        if (loading) {
            Timber.d("getNextPage: in progress");
            return;
        }

        setLoading(true);

        loadNextPage();
    }

    protected void loadNextPage() {
        listener.loadPage(endCursor);
    }

    protected void onNewPosition(int position) {
        if (position <= lastVisibleItem) {
            return;
        }

        if (completed || refreshing || loading) {
            return;
        }

        lastVisibleItemInProgress = position;

        if (lastVisibleItemInProgress > adapter.getItemCount() - preloadPositionOffset) {
            getNextPage();
        } else {
            lastVisibleItem = lastVisibleItemInProgress;
        }
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

    public void setLoading(boolean loading) {
        if (this.loading != loading) {
            this.loading = loading;
            adapter.showLoading(loading);
        }
    }

    public void refresh() {
        refreshing = true;
        loadFirstPage();
    }

    protected void loadFirstPage() {
        endCursor = null;
        loadNextPage();
    }
}
