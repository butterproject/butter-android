/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.popcorn.tv.fragments;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v17.leanback.app.BackgroundManager;
import android.support.v17.leanback.app.BrowseFragment;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.HeaderItem;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.ListRowPresenter;
import android.util.Log;
import android.widget.Toast;

import com.popcorn.tv.R;
import com.popcorn.tv.presenters.CardPresenter;
import com.popcorn.tv.presenters.MorePresenter;

import java.net.URI;
import java.util.ArrayList;
import java.util.Timer;

import hugo.weaving.DebugLog;
import pct.droid.base.providers.media.EZTVProvider;
import pct.droid.base.providers.media.MediaProvider;
import pct.droid.base.providers.media.YTSProvider;
import pct.droid.base.providers.media.types.Media;

/*
 * Main class to show BrowseFragment with header and rows of videos
 */
public class OverviewFragment extends BrowseFragment {
	private static final String TAG = "MainFragment";

	private static int BACKGROUND_UPDATE_DELAY = 300;
	private Drawable mDefaultBackground;
	private Timer mBackgroundTimer;
	private URI mBackgroundURI;
	private BackgroundManager mBackgroundManager;

	private ArrayObjectAdapter mRowsAdapter;
	private ListRowPresenter mListRowPresenter;
	private ArrayObjectAdapter mShowsRowAdapter;

	private YTSProvider mMoviesProvider = new YTSProvider();
	private EZTVProvider mShowsProvider = new EZTVProvider();
	private ArrayObjectAdapter mMoviesRowAdapter;


	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		//		loadVideoData();

		prepareBackgroundManager();
		setupUIElements();

		//		setupEventListeners();
		setupAdapters();

