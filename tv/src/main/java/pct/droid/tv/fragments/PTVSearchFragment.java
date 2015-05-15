package pct.droid.tv.fragments;

import android.os.Bundle;
import android.os.Handler;
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
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

import hugo.weaving.DebugLog;
import pct.droid.base.providers.media.EZTVProvider;
import pct.droid.base.providers.media.MediaProvider;
import pct.droid.base.providers.media.YTSProvider;
import pct.droid.base.providers.media.models.Media;
import pct.droid.tv.R;
import pct.droid.tv.activities.PTVMediaDetailActivity;
import pct.droid.tv.presenters.MediaCardPresenter;
import pct.droid.tv.utils.BackgroundUpdater;

public class PTVSearchFragment extends android.support.v17.leanback.app.SearchFragment
		implements android.support.v17.leanback.app.SearchFragment.SearchResultProvider {
	private static final int SEARCH_DELAY_MS = 300;

	private EZTVProvider mShowsProvider = new EZTVProvider();
	private YTSProvider mMovieProvider = new YTSProvider();
	private MediaProvider.Filters mSearchFilter = new MediaProvider.Filters();

	private ArrayObjectAdapter mRowsAdapter;
	private Handler mHandler = new Handler();
	private SearchRunnable mDelayedLoad;
	private ListRowPresenter mListRowPresenter;
	private ListRow mLoadingRow;
	private BackgroundUpdater mBackgroundUpdater = new BackgroundUpdater();

	@Override public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mBackgroundUpdater.initialise(getActivity(), R.color.black);
		mListRowPresenter = new ListRowPresenter();
		mListRowPresenter.setShadowEnabled(false);
		mRowsAdapter = new ArrayObjectAdapter(mListRowPresenter);
		setSearchResultProvider(this);
		setOnItemViewClickedListener(getDefaultItemClickedListener());
		setOnItemViewSelectedListener(new ItemViewSelectedListener());
		mDelayedLoad = new SearchRunnable();

		//setup row to use for loading
		mLoadingRow = createLoadingRow();
	}

	@Override
	public ObjectAdapter getResultsAdapter() {
		return mRowsAdapter;
	}

	private ListRow createLoadingRow() {
		HeaderItem loadingHeader = new HeaderItem(0, getString(R.string.search_results));
		ArrayObjectAdapter loadingRowAdapter = new ArrayObjectAdapter(new MediaCardPresenter(getActivity()));
		loadingRowAdapter.add(new MediaCardPresenter.MediaCardItem(true));
		return new ListRow(loadingHeader, loadingRowAdapter);
	}

	private void queryByWords(String words) {
		mRowsAdapter.clear();
		if (!TextUtils.isEmpty(words)) {
			mDelayedLoad.setSearchQuery(words);
			mHandler.removeCallbacks(mDelayedLoad);
			mHandler.postDelayed(mDelayedLoad, SEARCH_DELAY_MS);
		}
	}

	@Override
	public boolean onQueryTextChange(String newQuery) {
		if (newQuery.length() > 3)
			queryByWords(newQuery);
		return true;
	}

	@Override
	public boolean onQueryTextSubmit(String query) {
		queryByWords(query);
		return true;
	}

	@DebugLog
	private void loadRows(String query) {
		mMovieProvider.cancel();
		mShowsProvider.cancel();
		mRowsAdapter.clear();
		addLoadingRow();

		mSearchFilter.keywords = query;
		mSearchFilter.page = 1;
		mShowsProvider.getList(mSearchFilter, new MediaProvider.Callback() {
			@Override public void onSuccess(MediaProvider.Filters filters, ArrayList<Media> items, boolean changed) {
				List<MediaCardPresenter.MediaCardItem> list = MediaCardPresenter.convertMediaToOverview(items);
				addRow(getString(R.string.show_results), list);
			}

			@Override public void onFailure(Exception e) {

			}
		});

		mMovieProvider.getList(mSearchFilter, new MediaProvider.Callback() {
			@Override public void onSuccess(MediaProvider.Filters filters, ArrayList<Media> items, boolean changed) {
						List<MediaCardPresenter.MediaCardItem> list = MediaCardPresenter.convertMediaToOverview(items);
						addRow(getString(R.string.movie_results), list);
					}

					@Override public void onFailure(Exception e) {

					}
				}

		);

	}

	private void addRow(String title, List<MediaCardPresenter.MediaCardItem> items) {
		mRowsAdapter.remove(mLoadingRow);

		HeaderItem header = new HeaderItem(0, title);
		ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(new MediaCardPresenter(getActivity()));
		listRowAdapter.addAll(0, items);
		mRowsAdapter.add(new ListRow(header, listRowAdapter));
	}

	private void addLoadingRow() {
		mRowsAdapter.add(mLoadingRow);
	}

	protected OnItemViewClickedListener getDefaultItemClickedListener() {
		return new OnItemViewClickedListener() {
			@Override public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object object, RowPresenter.ViewHolder rowViewHolder,
					Row row) {
				if (object instanceof MediaCardPresenter.MediaCardItem) {
					MediaCardPresenter.MediaCardItem item = (MediaCardPresenter.MediaCardItem) object;
					Media media = item.getMedia();
					PTVMediaDetailActivity.startActivity(getActivity(), media,media.headerImage,media.image);
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

				mBackgroundUpdater.updateBackgroundAsync(overviewItem.getMedia().headerImage);
			}
		}
	}

	private class SearchRunnable implements Runnable {

		private volatile String searchQuery;

		public SearchRunnable() {
		}

		public void run() {
			loadRows(searchQuery);
		}

		public void setSearchQuery(String value) {
			this.searchQuery = value;
		}
	}
}
