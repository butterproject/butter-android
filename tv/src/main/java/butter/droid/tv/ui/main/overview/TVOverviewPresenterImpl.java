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
import butter.droid.provider.base.module.ItemsWrapper;
import butter.droid.provider.base.module.Media;
import butter.droid.provider.base.nav.NavItem;
import butter.droid.tv.R;
import butter.droid.tv.presenters.MediaCardPresenter.MediaCardItem;
import butter.droid.tv.presenters.MorePresenter.MoreItem;
import io.reactivex.MaybeObserver;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import java.util.ArrayList;
import java.util.List;

public class TVOverviewPresenterImpl implements TVOverviewPresenter {

    private final TVOverviewView view;
    private final ProviderManager providerManager;
    private final YouTubeManager youTubeManager;

    private int selectedRow = 0;

    @Nullable private Disposable listRequest;
    @Nullable private Disposable sortersRequest;

    public TVOverviewPresenterImpl(final TVOverviewView view, final ProviderManager providerManager, final YouTubeManager youTubeManager) {
        this.view = view;
        this.providerManager = providerManager;
        this.youTubeManager = youTubeManager;
    }

    @Override public void onActivityCreated() {

        view.setupProviderRows(providerManager.getProviderCount());
        view.setupMoreRow();

        loadProvidersData();
    }

    @Override public void rowSelected(final int index, @Nullable final Media mediaItem) {
        if (selectedRow != index) {
            selectedRow = index;
        }

        if (mediaItem != null) {
            view.updateBackgroundImage(mediaItem.getBackdrop());
        }
    }

    @Override public void moreItemClicked(final MoreItem item) {
        switch (item.getId()) {
            case R.id.more_item_settings:
                view.openPreferencesScreen();
                break;
            case R.id.more_item_filter:
                //noinspection ConstantConditions
                view.openMediaActivity(item.getTitle(), item.getProviderId(), item.getFilter());
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

        // TODO
        /*
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
        */
    }

    @Override public void onDestroy() {
        cancelMovieCall();
        cancelMovieSortersCall();
    }

    private void loadProvidersData() {
        for (int i = 0; i < providerManager.getProviderCount(); i++) {
            loadProviderData(i);
        }
    }

    private void loadProviderData(final int providerId) {
        loadProviderMedia(providerId);
        loadProviderSorters(providerId);
    }

    private void loadProviderMedia(final int providerId) {
        final MediaProvider.Filters movieFilters = new MediaProvider.Filters();
        movieFilters.sort = MediaProvider.Filters.Sort.POPULARITY;
        movieFilters.order = MediaProvider.Filters.Order.DESC;

        // TODO: 6/17/17 Disposable per provider
        cancelMovieCall();
        providerManager.getProvider(providerId).items(null, null)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<ItemsWrapper>() {
                    @Override public void onSubscribe(final Disposable d) {
                        listRequest = d;
                    }

                    @Override public void onSuccess(final ItemsWrapper items) {
                        List<Media> mediaItems = items.getMedia();
                        List<MediaCardItem> cardItems = convertMediaToOverview(providerId, mediaItems);
                        view.displayProviderData(providerId, cardItems);

                        if (selectedRow == 0) {
                            view.updateBackgroundImage(mediaItems.get(0).getBackdrop());
                        }
                    }

                    @Override public void onError(final Throwable e) {
                        view.showErrorMessage(R.string.movies_error);
                    }
                });

    }

    private void loadProviderSorters(final int providerId) {
        // TODO: 6/17/17 Disposable per provider
        cancelMovieSortersCall();
        providerManager.getProvider(providerId).navigation()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new MaybeObserver<List<NavItem>>() {
                    @Override public void onSubscribe(final Disposable d) {
                        sortersRequest = d;
                    }

                    @Override public void onSuccess(final List<NavItem> value) {
                        view.displayProviderSorters(providerId, value);
                    }

                    @Override public void onError(final Throwable e) {
                        // fail quietly
                    }

                    @Override public void onComplete() {
                        // nothing to do
                    }
                });
    }

    private void cancelMovieCall() {
        if (listRequest != null) {
            listRequest.dispose();
            listRequest = null;
        }
    }

    private void cancelMovieSortersCall() {
        if (sortersRequest != null) {
            sortersRequest.dispose();
            sortersRequest = null;
        }
    }

    public static List<MediaCardItem> convertMediaToOverview(final int providerId, final List<Media> items) {
        List<MediaCardItem> list = new ArrayList<>();
        for (Media media : items) {
            list.add(new MediaCardItem(providerId, media));
        }
        return list;
    }

}
