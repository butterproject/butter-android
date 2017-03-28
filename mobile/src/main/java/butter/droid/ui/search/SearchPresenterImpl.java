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

package butter.droid.ui.search;

import butter.droid.R;
import butter.droid.base.content.preferences.PreferencesHandler;
import butter.droid.base.manager.network.NetworkManager;
import butter.droid.base.manager.provider.ProviderManager;
import butter.droid.base.utils.StringUtils;
import butter.droid.ui.media.list.base.BaseMediaListPresenterImpl;
import okhttp3.OkHttpClient;

public class SearchPresenterImpl extends BaseMediaListPresenterImpl implements SearchPresenter {

    private final SearchView view;
    private final NetworkManager networkManager;

    public SearchPresenterImpl(SearchView view, ProviderManager providerManager, OkHttpClient client,
            PreferencesHandler preferencesHandler, NetworkManager networkManager) {
        super(view, providerManager, client, preferencesHandler);
        this.view = view;
        this.networkManager = networkManager;
    }

    public void triggerSearch(String searchQuery) {

        if (!networkManager.isNetworkConnected()) {
            view.showErrorMessage(R.string.network_message);
        } else {
            items.clear();

            if (StringUtils.isEmpty(searchQuery)) {
                view.clearAdapter();
                showLoaded();
            } else {
                filters.keywords = searchQuery;
                view.refreshAdapter();
            }
        }
    }

    @Override protected int getLoadingMessage() {
        return R.string.searching;
    }
}
