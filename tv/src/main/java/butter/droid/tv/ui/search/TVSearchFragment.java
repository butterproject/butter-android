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

package butter.droid.tv.ui.search;

import android.content.Context;
import android.os.Bundle;

import java.util.List;

import javax.inject.Inject;

import androidx.annotation.StringRes;
import androidx.leanback.app.SearchSupportFragment;
import androidx.leanback.app.SearchSupportFragment.SearchResultProvider;
import androidx.leanback.widget.ArrayObjectAdapter;
import androidx.leanback.widget.ClassPresenterSelector;
import androidx.leanback.widget.HeaderItem;
import androidx.leanback.widget.ListRow;
import androidx.leanback.widget.ListRowPresenter;
import androidx.leanback.widget.ObjectAdapter;
import androidx.leanback.widget.OnItemViewClickedListener;
import androidx.leanback.widget.OnItemViewSelectedListener;
import androidx.leanback.widget.Presenter;
import androidx.leanback.widget.Row;
import androidx.leanback.widget.RowPresenter;
import butter.droid.base.providers.media.model.MediaWrapper;
import butter.droid.tv.R;
import butter.droid.tv.manager.internal.background.BackgroundUpdater;
import butter.droid.tv.presenters.LoadingCardPresenter;
import butter.droid.tv.presenters.LoadingCardPresenter.LoadingCardItem;
import butter.droid.tv.presenters.MediaCardPresenter;
import butter.droid.tv.presenters.MediaCardPresenter.MediaCardItem;
import butter.droid.tv.ui.detail.TVMediaDetailActivity;
import dagger.android.support.AndroidSupportInjection;

public class TVSearchFragment extends SearchSupportFragment implements SearchResultProvider, TVSearchView {

    @Inject TVSearchPresenter presenter;
    @Inject BackgroundUpdater backgroundUpdater;

    private ArrayObjectAdapter rowsAdapter;
    private ListRowPresenter listRowPresenter;
    private ListRow loadingRow;

    @Override public void onAttach(final Context context) {
        AndroidSupportInjection.inject(this);
        super.onAttach(context);
    }

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        presenter.onCreate();
    }

    @Override public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        backgroundUpdater.initialise(getActivity(), R.color.black);
        listRowPresenter = new ListRowPresenter();
        listRowPresenter.setShadowEnabled(false);
        rowsAdapter = new ArrayObjectAdapter(listRowPresenter);
        setSearchResultProvider(this);
        setOnItemViewClickedListener(getDefaultItemClickedListener());
        setOnItemViewSelectedListener(new ItemViewSelectedListener());

        //setup row to use for loading
        loadingRow = createLoadingRow();
    }

    @Override
    public ObjectAdapter getResultsAdapter() {
        return rowsAdapter;
    }

    private ListRow createLoadingRow() {
        final HeaderItem loadingHeader = new HeaderItem(getString(R.string.search_results));

        ClassPresenterSelector presenterSelector = new ClassPresenterSelector();
        presenterSelector.addClassPresenter(MediaCardItem.class, new MediaCardPresenter(getActivity()));
        presenterSelector.addClassPresenter(LoadingCardItem.class, new LoadingCardPresenter(getActivity()));

        ArrayObjectAdapter loadingRowAdapter = new ArrayObjectAdapter(presenterSelector);
        loadingRowAdapter.add(new LoadingCardItem());
        return new ListRow(loadingHeader, loadingRowAdapter);
    }

    @Override
    public boolean onQueryTextChange(String newQuery) {
        presenter.onTextChanged(newQuery);
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        presenter.onTextSubmitted(query);
        return true;
    }

    @Override public void clearData() {
        rowsAdapter.clear();
    }

    @Override public void showLoadingRow() {
        rowsAdapter.add(loadingRow);
    }

    @Override public void replaceRow(int index, @StringRes int title, List<MediaCardItem> items) {
        rowsAdapter.remove(loadingRow);

        HeaderItem header = new HeaderItem(getString(title));
        ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(new MediaCardPresenter(getActivity()));
        listRowAdapter.addAll(0, items);
        ListRow row = new ListRow(header, listRowAdapter);
        if (rowsAdapter.size() > index) {
            rowsAdapter.replace(index, row);
        } else {
            rowsAdapter.add(index, row);
        }
    }

    protected OnItemViewClickedListener getDefaultItemClickedListener() {
        return (itemViewHolder, object, rowViewHolder, row) -> {
            if (object instanceof MediaCardItem) {
                MediaCardItem item = (MediaCardItem) object;
                MediaWrapper media = item.getMediaWrapper();
                startActivity(TVMediaDetailActivity.getIntent(getActivity(), media));
            }
        };
    }

    private final class ItemViewSelectedListener implements OnItemViewSelectedListener {

        @Override
        public void onItemSelected(Presenter.ViewHolder itemViewHolder, Object item,
                RowPresenter.ViewHolder rowViewHolder, Row row) {
            if (item instanceof MediaCardPresenter.MediaCardItem) {
                MediaCardPresenter.MediaCardItem overviewItem = (MediaCardPresenter.MediaCardItem) item;
                backgroundUpdater.updateBackgroundAsync(overviewItem.getMediaWrapper().getMedia().getBackdrop());
            }
        }
    }
}
