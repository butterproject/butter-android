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

package butter.droid.tv.ui.trailer.fragment;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.Nullable;
import android.view.WindowManager;
import butter.droid.base.content.preferences.PreferencesHandler;
import butter.droid.base.manager.internal.beaming.BeamManager;
import butter.droid.base.manager.internal.phone.PhoneManager;
import butter.droid.base.manager.internal.provider.ProviderManager;
import butter.droid.base.manager.internal.vlc.PlayerManager;
import butter.droid.base.manager.internal.vlc.VlcPlayer;
import butter.droid.base.manager.internal.youtube.YouTubeManager;
import butter.droid.base.manager.network.NetworkManager;
import butter.droid.base.manager.prefs.PrefManager;
import butter.droid.base.ui.FragmentScope;
import dagger.Module;
import dagger.Provides;
import org.videolan.libvlc.LibVLC;

@Module(includes = TVTrailerPlayerBindModule.class)
public class TVTrailerPlayerModule {

    private final TVTrailerPlayerView view;
    private final Activity activity;

    public TVTrailerPlayerModule(final TVTrailerPlayerView view, final Activity activity) {
        this.view = view;
        this.activity = activity;
    }

    @Provides @FragmentScope TVTrailerPlayerView provideView() {
        return view;
    }

    @Provides @FragmentScope TVTrailerPlayerPresenter providePresenter(TVTrailerPlayerView view, Context context, PrefManager prefManager,
            PreferencesHandler preferencesHandler, ProviderManager providerManager, PlayerManager playerManager, BeamManager beamManager,
            VlcPlayer player, YouTubeManager youTubeManager, NetworkManager networkManager, PhoneManager phoneManager) {
        return new TVTrailerPlayerPresenterImpl(view, context, prefManager, preferencesHandler, providerManager, playerManager,
                beamManager, player, youTubeManager, networkManager, phoneManager);
    }

    @Provides @FragmentScope Activity provideActivity() {
        return activity;
    }

    @Provides @FragmentScope VlcPlayer provideVlcPlayer(@Nullable LibVLC libVLC, WindowManager windowManager) {
        return new VlcPlayer(libVLC, windowManager);
    }

}
