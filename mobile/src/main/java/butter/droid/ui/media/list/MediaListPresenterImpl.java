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

package butter.droid.ui.media.list;

import butter.droid.base.content.preferences.PreferencesHandler;
import butter.droid.base.manager.provider.ProviderManager;
import butter.droid.base.utils.ProviderUtils;
import butter.droid.ui.media.list.base.BaseMediaListPresenterImpl;
import okhttp3.OkHttpClient;

public class MediaListPresenterImpl extends BaseMediaListPresenterImpl implements MediaListPresenter {

    private final MediaListView view;
    private final ProviderManager providerManager;

    public MediaListPresenterImpl(MediaListView view,
            ProviderManager providerManager, OkHttpClient client,
            PreferencesHandler preferencesHandler) {
        super(view, providerManager, client, preferencesHandler);
        this.view = view;
        this.providerManager = providerManager;
    }

    public void changeGenre(String genre) {
        if (!(filters.genre == null ? "" : filters.genre).equals(genre == null ? "" : genre)) {
            items.clear();
            view.refreshAdapter();
        }
    }

    @Override protected int getLoadingMessage() {
        return ProviderUtils.getProviderLoadingMessage(providerManager.getCurrentMediaProviderType());
    }
}
