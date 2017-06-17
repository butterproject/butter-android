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

import android.support.annotation.StringRes;
import butter.droid.R;
import butter.droid.base.ButterApplication;
import butter.droid.base.content.preferences.PreferencesHandler;
import butter.droid.base.manager.internal.provider.ProviderManager;
import butter.droid.base.providers.media.MediaProvider;
import butter.droid.provider.base.ItemsWrapper;
import butter.droid.provider.base.Media;
import butter.droid.provider.base.filter.Filter;
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
    protected final MediaProvider.Filters filters = new MediaProvider.Filters();

    private int providerId;

    protected Disposable currentCall;

    public BaseMediaListPresenterImpl(BaseMediaListView view, ProviderManager providerManager, PreferencesHandler preferencesHandler) {
        this.view = view;
        this.providerManager = providerManager;
        this.preferencesHandler = preferencesHandler;
    }

    @Override public void onActivityCreated(final int providerId, final Filter filter) {
        this.providerId = providerId;
        // TODO: 6/17/17  2
        //        filters.sort = sort;
//        filters.order = sortOrder;
//        filters.genre = genre;
//        filters.langCode = LocaleUtils.toLocale(preferencesHandler.getLocale()).getLanguage();
    }

    @Override public void loadNextPage(int page) {
        cancelOngoingCall();

        if (items.isEmpty()) {
            updateLoadingMessage(getLoadingMessage());
            showLoading();
        }

        filters.page = page;
        providerManager.getProvider(providerId).items(null)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<ItemsWrapper>() {
                    @Override public void onSubscribe(final Disposable d) {
                        currentCall = d;
                    }

                    @Override public void onSuccess(final ItemsWrapper value) {
                        view.addItems(value.getMedia());
                        showLoaded();
                    }

                    @Override public void onError(final Throwable e) {
                        if (e.getMessage().equals("Canceled")) {
                            showLoaded();
                        } else if (e.getMessage() != null
                                && e.getMessage().equals(ButterApplication.getAppContext().getString(R.string.movies_error))) {
                            view.addItems(null);
                            showLoaded();
                        } else {
                            Timber.e(e.getMessage());
                            view.showErrorMessage(R.string.unknown_error);
                            showLoaded();
                        }
                    }
                });
    }

    @Override public ArrayList<Media> getCurrentList() {
        return items;
    }

    @StringRes protected abstract int getLoadingMessage();

    protected void cancelOngoingCall() {
        if (currentCall != null) {
            currentCall.dispose();
            currentCall = null;
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
