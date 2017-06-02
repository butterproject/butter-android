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
import android.support.annotation.StringRes;
import android.support.v17.leanback.app.VerticalGridFragment;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
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
import butter.droid.base.providers.media.MediaProvider;
import butter.droid.base.utils.StringUtils;
import butter.droid.provider.base.Media;
import butter.droid.provider.base.Movie;
import butter.droid.provider.base.Show;
import butter.droid.tv.R;
import butter.droid.tv.TVButterApplication;
import butter.droid.tv.manager.internal.background.BackgroundUpdater;
import butter.droid.tv.manager.internal.background.BackgroundUpdaterModule;
import butter.droid.tv.presenters.LoadingCardPresenter;
import butter.droid.tv.presenters.LoadingCardPresenter.LoadingCardItem;
import butter.droid.tv.presenters.MediaCardPresenter;
import butter.droid.tv.presenters.MediaCardPresenter.MediaCardItem;
import butter.droid.tv.ui.detail.TVMediaDetailActivity;
import com.squareup.picasso.Picasso;
import java.util.List;
import javax.inject.Inject;

/*
 * VerticalGridFragment shows a grid of videos
 */
public class TVMediaGridFragment extends VerticalGridFragment implements TVMediaGridView, OnItemViewClickedListener,
        OnItemViewSelectedListener {

    private static final String ARG_TITLE = "butter.droid.tv.ui.media.TVMediaGridFragment.title";
    private static final String ARG_SORT = "butter.droid.tv.ui.media.TVMediaGridFragment.sort";
    private static final String ARG_ORDER = "butter.droid.tv.ui.media.TVMediaGridFragment.order";
    private static final String ARG_GENRE = "butter.droid.tv.ui.media.TVMediaGridFragment.genre";

    private static final int NUM_COLUMNS = 6;

    @Inject TVMediaGridPresenter presenter;
    @Inject BackgroundUpdater backgroundUpdater;
    @Inject Picasso picasso;

    private ArrayObjectAdapter adapter;

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

        MediaProvider.Filters filter = new MediaProvider.Filters();
        filter.sort = (MediaProvider.Filters.Sort) args.getSerializable(ARG_SORT);
        filter.order = (MediaProvider.Filters.Order) args.getSerializable(ARG_ORDER);
        filter.genre = args.getString(ARG_GENRE);

        presenter.onCreate(filter);
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

    @Override public void appendItems(final List<MediaCardItem> list) {
        int previousSize = adapter.size();
        adapter.addAll(previousSize, list);
        adapter.notifyArrayItemRangeChanged(previousSize, list.size());
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
        int itemPosition = adapter.indexOf(item);

        //when we are within 3 rows of the end, load more items
        if (itemPosition > getAdapter().size() - (NUM_COLUMNS * 3)) {
            presenter.loadNextPage();
        }
    }

    private void onMediaItemClicked(ImageCardView view, MediaCardPresenter.MediaCardItem item) {
        Bundle options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                getActivity(),
                view.getMainImageView(),
                TVMediaDetailActivity.SHARED_ELEMENT_NAME).toBundle();

        Media media = item.getMedia();
        if (media instanceof Movie || media instanceof Show) {
            startActivity(TVMediaDetailActivity.getIntent(getActivity(), media), options);
        }
    }


    private void setupUi() {

        VerticalGridPresenter gridPresenter = new VerticalGridPresenter();
        gridPresenter.setNumberOfColumns(NUM_COLUMNS);
        setGridPresenter(gridPresenter);

        ClassPresenterSelector presenterSelector = new ClassPresenterSelector();
        presenterSelector.addClassPresenter(MediaCardItem.class, new MediaCardPresenter(getActivity(), picasso));
        presenterSelector.addClassPresenter(LoadingCardItem.class, new LoadingCardPresenter(getActivity()));

        adapter = new ArrayObjectAdapter(presenterSelector);
        setAdapter(adapter);

        setOnItemViewClickedListener(this);
        setOnItemViewSelectedListener(this);
    }

    public static TVMediaGridFragment newInstance(@StringRes int title, MediaProvider.Filters.Sort sort,
            MediaProvider.Filters.Order defOrder, String genre) {
        final Bundle args = new Bundle();
        args.putSerializable(ARG_TITLE, title);
        args.putSerializable(ARG_SORT, sort);
        args.putSerializable(ARG_ORDER, defOrder);
        args.putString(ARG_GENRE, genre);

        TVMediaGridFragment fragment = new TVMediaGridFragment();
        fragment.setArguments(args);
        return fragment;
    }

}
