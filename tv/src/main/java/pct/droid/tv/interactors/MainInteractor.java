package pct.droid.tv.interactors;

import android.content.Context;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.HeaderItem;
import android.util.Log;

import pct.droid.tv.R;
import pct.droid.tv.adapters.MediaObjectAdapter;
import pct.droid.tv.interfaces.main.MainDataManagerCallback;
import pct.droid.tv.interfaces.main.MainDataManagerInputInterface;
import pct.droid.tv.interfaces.main.MainInteractorInputInterface;
import pct.droid.tv.interfaces.main.MainInteractorOutputInterface;
import pct.droid.tv.presenters.BasicRowPresenter;
import pct.droid.tv.presenters.MediaRowPresenter;
import pct.droid.tv.utils.Capitalize;
import pct.droid.tv.utils.MediaListRow;

import java.util.ArrayList;
import java.util.List;

import pct.droid.base.providers.media.types.Media;

public class MainInteractor implements MainInteractorInputInterface {
	//region Attributes
	private static String TAG = "MainInteractorInputInterface";
	private MainInteractorOutputInterface presenter;
	private MainDataManagerInputInterface ytsDataManager;
	private List<ArrayObjectAdapter> adapters;
	private List<HeaderItem> headers;
	private List<Integer> activeRowsUpdates = new ArrayList<>();
	//endregion

	//region Constructors
	public MainInteractor(MainInteractorOutputInterface presenter) {
		this.presenter = presenter;
//		this.ytsDataManager = new YTSDataManager();
	}
	//endregion

	//region MainInteractorInputInterface
	@Override
	public int getNumberOfSections(Context context) {
		return context.getResources().getStringArray(R.array.categories).length;
	}

	@Override
	public HeaderItem getSectionHeaderAtIndex(int index, Context context) {

		return getHeaders(context).get(index);
	}

	@Override
	public ArrayObjectAdapter getSectionAdapterAtIndex(int index, Context context) {
		return getAdapters(context).get(index);
	}

	@Override
	public int getRightItemsNextTo(Object item, MediaListRow row) {
		ArrayObjectAdapter adapter = adapters.get(row.getRowIndex());
		if (adapter instanceof MediaObjectAdapter) return ((MediaObjectAdapter) adapter).getCount();
		return -1;
	}

	@Override
	public void getMore(MediaListRow row, Context context) {
		getMore(row.getRowIndex(), context);
	}

	private void getMore(int index, Context context) {
		ArrayObjectAdapter adapter = adapters.get(index);
		if (!(adapter instanceof MediaObjectAdapter)) { return; }
		final MediaObjectAdapter mediaAdapter = ((MediaObjectAdapter) adapter);
		if (mediaAdapter.getIsUpdating()) { return; }
		mediaAdapter.setIsUpdating(true);

		final String genre = context.getResources().getStringArray(R.array.categories)[index];
		int lastPage = mediaAdapter.getLastPage();
		ytsDataManager.getList(genre, lastPage + 1, new MainDataManagerCallback() {
			@Override
			public void onSuccess(ArrayList<Media> items) {
				mediaAdapter.setIsUpdating(false);
				mediaAdapter.addAll(mediaAdapter.getCount(), items);
			}

			@Override
			public void onFailure(Exception e) {
				mediaAdapter.setIsUpdating(false);
				Log.e(TAG, "Error getting more media items of genre: " + genre);
			}
		});
	}

	@Override
	public void synchronize(Context context) {
		for (int i = 0; i < getAdapters(context).size(); i++) {
			getMore(i, context);
		}
	}

	//endregion

	//region Custom Getters
	private List<ArrayObjectAdapter> getAdapters(Context context) {
		if (adapters == null) {
			adapters = new ArrayList<>();
			addRowAdapters(context);
		}
		return adapters;
	}

	private List<HeaderItem> getHeaders(Context context) {
		if (headers == null) {
			headers = new ArrayList<>();
			addRowHeaders(context);
		}
		return headers;
	}
	//endregion

	//region Helpers
	private void addRowHeaders(Context context) {
		for (int i = 0; i < context.getResources().getStringArray(R.array.categories).length; i++) {
			addRowMediaHeader(context, i);
		}
		addRowSettingsHeader(context);
	}

	private void addRowSettingsHeader(Context context) {
		headers.add(
				new HeaderItem(context.getResources().getStringArray(R.array.categories).length, Capitalize.capitalize("settings"), null));
	}

	private void addRowMediaHeader(Context context, int index) {
		headers.add(new HeaderItem(index, Capitalize.capitalize(context.getResources().getStringArray(R.array.categories)[index]), null));
	}

	private void addRowAdapters(Context context) {
		MediaRowPresenter mediaRowPresenter = new MediaRowPresenter();
		for (int i = 0; i < context.getResources().getStringArray(R.array.categories).length; i++) {
			addRowMediaAdapter(context, i, mediaRowPresenter);
		}
		addRowSettingsAdapter(context);
	}

	private void addRowMediaAdapter(Context context, int index, MediaRowPresenter mediaRowPresenter) {
		MediaObjectAdapter listRowAdapter = new MediaObjectAdapter(mediaRowPresenter);
		adapters.add(listRowAdapter);
	}

	private void addRowSettingsAdapter(Context context) {
		BasicRowPresenter mGridPresenter = new BasicRowPresenter();
		ArrayObjectAdapter gridRowAdapter = new ArrayObjectAdapter(mGridPresenter);
		gridRowAdapter.add(context.getResources().getString(R.string.grid_view));
		gridRowAdapter.add(context.getResources().getString(R.string.send_feeback));
		gridRowAdapter.add(context.getResources().getString(R.string.personal_settings));
		adapters.add(gridRowAdapter);
	}
	//end
}
