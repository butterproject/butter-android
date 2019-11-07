/*
 * This file is part of Butter.
 *
 * Butter is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Butter is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Butter. If not, see <http://www.gnu.org/licenses/>.
 */

package butter.droid.tv.ui.main.overview;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.leanback.app.BrowseSupportFragment;
import androidx.leanback.widget.ArrayObjectAdapter;
import androidx.leanback.widget.ClassPresenterSelector;
import androidx.leanback.widget.HeaderItem;
import androidx.leanback.widget.ListRow;
import androidx.leanback.widget.ListRowPresenter;
import androidx.leanback.widget.OnItemViewClickedListener;
import androidx.leanback.widget.OnItemViewSelectedListener;
import androidx.leanback.widget.Presenter;
import androidx.leanback.widget.Row;
import androidx.leanback.widget.RowPresenter;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import android.widget.Toast;

import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import butter.droid.base.manager.internal.provider.model.ProviderWrapper;
import butter.droid.base.providers.media.model.MediaWrapper;
import butter.droid.provider.base.filter.Filter;
import butter.droid.provider.base.nav.NavItem;
import butter.droid.tv.R;
import butter.droid.tv.manager.internal.background.BackgroundUpdater;
import butter.droid.tv.presenters.LoadingCardPresenter;
import butter.droid.tv.presenters.LoadingCardPresenter.LoadingCardItem;
import butter.droid.tv.presenters.MediaCardPresenter;
import butter.droid.tv.presenters.MediaCardPresenter.MediaCardItem;
import butter.droid.tv.presenters.MorePresenter;
import butter.droid.tv.ui.detail.TVMediaDetailActivity;
import butter.droid.tv.ui.media.TVMediaGridActivity;
import butter.droid.tv.ui.preferences.TVPreferencesActivity;
import butter.droid.tv.ui.search.TVSearchActivity;
import dagger.android.support.AndroidSupportInjection;

