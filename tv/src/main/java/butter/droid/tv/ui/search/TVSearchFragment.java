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

import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.HeaderItem;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.ListRowPresenter;
import android.support.v17.leanback.widget.ObjectAdapter;
import android.support.v17.leanback.widget.OnItemViewClickedListener;
import android.support.v17.leanback.widget.OnItemViewSelectedListener;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.RowPresenter;

import java.util.List;

import javax.inject.Inject;

import butter.droid.base.providers.media.models.Media;
import butter.droid.tv.R;
import butter.droid.tv.TVButterApplication;
import butter.droid.tv.activities.TVMediaDetailActivity;
import butter.droid.tv.presenters.MediaCardPresenter;
import butter.droid.tv.presenters.MediaCardPresenter.MediaCardItem;
import butter.droid.tv.utils.BackgroundUpdater;

public class TVSearchFragment extends android.support.v17.leanback.app.SearchFragment
		implements android.support.v17.leanback.app.SearchFragment.SearchResultProvider, TVSearchView {

	@Inject TVSearchPresenter presenter;

	private final BackgroundUpdater backgroundUpdater = new BackgroundUpdater();

	private ArrayObjectAdapter rowsAdapter;
	private ListRowPresenter mListRowPresenter;
	private ListRow mLoadingRow;

	@Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TVButterApplication.getAppContext()
                .getComponent()
                .searchComponentBuilder()
                .searchModule(new TVSearchModue(this))
                .build()
                .inject(this);

    }

	@Override public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		backgroundUpdater.initialise(getActivity(), R.color.black);
		mListRowPresenter = new ListRowPresenter();
		mListRowPresenter.setShadowEnabled(false);
		rowsAdapter = new ArrayObjectAdapter(mListRowPresenter);
		setSearchResultProvider(this);
		setOnItemViewClickedListener(getDefaultItemClickedListener());
		setOnItemViewSelectedListener(new ItemViewSelectedListener());

		//setup row to use for loading
		mLoadingRow = createLoadingRow();
	}

	@Override
	public ObjectAdapter getResultsAdapter() {
		return rowsAdapter;
	}

	private ListRow createLoadingRow() {
		HeaderItem loadingHeader = new HeaderItem(0, getString(R.string.search_results));
		ArrayObjectAdapter loadingRowAdapter = new ArrayObjectAdapter(new MediaCardPresenter(getActivity()));
		loadingRowAdapter.add(new MediaCardPresenter.MediaCardItem(true));
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
		rowsAdapter.add(mLoadingRow);
	}

	@Override public void addRow(@StringRes int title, List<MediaCardItem> items) {
		rowsAdapter.remove(mLoadingRow);

		HeaderItem header = new HeaderItem(0, getString(title));
		ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(new MediaCardPresenter(getActivity()));
		listRowAdapter.addAll(0, items);
		rowsAdapter.add(new ListRow(header, listRowAdapter));
	}

	protected OnItemViewClickedListener getDefaultItemClickedListener() {
		return new OnItemViewClickedListener() {
			@Override public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object object, RowPresenter.ViewHolder rowViewHolder,
					Row row) {
				if (object instanceof MediaCardPresenter.MediaCardItem) {
					MediaCardPresenter.MediaCardItem item = (MediaCardPresenter.MediaCardItem) object;
					Media media = item.getMedia();
					TVMediaDetailActivity.startActivity(getActivity(), media);
				}
			}
		};
	}

	private final class ItemViewSelectedListener implements OnItemViewSelectedListener {
		@Override
		public void onItemSelected(Presenter.ViewHolder itemViewHolder, Object item,
				RowPresenter.ViewHolder rowViewHolder, Row row) {
			if (item instanceof MediaCardPresenter.MediaCardItem) {
				MediaCardPresenter.MediaCardItem overviewItem = (MediaCardPresenter.MediaCardItem) item;
				if (overviewItem.isLoading()) return;

				backgroundUpdater.updateBackgroundAsync(overviewItem.getMedia().headerImage);
			}
		}
	}
}
