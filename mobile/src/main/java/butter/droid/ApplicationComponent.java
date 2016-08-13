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

import butter.droid.activities.MainActivity;
import butter.droid.fragments.MediaContainerFragment;
import butter.droid.fragments.MediaGenreSelectionFragment;
import butter.droid.fragments.MediaListFragment;
import butter.droid.fragments.MovieDetailFragment;
import butter.droid.fragments.NavigationDrawerFragment;
import butter.droid.fragments.StreamLoadingFragment;
import butter.droid.fragments.dialog.EpisodeDialogFragment;
import butter.droid.fragments.dialog.LoadingDetailDialogFragment;
import dagger.Component;

@Singleton @Component(
        modules = ApplicationModule.class
)
public interface ApplicationComponent {

    void inject(MobileButterApplication application);

    void inject(MainActivity activity);

    void inject(NavigationDrawerFragment fragment);
    void inject(MediaContainerFragment fragment);
    void inject(MediaListFragment fragment);
    void inject(MediaGenreSelectionFragment fragment);
    void inject(LoadingDetailDialogFragment fragment);
    void inject(StreamLoadingFragment fragment);
    void inject(EpisodeDialogFragment fragment);
    void inject(MovieDetailFragment fragment);

}
