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

package butter.droid.tv.ui.media;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.leanback.app.VerticalGridSupportFragment;
import androidx.leanback.widget.ClassPresenterSelector;
import androidx.leanback.widget.ImageCardView;
import androidx.leanback.widget.OnItemViewClickedListener;
import androidx.leanback.widget.OnItemViewSelectedListener;
import androidx.leanback.widget.Presenter;
import androidx.leanback.widget.Row;
import androidx.leanback.widget.RowPresenter;
import androidx.leanback.widget.VerticalGridPresenter;
import androidx.core.app.ActivityOptionsCompat;
import android.widget.Toast;

import java.util.List;

import javax.inject.Inject;

import butter.droid.base.manager.internal.paging.CursorPagingListener;
import butter.droid.base.providers.media.model.MediaWrapper;
import butter.droid.base.utils.StringUtils;
import butter.droid.provider.base.filter.Filter;
import butter.droid.tv.R;
import butter.droid.tv.manager.internal.background.BackgroundUpdater;
import butter.droid.tv.manager.internal.paging.GridPagingAdapter;
import butter.droid.tv.manager.internal.paging.GridPagingManager;
import butter.droid.tv.presenters.LoadingCardPresenter;
import butter.droid.tv.presenters.LoadingCardPresenter.LoadingCardItem;
import butter.droid.tv.presenters.MediaCardPresenter;
import butter.droid.tv.presenters.MediaCardPresenter.MediaCardItem;
import butter.droid.tv.ui.detail.TVMediaDetailActivity;
import dagger.android.support.AndroidSupportInjection;

/*
 * VerticalGridFragment shows a grid of videos
 */
public class TVMediaGridFragment extends VerticalGridSupportFragment implements TVMediaGridView, OnItemViewClickedListener,
        OnItemViewSelectedListener, CursorPagingListener {

    private static final String ARG_TITLE = "butter.droid.tv.ui.media.TVMediaGridFragment.title";
    private static final String ARG_FILTER = "butter.droid.tv.ui.media.TVMediaGridFragment.filter";
    private static final String ARG_PROVIDER = "butter.droid.tv.ui.media.TVMediaGridFragment.provider";

    private static final int NUM_COLUMNS = 6;

    @Inject TVMediaGridPresenter presenter;
    @Inject BackgroundUpdater backgroundUpdater;

    private GridPagingAdapter adapter;
    private GridPagingManager<MediaCardItem> pagingManager;

    @Override public void onAttach(final Context context) {
        AndroidSupportInjection.inject(this);
        super.onAttach(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        backgroundUpdater.initialise(getActivity(), R.color.black);
        setupUi();

        Bundle args = getArguments();

        int providerId = args.getInt(ARG_PROVIDER);
        Filter filter = args.getParcelable(ARG_FILTER);
        presenter.onCreate(providerId, filter);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setTitle(StringUtils.capWords(getString(getArguments().getInt(ARG_TITLE))));

        presenter.onActivityCreated();
    }

    @Override
    public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item, RowPresenter.ViewHolder rowViewHolder, Row row) {
        if (item instanceof MediaCardPresenter.MediaCardItem) {
            onMediaItemClicked((ImageCardView) itemViewHolder.view, (MediaCardPresenter.MediaCardItem) item);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        presenter.onDestroy();
        backgroundUpdater.destroy();
    }

    @Override public void appendItems(final List<MediaCardItem> list, final boolean isFinished, final String endCursor) {
        pagingManager.addItems(list, isFinished, endCursor);
//        adapter.addAll(previousSize, list);
//        adapter.notifyArrayItemRangeChanged(previousSize, list.size());
    }

    @Override public void displayError(final String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onItemSelected(Presenter.ViewHolder itemViewHolder, Object item, RowPresenter.ViewHolder rowViewHolder, Row row) {
        if (item instanceof MediaCardPresenter.MediaCardItem) {
            MediaCardPresenter.MediaCardItem overviewItem = (MediaCardPresenter.MediaCardItem) item;
            backgroundUpdater.updateBackgroundAsync(overviewItem.getMediaWrapper().getMedia().getBackdrop());
        }

        //really hacky way of making and 'endless' adapter

        //trigger items to update
        pagingManager.onItemSelected(item);
    }

    private void onMediaItemClicked(ImageCardView view, MediaCardPresenter.MediaCardItem item) {
        Bundle options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                getActivity(),
                view.getMainImageView(),
                TVMediaDetailActivity.SHARED_ELEMENT_NAME).toBundle();

        MediaWrapper media = item.getMediaWrapper();
        startActivity(TVMediaDetailActivity.getIntent(getActivity(), media), options);
    }

    private void setupUi() {

        VerticalGridPresenter gridPresenter = new VerticalGridPresenter();
        gridPresenter.setNumberOfColumns(NUM_COLUMNS);
        setGridPresenter(gridPresenter);

        Context context = requireContext();
        ClassPresenterSelector presenterSelector = new ClassPresenterSelector();
        presenterSelector.addClassPresenter(MediaCardItem.class, new MediaCardPresenter(context));
        presenterSelector.addClassPresenter(LoadingCardItem.class, new LoadingCardPresenter(context));

        adapter = new GridPagingAdapter(presenterSelector);
        setAdapter(adapter);

        pagingManager = new GridPagingManager<>();
        pagingManager.init(NUM_COLUMNS, adapter, this);

        setOnItemViewClickedListener(this);
        setOnItemViewSelectedListener(this);
    }

    @Override public void loadPage(@Nullable final String endCursor) {
        presenter.loadNextPage(endCursor);
    }

    public static TVMediaGridFragment newInstance(final int provider, @StringRes int title, Filter filter) {
        final Bundle args = new Bundle();
        args.putInt(ARG_TITLE, title);
        args.putParcelable(ARG_FILTER, filter);
        args.putInt(ARG_PROVIDER, provider);

        TVMediaGridFragment fragment = new TVMediaGridFragment();
        fragment.setArguments(args);
        return fragment;
    }

}
