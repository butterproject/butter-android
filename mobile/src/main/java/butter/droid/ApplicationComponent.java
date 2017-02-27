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

package butter.droid;

import javax.inject.Singleton;

import butter.droid.activities.BeamPlayerActivity;
import butter.droid.activities.MediaDetailActivity;
import butter.droid.activities.SearchActivity;
import butter.droid.activities.TrailerPlayerActivity;
import butter.droid.activities.VideoPlayerActivity;
import butter.droid.base.BaseApplicationComponent;
import butter.droid.fragments.BeamPlayerFragment;
import butter.droid.fragments.MediaContainerFragment;
import butter.droid.fragments.MediaGenreSelectionFragment;
import butter.droid.fragments.MediaListFragment;
import butter.droid.fragments.MovieDetailFragment;
import butter.droid.fragments.NavigationDrawerFragment;
import butter.droid.fragments.VideoPlayerFragment;
import butter.droid.fragments.dialog.BeamDeviceSelectorDialogFragment;
import butter.droid.fragments.dialog.EpisodeDialogFragment;
import butter.droid.fragments.dialog.LoadingDetailDialogFragment;
import butter.droid.ui.about.AboutActivity;
import butter.droid.ui.about.AboutComponent;
import butter.droid.ui.loading.StreamLoadingComponent;
import butter.droid.ui.main.MainComponent;
import butter.droid.ui.preferences.PreferencesComponent;
import butter.droid.ui.terms.TermsComponent;
import dagger.Component;

@Singleton @Component(
        modules = ApplicationModule.class
)
public interface ApplicationComponent extends BaseApplicationComponent {

    void inject(MobileButterApplication application);

    void inject(TrailerPlayerActivity activity);
    void inject(AboutActivity activity);
    void inject(BeamPlayerActivity activity);
    void inject(MediaDetailActivity activity);
    void inject(SearchActivity activity);
    void inject(VideoPlayerActivity activity);

    void inject(NavigationDrawerFragment fragment);
    void inject(MediaContainerFragment fragment);
    void inject(MediaListFragment fragment);
    void inject(MediaGenreSelectionFragment fragment);
    void inject(LoadingDetailDialogFragment fragment);
//    void inject(StreamLoadingFragment fragment);
    void inject(EpisodeDialogFragment fragment);
    void inject(MovieDetailFragment fragment);
    void inject(VideoPlayerFragment fragment);
    void inject(BeamPlayerFragment fragment);
    void inject(BeamDeviceSelectorDialogFragment fragment);

    MainComponent.Builder mainComponentBuilder();
    PreferencesComponent.Builder preferencesComponentBuilder();
    TermsComponent.Builder termsComponentBuilder();
    AboutComponent.Builder aboutComponentBuilder();
    StreamLoadingComponent.Builder streamLoadingComponentBuilder();

}
