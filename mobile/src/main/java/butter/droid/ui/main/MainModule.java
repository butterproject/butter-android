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

package butter.droid.ui.main;

import android.content.Context;

import butter.droid.base.manager.beaming.BeamManager;
import butter.droid.base.manager.prefs.PrefManager;
import butter.droid.base.manager.provider.ProviderManager;
import butter.droid.base.manager.youtube.YouTubeManager;
import butter.droid.ui.ActivityScope;
import dagger.Module;
import dagger.Provides;

@Module
public class MainModule {

    private final MainView view;

    public MainModule(MainView view) {
        this.view = view;
    }

    @Provides @ActivityScope MainView provideView() {
        return view;
    }

    @Provides @ActivityScope MainPresenter providePresenter(MainView view, YouTubeManager youTubeManager,
            ProviderManager providerManager, BeamManager beamManager, Context context, PrefManager prefManager) {
        return new MainPresenterImpl(view, youTubeManager, providerManager, beamManager, context, prefManager);
    }
}

