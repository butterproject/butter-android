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

package butter.droid.tv.ui.detail;

import android.app.Activity;
import butter.droid.base.ui.ActivityScope;
import butter.droid.base.ui.FragmentScope;
import butter.droid.tv.manager.internal.background.BackgroundUpdaterModule;
import butter.droid.tv.ui.detail.TVMediaDetailModule.TVMediaDetailBindModule;
import butter.droid.tv.ui.detail.movie.TVMovieDetailsFragment;
import butter.droid.tv.ui.detail.movie.TVMovieDetailsModule;
import butter.droid.tv.ui.detail.show.TVShowDetailModule;
import butter.droid.tv.ui.detail.show.TVShowDetailsFragment;
import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import dagger.android.ContributesAndroidInjector;

@Module(includes = {
        BackgroundUpdaterModule.class,
        TVMediaDetailBindModule.class}
)
public class TVMediaDetailModule {

    @Provides @ActivityScope TVMediaDetailPresenter providePresenter(TVMediaDetailView view) {
        return new TVMediaDetailPresenterImpl(view);
    }

    @Module
    public interface TVMediaDetailBindModule {

        @Binds TVMediaDetailView bindView(TVMediaDetailActivity activity);

        @Binds Activity bindActivity(TVMediaDetailActivity activity);

        @FragmentScope
        @ContributesAndroidInjector(modules = TVMovieDetailsModule.class)
        TVMovieDetailsFragment contributeTVMovieDetailFragmentInjector();

        @FragmentScope
        @ContributesAndroidInjector(modules = TVShowDetailModule.class)
        TVShowDetailsFragment contributeTVShowDetailFragmentInjector();

    }

}
