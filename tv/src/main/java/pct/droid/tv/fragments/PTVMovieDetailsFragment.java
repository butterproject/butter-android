package pct.droid.tv.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v17.leanback.widget.AbstractDetailsDescriptionPresenter;
import android.support.v17.leanback.widget.Action;
import android.support.v17.leanback.widget.ClassPresenterSelector;
import android.support.v17.leanback.widget.DetailsOverviewRow;
import android.support.v17.leanback.widget.OnActionClickedListener;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import pct.droid.base.providers.media.MediaProvider;
import pct.droid.base.providers.media.YTSProvider;
import pct.droid.base.providers.media.models.Media;
import pct.droid.base.providers.media.models.Movie;
import pct.droid.base.torrent.StreamInfo;
import pct.droid.base.utils.NetworkUtils;
import pct.droid.tv.R;
import pct.droid.tv.activities.PTVStreamLoadingActivity;
import pct.droid.tv.presenters.MovieDetailsDescriptionPresenter;

public class PTVMovieDetailsFragment extends PTVBaseDetailsFragment implements MediaProvider.Callback, OnActionClickedListener {

	YTSProvider mMovieProvider = new YTSProvider();

	public static Fragment newInstance(Media media, String hero) {
		PTVMovieDetailsFragment fragment = new PTVMovieDetailsFragment();

		Bundle bundle = new Bundle();
		bundle.putParcelable(EXTRA_ITEM, media);
		bundle.putString(EXTRA_HERO_URL, hero);

		fragment.setArguments(bundle);
		return fragment;
	}


	private Movie getMovieItem() {
		return (Movie) getMediaItem();
	}

	@Override void loadDetails() {
		mMovieProvider.getDetail(getMovieItem().videoId, this);
	}

	@Override AbstractDetailsDescriptionPresenter getDetailPresenter() {
		return new MovieDetailsDescriptionPresenter();
	}

	@Override void onDetailLoaded() {
		addActions(getMovieItem());
	}

	@Override void addActions(Media item) {
		if (item instanceof Movie) {
			Movie movie = (Movie) item;

			List<String> qualities = new ArrayList(movie.torrents.keySet());

			for (String quality : qualities) {

				Media.Torrent torrent = movie.torrents.get(quality);

				//add action
				addAction(new WatchAction((long) qualities.indexOf(quality), getResources().getString(
						R.string.watch), quality, torrent));
			}
		}
	}

	@Override
	ClassPresenterSelector createPresenters(ClassPresenterSelector selector) {
		return null;
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
					new StreamInfo(getMovieItem(), torrent.url, "no-subs",
							action.getLabel2().toString());

			PTVStreamLoadingActivity.startActivity(getActivity(), info);
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
