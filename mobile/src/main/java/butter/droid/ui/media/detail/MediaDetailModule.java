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

package butter.droid.ui.media.detail;

import butter.droid.base.content.preferences.PreferencesHandler;
import butter.droid.base.manager.network.NetworkManager;
import butter.droid.base.ui.ActivityScope;
import butter.droid.base.ui.FragmentScope;
import butter.droid.ui.media.detail.MediaDetailModule.MediaDetailBindModule;
import butter.droid.ui.media.detail.movie.MovieDetailFragment;
import butter.droid.ui.media.detail.movie.MovieDetailModule;
import butter.droid.ui.media.detail.show.ShowDetailFragment;
import butter.droid.ui.media.detail.show.ShowDetailModule;
import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import dagger.android.ContributesAndroidInjector;

@Module(includes = MediaDetailBindModule.class)
public class MediaDetailModule {

    @Provides @ActivityScope MediaDetailPresenter provideMediaDetailPresenter(MediaDetailView view,
            PreferencesHandler preferencesHandler, NetworkManager networkManager) {
        return new MediaDetailPresenterImpl(view, preferencesHandler, networkManager);
    }

    @Module
    public interface MediaDetailBindModule {
        @Binds MediaDetailView bindView(MediaDetailActivity activity);

        @FragmentScope
        @ContributesAndroidInjector(modules = MovieDetailModule.class)
        MovieDetailFragment contributeMovieDetailFragmentInjector();

        @FragmentScope
        @ContributesAndroidInjector(modules = ShowDetailModule.class)
        ShowDetailFragment contributeShowDetailFragmentInjector();
    }
}
