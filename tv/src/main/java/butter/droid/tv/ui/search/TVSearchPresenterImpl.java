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

package butter.droid.tv.ui.search;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Pair;
import butter.droid.base.manager.internal.provider.ProviderManager;
import butter.droid.base.providers.media.MediaProvider.Filters;
import butter.droid.provider.base.ItemsWrapper;
import butter.droid.provider.base.Media;
import butter.droid.tv.R;
import butter.droid.tv.presenters.MediaCardPresenter;
import butter.droid.tv.presenters.MediaCardPresenter.MediaCardItem;
import io.reactivex.Observer;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import timber.log.Timber;

public class TVSearchPresenterImpl implements TVSearchPresenter {

    private static final int SEARCH_DELAY_MS = 300;

    private final TVSearchView view;
    private final ProviderManager providerManager;

    private final SearchRunnable delayedLoad = new SearchRunnable();
    private final Filters searchFilter = new Filters();
    private final Handler handler = new Handler(Looper.getMainLooper());

    private final CompositeDisposable searchRequests = new CompositeDisposable();
    private final BehaviorSubject<String> querySubject = BehaviorSubject.create();

    public TVSearchPresenterImpl(TVSearchView view, ProviderManager providerManager) {
        this.view = view;
        this.providerManager = providerManager;
    }

    @Override public void onTextChanged(String newQuery) {
        querySubject.onNext(newQuery);
    }

    @Override public void onTextSubmitted(String query) {
        // TODO: 6/4/17 Handle properly (no delay)
        //        queryByWords(query);
    }

    @Override public void onCreate() {
        querySubject.filter(q -> q.length() > 3)
                .debounce(SEARCH_DELAY_MS, TimeUnit.MILLISECONDS)
                .flatMap(q -> Single.zip(getProviderRequests(q), s -> (Pair<Integer, List<Media>>[]) s)
                        .toObservable())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Pair<Integer, List<Media>>[]>() {
                    @Override public void onSubscribe(final Disposable d) {
                        searchRequests.add(d);
                    }

                    @Override public void onNext(final Pair<Integer, List<Media>>[] value) {
                        for (final Pair<Integer, List<Media>> provider : value) {
                            List<MediaCardItem> list = MediaCardPresenter.convertMediaToOverview(provider.second);
                            view.addRow(provider.first, list);
                        }
                    }

                    @Override public void onError(final Throwable e) {
                        Timber.d("Test");
                        // TODO: 6/4/17 Handle error
                    }

                    @Override public void onComplete() {
                        // should never happen
                    }
                });
    }

    private void queryByWords(String words) {
        querySubject.onNext(words);

        view.clearData();
        if (!TextUtils.isEmpty(words)) {
            delayedLoad.setSearchQuery(words);
            handler.removeCallbacks(delayedLoad);
            handler.postDelayed(delayedLoad, SEARCH_DELAY_MS);
        }
    }

    private List<Single<Pair<Integer, List<Media>>>> getProviderRequests(String query) {
        // TODO: 6/4/17 Add query and filters
        List<Single<Pair<Integer, List<Media>>>> requests = new ArrayList<>();

        if (providerManager.hasProvider(ProviderManager.PROVIDER_TYPE_MOVIE)) {
            requests.add(providerManager.getMediaProvider(ProviderManager.PROVIDER_TYPE_MOVIE)
                    .items()
                    .map(itemsWrapper -> Pair.create(R.string.movie_results, itemsWrapper.getMedia())));
        }

        if (providerManager.hasProvider(ProviderManager.PROVIDER_TYPE_SHOW)) {
            requests.add(providerManager.getMediaProvider(ProviderManager.PROVIDER_TYPE_SHOW)
                    .items()
                    .map(itemsWrapper -> Pair.create(R.string.movie_results, itemsWrapper.getMedia())));
        }

        return requests;

    }


    private void loadRows(String query) {
        view.clearData();
        view.showLoadingRow();

        searchFilter.keywords = query;
        searchFilter.page = 1;

        if (providerManager.hasProvider(ProviderManager.PROVIDER_TYPE_SHOW)) {
            // TODO: 6/4/17 Add Filters
            providerManager.getMediaProvider(ProviderManager.PROVIDER_TYPE_SHOW)
                    .items()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SingleObserver<ItemsWrapper>() {
                        @Override public void onSubscribe(final Disposable d) {
                            searchRequests.add(d);
                        }

                        @Override public void onSuccess(final ItemsWrapper value) {
                            List<MediaCardItem> list = MediaCardPresenter.convertMediaToOverview(value.getMedia());
                            view.addRow(R.string.show_results, list);
                        }

                        @Override public void onError(final Throwable e) {
                            // TODO: 6/4/17 Handle errors
                        }
                    });
        }


//        if (providerManager.hasProvider(ProviderManager.PROVIDER_TYPE_MOVIE)) {
//            MediaProvider mediaProvider = providerManager.getMediaProvider(ProviderManager.PROVIDER_TYPE_MOVIE);
//            //noinspection ConstantConditions
//            mediaProvider.cancel();
//            mediaProvider.getList(searchFilter, new MediaProvider.Callback() {
//                        @Override
//                        public void onSuccess(MediaProvider.Filters filters, final ArrayList<Media> items, boolean changed) {
//                            ThreadUtils.runOnUiThread(new Runnable() {
//                                @Override public void run() {
//                                    List<MediaCardItem> list = MediaCardPresenter.convertMediaToOverview(items);
//                                    view.addRow(R.string.movie_results, list);
//                                }
//                            });
//                        }
//
//                        @Override public void onFailure(Exception ex) {
//
//                        }
//                    }
//
//            );
//        }

    }

    private class SearchRunnable implements Runnable {

        private volatile String searchQuery;

        public SearchRunnable() {
        }

        public void run() {
            loadRows(searchQuery);
        }

        public void setSearchQuery(String value) {
            this.searchQuery = value;
        }
    }


}
