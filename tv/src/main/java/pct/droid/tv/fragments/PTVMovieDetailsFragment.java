package pct.droid.tv.fragments;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v17.leanback.app.DetailsFragment;
import android.support.v17.leanback.widget.Action;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.ClassPresenterSelector;
import android.support.v17.leanback.widget.DetailsOverviewRow;
import android.support.v17.leanback.widget.DetailsOverviewRowPresenter;
import android.support.v17.leanback.widget.OnActionClickedListener;
import android.support.v17.leanback.widget.OnItemViewClickedListener;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.RowPresenter;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;

import pct.droid.base.preferences.Prefs;
import pct.droid.base.providers.media.EZTVProvider;
import pct.droid.base.providers.media.MediaProvider;
import pct.droid.base.providers.media.YTSProvider;
import pct.droid.base.providers.media.types.Media;
import pct.droid.base.providers.media.types.Movie;
import pct.droid.base.providers.media.types.Show;
import pct.droid.base.utils.NetworkUtils;
import pct.droid.base.utils.PrefUtils;
import pct.droid.base.utils.ThreadUtils;
import pct.droid.tv.R;
import pct.droid.tv.activities.PTVMovieDetailActivity;
import pct.droid.tv.presenters.MovieDetailsDescriptionPresenter;
import pct.droid.tv.utils.BackgroundUpdater;

public class PTVMovieDetailsFragment extends DetailsFragment implements MediaProvider.Callback {

	private Callback mCallback;

	private static final int ACTION_WATCH = 1;

	private ArrayObjectAdapter mAdapter;
	private ClassPresenterSelector mPresenterSelector;
	BackgroundUpdater mBackgroundUpdater = new BackgroundUpdater();

	EZTVProvider mTvProvider = new EZTVProvider();
	YTSProvider mMovieProvider = new YTSProvider();

	private ItemWrapper mItem;
	private CustomDetailsOverviewRow mDetailsRow;

	@Override public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		mItem = new ItemWrapper(mCallback.getItem());
		mBackgroundUpdater.initialise(getActivity(), R.drawable.default_background);

		//		prepareBackgroundManager();
		//
		//		mSelectedMovie = (Movie) getActivity().getIntent()
		//				.getSerializableExtra(MovieDetailsActivity.MOVIE);
		//
		//		if (null != mSelectedMovie || checkGlobalSearchIntent()) {
		//			Log.d(TAG, "DetailsActivity movie: " + mSelectedMovie.toString());
		//			removeNotification(getActivity().getIntent()
		//					.getIntExtra(MovieDetailsActivity.NOTIFICATION_ID, NO_NOTIFICATION));
		setupAdapter();
		addDetailsOverviewRow();
		setupDetailsOverviewRowPresenter();

		//			setupMovieListRow();
		//			setupMovieListRowPresenter();

		//			updateBackground(mSelectedMovie.getBackgroundImageUrl());
		//			setOnItemViewClickedListener(new ItemViewClickedListener());
		//		} else {
		//			Intent intent = new Intent(getActivity(), MainActivity.class);
		//			startActivity(intent);
		//		}
		//