public class TVOverviewFragment extends BrowseSupportFragment implements TVOverviewView, OnItemViewClickedListener,
        OnItemViewSelectedListener {

    @Inject TVOverviewPresenter presenter;
    @Inject BackgroundUpdater backgroundUpdater;

    private ArrayObjectAdapter rowsAdapter;
    private ArrayObjectAdapter[] mediaListAdapters;
    private ArrayObjectAdapter[] moreOptionsAdapters;

    @Override public void onAttach(final Context context) {
        AndroidSupportInjection.inject(this);
        super.onAttach(context);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //setup background updater
        backgroundUpdater.initialise(getActivity(), R.color.black);

        setupUIElements();
        setupEventListeners();
        setupAdapters();

        presenter.onActivityCreated();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        presenter.onDestroy();
        backgroundUpdater.destroy();
    }

    @Override
    public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item, RowPresenter.ViewHolder rowViewHolder, Row row) {
        if (item instanceof MediaCardPresenter.MediaCardItem) {
            onMediaItemClicked((MediaCardPresenter.CustomImageCardView) itemViewHolder.view, (MediaCardPresenter.MediaCardItem) item);
        } else if (item instanceof MorePresenter.MoreItem) {
            presenter.moreItemClicked((MorePresenter.MoreItem) item);
        }
    }

    @Override
    public void onItemSelected(Presenter.ViewHolder itemViewHolder, Object item, RowPresenter.ViewHolder rowViewHolder, Row row) {
        int index = rowsAdapter.indexOf(row);

        MediaWrapper mediaItem = null;
        if (item instanceof MediaCardPresenter.MediaCardItem) {
            mediaItem = ((MediaCardItem) item).getMediaWrapper();
        }

        presenter.rowSelected(index, mediaItem);
    }

    @Override public void displayProviderData(final int providerId, final List<MediaCardItem> list) {
        ArrayObjectAdapter adapter = mediaListAdapters[providerId];
        adapter.clear();
        adapter.addAll(0, list);
    }

    @Override public void updateBackgroundImage(final String url) {
        backgroundUpdater.updateBackgroundAsync(url);
    }

    @Override public void showErrorMessage(@StringRes final int message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }

    @Override public void openPreferencesScreen() {
        startActivity(TVPreferencesActivity.getIntent(getActivity()));
    }

    @Override public void openMediaActivity(@StringRes int title, final int providerId, @NonNull final Filter filter) {
        startActivity(TVMediaGridActivity.newIntent(getActivity(), providerId, title, filter));
    }

    @Override public void setupProviderRows(final ProviderWrapper[] providers) {
        int providerCount = providers.length;
        mediaListAdapters = new ArrayObjectAdapter[providerCount];
        moreOptionsAdapters = new ArrayObjectAdapter[providerCount];

        for (int i = 0; i < providerCount; i++) {
            String providerName = getString(providers[i].getDisplayName());
            mediaListAdapters[i] = addNewMediaListAdapter(providerName);
            moreOptionsAdapters[i] = addMoreOptionsAdapter(providerName);
        }
    }

    private ArrayObjectAdapter addMoreOptionsAdapter(final String providerName) {
        HeaderItem moreOptionsHeader = new HeaderItem(String.format(Locale.getDefault(), "%s %s",
                getString(R.string.provider_name_more), providerName));
        MorePresenter morePresenter = new MorePresenter(requireContext());
        ArrayObjectAdapter moreOptionsAdapter = new ArrayObjectAdapter(morePresenter);
        rowsAdapter.add(new ListRow(moreOptionsHeader, moreOptionsAdapter));
        return moreOptionsAdapter;
    }

    @Override public void setupMoreRow() {
        HeaderItem gridHeader = new HeaderItem(getString(R.string.more));
        MorePresenter gridPresenter = new MorePresenter(requireContext());
        ArrayObjectAdapter gridRowAdapter = new ArrayObjectAdapter(gridPresenter);

        gridRowAdapter.add(new MorePresenter.MoreItem(R.id.more_item_settings, R.string.preferences, R.drawable.ic_settings));
        rowsAdapter.add(new ListRow(gridHeader, gridRowAdapter));
    }

    public void displayProviderSorters(int providerId, final List<NavItem> value) {
        ArrayObjectAdapter moreOptionsAdapter = moreOptionsAdapters[providerId];
        for (final NavItem navItem : value) {
            moreOptionsAdapter.add(new MorePresenter.MoreItem(navItem, providerId));
        }
    }

    private ArrayObjectAdapter addNewMediaListAdapter(final String sorterName) {
        Context context = requireContext();
        ClassPresenterSelector presenterSelector = new ClassPresenterSelector();
        presenterSelector.addClassPresenter(MediaCardItem.class, new MediaCardPresenter(context));
        presenterSelector.addClassPresenter(LoadingCardItem.class, new LoadingCardPresenter(context));

        ArrayObjectAdapter mediaAdapter = new ArrayObjectAdapter(presenterSelector);
        mediaAdapter.add(new LoadingCardItem());

        HeaderItem moviesHeader = new HeaderItem(sorterName);
        rowsAdapter.add(new ListRow(moviesHeader, mediaAdapter));
        return mediaAdapter;
    }

    private void setupUIElements() {
        Context context = requireContext();
        setBadgeDrawable(ActivityCompat.getDrawable(context, R.mipmap.ic_launcher_foreground));
        setTitle(getString(R.string.app_name)); // Badge, when set, takes precedent over title
        setHeadersState(HEADERS_ENABLED);
        setHeadersTransitionOnBackEnabled(true);
        // set fastLane (or headers) background color
        setBrandColor(ActivityCompat.getColor(context, R.color.primary));
        // set search icon color
        setSearchAffordanceColor(ActivityCompat.getColor(context, R.color.primary_dark));
    }

    private void setupEventListeners() {
        setOnSearchClickedListener(view -> startActivity(TVSearchActivity.newIntent(getActivity())));

        setOnItemViewClickedListener(this);
        setOnItemViewSelectedListener(this);
    }

    private void setupAdapters() {
        //setup main adapter
        ListRowPresenter mainMenuRowPresenter = new ListRowPresenter();
        mainMenuRowPresenter.setShadowEnabled(false);
        rowsAdapter = new ArrayObjectAdapter(mainMenuRowPresenter);
        setAdapter(rowsAdapter);
    }

    private void onMediaItemClicked(MediaCardPresenter.CustomImageCardView view, MediaCardPresenter.MediaCardItem media) {
        Bundle options = ActivityOptionsCompat.makeSceneTransitionAnimation(requireActivity(), view.getMainImageView(),
                TVMediaDetailActivity.SHARED_ELEMENT_NAME).toBundle();
        MediaWrapper mediaItem = media.getMediaWrapper();
        startActivity(TVMediaDetailActivity.getIntent(getActivity(), mediaItem), options);
    }

}
