package com.popcorn.tv.fragments;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v17.leanback.app.BackgroundManager;
import android.support.v17.leanback.app.DetailsFragment;
import android.support.v17.leanback.widget.OnItemClickedListener;
import android.support.v17.leanback.widget.Row;
import android.util.DisplayMetrics;

import com.popcorn.tv.R;
import com.popcorn.tv.activities.DetailsActivity;
import com.popcorn.tv.utils.PicassoBackgroundManagerTarget;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.net.URI;

import hugo.weaving.DebugLog;
import pct.droid.base.providers.media.types.Media;
import timber.log.Timber;

public class VideoDetailsFragment extends DetailsFragment {
	private static final int ACTION_WATCH_TRAILER = 1;
	private static final int ACTION_RENT = 2;
	private static final int ACTION_BUY = 3;
	private static final int DETAIL_THUMB_WIDTH = 274;
	private static final int DETAIL_THUMB_HEIGHT = 274;
	private static final int NUM_COLS = 10;
	private Drawable mDefaultBackground;
	private Target mBackgroundTarget;
	private DisplayMetrics mMetrics;
	private Callback mCallback;

	@Override
	@DebugLog
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		BackgroundManager backgroundManager = BackgroundManager.getInstance(getActivity());
		backgroundManager.attach(getActivity().getWindow());
		mBackgroundTarget = new PicassoBackgroundManagerTarget(backgroundManager);
		mDefaultBackground = getResources().getDrawable(R.drawable.default_background);
		mMetrics = new DisplayMetrics();
		getActivity().getWindowManager().getDefaultDisplay().getMetrics(mMetrics);
		//		new DetailRowBuilderTask().execute(selectedMedia);

		setOnItemClickedListener(getDefaultItemClickedListener());
		updateBackground(URI.create(mCallback.getItem().headerImage));
	}

	@Override public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (activity instanceof Callback) mCallback = (Callback) activity;
	}

	//	private class DetailRowBuilderTask extends AsyncTask<Media, Integer, DetailsOverviewRow> {
	//		@Override
	//		protected DetailsOverviewRow doInBackground(Media... movies) {
	//			selectedMedia = movies[0];
	//
	//			DetailsOverviewRow row = new DetailsOverviewRow(selectedMedia);
	//			try {
	//				Bitmap poster = Picasso.with(getActivity())
	//						.load(selectedMedia.image)
	//						.resize(PixelUtils.getPixelsFromDp(getActivity().getApplicationContext(), DETAIL_THUMB_WIDTH),
	//								PixelUtils.getPixelsFromDp(getActivity().getApplicationContext(), DETAIL_THUMB_HEIGHT))
	//						.centerCrop()
	//						.get();
	//				row.setImageBitmap(getActivity(), poster);
	//			} catch (IOException e) {
	//			}
	//
	//			row.addAction(new Action(ACTION_WATCH_TRAILER, getResources().getString(
	//					R.string.watch_trailer_1), getResources().getString(R.string.watch_trailer_2)));
	//			row.addAction(new Action(ACTION_RENT, getResources().getString(R.string.rent_1),
	//					getResources().getString(R.string.rent_2)));
	//			row.addAction(new Action(ACTION_BUY, getResources().getString(R.string.buy_1),
	//					getResources().getString(R.string.buy_2)));
	//			return row;
	//		}
	//
	//		@Override
	//		protected void onPostExecute(DetailsOverviewRow detailRow) {
	//			ClassPresenterSelector ps = new ClassPresenterSelector();
	//			DetailsOverviewRowPresenter dorPresenter =
	//					new DetailsOverviewRowPresenter(new DetailsDescriptionPresenter());
	//			// set detail background and style
	//			dorPresenter.setBackgroundColor(getResources().getColor(R.color.detail_background));
	//			dorPresenter.setStyleLarge(true);
	//			dorPresenter.setOnActionClickedListener(new OnActionClickedListener() {
	//				@Override
	//				public void onActionClicked(Action action) {
	//					if (action.getId() == ACTION_WATCH_TRAILER) {
	//						Intent intent = new Intent(getActivity(), PlayerActivity.class);
	//						intent.putExtra(getResources().getString(R.string.movie), selectedMedia);
	//						intent.putExtra(getResources().getString(R.string.should_start), true);
	//						startActivity(intent);
	//					} else {
	//						Toast.makeText(getActivity(), action.toString(), Toast.LENGTH_SHORT).show();
	//					}
	//				}
	//			});
	//
	//			ps.addClassPresenter(DetailsOverviewRow.class, dorPresenter);
	//			ps.addClassPresenter(ListRow.class,
	//					new ListRowPresenter());
	//
	//			ArrayObjectAdapter adapter = new ArrayObjectAdapter(ps);
	//			adapter.add(detailRow);
	//
	//			String subcategories[] = {
	//					getString(R.string.related_movies)
	//			};
	//			//            List<Media> list = MovieList.list;
	//			//            Collections.shuffle(list);
	//			//            ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(new MediaRowPresenter());
	//			//            for (int j = 0; j < NUM_COLS; j++) {
	//			//                listRowAdapter.add(list.get(j % 5));
	//			//            }
	//			//
	//			//            HeaderItem header = new HeaderItem(0, subcategories[0], null);
	//			//            adapter.add(new ListRow(header, listRowAdapter));
	//
	//			setAdapter(adapter);
	//		}
	//
	//	}

	protected OnItemClickedListener getDefaultItemClickedListener() {
		return new OnItemClickedListener() {
			@Override
			public void onItemClicked(Object item, Row row) {
				if (item instanceof Media) {
					Media movie = (Media) item;
					DetailsActivity.startActivity(getActivity(), movie);
				}
			}
		};
	}

	protected void updateBackground(URI uri) {
		Timber.d("uri" + uri);
		Timber.d("metrics" + mMetrics.toString());
		Picasso.with(getActivity())
				.load(uri.toString())
				.resize(mMetrics.widthPixels, mMetrics.heightPixels)
				.error(mDefaultBackground)
				.into(mBackgroundTarget);
	}

	public interface Callback {
		Media getItem();
	}


}
