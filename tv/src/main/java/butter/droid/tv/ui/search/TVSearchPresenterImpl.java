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

import java.util.ArrayList;
import java.util.List;

import butter.droid.base.manager.provider.ProviderManager;
import butter.droid.base.providers.media.MediaProvider;
import butter.droid.base.providers.media.models.Media;
import butter.droid.base.utils.ThreadUtils;
import butter.droid.tv.R;
import butter.droid.tv.presenters.MediaCardPresenter;
import butter.droid.tv.presenters.MediaCardPresenter.MediaCardItem;
import timber.log.Timber;

public class TVSearchPresenterImpl implements TVSearchPresenter {

    private static final int SEARCH_DELAY_MS = 300;

    private final TVSearchView view;
    private final ProviderManager providerManager;

    private final SearchRunnable delayedLoad = new SearchRunnable();
    private final MediaProvider.Filters searchFilter = new MediaProvider.Filters();
    private final Handler handler = new Handler(Looper.getMainLooper());

    public TVSearchPresenterImpl(TVSearchView view, ProviderManager providerManager) {
        this.view = view;
        this.providerManager = providerManager;
    }

    @Override public void onTextChanged(String newQuery) {
        if (newQuery.length() > 3) {
            queryByWords(newQuery);
        }
    }

    @Override public void onTextSubmitted(String query) {
        queryByWords(query);
    }

    private void queryByWords(String words) {
        view.clearData();
        if (!TextUtils.isEmpty(words)) {
            delayedLoad.setSearchQuery(words);
            handler.removeCallbacks(delayedLoad);
            handler.postDelayed(delayedLoad, SEARCH_DELAY_MS);
        }
    }


    private void loadRows(String query) {
        view.clearData();
        view.showLoadingRow();

        searchFilter.keywords = query;
        searchFilter.page = 1;

        if (providerManager.hasProvider(ProviderManager.PROVIDER_TYPE_SHOW)) {
            MediaProvider mediaProvider = providerManager.getMediaProvider(ProviderManager.PROVIDER_TYPE_SHOW);
            //noinspection ConstantConditions
            mediaProvider.cancel();
            mediaProvider.getList(searchFilter, new MediaProvider.Callback() {
                @Override
                public void onSuccess(MediaProvider.Filters filters, final ArrayList<Media> items, boolean changed) {
                    ThreadUtils.runOnUiThread(new Runnable() {
                        @Override public void run() {
                            List<MediaCardPresenter.MediaCardItem> list = MediaCardPresenter.convertMediaToOverview(
                                    items);
                            view.addRow(R.string.show_results, list);
                        }
                    });
                }

                @Override public void onFailure(Exception e) {

                }
            });
        }


        if (providerManager.hasProvider(ProviderManager.PROVIDER_TYPE_MOVIE)) {
            MediaProvider mediaProvider = providerManager.getMediaProvider(ProviderManager.PROVIDER_TYPE_MOVIE);
            //noinspection ConstantConditions
            mediaProvider.cancel();
            mediaProvider.getList(searchFilter, new MediaProvider.Callback() {
                        @Override
                        public void onSuccess(MediaProvider.Filters filters, final ArrayList<Media> items, boolean changed) {
                            ThreadUtils.runOnUiThread(new Runnable() {
                                @Override public void run() {
                                    List<MediaCardItem> list = MediaCardPresenter.convertMediaToOverview(items);
                                    view.addRow(R.string.movie_results, list);
                                }
                            });
                        }

                        @Override public void onFailure(Exception e) {

                        }
                    }

            );
        }

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
