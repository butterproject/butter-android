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

package butter.droid.ui.media.list.base;

import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import butter.droid.R;
import butter.droid.base.ButterApplication;
import butter.droid.base.content.preferences.PreferencesHandler;
import butter.droid.base.manager.internal.provider.ProviderManager;
import butter.droid.provider.base.filter.Filter;
import butter.droid.provider.base.module.ItemsWrapper;
import butter.droid.provider.base.module.Media;
import butter.droid.provider.base.module.Paging;
import butter.droid.provider.filter.Pager;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import java.util.ArrayList;
import timber.log.Timber;

public abstract class BaseMediaListPresenterImpl implements BaseMediaListPresenter {

    private final BaseMediaListView view;
    private final ProviderManager providerManager;
    private final PreferencesHandler preferencesHandler;

    protected final ArrayList<Media> items = new ArrayList<>();
    protected Filter filter = null;

    protected int providerId;

    protected Disposable listCall;
    protected Disposable detailsCall;

    public BaseMediaListPresenterImpl(BaseMediaListView view, ProviderManager providerManager, PreferencesHandler preferencesHandler) {
        this.view = view;
        this.providerManager = providerManager;
        this.preferencesHandler = preferencesHandler;
    }

    @Override public void onActivityCreated(final int providerId, final Filter filter) {
        this.providerId = providerId;
        this.filter = filter;

        // TODO: 6/17/17
//        filters.langCode = LocaleUtils.toLocale(preferencesHandler.getLocale()).getLanguage();
    }

    @Override public void loadNextPage(@Nullable String endCursor) {
        cancelListCall();

        if (items.isEmpty()) {
            updateLoadingMessage(getLoadingMessage());
            showLoading();
        }

        providerManager.getProvider(providerId).items(filter, new Pager(endCursor))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<ItemsWrapper>() {
                    @Override public void onSubscribe(final Disposable d) {
                        listCall = d;
                    }

                    @Override public void onSuccess(final ItemsWrapper value) {
                        Paging paging = value.getPaging();
                        view.addItems(value.getMedia(), !paging.getHasNextPage(), paging.getEndCursor());
                        items.addAll(value.getMedia());
                        showLoaded();
                    }

                    @Override public void onError(final Throwable e) {
                        // TODO: 6/24/17 Most of this is not relevant any more
                        if (e.getMessage().equals("Canceled")) {
                            showLoaded();
                        } else if (e.getMessage() != null
                                && e.getMessage().equals(ButterApplication.getAppContext().getString(R.string.movies_error))) {
                            view.addItems(null, false, null);
                            showLoaded();
                        } else {
                            Timber.e(e.getMessage());
                            view.showErrorMessage(R.string.unknown_error);
                            showLoaded();
                        }
                    }
                });
    }

    @Override public void onMediaItemClicked(final Media media) {
        view.showMediaLoadingDialog();

        cancelDetailsCall();
        providerManager.getProvider(providerId).detail(media)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<Media>() {
                    @Override public void onSubscribe(final Disposable d) {

                    }

                    @Override public void onSuccess(final Media value) {
                        view.showDetails(value);
                    }

                    @Override public void onError(final Throwable e) {
                        view.showErrorMessage(R.string.unknown_error);
                    }
                });
    }

    @StringRes protected abstract int getLoadingMessage();

    @Override public void onDestroy() {
        cancelListCall();
        cancelDetailsCall();
    }

    protected void cancelListCall() {
        if (listCall != null) {
            listCall.dispose();
            listCall = null;
        }
    }

    protected void cancelDetailsCall() {
        if (detailsCall != null) {
            detailsCall.dispose();
            detailsCall = null;
        }
    }

    private void showLoading() {
        view.showLoading();
    }

    protected void showLoaded() {
        if (!items.isEmpty()) {
            showData();
        } else {
            showEmpty();
        }
    }

    private void updateLoadingMessage(@StringRes int messageRes) {
        view.updateLoadingMessage(messageRes);
    }

    private void showData() {
        view.showData();
    }

    private void showEmpty() {
        view.showEmpty();
    }

}
