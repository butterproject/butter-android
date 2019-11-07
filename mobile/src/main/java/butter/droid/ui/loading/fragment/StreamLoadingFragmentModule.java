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

package butter.droid.ui.loading.fragment;

import android.content.Context;

import androidx.annotation.Nullable;
import butter.droid.base.manager.internal.beaming.BeamManager;
import butter.droid.base.manager.internal.provider.ProviderManager;
import butter.droid.base.manager.internal.subtitle.SubtitleManager;
import butter.droid.base.manager.internal.vlc.PlayerManager;
import butter.droid.base.ui.FragmentScope;
import dagger.Module;
import dagger.Provides;

@Module(
        includes = StreamLoadingFragmentBindModule.class
)
public class StreamLoadingFragmentModule {

    @Provides @FragmentScope public StreamLoadingFragmentPresenter providePresenter(StreamLoadingFragmentView view,
            ProviderManager providerManager, SubtitleManager subtitleManager, PlayerManager playerManager, Context context,
            @Nullable BeamManager beamManager) {
        return new StreamLoadingFragmentPresenterImpl(view, providerManager, subtitleManager, playerManager, context, beamManager);
    }
}
