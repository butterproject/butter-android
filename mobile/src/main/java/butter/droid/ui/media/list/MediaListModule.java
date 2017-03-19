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
import butter.droid.base.ui.FragmentScope;
import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;

@Module(includes = MediaListBindModule.class)
public class MediaListModule {

    private final MediaListView view;

    public MediaListModule(MediaListView view) {
        this.view = view;
    }

    @Provides @FragmentScope MediaListView provideView() {
        return view;
    }

    @Provides @FragmentScope MediaListPresenter providePresenter(MediaListView view, ProviderManager providerManager,
            OkHttpClient client, PreferencesHandler preferencesHandler) {
        return new MediaListPresenterImpl(view, providerManager, client, preferencesHandler, parentPresenter);
    }
}
