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

package butter.droid.ui.player;

import android.app.Activity;
import android.content.ContentResolver;
import butter.droid.base.ui.ActivityScope;
import butter.droid.base.ui.FragmentScope;
import butter.droid.ui.player.VideoPlayerModule.VideoPlayerBindModule;
import butter.droid.ui.player.stream.PlayerFragment;
import butter.droid.ui.player.stream.PlayerModule;
import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import dagger.android.ContributesAndroidInjector;

@Module(includes = VideoPlayerBindModule.class)
public class VideoPlayerModule {

    @Provides @ActivityScope VideoPlayerPresenter providePresenter(VideoPlayerView view, ContentResolver contentResolver) {
        return new VideoPlayerPresenterImpl(view, contentResolver);
    }

    @Module
    public interface VideoPlayerBindModule {
        @Binds VideoPlayerView bindView(VideoPlayerActivity activity);

        @Binds Activity bindActivity(VideoPlayerActivity activity);

        @FragmentScope
        @ContributesAndroidInjector(modules = PlayerModule.class)
        PlayerFragment contributePlayerFragmentInjector();
    }

}
