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

import android.support.annotation.StringRes;
import butter.droid.base.manager.internal.provider.ProviderManager;
import butter.droid.provider.MediaProvider;
import butter.droid.provider.base.filter.Filter;
import butter.droid.provider.base.module.Media;
import butter.droid.provider.filter.Pager;
import butter.droid.tv.R;
import butter.droid.tv.presenters.MediaCardPresenter;
import butter.droid.tv.presenters.MediaCardPresenter.MediaCardItem;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import timber.log.Timber;

public class TVSearchPresenterImpl implements TVSearchPresenter {

    private static final int SEARCH_DELAY_MS = 300;

    private final TVSearchView view;
    private final ProviderManager providerManager;

    private final CompositeDisposable searchRequests = new CompositeDisposable();
    private final BehaviorSubject<SearchRequest> querySubject = BehaviorSubject.create();
    private final PublishSubject<String> loadingSubject = PublishSubject.create();

    public TVSearchPresenterImpl(TVSearchView view, ProviderManager providerManager) {
        this.view = view;
        this.providerManager = providerManager;
    }

    @Override public void onTextChanged(String newQuery) {
        querySubject.onNext(new SearchRequest(newQuery, false));
    }

    @Override public void onTextSubmitted(String query) {
        querySubject.onNext(new SearchRequest(query, true));
    }

    @Override public void onCreate() {

        searchRequests.add(loadingSubject
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(q -> {
                    view.clearData();
                    view.showLoadingRow();
                }));

        querySubject.debounce(q ->
                Observable.just(q)
                        .delay(searchRequest -> {
                            if (searchRequest.immediate) {
                                return Observable.just(0L);
                            } else {
                                return Observable.timer(SEARCH_DELAY_MS, TimeUnit.MILLISECONDS);
                            }
                        }))
                .map(q -> q.query)
                .doOnNext(q -> Timber.d(q))
                .distinct()
                .filter(q -> q.length() > 3)
                .doOnNext(loadingSubject::onNext)
                .flatMap(q -> Single.just(q)
                        .flatMapPublisher(q1 -> Single.concat(getProviderRequests(q1)))
                        .toList()
                        .toObservable()
                        .subscribeOn(Schedulers.io()))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<SearchResult>>() {
                    @Override public void onSubscribe(final Disposable d) {
                        searchRequests.add(d);
                    }

                    @Override public void onNext(final List<SearchResult> value) {
                        for (int i = 0; i < value.size(); i++) {
                            SearchResult provider = value.get(i);
                            List<MediaCardItem> list = MediaCardPresenter.convertMediaToOverview(provider.providerId, provider.media);
                            view.replaceRow(i, provider.title, list);
                        }
                    }

                    @Override public void onError(final Throwable e) {
                        // TODO: 6/11/17 Show error
                        Timber.d("error");
                    }

                    @Override public void onComplete() {
                        Timber.d("complete");
                    }
                });
    }

    private List<Single<SearchResult>> getProviderRequests(String query) {

        List<Single<SearchResult>> requests = new ArrayList<>();

        for (int i = 0; i < providerManager.getProviders().length; i++) {
            MediaProvider provider = providerManager.getProvider(i);
            final int providerId = i;
            // TODO: 6/17/17 Define title of search row
            requests.add(provider.items(new Filter(null, null, query), new Pager(null))
                    .map(itemsWrapper -> new SearchResult(providerId, R.string.movie_results, itemsWrapper.getMedia())));
        }

        return requests;

    }

    private class SearchRequest {

        private final String query;
        private final boolean immediate;

        private SearchRequest(final String query, final boolean immediate) {
            this.query = query;
            this.immediate = immediate;
        }
    }

    private class SearchResult {

        private final int providerId;
        @StringRes private final int title;
        private final List<Media> media;

        public SearchResult(final int providerId, final int title, final List<Media> media) {
            this.providerId = providerId;
            this.title = title;
            this.media = media;
        }

    }
}
