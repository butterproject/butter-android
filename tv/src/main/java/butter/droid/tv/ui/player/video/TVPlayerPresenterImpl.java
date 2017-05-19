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
import butter.droid.base.content.preferences.PreferencesHandler;
import butter.droid.base.manager.internal.beaming.BeamManager;
import butter.droid.base.manager.internal.provider.ProviderManager;
import butter.droid.base.manager.internal.vlc.PlayerManager;
import butter.droid.base.manager.internal.vlc.VlcPlayer;
import butter.droid.base.manager.prefs.PrefManager;
import butter.droid.base.ui.player.stream.StreamPlayerPresenterImpl;

public class TVPlayerPresenterImpl extends StreamPlayerPresenterImpl implements TVPlayerPresenter {

    private final TVPlayerView view;

    public TVPlayerPresenterImpl(final TVPlayerView view, final Context context, final PrefManager prefManager,
            final PreferencesHandler preferencesHandler, final ProviderManager providerManager, final PlayerManager playerManager,
            final BeamManager beamManager, final VlcPlayer vlcPlayer) {
        super(view, context, prefManager, preferencesHandler, providerManager, playerManager, beamManager, vlcPlayer);

        this.view = view;
    }

    @Override protected void startBeamPlayerActivity() {

    }

}
