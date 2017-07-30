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

package butter.droid.tv.ui.media;

import android.support.annotation.Nullable;
import android.util.Pair;
import butter.droid.base.manager.internal.provider.ProviderManager;
import butter.droid.base.providers.model.MediaWrapper;
import butter.droid.provider.base.filter.Filter;
import butter.droid.provider.base.module.ItemsWrapper;
import butter.droid.provider.base.module.Paging;
import butter.droid.provider.filter.Pager;
import butter.droid.tv.presenters.MediaCardPresenter.MediaCardItem;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import java.util.List;

public class TVMediaGridPresenterImpl implements TVMediaGridPresenter {

    private final TVMediaGridView view;
    private final ProviderManager providerManager;

    private int providerId;
    private Filter filter;

    private Disposable currentCall;

    public TVMediaGridPresenterImpl(final TVMediaGridView view, final ProviderManager providerManager) {
        this.view = view;
        this.providerManager = providerManager;
    }

    @Override public void onCreate(final int providerId, final Filter filter) {
        this.providerId = providerId;
        this.filter = filter;
    }

    @Override public void onActivityCreated() {
        loadItems(null);
    }

    @Override public void loadNextPage(final String endCursor) {
        loadItems(endCursor);
    }

    @Override public void onDestroy() {
        cancelCurrentCall();
    }

    private void loadItems(@Nullable String endCursor) {
        cancelCurrentCall();
        providerManager.getProvider(providerId)
                .items(filter, new Pager(endCursor))
                .flatMap(i -> Single.zip(
                        Single.just(i)
                                .map(ItemsWrapper::getMedia)
                                .flatMapObservable(Observable::fromIterable)
                                .map(media -> new MediaWrapper(media, providerId))
                                .map(MediaCardItem::new)
                                .toList(),
                        Single.just(i)
                                .map(ItemsWrapper::getPaging),
                        Pair::create
                ))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<Pair<List<MediaCardItem>, Paging>>() {
                    @Override public void onSubscribe(final Disposable d) {
                        currentCall = d;
                    }

                    @Override public void onSuccess(final Pair<List<MediaCardItem>, Paging> value) {
                        view.appendItems(value.first, !value.second.getHasNextPage(), value.second.getEndCursor());
                    }

                    @Override public void onError(final Throwable e) {
                        view.displayError("Error getting show list");
                    }
                });
    }

    private void cancelCurrentCall() {
        if (currentCall != null) {
            currentCall.dispose();
            currentCall = null;
        }
    }
}
