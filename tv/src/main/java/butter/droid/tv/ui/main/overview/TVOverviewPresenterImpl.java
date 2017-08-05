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

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Pair;
import butter.droid.base.manager.internal.provider.ProviderManager;
import butter.droid.base.providers.model.MediaWrapper;
import butter.droid.base.utils.rx.KeyDisposable;
import butter.droid.provider.base.filter.Filter;
import butter.droid.provider.base.module.Paging;
import butter.droid.provider.base.nav.NavItem;
import butter.droid.provider.filter.Pager;
import butter.droid.tv.R;
import butter.droid.tv.presenters.MediaCardPresenter.MediaCardItem;
import butter.droid.tv.presenters.MorePresenter.MoreItem;
import io.reactivex.MaybeObserver;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import java.util.ArrayList;
import java.util.List;

public class TVOverviewPresenterImpl implements TVOverviewPresenter {

    private final TVOverviewView view;
    private final ProviderManager providerManager;

    private int selectedRow = 0;

    @NonNull private final KeyDisposable listRequests = new KeyDisposable();
    @NonNull private final KeyDisposable sortersRequests = new KeyDisposable();

    public TVOverviewPresenterImpl(final TVOverviewView view, final ProviderManager providerManager) {
        this.view = view;
        this.providerManager = providerManager;
    }

    @Override public void onActivityCreated() {

        view.setupProviderRows(providerManager.getProviderCount());
        view.setupMoreRow();

        loadProvidersData();
    }

    @Override public void rowSelected(final int index, @Nullable final MediaWrapper mediaItem) {
        if (selectedRow != index) {
            selectedRow = index;
        }

        if (mediaItem != null) {
            view.updateBackgroundImage(mediaItem.getMedia().getBackdrop());
        }
    }

    @Override public void moreItemClicked(final MoreItem item) {
        switch (item.getId()) {
            case R.id.more_item_settings:
                view.openPreferencesScreen();
                break;
            case R.id.more_item_filter:
                //noinspection ConstantConditions
                view.openMediaActivity(item.getTitle(), item.getProviderId(), new Filter(null, item.getSorter()));
                break;
            default:
                throw new IllegalStateException("Unknown item id");
        }
    }

    @Override public void onDestroy() {
        listRequests.dispose();
        sortersRequests.dispose();
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
        cancelMovieCall(providerId);
        final butter.droid.provider.MediaProvider provider = providerManager.getProvider(providerId);
        provider.getDefaultSorter()
                .flatMap(sorter -> {
                    Filter f;
                    if (sorter.isPresent()) {
                        f = new Filter(null, sorter.get());
                    } else {
                        f = new Filter(null, null);
                    }
                    return provider.items(f, new Pager(null));
                })
                .flatMap(w -> Single.zip(Single.just(w.getPaging()),
                        Observable.fromIterable(w.getMedia())
                                .map(m -> new MediaWrapper(m, providerId))
                                .toList(),
                        Pair::create
                ))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<Pair<Paging, List<MediaWrapper>>>() {
                    @Override public void onSubscribe(final Disposable d) {
                        listRequests.add(providerId, d);
                    }

                    @Override public void onSuccess(final Pair<Paging, List<MediaWrapper>> items) {
                        List<MediaWrapper> mediaItems = items.second;
                        List<MediaCardItem> cardItems = convertMediaToOverview(mediaItems);

                        view.displayProviderData(providerId, cardItems);

                        if (selectedRow == 0) {
                            view.updateBackgroundImage(mediaItems.get(0).getMedia().getBackdrop());
                        }
                    }

                    @Override public void onError(final Throwable e) {
                        view.showErrorMessage(R.string.movies_error);
                    }
                });

    }

    private void loadProviderSorters(final int providerId) {
        cancelMovieSortersCall(providerId);
        providerManager.getProvider(providerId).navigation()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new MaybeObserver<List<NavItem>>() {
                    @Override public void onSubscribe(final Disposable d) {
                        sortersRequests.add(providerId, d);
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

    private void cancelMovieCall(int providerId) {
        listRequests.disposeSingle(providerId);
    }

    private void cancelMovieSortersCall(int providerId) {
        sortersRequests.disposeSingle(providerId);
    }

    private static List<MediaCardItem> convertMediaToOverview(final List<MediaWrapper> items) {
        List<MediaCardItem> list = new ArrayList<>();
        for (MediaWrapper media : items) {
            list.add(new MediaCardItem(media));
        }
        return list;
    }

}
