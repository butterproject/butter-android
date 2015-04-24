package pct.droid.tv.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v17.leanback.app.DetailsFragment;
import android.support.v17.leanback.widget.AbstractDetailsDescriptionPresenter;
import android.support.v17.leanback.widget.Action;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.ClassPresenterSelector;
import android.support.v17.leanback.widget.DetailsOverviewRow;
import android.support.v17.leanback.widget.DetailsOverviewRowPresenter;
import android.support.v17.leanback.widget.HeaderItem;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.ListRowPresenter;
import android.support.v17.leanback.widget.OnActionClickedListener;
import android.support.v17.leanback.widget.OnItemViewClickedListener;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.RowPresenter;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import pct.droid.base.providers.media.EZTVProvider;
import pct.droid.base.providers.media.MediaProvider;
import pct.droid.base.providers.media.models.Episode;
import pct.droid.base.providers.media.models.Media;
import pct.droid.base.providers.media.models.Show;
import pct.droid.base.torrent.StreamInfo;
import pct.droid.base.utils.NetworkUtils;
import pct.droid.base.utils.ThreadUtils;
import pct.droid.tv.R;
import pct.droid.tv.activities.PTVMediaDetailActivity;
import pct.droid.tv.activities.PTVStreamLoadingActivity;
import pct.droid.tv.presenters.EpisodeCardPresenter;
import pct.droid.tv.presenters.ShowDetailsDescriptionPresenter;
import pct.droid.tv.utils.BackgroundUpdater;

public abstract class PTVBaseDetailsFragment extends DetailsFragment implements MediaProvider.Callback, OnActionClickedListener {

	public static final String EXTRA_ITEM = "item";
	public static final String EXTRA_HERO_URL = "hero_url";

	private Callback mCallback;
	private ArrayObjectAdapter mAdapter;
	private ClassPresenterSelector mPresenterSelector;
	private Media mItem;
	private String mHeroImage;

	@Override public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		mItem = getArguments().getParcelable(EXTRA_ITEM);
		mHeroImage = getArguments().getString(EXTRA_HERO_URL);

		setupAdapter();

		setupDetailsOverviewRowPresenter();

		final DetailsOverviewRow detailRow = createDetailsOverviewRow();
		mAdapter.add(detailRow);

		loadDetails();
	}

	abstract void loadDetails();

	abstract AbstractDetailsDescriptionPresenter getDetailPresenter();

	abstract void onDetailLoaded();

	protected ArrayObjectAdapter getObjectArrayAdapter() {
		return mAdapter;
	}

	public Media getMediaItem() {
		return mItem;
	}

	@Override public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (activity instanceof Callback) mCallback = (Callback) activity;
	}


	@Override
	public void onDestroy() {
		super.onDestroy();
	}


	private void setupAdapter() {
		mPresenterSelector = new ClassPresenterSelector();
		mAdapter = new ArrayObjectAdapter(mPresenterSelector);
		setAdapter(mAdapter);
	}


	private void setupDetailsOverviewRowPresenter() {
		// Set detail background and style.
		DetailsOverviewRowPresenter headerPresenter =
				new DetailsOverviewRowPresenter(getDetailPresenter());
		headerPresenter.setStyleLarge(true);
		headerPresenter.setOnActionClickedListener(this);

		// Hook up transition element.
		headerPresenter.setSharedElementEnterTransition(getActivity(),
						PTVMediaDetailActivity.SHARED_ELEMENT_NAME);

		mPresenterSelector.addClassPresenter(DetailsOverviewRow.class, headerPresenter);
		mPresenterSelector.addClassPresenter(ListRow.class, new ListRowPresenter());
	}

	private DetailsOverviewRow createDetailsOverviewRow() {
		final DetailsOverviewRow detailsRow = new DetailsOverviewRow(mItem);

		Picasso.with(getActivity()).load(mHeroImage).into(new Target() {
			@Override
			public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
				detailsRow.setImageBitmap(getActivity(), bitmap);
				mAdapter.notifyArrayItemRangeChanged(0, mAdapter.size());

			}

			@Override
			public void onBitmapFailed(Drawable errorDrawable) {
				getActivity().startPostponedEnterTransition();
			}

			@Override
			public void onPrepareLoad(Drawable placeHolderDrawable) {

			}
		});

		return detailsRow;
	}

	protected void addAction(Action action) {
		DetailsOverviewRow detailRow = (DetailsOverviewRow) mAdapter.get(0);
		detailRow.addAction(action);
	}

	abstract void addActions(Media item);

	@Override public void onSuccess(MediaProvider.Filters filters, ArrayList<Media> items, boolean changed) {
		if (!isAdded()) return;

		if (null == items || items.size() == 0) return;

		Media itemDetail = items.get(0);

		mItem = itemDetail;

		ThreadUtils.runOnUiThread(new Runnable() {
			@Override public void run() {

				final DetailsOverviewRow detailRow = createDetailsOverviewRow();
				mAdapter.replace(0, detailRow);
				onDetailLoaded();
			}
		});
	}


	@Override public void onFailure(Exception e) {
		//todo: on load failure
	}

	public interface Callback {
	}

}
