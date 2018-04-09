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

package butter.droid.ui.media.detail.streamable;

import android.content.res.Resources;

import butter.droid.base.content.preferences.PreferencesHandler;
import butter.droid.base.manager.internal.media.MediaDisplayManager;
import butter.droid.base.manager.internal.provider.ProviderManager;
import butter.droid.base.manager.internal.youtube.YouTubeManager;
import butter.droid.base.ui.FragmentScope;
import butter.droid.ui.media.detail.MediaDetailPresenter;
import butter.droid.ui.media.detail.streamable.StreamableDetailModule.MovieDetailBindModule;
import dagger.Binds;
import dagger.Module;
import dagger.Provides;

@Module(includes = MovieDetailBindModule.class)
public class StreamableDetailModule {

    @Provides @FragmentScope StreamableDetailPresenter providePresenter(StreamableDetailView view,
            MediaDetailPresenter parentPresenter, YouTubeManager youTubeManager, PreferencesHandler preferencesHandler,
            ProviderManager providerManager, Resources resources, MediaDisplayManager mediaDisplayManager) {
        return new StreamableDetailPresenterImpl(view, parentPresenter, youTubeManager, preferencesHandler, providerManager,
                resources, mediaDisplayManager);
    }

    @Module
    public interface MovieDetailBindModule {
        @Binds StreamableDetailView bindView(StreamableDetailFragment fragment);
    }

}
