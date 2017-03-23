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

package butter.droid.manager.paging;

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

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int DEFAULT_PRELOAD_POSITIONS = 5;
    private static final int FIRST_PAGE_INDEX = 1;

    private int preloadPositionOffset = DEFAULT_PRELOAD_POSITIONS;
    private int lastVisibleItem;
    private int lastVisibleItemInProgress;
    private boolean completed;
    private boolean loading;
    private boolean refreshing;

    protected LinearLayoutManager layoutManager;
    private PagingAdapter<T> adapter;

    private IndexPagingListener listener;
    private int pageIndex;

    public void setPreloadPositionOffset(int preloadPositionOffset) {
        this.preloadPositionOffset = preloadPositionOffset;
    }

    public int getPageSize() {
        return DEFAULT_PAGE_SIZE;
    }

    public void addItems(@Nullable List<T> items) {
        if (refreshing) {
            adapter.clear();
            lastVisibleItem = 0;
            lastVisibleItemInProgress = 0;
            refreshing = false;
        }

        lastVisibleItem = lastVisibleItemInProgress;

        adapter.addItems(items);

        int pageSize = items == null || items.size() == 0 ? 0 : items.size();
        completed = pagingIsCompleted(pageSize);
        if (!completed) {
            onItemsAdded();
        }

        setLoading(false);
    }

    private boolean pagingIsCompleted(int itemsCount) {
        return itemsCount == 0;
    }

    protected void onItemsAdded() {
        pageIndex++;
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

    protected int getPageIndex() {
        return pageIndex;
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
        pageIndex = FIRST_PAGE_INDEX;
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

        loadNextPage(getPageSize());
    }

    protected void loadNextPage(int pageSize) {
        listener.loadPage(pageIndex, pageSize);
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
                handler.post(new Runnable() {
                    @Override public void run() {
                        onNewPosition(position);
                    }
                });
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
        loadFirstPage(getPageSize());
    }

    protected void loadFirstPage(int pageSize) {
        pageIndex = FIRST_PAGE_INDEX;
        loadNextPage(pageSize);
    }
}
