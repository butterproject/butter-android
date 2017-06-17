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
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import butter.droid.R;
import butter.droid.base.widget.recycler.RecyclerClickListener;
import butter.droid.base.widget.recycler.RecyclerItemClickListener;
import butter.droid.manager.paging.IndexPagingListener;
import butter.droid.manager.paging.PagingManager;
import butter.droid.provider.base.Media;
import butter.droid.provider.base.filter.Filter;
import butter.droid.ui.media.list.base.dialog.LoadingDetailDialogFragment;
import butter.droid.ui.media.list.base.list.MediaGridAdapter;
import butter.droid.ui.media.list.base.list.MediaGridAdapter.MediaGridSpacingItemDecoration;
import butterknife.BindView;
import butterknife.ButterKnife;
import java.util.List;
import javax.inject.Inject;
import org.parceler.Parcels;

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
public class BaseMediaListFragment extends Fragment implements BaseMediaListView, IndexPagingListener, RecyclerClickListener {

    public static final String EXTRA_PROVIDER = "butter.droid.ui.media.list.base.BaseMediaListFragment.extra_provider";
    public static final String EXTRA_FILTER = "butter.droid.ui.media.list.base.BaseMediaListFragment.extra_filter";

    public static final String DIALOG_LOADING_DETAIL = "DIALOG_LOADING_DETAIL";

    public static final int LOADING_DIALOG_FRAGMENT = 1;

    @Inject BaseMediaListPresenter presenter;

    private Context context;
    private MediaGridAdapter adapter;
    private GridLayoutManager layoutManager;
    private Integer columns = 2;
    protected PagingManager<Media> pagingManager;

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
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final MediaGridSpacingItemDecoration gridSpacingDecoration = new MediaGridSpacingItemDecoration(context, columns);
        final int itemHeight = gridSpacingDecoration.getItemHeight();
        final int itemWidth = gridSpacingDecoration.getItemWidth();

        adapter = new MediaGridAdapter(itemHeight, itemWidth);

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
        Filter filter = Parcels.unwrap(arguments.getParcelable(EXTRA_FILTER));

        presenter.onActivityCreated(providerId, filter);
    }

    @Override public void loadPage(int index, int pageSize) {
        presenter.loadNextPage(index);
    }

    @Override public void onItemClick(View view, final int position) {
        final Media media = adapter.getItem(position);

        RecyclerView.ViewHolder holder = recyclerView.getChildViewHolder(view);
        if (holder instanceof MediaGridAdapter.ViewHolder) {
            ImageView coverImage = ((MediaGridAdapter.ViewHolder) holder).getCoverImage();

            if (coverImage.getDrawable() == null) {
                showLoadingDialog(position);
                return;
            }

            Bitmap cover = ((BitmapDrawable) coverImage.getDrawable()).getBitmap();
            Palette.from(cover)
                    .maximumColorCount(5)
                    .generate(palette -> {
                        int vibrantColor = palette.getVibrantColor(Color.TRANSPARENT);
                        int paletteColor;
                        if (vibrantColor == Color.TRANSPARENT) {
                            paletteColor = palette.getMutedColor(ContextCompat.getColor(getContext(), R.color.primary));
                        } else {
                            paletteColor = vibrantColor;
                        }
                        // TODO: 6/17/17
//                            media.color = paletteColor;
                        showLoadingDialog(position);
                    });
        } else {
            showLoadingDialog(position);
        }
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

    @Override public void addItems(List<Media> items) {
        pagingManager.addItems(items);
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

    private void showLoadingDialog(Integer position) {
        LoadingDetailDialogFragment loadingFragment = LoadingDetailDialogFragment.newInstance(position);
        loadingFragment.setTargetFragment(BaseMediaListFragment.this, LOADING_DIALOG_FRAGMENT);
        loadingFragment.show(getFragmentManager(), DIALOG_LOADING_DETAIL);
    }

    // TODO: 6/17/17
//    /**
//     * Called when loading media details fails
//     */
//    @Override
//    public void onDetailLoadFailure() {
//        Snackbar.make(rootView, R.string.unknown_error, Snackbar.LENGTH_SHORT).show();
//    }
//
//    /**
//     * Called when media details have been loaded. This should be called on a background thread.
//     *
//     * @param item
//     */
//    @Override
//    public void onDetailLoadSuccess(final Media item) {
//        startActivity(MediaDetailActivity.getIntent(getActivity(), item));
//    }
//
//    /**
//     * Called when loading media details
//     * @return mItems
//     */
//    @Override
//    public ArrayList<Media> getCurrentList() {
//        return presenter.getCurrentList();
//    }
}
