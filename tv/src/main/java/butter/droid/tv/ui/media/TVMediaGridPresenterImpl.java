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

import butter.droid.base.manager.internal.provider.ProviderManager;
import butter.droid.base.providers.media.MediaProvider;
import butter.droid.base.providers.media.MediaProvider.Filters;
import butter.droid.base.providers.media.models.Media;
import butter.droid.base.utils.ThreadUtils;
import butter.droid.tv.presenters.MediaCardPresenter;
import butter.droid.tv.presenters.MediaCardPresenter.MediaCardItem;
import java.util.ArrayList;
import java.util.List;
import okhttp3.Call;

public class TVMediaGridPresenterImpl implements TVMediaGridPresenter {

    private final TVMediaGridView view;
    private final ProviderManager providerManager;

    private final List<MediaCardPresenter.MediaCardItem> items = new ArrayList<>();

    private Filters filter;
    private int currentPage = 1;

    private Call currentCall;

    public TVMediaGridPresenterImpl(final TVMediaGridView view, final ProviderManager providerManager) {
        this.view = view;
        this.providerManager = providerManager;
    }

    @Override public void onCreate(final Filters filter) {
        this.filter = filter;
    }

    @Override public void onActivityCreated() {

    }

    @Override public void loadNextPage() {
        filter.page++;
        loadItems();
    }

    @Override public void onDestroy() {
        if (currentCall !=  null) {
            currentCall.cancel();
            currentCall = null;
        }
    }

    private void loadItems() {
        currentCall = providerManager.getCurrentMediaProvider().getList(null, filter, new MediaProvider.Callback() {
            @Override
            public void onSuccess(MediaProvider.Filters filters, ArrayList<Media> items, boolean changed) {
                currentPage = filters.page;
                final List<MediaCardItem> list = MediaCardPresenter.convertMediaToOverview(items);

                TVMediaGridPresenterImpl.this.items.addAll(list);

                ThreadUtils.runOnUiThread(new Runnable() {
                    @Override public void run() {
                        view.appendItems(list);
                    }
                });
            }

            @Override
            public void onFailure(Exception ex) {
                ThreadUtils.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        view.displayError("Error getting show list");
                    }
                });
            }
        });
    }
}
