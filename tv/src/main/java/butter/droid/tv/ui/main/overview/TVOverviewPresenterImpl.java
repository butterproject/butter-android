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

import android.support.annotation.Nullable;
import butter.droid.base.PlayerTestConstants;
import butter.droid.base.manager.internal.provider.ProviderManager;
import butter.droid.base.manager.internal.youtube.YouTubeManager;
import butter.droid.base.providers.media.MediaProvider;
import butter.droid.base.providers.media.MediaProvider.Filters;
import butter.droid.base.providers.media.models.Media;
import butter.droid.base.providers.media.models.Movie;
import butter.droid.base.torrent.StreamInfo;
import butter.droid.base.utils.ThreadUtils;
import butter.droid.tv.R;
import butter.droid.tv.presenters.MediaCardPresenter;
import butter.droid.tv.presenters.MediaCardPresenter.MediaCardItem;
import butter.droid.tv.presenters.MorePresenter.MoreItem;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class TVOverviewPresenterImpl implements TVOverviewPresenter {

    private final TVOverviewView view;
    private final ProviderManager providerManager;
    private final YouTubeManager youTubeManager;

    private int selectedRow = 0;

    @Nullable private Call movieListCall;

    public TVOverviewPresenterImpl(final TVOverviewView view, final ProviderManager providerManager, final YouTubeManager youTubeManager) {
        this.view = view;
        this.providerManager = providerManager;
        this.youTubeManager = youTubeManager;
    }

    @Override public void onActivityCreated() {

        view.setupMoviesRow();
//        view.setupTVShowsRow();
        view.setupMoreMoviesRow(providerManager.getMediaProvider(ProviderManager.PROVIDER_TYPE_MOVIE).getNavigation());
//        view.setupMoreTVShowsRow(providerManager.getMediaProvider(ProviderManager.PROVIDER_TYPE_SHOW).getNavigation());
        view.setupMoreRow();

        loadData();
    }

    @Override public void rowSelected(final int index, @Nullable final Media mediaItem) {
        if (selectedRow != index) {
            selectedRow = index;
        }

        if (mediaItem != null) {
            view.updateBackgroundImage(mediaItem.headerImage);
        }
    }

    @Override public void moreItemClicked(final MoreItem item) {
        switch (item.getId()) {
            case R.id.more_item_settings:
                view.openPreferencesScreen();
                break;
            case R.id.yts_filter_a_to_z:
            case R.id.yts_filter_trending:
            case R.id.yts_filter_release_date:
            case R.id.yts_filter_popular_now:
            case R.id.yts_filter_year:
            case R.id.yts_filter_top_rated:
                providerManager.setCurrentProviderType(ProviderManager.PROVIDER_TYPE_MOVIE);
                view.openMediaActivity(item.getNavInfo());
                break;
            case R.id.eztv_filter_a_to_z:
            case R.id.eztv_filter_trending:
            case R.id.eztv_filter_last_updated:
            case R.id.eztv_filter_popular_now:
            case R.id.eztv_filter_year:
            case R.id.eztv_filter_top_rated:
                providerManager.setCurrentProviderType(ProviderManager.PROVIDER_TYPE_SHOW);
                view.openMediaActivity(item.getNavInfo());
                break;
            case R.id.yts_filter_genres:
                view.showErrorMessage("Not implemented yet");
                break;
            case R.id.more_player_tests:
                view.openTestPlayerPicker();
                break;
            default:
                throw new IllegalStateException("Unknown item id");
        }
    }

    @Override public void debugVideoSelected(final int index) {

        final String location = PlayerTestConstants.FILES[index];

        if (location.equals("dialog")) {
            view.showCustomDebugUrl();
        } else if (youTubeManager.isYouTubeUrl(location)) {
            Movie movie = new Movie(PlayerTestConstants.FILE_TYPES[index]);
            view.startTrailerScreen(movie, location);
        } else {
            final Movie media = new Movie();
            media.videoId = "bigbucksbunny";
            media.title = PlayerTestConstants.FILE_TYPES[index];
            media.subtitles = new HashMap<>();
            media.subtitles.put("en", "http://sv244.cf/bbb-subs.srt");

            providerManager.getCurrentSubsProvider().download(media, "en", new Callback() {
                @Override public void onFailure(Call call, IOException ex) {
                    view.startPlayerActivity(new StreamInfo(media, null, null, null, null, location));
                }

                @Override public void onResponse(Call call, Response response) throws IOException {
                    view.startPlayerActivity(new StreamInfo(media, null, null, null, null, location));
                }
            });
        }
    }

    @Override public void onDestroy() {
        cancleMovieCall();
    }

    private void loadData() {
        /*
        final MediaProvider.Filters showsFilter = new MediaProvider.Filters();
        showsFilter.sort = MediaProvider.Filters.Sort.DATE;
        showsFilter.order = MediaProvider.Filters.Order.DESC;

        mShowsProvider.getList(null, showsFilter, new MediaProvider.Callback() {
            @DebugLog
            @Override
            public void onSuccess(MediaProvider.Filters filters, ArrayList<Media> items, boolean changed) {
                List<MediaCardPresenter.MediaCardItem> list = MediaCardPresenter.convertMediaToOverview(items);
                showAdapter.clear();
                showAdapter.addAll(0, list);

                if(selectedRow == 1)
                    backgroundUpdater.updateBackgroundAsync(items.get(0).headerImage);
            }

            @DebugLog
            @Override
            public void onFailure(Exception e) {
                e.printStackTrace();
                ThreadUtils.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getActivity(), R.string.encountered_error, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        */

        final MediaProvider.Filters movieFilters = new MediaProvider.Filters();
        movieFilters.sort = MediaProvider.Filters.Sort.POPULARITY;
        movieFilters.order = MediaProvider.Filters.Order.DESC;

        cancleMovieCall();
        movieListCall = providerManager.getMediaProvider(ProviderManager.PROVIDER_TYPE_MOVIE)
                .getList(null, movieFilters, new MediaProvider.Callback() {
                    @Override
                    public void onSuccess(Filters filters, final ArrayList<Media> items, boolean changed) {
                        ThreadUtils.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                List<MediaCardItem> list = MediaCardPresenter.convertMediaToOverview(items);
                                view.displayMovies(list);

                                if (selectedRow == 0) {
                                    view.updateBackgroundImage(items.get(0).headerImage);
                                }
                            }
                        });
                    }

                    @Override
                    public void onFailure(Exception ex) {
                        ThreadUtils.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                view.showErrorMessage(R.string.movies_error);
                            }
                        });
                    }
                });
    }

    private void cancleMovieCall() {
        if (movieListCall != null) {
            movieListCall.cancel();
            movieListCall = null;
        }
    }
}
