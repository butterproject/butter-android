/*
 * This file is part of Popcorn Time.
 *
 * Popcorn Time is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Popcorn Time is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Popcorn Time. If not, see <http://www.gnu.org/licenses/>.
 */

package pct.droid.fragments;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;
import hugo.weaving.DebugLog;
import pct.droid.R;
import pct.droid.activities.MediaDetailActivity;
import pct.droid.adapters.MediaGridAdapter;
import pct.droid.base.PopcornApplication;
import pct.droid.base.providers.media.MediaProvider;
import pct.droid.base.providers.media.models.Media;
import pct.droid.base.utils.LogUtils;
import pct.droid.base.utils.ThreadUtils;
import pct.droid.dialogfragments.LoadingDetailDialogFragment;

/**
 * This fragment is the main screen for viewing a collection of media items.
 *
 * LOADING
 *
 * This fragment has 2 ways of representing a loading state; If the data is being loaded for the first time, or the media detail for the
 * detail screen is being loaded,a progress layout is displayed with a message.
 *
 * If a page is being loaded, the adapter will display a progress item.
 *
 * MODE
 *
 * This fragment can be instantiated with ether a SEARCH mode, or a NORMAL mode. SEARCH mode simply does not load any initial data.
 */
public class MediaListFragment extends Fragment implements MediaProvider.Callback, LoadingDetailDialogFragment.Callback {

	public static final String EXTRA_PROVIDER = "extra_provider";
	public static final String EXTRA_SORT = "extra_sort";
    public static final String EXTRA_ORDER = "extra_order";
	public static final String EXTRA_MODE = "extra_mode";
	public static final String DIALOG_LOADING_DETAIL = "DIALOG_LOADING_DETAIL";

	public static final int LOADING_DIALOG_FRAGMENT = 1;

    private Context mContext;
	private MediaGridAdapter mAdapter;
	private GridLayoutManager mLayoutManager;
	private Integer mColumns = 2, mRetries = 0;

	//overrides the default loading message
	private int mLoadingMessage = R.string.loading_data;

    private Boolean mIsAttached;
	private State mState = State.UNINITIALISED;
	private Mode mMode;
    private MediaProvider.Filters.Sort mSort;
    private MediaProvider.Filters.Order mDefOrder;

    public enum Mode {
		NORMAL, SEARCH
	}

	private enum State {
		UNINITIALISED, LOADING, SEARCHING, LOADING_PAGE, LOADED, LOADING_DETAIL
	}

	private ArrayList<Media> mItems = new ArrayList<>();

	private boolean mEndOfListReached = false;

	private int mFirstVisibleItem, mVisibleItemCount, mTotalItemCount = 0, mLoadingTreshold = mColumns * 3, mPreviousTotal = 0;

	private MediaProvider mProvider;
	private int mPage = 1;
	private MediaProvider.Filters mFilters = new MediaProvider.Filters();

	@InjectView(R.id.progressOverlay)
	LinearLayout progressOverlay;
	@InjectView(R.id.recyclerView)
	RecyclerView recyclerView;
	@InjectView(R.id.emptyView)
	TextView emptyView;
	@InjectView(R.id.progress_textview)
	TextView progressTextView;

	public static MediaListFragment newInstance(Mode mode, MediaProvider provider, MediaProvider.Filters.Sort filter, MediaProvider.Filters.Order defOrder) {
		MediaListFragment frag = new MediaListFragment();
		Bundle args = new Bundle();
		args.putParcelable(EXTRA_PROVIDER, provider);
		args.putSerializable(EXTRA_MODE, mode);
		args.putSerializable(EXTRA_SORT, filter);
        args.putSerializable(EXTRA_ORDER, defOrder);
		frag.setArguments(args);
		return frag;
	}