		loadData();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (null != mBackgroundTimer) {
			Log.d(TAG, "onDestroy: " + mBackgroundTimer.toString());
			mBackgroundTimer.cancel();
		}
	}

	private void prepareBackgroundManager() {
		mBackgroundManager = BackgroundManager.getInstance(getActivity());
		mBackgroundManager.attach(getActivity().getWindow());
		mDefaultBackground = getResources().getDrawable(R.drawable.default_background);
		//		mMetrics = new DisplayMetrics();
		//		getActivity().getWindowManager().getDefaultDisplay().getMetrics(mMetrics);
	}

	private void setupUIElements() {
		setBadgeDrawable(getActivity().getResources().getDrawable(R.drawable.header_logo));
		setTitle(getString(R.string.app_name)); // Badge, when set, takes precedent over title
		setHeadersState(HEADERS_ENABLED);
		setHeadersTransitionOnBackEnabled(true);

		// set fastLane (or headers) background color
		setBrandColor(getResources().getColor(R.color.primary));
		// set search icon color
		setSearchAffordanceColor(getResources().getColor(R.color.primary_dark));
	}

	private void loadData() {
		mShowsProvider.getList(null, null, new MediaProvider.Callback() {
			@DebugLog
			@Override public void onSuccess(ArrayList<Media> items) {
				mShowsRowAdapter.clear();
				mShowsRowAdapter.addAll(0, items);
			}

			@DebugLog
			@Override public void onFailure(Exception e) {
				e.printStackTrace();
				Toast.makeText(getActivity(), "error getting show list", Toast.LENGTH_SHORT).show();

			}
		});

		mMoviesProvider.getList(null, null, new MediaProvider.Callback() {
			@DebugLog
			@Override public void onSuccess(ArrayList<Media> items) {
				mMoviesRowAdapter.clear();
				mMoviesRowAdapter.addAll(0, items);
			}

			@DebugLog
			@Override public void onFailure(Exception e) {
				e.printStackTrace();
				Toast.makeText(getActivity(), "error getting movie list", Toast.LENGTH_SHORT).show();

			}
		});
	}

	//	private void setupEventListeners() {
	//		setOnSearchClickedListener(new View.OnClickListener() {
	//
	//			@Override
	//			public void onClick(View view) {
	//				Intent intent = new Intent(getActivity(), SearchActivity.class);
	//				startActivity(intent);
	//			}
	//		});
	//
	//		setOnItemViewClickedListener(new ItemViewClickedListener());
	//		setOnItemViewSelectedListener(new ItemViewSelectedListener());
	//	}


	private void setupAdapters() {
		mListRowPresenter = new ListRowPresenter();
		mListRowPresenter.setShadowEnabled(false);
		mRowsAdapter = new ArrayObjectAdapter(mListRowPresenter);

		setupMovies();
		setupShows();
		setupMore();
		setAdapter(mRowsAdapter);
	}


	private void setupMovies() {
		HeaderItem moviesHeader = new HeaderItem(0, getString(R.string.top_movies),
				null);
		CardPresenter cardPresenter = new CardPresenter();
		mMoviesRowAdapter = new ArrayObjectAdapter(cardPresenter);
		mRowsAdapter.add(new ListRow(moviesHeader, mMoviesRowAdapter));
	}

	private void setupShows() {
		HeaderItem showsHeader = new HeaderItem(0, getString(R.string.top_shows),
				null);
		CardPresenter cardPresenter = new CardPresenter();
		mShowsRowAdapter = new ArrayObjectAdapter(cardPresenter);
		mRowsAdapter.add(new ListRow(showsHeader, mShowsRowAdapter));
	}

	private void setupMore() {
		HeaderItem gridHeader = new HeaderItem(0, getString(R.string.more),
				null);

		MorePresenter gridPresenter = new MorePresenter(getActivity());
		ArrayObjectAdapter gridRowAdapter = new ArrayObjectAdapter(gridPresenter);
		gridRowAdapter.add(new MorePresenter.MoreItem(R.string.about, R.drawable.ic_about));
		gridRowAdapter.add(new MorePresenter.MoreItem(R.string.settings, R.drawable.ic_settings));
		mRowsAdapter.add(new ListRow(gridHeader, gridRowAdapter));
	}
	//
	//	@Override
	//	public void onLoadFinished(Loader<HashMap<String, List<Movie>>> arg0,
	//			HashMap<String, List<Movie>> data) {
	//
	//		CardPresenter cardPresenter = new CardPresenter();
	//
	//		int i = 0;
	//
	//		for (Map.Entry<String, List<Movie>> entry : data.entrySet()) {
	//			ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(cardPresenter);
	//			List<Movie> list = entry.getValue();
	//
	//			for (int j = 0; j < list.size(); j++) {
	//				listRowAdapter.add(list.get(j));
	//			}
	//			HeaderItem header = new HeaderItem(i, entry.getKey(), null);
	//			i++;
	//			mRowsAdapter.add(new ListRow(header, listRowAdapter));
	//		}
	//
	//		HeaderItem gridHeader = new HeaderItem(i, getString(R.string.more_samples),
	//				null);
	//
	//
	//		updateRecommendations();
	//	}

	//	@Override
	//	public void onLoaderReset(Loader<HashMap<String, List<Movie>>> arg0) {
	//		mRowsAdapter.clear();
	//	}

	protected void setDefaultBackground(Drawable background) {
		mDefaultBackground = background;
	}

	protected void setDefaultBackground(int resourceId) {
		mDefaultBackground = getResources().getDrawable(resourceId);
	}
	//
	//	protected void updateBackground(String uri) {
	//		int width = mMetrics.widthPixels;
	//		int height = mMetrics.heightPixels;
	//		Glide.with(getActivity())
	//				.load(uri)
	//				.centerCrop()
	//				.error(mDefaultBackground)
	//				.into(new SimpleTarget<GlideDrawable>(width, height) {
	//					@Override
	//					public void onResourceReady(GlideDrawable resource,
	//							GlideAnimation<? super GlideDrawable>
	//									glideAnimation) {
	//						mBackgroundManager.setDrawable(resource);
	//					}
	//				});
	//		mBackgroundTimer.cancel();
	//	}

	//	protected void updateBackground(Drawable drawable) {
	//		BackgroundManager.getInstance(getActivity()).setDrawable(drawable);
	//	}
	//
	//	protected void clearBackground() {
	//		BackgroundManager.getInstance(getActivity()).setDrawable(mDefaultBackground);
	//	}

	//	private void startBackgroundTimer() {
	//		if (null != mBackgroundTimer) {
	//			mBackgroundTimer.cancel();
	//		}
	//		mBackgroundTimer = new Timer();
	//		mBackgroundTimer.schedule(new UpdateBackgroundTask(), BACKGROUND_UPDATE_DELAY);
	//	}


	//	private class UpdateBackgroundTask extends TimerTask {
	//
	//		@Override
	//		public void run() {
	//			mHandler.post(new Runnable() {
	//				@Override
	//				public void run() {
	//					if (mBackgroundURI != null) {
	//						updateBackground(mBackgroundURI.toString());
	//					}
	//				}
	//			});
	//		}
	//	}
	//
	//	private final class ItemViewClickedListener implements OnItemViewClickedListener {
	//		@Override
	//		public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item,
	//				RowPresenter.ViewHolder rowViewHolder, Row row) {
	//
	//			if (item instanceof Movie) {
	//				Movie movie = (Movie) item;
	//				Log.d(TAG, "Item: " + item.toString());
	//				Intent intent = new Intent(getActivity(), MovieDetailsActivity.class);
	//				intent.putExtra(MovieDetailsActivity.MOVIE, movie);
	//
	//				Bundle bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(
	//						getActivity(),
	//						((ImageCardView) itemViewHolder.view).getMainImageView(),
	//						MovieDetailsActivity.SHARED_ELEMENT_NAME).toBundle();
	//				getActivity().startActivity(intent, bundle);
	//			} else if (item instanceof String) {
	//				if (((String) item).indexOf(getString(R.string.grid_view)) >= 0) {
	//					Intent intent = new Intent(getActivity(), VerticalGridActivity.class);
	//					startActivity(intent);
	//				} else if (((String) item).indexOf(getString(R.string.error_fragment)) >= 0) {
	//					Intent intent = new Intent(getActivity(), BrowseErrorActivity.class);
	//					startActivity(intent);
	//				} else {
	//					Toast.makeText(getActivity(), ((String) item), Toast.LENGTH_SHORT)
	//							.show();
	//				}
	//			}
	//		}
	//	}

	//
	//	private final class ItemViewSelectedListener implements OnItemViewSelectedListener {
	//		@Override
	//		public void onItemSelected(Presenter.ViewHolder itemViewHolder, Object item,
	//				RowPresenter.ViewHolder rowViewHolder, Row row) {
	//			if (item instanceof Movie) {
	//				mBackgroundURI = ((Movie) item).getBackgroundImageURI();
	//				startBackgroundTimer();
	//			}
	//
	//		}
	//	}
}
