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

package butter.droid.ui.media.list.base;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import javax.inject.Inject;

import butter.droid.R;
import butter.droid.base.manager.internal.paging.CursorPagingListener;
import butter.droid.base.providers.media.model.MediaWrapper;
import butter.droid.base.widget.recycler.RecyclerClickListener;
import butter.droid.base.widget.recycler.RecyclerItemClickListener;
import butter.droid.manager.internal.paging.PagingManager;
import butter.droid.provider.base.filter.Filter;
import butter.droid.ui.media.detail.MediaDetailActivity;
import butter.droid.ui.media.list.base.dialog.LoadingDetailDialogFragment;
import butter.droid.ui.media.list.base.list.MediaGridAdapter;
import butter.droid.ui.media.list.base.list.MediaGridAdapter.MediaGridSpacingItemDecoration;
import butterknife.BindView;
import butterknife.ButterKnife;
import dagger.android.support.DaggerFragment;

/**
 * This fragment is the main screen for viewing a collection of media items.
 * <p/>
 * LOADING
 * <p/>
 * This fragment has 2 ways of representing a loading state; If the data is being loaded for the first time, or the media detail for the
 * detail screen is being loaded,a progress layout is displayed with a message.
 * <p/>
 * If a page is being loaded, the adapter will display a progress item.
 * <p/>
 * MODE
 * <p/>
 * This fragment can be instantiated with ether a SEARCH mode, or a NORMAL mode. SEARCH mode simply does not load any initial data.
 */
public class BaseMediaListFragment extends DaggerFragment implements BaseMediaListView, CursorPagingListener, RecyclerClickListener {

    public static final String EXTRA_PROVIDER = "butter.droid.ui.media.list.base.BaseMediaListFragment.extra_provider";
    public static final String EXTRA_FILTER = "butter.droid.ui.media.list.base.BaseMediaListFragment.extra_filter";

    @Inject BaseMediaListPresenter presenter;

    private Context context;
    private MediaGridAdapter adapter;
    private GridLayoutManager layoutManager;
    private Integer columns = 2;
    protected PagingManager<MediaWrapper> pagingManager;

    View rootView;
    @BindView(R.id.progressOverlay) LinearLayout progressOverlay;
    @BindView(R.id.recyclerView) RecyclerView recyclerView;
    @BindView(R.id.emptyView) public TextView emptyView;
    @BindView(R.id.progress_textview) TextView progressTextView;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        context = getActivity();

        rootView = inflater.inflate(R.layout.fragment_media, container, false);
        ButterKnife.bind(this, rootView);

        columns = getResources().getInteger(R.integer.overview_cols);

        layoutManager = new GridLayoutManager(context, columns);
        recyclerView.setLayoutManager(layoutManager);

        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final MediaGridSpacingItemDecoration gridSpacingDecoration = new MediaGridSpacingItemDecoration(context, columns);
        final int itemHeight = gridSpacingDecoration.getItemHeight();

        adapter = new MediaGridAdapter(itemHeight);

        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(gridSpacingDecoration);
        recyclerView.setHasFixedSize(true);
        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(getContext(), this));

        pagingManager = new PagingManager<>();
        pagingManager.init(recyclerView, adapter, this);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Bundle arguments = getArguments();
        int providerId = arguments.getInt(EXTRA_PROVIDER);
        Filter filter = arguments.getParcelable(EXTRA_FILTER);

        presenter.onActivityCreated(providerId, filter);
    }

    @Override public void onDestroy() {
        super.onDestroy();

        presenter.onDestroy();
    }

    @Override public void loadPage(@Nullable String endCursor) {
        presenter.loadNextPage(endCursor);
    }

    @Override public void onItemClick(View view, final int position) {
        final MediaWrapper media = adapter.getItem(position);
        presenter.onMediaItemClicked(media);
    }

    @Override public void updateLoadingMessage(@StringRes int messageRes) {
        progressTextView.setText(messageRes);
    }

    @Override public void showData() {
        pagingManager.setLoading(false);
        progressOverlay.setVisibility(View.GONE);
        emptyView.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
    }

    @Override public void addItems(List<MediaWrapper> items, boolean completed, String endCursor) {
        pagingManager.addItems(items, completed, endCursor);
    }

    @Override public void showEmpty() {
        pagingManager.setLoading(false);
        progressOverlay.setVisibility(View.GONE);
        emptyView.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
    }

    @Override public void showErrorMessage(@StringRes int message) {
        Snackbar.make(rootView, message, Snackbar.LENGTH_SHORT).show();
    }

    @Override public void clearAdapter() {
        pagingManager.reset();
    }

    @Override public void refreshAdapter() {
        pagingManager.reset();
        pagingManager.getNextPage();
    }

    @Override public void showLoading() {
        emptyView.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
        progressOverlay.setVisibility(View.VISIBLE);
    }

    @Override public void showMediaLoadingDialog() {
        getChildFragmentManager().beginTransaction()
                .show(LoadingDetailDialogFragment.newInstance())
                .commit();
    }

    @Override public void showDetails(final MediaWrapper item) {
        startActivity(MediaDetailActivity.getIntent(getActivity(), item));
    }

}
