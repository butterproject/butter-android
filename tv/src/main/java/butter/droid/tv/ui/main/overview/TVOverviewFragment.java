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

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v17.leanback.app.BrowseFragment;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.ClassPresenterSelector;
import android.support.v17.leanback.widget.HeaderItem;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.ListRowPresenter;
import android.support.v17.leanback.widget.OnItemViewClickedListener;
import android.support.v17.leanback.widget.OnItemViewSelectedListener;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.RowPresenter;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.widget.Toast;
import butter.droid.base.PlayerTestConstants;
import butter.droid.base.torrent.StreamInfo;
import butter.droid.provider.base.module.Media;
import butter.droid.provider.base.module.Movie;
import butter.droid.provider.base.filter.Filter;
import butter.droid.provider.base.nav.NavItem;
import butter.droid.tv.BuildConfig;
import butter.droid.tv.R;
import butter.droid.tv.manager.internal.background.BackgroundUpdater;
import butter.droid.tv.manager.internal.background.BackgroundUpdaterModule;
import butter.droid.tv.presenters.LoadingCardPresenter;
import butter.droid.tv.presenters.LoadingCardPresenter.LoadingCardItem;
import butter.droid.tv.presenters.MediaCardPresenter;
import butter.droid.tv.presenters.MediaCardPresenter.MediaCardItem;
import butter.droid.tv.presenters.MorePresenter;
import butter.droid.tv.ui.detail.TVMediaDetailActivity;
import butter.droid.tv.ui.main.TVMainActivity;
import butter.droid.tv.ui.media.TVMediaGridActivity;
import butter.droid.tv.ui.player.TVVideoPlayerActivity;
import butter.droid.tv.ui.preferences.TVPreferencesActivity;
import butter.droid.tv.ui.search.TVSearchActivity;
import butter.droid.tv.ui.trailer.TVTrailerPlayerActivity;
import com.squareup.picasso.Picasso;
import java.util.List;
import javax.inject.Inject;

/*
 * Main class to show BrowseFragment with header and rows of videos
 */
public class TVOverviewFragment extends BrowseFragment implements TVOverviewView, OnItemViewClickedListener, OnItemViewSelectedListener {

    @Inject TVOverviewPresenter presenter;
    @Inject BackgroundUpdater backgroundUpdater;
    @Inject Picasso picasso;

    private ArrayObjectAdapter rowsAdapter;
    private ArrayObjectAdapter[] mediaListAdapters;
    private ArrayObjectAdapter[] moreOptionsAdapters;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ((TVMainActivity) getActivity()).getComponent()
                .overviewComponentBuilder()
                .overviewModule(new TVOverviewModule(this))
                .backgroundUpdaterModule(new BackgroundUpdaterModule(getActivity()))
                .build()
                .inject(this);
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

        Media mediaItem = null;
        if (item instanceof MediaCardPresenter.MediaCardItem) {
            mediaItem = ((MediaCardItem) item).getMedia();
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

    @Override public void openTestPlayerPicker() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        final String[] file_types = PlayerTestConstants.FILE_TYPES;

        builder.setTitle(R.string.overview_player_test)
                .setNegativeButton(R.string.cancel,
                        (dialogInterface, index) -> dialogInterface.dismiss())
                .setSingleChoiceItems(file_types, -1,
                        (dialogInterface, index) -> {
                            dialogInterface.dismiss();

                            presenter.debugVideoSelected(index);
                        });

        builder.show();
    }

    @Override public void openPreferencesScreen() {
        startActivity(TVPreferencesActivity.getIntent(getActivity()));
    }

    @Override public void openMediaActivity(@StringRes int title, final int providerId, @NonNull final Filter filter) {
        startActivity(TVMediaGridActivity.newIntent(getActivity(), providerId, title, filter));
    }

    @Override public void setupProviderRows(final int providerCount) {
        mediaListAdapters = new ArrayObjectAdapter[providerCount];
        moreOptionsAdapters = new ArrayObjectAdapter[providerCount];

        for (int i = 0; i < providerCount; i++) {
            mediaListAdapters[i] = addNewMediaListAdapter();
            moreOptionsAdapters[i] = addMoreOptionsAdapter();
        }
    }

