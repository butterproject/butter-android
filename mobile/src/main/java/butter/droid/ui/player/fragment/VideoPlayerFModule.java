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

package butter.droid.ui.player.fragment;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.support.annotation.Nullable;
import butter.droid.base.content.preferences.PreferencesHandler;
import butter.droid.base.manager.internal.beaming.BeamManager;
import butter.droid.base.manager.internal.provider.ProviderManager;
import butter.droid.base.manager.internal.vlc.PlayerManager;
import butter.droid.base.manager.prefs.PrefManager;
import butter.droid.base.ui.FragmentScope;
import dagger.Module;
import dagger.Provides;
import org.videolan.libvlc.LibVLC;

@Module(includes = VideoPlayerFBindModule.class)
public class VideoPlayerFModule {

    private final VideoPlayerFView view;
    private final Activity activity;

    public VideoPlayerFModule(final VideoPlayerFView view, final Activity activity) {
        this.view = view;
        this.activity = activity;
    }

    @Provides @FragmentScope VideoPlayerFView provideView() {
        return view;
    }

    @Provides @FragmentScope VideoPlayerFPresenter providePresenter(VideoPlayerFView view, Context context, PrefManager prefManager,
            @Nullable LibVLC libVLC, PreferencesHandler preferencesHandler, ProviderManager providerManager, PlayerManager playerManager,
            BeamManager beamManager, AudioManager audioManager) {
        return new VideoPlayerFPresenterImpl(view, context, prefManager, libVLC, preferencesHandler, providerManager, playerManager,
                beamManager, audioManager);
    }

    @Provides @FragmentScope Activity provideActivity() {
        return activity;
    }

}