		setOnItemViewClickedListener(getDefaultItemClickedListener());
		updateBackground();
		getDetails();
	}

	@Override public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (activity instanceof Callback) mCallback = (Callback) activity;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (null != mBackgroundUpdater) mBackgroundUpdater.destroy();
	}


	private void setupAdapter() {
		mPresenterSelector = new ClassPresenterSelector();
		mAdapter = new ArrayObjectAdapter(mPresenterSelector);
		setAdapter(mAdapter);
	}

	private void getDetails() {
		if (mItem.getMedia() instanceof Movie) {
			mItem.setLoadingDetail(true);
			mMovieProvider.getDetail(mItem.getMedia().videoId, this);

		} else if (mItem.getMedia() instanceof Show) {
			mItem.setLoadingDetail(true);
			mTvProvider.getDetail(mItem.getMedia().videoId, this);
		}
	}

	private void setupDetailsOverviewRowPresenter() {
		// Set detail background and style.
		DetailsOverviewRowPresenter detailsPresenter =
				new DetailsOverviewRowPresenter(new MovieDetailsDescriptionPresenter());

		detailsPresenter.setStyleLarge(true);

		// Hook up transition element.
		detailsPresenter.setSharedElementEnterTransition(getActivity(),
				PTVMovieDetailActivity.SHARED_ELEMENT_NAME);

		detailsPresenter.setOnActionClickedListener(new OnActionClickedListener() {
			@Override
			public void onActionClicked(Action action) {


//				final String streamUrl = mItem.torrents.get(mQuality).url;
//				if (PrefUtils.get(MovieDetailActivity.this, Prefs.WIFI_ONLY, true) && !NetworkUtils
//						.isWifiConnected(MovieDetailActivity.this) &&
//						NetworkUtils
//								.isNetworkConnected(MovieDetailActivity.this)) {
//					MessageDialogFragment.show(getFragmentManager(), R.string.wifi_only, R.string.wifi_only_message);
//				} else {
//					Intent streamIntent = new Intent(MovieDetailActivity.this, StreamLoadingActivity.class);
//					streamIntent.putExtra(StreamLoadingActivity.STREAM_URL, streamUrl);
//					streamIntent.putExtra(StreamLoadingActivity.QUALITY, mQuality);
//					streamIntent.putExtra(StreamLoadingActivity.DATA, mItem);
//					if (mSubLanguage != null)
//						streamIntent.putExtra(StreamLoadingActivity.SUBTITLES, mSubLanguage);
//					startActivity(streamIntent);
//				}



//				if (action.getId() == ACTION_WATCH_TRAILER) {
//					Intent intent = new Intent(getActivity(), PlaybackOverlayActivity.class);
//					intent.putExtra(MovieDetailsActivity.MOVIE, mSelectedMovie);
//					startActivity(intent);
//				} else {
//					Toast.makeText(getActivity(), action.toString(), Toast.LENGTH_SHORT).show();
//				}
			}
		});
		mPresenterSelector.addClassPresenter(DetailsOverviewRow.class, detailsPresenter);
	}

	private void addDetailsOverviewRow() {

		mDetailsRow = new CustomDetailsOverviewRow(mItem);
		mDetailsRow.setImageDrawable(getResources().getDrawable(R.drawable.default_background));

		Picasso.with(getActivity()).load(mItem.getMedia().image).into(new Target() {
			@Override public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
				mDetailsRow.setImageBitmap(getActivity(), bitmap);
				mAdapter.notifyArrayItemRangeChanged(0, mAdapter.size());
			}

			@Override public void onBitmapFailed(Drawable errorDrawable) {

			}

			@Override public void onPrepareLoad(Drawable placeHolderDrawable) {

			}
		});

		mDetailsRow.addAction(new Action(ACTION_WATCH, getResources().getString(
				R.string.watch), getResources().getString(R.string.quality_720)));
		mDetailsRow.addAction(new Action(ACTION_WATCH, getResources().getString(
				R.string.watch), getResources().getString(R.string.quality_1080)));

		mAdapter.add(mDetailsRow);
	}


	protected OnItemViewClickedListener getDefaultItemClickedListener() {
		return new OnItemViewClickedListener() {
			@Override public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item, RowPresenter.ViewHolder rowViewHolder,
					Row row) {
				Media movie = (Media) item;
			}
		};
	}

	protected void updateBackground() {
		Media item = mItem.getMedia();
		if (null == item) return;
		mBackgroundUpdater.updateBackgroundAsync(item.headerImage);
	}

	@Override public void onSuccess(ArrayList<Media> items) {
		if (null != mItem) mItem.setLoadingDetail(false);
		if (null == items || items.size() == 0) return;
		Media itemDetail = items.get(0);
		mItem.setMedia(itemDetail);

		ThreadUtils.runOnUiThread(new Runnable() {
			@Override public void run() {
				mAdapter.notifyArrayItemRangeChanged(0, mAdapter.size());
			}
		});
	}

	@Override public void onFailure(Exception e) {
		if (null != mItem) mItem.setLoadingDetail(false);

	}

	public interface Callback {
		Media getItem();
	}


	private static class CustomDetailsOverviewRow extends DetailsOverviewRow {

		//		Field mItemField = null;

		private CustomDetailsOverviewRow(Object item) {
			super(item);
			//			try {
			//				mItemField = getClass().getSuperclass().getDeclaredField("mItem");
			//				mItemField.setAccessible(true);
			//			} catch (NoSuchFieldException e) {
			//				e.printStackTrace();
			//			}
		}
		//
		//		public void setItem(Media item) {
		//			try {
		//				//				mItemField.set
		//				mItemField.set(this, item);
		//			} catch (IllegalAccessException e) {
		//				e.printStackTrace();
		//			}
		//		}
		//

	}

	public static class ItemWrapper {
		private Media mItem;
		private boolean mLoadingDetail;

		public ItemWrapper(Media item) {
			mItem = item;
		}

		public Media getMedia() {
			return mItem;
		}

		public void setMedia(Media item) {
			mItem = item;
		}

		public boolean isLoadingDetail() {
			return mLoadingDetail;
		}

		public void setLoadingDetail(boolean loadingDetail) {
			mLoadingDetail = loadingDetail;
		}
	}

}