    private ArrayObjectAdapter addMoreOptionsAdapter() {
        HeaderItem moreOptionsHeader = new HeaderItem(getString(R.string.more_movies));
        MorePresenter morePresenter = new MorePresenter(getActivity());
        ArrayObjectAdapter moreOptionsAdapter = new ArrayObjectAdapter(morePresenter);
        rowsAdapter.add(new ListRow(moreOptionsHeader, moreOptionsAdapter));
        return moreOptionsAdapter;
    }

    @Override public void setupMoreRow() {
        HeaderItem gridHeader = new HeaderItem(getString(R.string.more));
        MorePresenter gridPresenter = new MorePresenter(getActivity());
        ArrayObjectAdapter gridRowAdapter = new ArrayObjectAdapter(gridPresenter);

        if (BuildConfig.DEBUG) {
            gridRowAdapter.add(new MorePresenter.MoreItem(R.id.more_player_tests, R.string.tests, R.drawable.more_player_tests));
        }

        gridRowAdapter.add(new MorePresenter.MoreItem(R.id.more_item_settings, R.string.preferences, R.drawable.ic_settings));
        rowsAdapter.add(new ListRow(gridHeader, gridRowAdapter));
    }

    @Override public void showCustomDebugUrl() {
        // TODO
        /*
        final EditText dialogInput = new EditText(getActivity());
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setView(dialogInput)
                .setPositiveButton("Start", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Movie media = new Movie();
                        media.videoId = "dialogtestvideo";
                        media.title = "User input test video";

                        TVVideoPlayerActivity.startActivity(getActivity(), new StreamInfo(media, null, null, null, null,
                                dialogInput.getText().toString()), 0);
                    }
                });
        builder.show();
        */
    }

    @Override public void startTrailerScreen(final Movie movie, final String location) {
        startActivity(TVTrailerPlayerActivity.getIntent(getActivity(), movie, location));
    }

    @Override public void startPlayerActivity(final StreamInfo streamInfo) {
        TVVideoPlayerActivity.startActivity(getActivity(), streamInfo);
    }

    public void displayProviderSorters(int providerId, final List<NavItem> value) {
        ArrayObjectAdapter moreOptionsAdapter = moreOptionsAdapters[providerId];
        for (final NavItem navItem : value) {
            moreOptionsAdapter.add(new MorePresenter.MoreItem(navItem, providerId));
        }
    }

    private ArrayObjectAdapter addNewMediaListAdapter() {
        ClassPresenterSelector presenterSelector = new ClassPresenterSelector();
        presenterSelector.addClassPresenter(MediaCardItem.class, new MediaCardPresenter(getActivity(), picasso));
        presenterSelector.addClassPresenter(LoadingCardItem.class, new LoadingCardPresenter(getActivity()));

        ArrayObjectAdapter mediaAdapter = new ArrayObjectAdapter(presenterSelector);
        mediaAdapter.add(new LoadingCardItem());

        // TODO: 6/17/17 Define title
        HeaderItem moviesHeader = new HeaderItem(getString(R.string.top_movies));
        rowsAdapter.add(new ListRow(moviesHeader, mediaAdapter));
        return mediaAdapter;
    }

    private void setupUIElements() {
        setBadgeDrawable(ActivityCompat.getDrawable(getActivity(), R.drawable.header_logo));
        setTitle(getString(R.string.app_name)); // Badge, when set, takes precedent over title
        setHeadersState(HEADERS_ENABLED);
        setHeadersTransitionOnBackEnabled(true);
        // set fastLane (or headers) background color
        setBrandColor(ActivityCompat.getColor(getActivity(), R.color.primary));
        // set search icon color
        setSearchAffordanceColor(ActivityCompat.getColor(getActivity(), R.color.primary_dark));
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
        Bundle options = ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(), view.getMainImageView(),
                TVMediaDetailActivity.SHARED_ELEMENT_NAME).toBundle();

        Media mediaItem = media.getMedia();
        // TODO
        /*if (view.getCustomSelectedSwatch() != null) {
            mediaItem.color = view.getCustomSelectedSwatch().getRgb();
        }*/

        startActivity(TVMediaDetailActivity.getIntent(getActivity(), media.getProviderId(), mediaItem), options);
    }

}
