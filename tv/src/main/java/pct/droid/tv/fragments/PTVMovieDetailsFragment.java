package pct.droid.tv.fragments;

import android.app.Activity;
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
import java.util.List;

import pct.droid.base.fragments.BaseStreamLoadingFragment;
import pct.droid.base.providers.media.EZTVProvider;
import pct.droid.base.providers.media.MediaProvider;
import pct.droid.base.providers.media.YTSProvider;
import pct.droid.base.providers.media.models.Media;
import pct.droid.base.providers.media.models.Movie;
import pct.droid.base.providers.media.models.Show;
import pct.droid.base.torrent.StreamInfo;
import pct.droid.base.utils.NetworkUtils;
import pct.droid.base.utils.ThreadUtils;
import pct.droid.tv.R;
import pct.droid.tv.activities.PTVMovieDetailActivity;
import pct.droid.tv.activities.PTVStreamLoadingActivity;
import pct.droid.tv.presenters.MovieDetailsDescriptionPresenter;
import pct.droid.tv.utils.BackgroundUpdater;

public class PTVMovieDetailsFragment extends DetailsFragment implements MediaProvider.Callback, OnActionClickedListener {

	private Callback mCallback;

	private static final int ACTION_WATCH_720 = 1;
	private static final int ACTION_WATCH_1080 = 2;

	private ArrayObjectAdapter mAdapter;
	private ClassPresenterSelector mPresenterSelector;
	BackgroundUpdater mBackgroundUpdater = new BackgroundUpdater();

	EZTVProvider mTvProvider = new EZTVProvider();
	YTSProvider mMovieProvider = new YTSProvider();

	private ItemWrapper mItem;
	private DetailsOverviewRow mDetailsRow;

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
		//		addDetailsOverviewRow();
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


		//		setOnItemViewClickedListener(getDefaultItemClickedListener());
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

		detailsPresenter.setOnActionClickedListener(this);
		mPresenterSelector.addClassPresenter(DetailsOverviewRow.class, detailsPresenter);
	}

	private void addDetailsOverviewRow() {

		mDetailsRow = new DetailsOverviewRow(mItem);
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

		addActions(mItem.getMedia());

		mAdapter.add(mDetailsRow);
	}

	private void addActions(Media item) {
		if (item instanceof Movie) {
			Movie movie = (Movie) item;

			List<String> qualities = new ArrayList(movie.torrents.keySet());

			for (String quality : qualities) {

				Media.Torrent torrent = movie.torrents.get(quality);

				//add action
				mDetailsRow.addAction(new WatchAction((long) qualities.indexOf(quality), getResources().getString(
						R.string.watch), quality, torrent));
			}

		} else if (item instanceof Show) {
			//todo:
			//			Show show = (Show) item;
			//			show.

		}

	}


	protected OnItemViewClickedListener getDefaultItemClickedListener() {
		return new OnItemViewClickedListener() {
			@Override public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item, RowPresenter.ViewHolder rowViewHolder,
					Row row) {


			}
		};
	}

	protected void updateBackground() {
		Media item = mItem.getMedia();
		if (null == item) return;
		mBackgroundUpdater.updateBackgroundAsync(item.headerImage);
	}

	@Override public void onSuccess(MediaProvider.Filters filters, ArrayList<Media> items, boolean changed) {
		if (!isAdded()) return;
		if (null != mItem) mItem.setLoadingDetail(false);
		if (null == items || items.size() == 0) return;
		Media itemDetail = items.get(0);
		mItem.setMedia(itemDetail);

		ThreadUtils.runOnUiThread(new Runnable() {
			@Override public void run() {
				addDetailsOverviewRow();
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


	@Override public void onActionClicked(Action a) {
		if (!(a instanceof WatchAction)) return;

		//check for network
		if (!NetworkUtils
				.isNetworkConnected(getActivity())) {
			Toast.makeText(getActivity(), R.string.network_message, Toast.LENGTH_SHORT).show();
		} else {
			WatchAction action = (WatchAction) a;
			Media.Torrent torrent = action.getTorrent();

			StreamInfo info =
					new StreamInfo(mItem.getMedia(), torrent.url, "no-subs",
							action.getLabel2().toString());

			PTVStreamLoadingActivity.startActivity(getActivity(), info);
		}
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

	public static class WatchAction extends android.support.v17.leanback.widget.Action {

		private Media.Torrent mTorrent;

		public WatchAction(long id, CharSequence label, CharSequence label2, Media.Torrent torrent) {
			super(id, label, label2);
			this.mTorrent = torrent;
		}

		public Media.Torrent getTorrent() {
			return mTorrent;
		}

		public void setTorrent(Media.Torrent torrent) {
			mTorrent = torrent;
		}
	}

}
