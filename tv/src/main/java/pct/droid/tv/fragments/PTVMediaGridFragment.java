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

package pct.droid.tv.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.v17.leanback.app.VerticalGridFragment;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.ImageCardView;
import android.support.v17.leanback.widget.OnItemViewClickedListener;
import android.support.v17.leanback.widget.OnItemViewSelectedListener;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.RowPresenter;
import android.support.v17.leanback.widget.VerticalGridPresenter;
import android.support.v4.app.ActivityOptionsCompat;
import android.text.TextUtils;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import hugo.weaving.DebugLog;
import pct.droid.base.providers.media.EZTVProvider;
import pct.droid.base.providers.media.MediaProvider;
import pct.droid.base.providers.media.YTSProvider;
import pct.droid.base.providers.media.models.Media;
import pct.droid.base.providers.media.models.Movie;
import pct.droid.base.providers.media.models.Show;
import pct.droid.base.utils.StringUtils;
import pct.droid.base.utils.ThreadUtils;
import pct.droid.tv.R;
import pct.droid.tv.activities.PTVMediaDetailActivity;
import pct.droid.tv.activities.PTVMediaGridActivity;
import pct.droid.tv.presenters.MediaCardPresenter;
import pct.droid.tv.utils.BackgroundUpdater;
import timber.log.Timber;


/*
 * VerticalGridFragment shows a grid of videos
 */
public class PTVMediaGridFragment extends VerticalGridFragment implements OnItemViewClickedListener, OnItemViewSelectedListener {

    private MediaProvider mProvider;

    private static final int NUM_COLUMNS = 6;

    private List<MediaCardPresenter.MediaCardItem> mItems = new ArrayList<>();

    private ArrayObjectAdapter mAdapter;
    private Callback mCallback;

    private BackgroundUpdater mBackgroundUpdater;
    private int mCurrentPage = 0 ;

    public static PTVMediaGridFragment newInstance() {
        PTVMediaGridFragment fragment = new PTVMediaGridFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(StringUtils.capWords((String) getActivity().getTitle()));
        setupFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mCallback = (Callback) context;
    }

    private void setupFragment() {
        //setup background updater
        mBackgroundUpdater = new BackgroundUpdater();
        mBackgroundUpdater.initialise(getActivity(), R.color.black);

        VerticalGridPresenter gridPresenter = new VerticalGridPresenter();
        gridPresenter.setNumberOfColumns(NUM_COLUMNS);
        setGridPresenter(gridPresenter);

        mAdapter = new ArrayObjectAdapter(new MediaCardPresenter(getActivity()));
        switch (mCallback.getType()) {
            case MOVIE:
                mProvider = new YTSProvider();
                break;
            case SHOW:
                mProvider = new EZTVProvider();
                break;
        }

        setAdapter(mAdapter);

        setOnItemViewClickedListener(this);
        setOnItemViewSelectedListener(this);

        loadItems();
    }

    private MediaProvider.Filters getFilters() {
        MediaProvider.Filters filters = mCallback.getFilters();
        filters.page = mCurrentPage;
        return filters;
    }

    private void loadItems() {
        mProvider.getList(null, getFilters(), new MediaProvider.Callback() {
            @DebugLog
            @Override
            public void onSuccess(MediaProvider.Filters filters, ArrayList<Media> items, boolean changed) {
                mCurrentPage = filters.page;
                List<MediaCardPresenter.MediaCardItem> list = MediaCardPresenter.convertMediaToOverview(items);

                mItems.addAll(list);

                int previousSize = mAdapter.size();
                mAdapter.addAll(previousSize,list);
                mAdapter.notifyArrayItemRangeChanged(previousSize,list.size());
            }

            @DebugLog
            @Override
            public void onFailure(Exception e) {
                e.printStackTrace();
                ThreadUtils.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getActivity(), "error getting show list", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void loadMore() {
        mCurrentPage++;
        loadItems();
    }

    @Override
    public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item, RowPresenter.ViewHolder rowViewHolder, Row row) {
        if (item instanceof MediaCardPresenter.MediaCardItem) {
            onMediaItemClicked((ImageCardView) itemViewHolder.view, (MediaCardPresenter.MediaCardItem) item);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (null != mBackgroundUpdater) mBackgroundUpdater.destroy();
    }

    private void onMediaItemClicked(ImageCardView view, MediaCardPresenter.MediaCardItem media) {
        if (media.isLoading()) return;
        Bundle options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                getActivity(),
                view.getMainImageView(),
                PTVMediaDetailActivity.SHARED_ELEMENT_NAME).toBundle();
        if (media.getMedia() instanceof Movie)
            PTVMediaDetailActivity.startActivity(getActivity(), options, media.getMedia(), media.getMedia().headerImage, media.getMedia().image);
        else if (media.getMedia() instanceof Show)
            PTVMediaDetailActivity.startActivity(getActivity(), options, media.getMedia(), media.getMedia().headerImage, media.getMedia().image);
    }

    @Override
    public void onItemSelected(Presenter.ViewHolder itemViewHolder, Object item, RowPresenter.ViewHolder rowViewHolder, Row row) {
        if (item instanceof MediaCardPresenter.MediaCardItem) {
            MediaCardPresenter.MediaCardItem overviewItem = (MediaCardPresenter.MediaCardItem) item;
            if (overviewItem.isLoading()) return;

            mBackgroundUpdater.updateBackgroundAsync(((MediaCardPresenter.MediaCardItem) item).getMedia().headerImage);
        }

        //really hacky way of making and 'endless' adapter

        //trigger items to update
        int itemPosition = mItems.indexOf(item);

        //when we are within 3 rows of the end, load more items
        if (itemPosition>getAdapter().size()-(NUM_COLUMNS*3)){
            Timber.d("Loading more items: page "+mCurrentPage);
            loadMore();
        }
    }

    public interface Callback {
        MediaProvider.Filters getFilters();

        PTVMediaGridActivity.ProviderType getType();
    }

    private static class EndlessArrayAdapter extends ArrayObjectAdapter{


    }
}
