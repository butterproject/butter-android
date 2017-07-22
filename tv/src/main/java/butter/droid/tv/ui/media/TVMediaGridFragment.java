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

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v17.leanback.app.VerticalGridFragment;
import android.support.v17.leanback.widget.ClassPresenterSelector;
import android.support.v17.leanback.widget.ImageCardView;
import android.support.v17.leanback.widget.OnItemViewClickedListener;
import android.support.v17.leanback.widget.OnItemViewSelectedListener;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.RowPresenter;
import android.support.v17.leanback.widget.VerticalGridPresenter;
import android.support.v4.app.ActivityOptionsCompat;
import android.widget.Toast;
import butter.droid.base.manager.internal.paging.CursorPagingListener;
import butter.droid.base.utils.StringUtils;
import butter.droid.provider.base.filter.Filter;
import butter.droid.provider.base.module.Media;
import butter.droid.provider.base.module.Movie;
import butter.droid.provider.base.module.Show;
import butter.droid.tv.R;
import butter.droid.tv.TVButterApplication;
import butter.droid.tv.manager.internal.background.BackgroundUpdater;
import butter.droid.tv.manager.internal.background.BackgroundUpdaterModule;
import butter.droid.tv.manager.internal.paging.GridPagingAdapter;
import butter.droid.tv.manager.internal.paging.GridPagingManager;
import butter.droid.tv.presenters.LoadingCardPresenter;
import butter.droid.tv.presenters.LoadingCardPresenter.LoadingCardItem;
import butter.droid.tv.presenters.MediaCardPresenter;
import butter.droid.tv.presenters.MediaCardPresenter.MediaCardItem;
import butter.droid.tv.ui.detail.TVMediaDetailActivity;
import com.squareup.picasso.Picasso;
import java.util.List;
import javax.inject.Inject;
import org.parceler.Parcels;

/*
 * VerticalGridFragment shows a grid of videos
 */
public class TVMediaGridFragment extends VerticalGridFragment implements TVMediaGridView, OnItemViewClickedListener,
        OnItemViewSelectedListener, CursorPagingListener {

    private static final String ARG_TITLE = "butter.droid.tv.ui.media.TVMediaGridFragment.title";
    private static final String ARG_FILTER = "butter.droid.tv.ui.media.TVMediaGridFragment.filter";
    private static final String ARG_PROVIDER = "butter.droid.tv.ui.media.TVMediaGridFragment.provider";

    private static final int NUM_COLUMNS = 6;

    @Inject TVMediaGridPresenter presenter;
    @Inject BackgroundUpdater backgroundUpdater;
    @Inject Picasso picasso;

    private GridPagingAdapter adapter;
    private GridPagingManager pagingManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TVButterApplication.getAppContext()
                .getComponent()
                .tvMediaGridComponentBuilder()
                .mediaGridModule(new TVMediaGridModule(this))
                .backgroundUpdaterModule(new BackgroundUpdaterModule(getActivity()))
                .build()
                .inject(this);

        backgroundUpdater.initialise(getActivity(), R.color.black);
        setupUi();

        Bundle args = getArguments();

        int providerId = args.getInt(ARG_PROVIDER);
        Filter filter = Parcels.unwrap(args.getParcelable(ARG_FILTER));
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
            backgroundUpdater.updateBackgroundAsync(overviewItem.getMedia().getBackdrop());
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

        Media media = item.getMedia();
        if (media instanceof Movie || media instanceof Show) {
            startActivity(TVMediaDetailActivity.getIntent(getActivity(), item.getProviderId(), media), options);
        }
    }

    private void setupUi() {

        VerticalGridPresenter gridPresenter = new VerticalGridPresenter();
        gridPresenter.setNumberOfColumns(NUM_COLUMNS);
        setGridPresenter(gridPresenter);

        ClassPresenterSelector presenterSelector = new ClassPresenterSelector();
        presenterSelector.addClassPresenter(MediaCardItem.class, new MediaCardPresenter(getActivity(), picasso));
        presenterSelector.addClassPresenter(LoadingCardItem.class, new LoadingCardPresenter(getActivity()));

        adapter = new GridPagingAdapter(presenterSelector);
        setAdapter(adapter);

        pagingManager = new GridPagingManager();
        pagingManager.init(NUM_COLUMNS, adapter, this);

        setOnItemViewClickedListener(this);
        setOnItemViewSelectedListener(this);
    }

    public static TVMediaGridFragment newInstance(final int provider, @StringRes int title, Filter filter) {
        final Bundle args = new Bundle();
        args.putInt(ARG_TITLE, title);
        args.putParcelable(ARG_FILTER, Parcels.wrap(filter));
        args.putInt(ARG_PROVIDER, provider);

        TVMediaGridFragment fragment = new TVMediaGridFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override public void loadPage(@Nullable final String endCursor) {
        presenter.loadNextPage(endCursor);
    }
}