	@Override public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		setRetainInstance(true);
	}

    @Override public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mContext = getActivity();
		return inflater.inflate(R.layout.fragment_media, container, false);
	}

	@Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		ButterKnife.inject(this, view);

		mColumns = getResources().getInteger(R.integer.overview_cols);
		mLoadingTreshold = mColumns * 3;
		recyclerView.setHasFixedSize(true);
		mLayoutManager = new GridLayoutManager(mContext, mColumns);
		recyclerView.setLayoutManager(mLayoutManager);
		recyclerView.setOnScrollListener(mScrollListener);
		//adapter should only ever be created once on fragment initialise.
		mAdapter = new MediaGridAdapter(mContext, mItems, mColumns);
		mAdapter.setOnItemClickListener(mOnItemClickListener);
		recyclerView.setAdapter(mAdapter);
	}

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        mAdapter.setOnItemClickListener(mOnItemClickListener);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mIsAttached = true;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mIsAttached = false;
    }

    @Override public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		//get the provider type and create a provider
		mProvider = getArguments().getParcelable(EXTRA_PROVIDER);
        mSort = (MediaProvider.Filters.Sort) getArguments().getSerializable(EXTRA_SORT);
        mDefOrder = (MediaProvider.Filters.Order) getArguments().getSerializable(EXTRA_ORDER);
        mFilters.sort = mSort;
        // if not changed use default order
        mFilters.order = mDefOrder;

		mMode = (Mode) getArguments().getSerializable(EXTRA_MODE);
		if (mMode == Mode.SEARCH) emptyView.setText(getString(R.string.no_search_results));

		//don't load initial data in search mode
		if (mMode != Mode.SEARCH && mAdapter.getItemCount() == 0) {
			mProvider.getList(mFilters, this);/* fetch new items */
			setState(State.LOADING);
		} else updateUI();
	}

	@Override public void onPause() {
		super.onPause();
		mProvider.cancel();
	}

	/**
	 * Responsible for updating the UI based on the state of this fragment
	 */
	private void updateUI() {
		if (!isAdded()) return;

		//animate recyclerview to full alpha
		//		if (recyclerView.getAlpha() != 1.0f)
		//			recyclerView.animate().alpha(1.0f).setDuration(100).start();

		//update loading message based on state
		switch (mState) {
			case LOADING_DETAIL:
				mLoadingMessage = R.string.loading_details;
				break;
			case SEARCHING:
				mLoadingMessage = R.string.searching;
				break;
			default:
				int providerMessage = mProvider.getLoadingMessage();
				mLoadingMessage = providerMessage > 0 ? providerMessage : R.string.loading_data;
				break;
		}

		switch (mState) {
			case LOADING_DETAIL:
			case SEARCHING:
			case LOADING:
				if (mAdapter.isLoading()) mAdapter.removeLoading();
				//show the progress bar
				recyclerView.setVisibility(View.VISIBLE);
				//				recyclerView.animate().alpha(0.5f).setDuration(500).start();
				emptyView.setVisibility(View.GONE);
				progressOverlay.setVisibility(View.VISIBLE);
				break;
			case LOADED:
				if (mAdapter.isLoading()) mAdapter.removeLoading();
				progressOverlay.setVisibility(View.GONE);
				boolean hasItems = mItems.size() > 0;
				//show either the recyclerview or the empty view
				recyclerView.setVisibility(hasItems ? View.VISIBLE : View.INVISIBLE);
				emptyView.setVisibility(hasItems ? View.GONE : View.VISIBLE);
				break;
			case LOADING_PAGE:
				//add a loading view to the adapter
				if (!mAdapter.isLoading()) mAdapter.addLoading();
				emptyView.setVisibility(View.GONE);
				recyclerView.setVisibility(View.VISIBLE);
				break;
		}
		updateLoadingMessage();
	}

	private void updateLoadingMessage() {
		progressTextView.setText(mLoadingMessage);
	}

	@DebugLog
	private void setState(State state) {
		if (mState == state) return;//do nothing
		mState = state;
		updateUI();
	}

	public void triggerSearch(String searchQuery) {
		if (!isAdded()) return;
		if (null == mAdapter) return;

		mEndOfListReached = false;

		mItems.clear();
		mAdapter.clearItems();//clear out adapter

		if (searchQuery.equals("")) {
			setState(State.LOADED);
			return; //don't do a search for empty queries
		}

		setState(State.SEARCHING);
		mFilters.keywords = searchQuery;
		mFilters.page = 1;
		mPage = 1;
		mProvider.getList(mFilters, this);
	}


	@Override
	@DebugLog
	public void onSuccess(final ArrayList<Media> items) {
		mItems.clear();
		if (null != items) mItems.addAll(items);

		//fragment may be detached, so we dont want to update the UI
		if (!isAdded()) return;

		mEndOfListReached = false;

		mPage = mPage + 1;
		ThreadUtils.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mAdapter.setItems(items);
				mPreviousTotal = mTotalItemCount = mAdapter.getItemCount();
				setState(State.LOADED);
			}
		});
	}

	@Override
	@DebugLog
	public void onFailure(Exception e) {
        if(!mIsAttached) {
            ThreadUtils.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mAdapter == null) {
                        return;
                    }

                    mAdapter.removeLoading();
                    setState(State.LOADED);
                }
            });
        } else if (e.getMessage() != null && e.getMessage().equals(getString(R.string.movies_error))) {
			mEndOfListReached = true;
			ThreadUtils.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					if (mAdapter == null) {
						return;
					}

					mAdapter.removeLoading();
					setState(State.LOADED);
				}
			});
		} else {
			e.printStackTrace();
			LogUtils.e(e.getMessage());
			if (mRetries > 1) {
				ThreadUtils.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Toast.makeText(mContext, R.string.unknown_error, Toast.LENGTH_SHORT).show();
						setState(State.LOADED);
					}
				});
			} else {
				mProvider.getList(null, this);
			}
			mRetries++;
		}
	}


    private MediaGridAdapter.OnItemClickListener mOnItemClickListener = new MediaGridAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(final View view, final Media item, final int position) {
            /**
             * We shouldn't really be doing the palette loading here without any ui feedback,
             * but it should be really quick
             */
            RecyclerView.ViewHolder holder = recyclerView.getChildViewHolder(view);
            if (holder instanceof MediaGridAdapter.ViewHolder) {
                ImageView coverImage = ((MediaGridAdapter.ViewHolder) holder).getCoverImage();

                if (coverImage.getDrawable() == null) {
                    showLoadingDialog(item, -1);
                    return;
                }

                Bitmap cover = ((BitmapDrawable) coverImage.getDrawable()).getBitmap();
                Palette.generateAsync(cover, 5, new Palette.PaletteAsyncListener() {
                    @Override
                    public void onGenerated(Palette palette) {
                        int vibrantColor = palette.getVibrantColor(-1);
                        int paletteColor;
                        if (vibrantColor == -1) {
                            paletteColor = palette.getMutedColor(getResources().getColor(R.color.primary));
                        } else {
                            paletteColor = vibrantColor;
                        }
                        showLoadingDialog(item, paletteColor);
                    }
                });
            } else {
                showLoadingDialog(item, -1);
            }

        }
    };

    private void showLoadingDialog(Media item, int paletteColor) {
        LoadingDetailDialogFragment loadingFragment = LoadingDetailDialogFragment.newInstance(item, paletteColor);
        loadingFragment.setTargetFragment(MediaListFragment.this, LOADING_DIALOG_FRAGMENT);
        loadingFragment.show(getFragmentManager(), DIALOG_LOADING_DETAIL);
    }


	private RecyclerView.OnScrollListener mScrollListener = new RecyclerView.OnScrollListener() {
		@Override
		public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
			mVisibleItemCount = mLayoutManager.getChildCount();
			mTotalItemCount = mLayoutManager.getItemCount();
			mFirstVisibleItem = mLayoutManager.findFirstVisibleItemPosition();

			if (mState == State.LOADING_PAGE) {
				if (mTotalItemCount > mPreviousTotal) {
					mPreviousTotal = mTotalItemCount;
					mPreviousTotal = mTotalItemCount = mLayoutManager.getItemCount();
					setState(State.LOADED);
				}
			}

			if (!mEndOfListReached && !(mState == State.LOADING_PAGE) && (mTotalItemCount - mVisibleItemCount) <= (mFirstVisibleItem +
					mLoadingTreshold)) {
				MediaProvider.Filters filters = mFilters;
				filters.page = mPage;
				mProvider.getList(mItems, filters, MediaListFragment.this);

				mFilters = filters;

				mPreviousTotal = mTotalItemCount = mLayoutManager.getItemCount();
				setState(State.LOADING_PAGE);
			}
		}
	};


	/**
	 * Called when loading media details fails
	 */
	@Override
    public void onDetailLoadFailure() {
		Toast.makeText(mContext, R.string.unknown_error, Toast.LENGTH_SHORT).show();
	}

	/**
	 * Called when media details have been loaded This should be called on a background thread
	 *
	 * @param item
	 */
	@Override
    public void onDetailLoadSuccess(final Media item, final int paletteColor) {
        MediaDetailActivity.startActivity(mContext, item, paletteColor);
	}
}
