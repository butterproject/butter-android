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

import java.util.ArrayList;

import butter.droid.R;
import butter.droid.base.ButterApplication;
import butter.droid.base.content.preferences.PreferencesHandler;
import butter.droid.base.manager.internal.provider.ProviderManager;
import butter.droid.base.providers.media.MediaProvider;
import butter.droid.base.providers.media.MediaProvider.Filters.Order;
import butter.droid.base.providers.media.MediaProvider.Filters.Sort;
import butter.droid.base.providers.media.models.Media;
import butter.droid.base.utils.LocaleUtils;
import butter.droid.base.utils.ThreadUtils;
import hugo.weaving.DebugLog;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import timber.log.Timber;

public abstract class BaseMediaListPresenterImpl implements BaseMediaListPresenter {

    private final BaseMediaListView view;
    private final ProviderManager providerManager;
    private final OkHttpClient client;
    private final PreferencesHandler preferencesHandler;

    protected final ArrayList<Media> items = new ArrayList<>();
    protected final MediaProvider.Filters filters = new MediaProvider.Filters();

    protected Call currentCall;

    public BaseMediaListPresenterImpl(BaseMediaListView view, ProviderManager providerManager, OkHttpClient client,
            PreferencesHandler preferencesHandler) {
        this.view = view;
        this.providerManager = providerManager;
        this.client = client;
        this.preferencesHandler = preferencesHandler;
    }

    @Override public void onActivityCreated(Sort sort, Order sortOrder, String genre) {
        filters.sort = sort;
        filters.order = sortOrder;
        filters.genre = genre;
        filters.langCode = LocaleUtils.toLocale(preferencesHandler.getLocale()).getLanguage();
    }

    @Override public void loadNextPage(int page) {

        cancelOngoingCall();

        if (items.isEmpty()) {
            updateLoadingMessage(getLoadingMessage());
            showLoading();
        }


        filters.page = page;
        currentCall = providerManager.getCurrentMediaProvider()
                .getList(items, new MediaProvider.Filters(filters), callback);

    }

    @Override public ArrayList<Media> getCurrentList() {
        return items;
    }

    @StringRes protected abstract int getLoadingMessage();

    protected void cancelOngoingCall() {
        if (currentCall != null) {
            final Call call = currentCall;
            client.dispatcher().executorService().execute(new Runnable() {
                @Override
                public void run() {
                    call.cancel();
                }
            });
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

    protected final MediaProvider.Callback callback = new MediaProvider.Callback() {
        @Override
        @DebugLog
        public void onSuccess(MediaProvider.Filters filters, final ArrayList<Media> items, boolean changed) {
            ArrayList<Media> allItems = BaseMediaListPresenterImpl.this.items;
            allItems.addAll(items);

            ThreadUtils.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    view.addItems(items);
                    showLoaded();
                }
            });
        }

        @Override
        @DebugLog
        public void onFailure(Exception e) {
            if (e.getMessage().equals("Canceled")) {
                ThreadUtils.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showLoaded();
                    }
                });
            } else if (e.getMessage() != null && e.getMessage().equals(
                    ButterApplication.getAppContext().getString(R.string.movies_error))) {
                ThreadUtils.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        view.addItems(null);
                        showLoaded();

                    }
                });
            } else {
                Timber.e(e.getMessage());
                ThreadUtils.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        view.showErrorMessage(R.string.unknown_error);
                        showLoaded();
                    }
                });
            }
        }
    };


}
