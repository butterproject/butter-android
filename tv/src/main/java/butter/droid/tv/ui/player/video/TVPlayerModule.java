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

package butter.droid.tv.ui.player.video;

import android.content.Context;
import android.support.annotation.Nullable;
import android.view.WindowManager;
import butter.droid.base.content.preferences.PreferencesHandler;
import butter.droid.base.manager.internal.provider.ProviderManager;
import butter.droid.base.manager.internal.subtitle.SubtitleManager;
import butter.droid.base.manager.internal.vlc.PlayerManager;
import butter.droid.base.manager.internal.vlc.VlcPlayer;
import butter.droid.base.ui.FragmentScope;
import dagger.Module;
import dagger.Provides;
import org.videolan.libvlc.LibVLC;

@Module(includes = TVPlayerBindModule.class)
public class TVPlayerModule {

    @Provides @FragmentScope TVPlayerPresenter providePresenter(TVPlayerView view, Context context, PreferencesHandler preferencesHandler,
            ProviderManager providerManager, PlayerManager playerManager, VlcPlayer vlcPlayer, SubtitleManager subtitleManager) {
        return new TVPlayerPresenterImpl(view, context, preferencesHandler, providerManager, playerManager, vlcPlayer, subtitleManager);
    }

    @Provides @FragmentScope VlcPlayer provideVlcPlayer(@Nullable LibVLC libVLC, WindowManager windowManager) {
        return new VlcPlayer(libVLC, windowManager);
    }

}
