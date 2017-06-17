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

package butter.droid.tv.ui.detail.base;

import android.support.annotation.CallSuper;
import android.support.annotation.MainThread;
import android.support.annotation.Nullable;
import butter.droid.base.manager.internal.provider.ProviderManager;
import butter.droid.provider.base.Media;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class TVBaseDetailsPresenterImpl implements TVBaseDetailsPresenter {

    protected static final int ACTION_TRAILER = -1;

    private final TVBaseDetailView view;
    private final ProviderManager providerManager;

    private Media item;
    private int providerId; // TODO: 6/17/17 This should probably go in some base class that would include both provider and media

    @Nullable private Disposable detailsRequest;

    public TVBaseDetailsPresenterImpl(final TVBaseDetailView view, final ProviderManager providerManager) {
        this.view = view;
        this.providerManager = providerManager;
    }


    @CallSuper protected void onCreate(final int providerId, final Media item) {
        this.providerId = providerId;
        this.item = item;

        view.initData(item);
        loadDetails();
    }

    @Override @CallSuper public void onDestroy() {
        if (detailsRequest != null) {
            detailsRequest.dispose();
            detailsRequest = null;
        }
    }

    @Override public void actionClicked(final long actionId) {
        // override if needed
    }

    @CallSuper @MainThread protected void detailsLoaded(Media media) {
        view.updateOverview(media);
    }

    private void loadDetails() {
        providerManager.getProvider(providerId)
                .detail(item)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<Media>() {
                    @Override public void onSubscribe(final Disposable d) {
                        detailsRequest = d;
                    }

                    @Override public void onSuccess(final Media value) {
                        item = value;

                        detailsLoaded(value);
                    }

                    @Override public void onError(final Throwable e) {
                        // TODO: 5/25/17 Show error message
                    }
                });
    }

}
