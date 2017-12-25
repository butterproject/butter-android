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

package butter.droid.ui.trailer;

import android.content.Context;
import android.support.annotation.Nullable;
import butter.droid.base.content.preferences.PreferencesHandler;
import butter.droid.base.manager.internal.phone.PhoneManager;
import butter.droid.base.manager.internal.vlc.VlcPlayer;
import butter.droid.base.manager.internal.youtube.YouTubeManager;
import butter.droid.base.manager.network.NetworkManager;
import butter.droid.base.ui.FragmentScope;
import butter.droid.manager.internal.audio.AudioManager;
import butter.droid.manager.internal.brightness.BrightnessManager;
import butter.droid.ui.player.VideoPlayerTouchHandler;
import dagger.Module;
import dagger.Provides;
import org.videolan.libvlc.LibVLC;

@Module(includes = TrailerPlayerBindModule.class)
public class TrailerPlayerModule {

    @Provides @FragmentScope TrailerPlayerPresenter providePresenter(TrailerPlayerView view, Context context,
            PreferencesHandler preferencesHandler, AudioManager audioManager, BrightnessManager brightnessManager,
            VideoPlayerTouchHandler touchHandler, VlcPlayer player, YouTubeManager youTubeManager, NetworkManager networkManager,
            PhoneManager phoneManager) {
        return new TrailerPlayerPresenterImpl(view, context, preferencesHandler, player, youTubeManager, networkManager,
                phoneManager, brightnessManager, touchHandler, audioManager);
    }

    @Provides @FragmentScope VlcPlayer provideVlcPlayer(@Nullable LibVLC libVLC) {
        return new VlcPlayer(libVLC);
    }

}
