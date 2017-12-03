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

package butter.droid.ui.preferences;

import android.content.res.Resources;

import butter.droid.base.content.preferences.PreferencesHandler;
import butter.droid.base.manager.prefs.PrefManager;
import butter.droid.base.manager.internal.updater.ButterUpdateManager;
import butter.droid.base.manager.internal.vlc.PlayerManager;
import butter.droid.base.ui.ActivityScope;
import butter.droid.ui.preferences.PreferencesModule.PreferencesBindModule;
import dagger.Binds;
import dagger.Module;
import dagger.Provides;

@Module(includes = PreferencesBindModule.class)
public class PreferencesModule {

    @Provides @ActivityScope PreferencesPresenter providePresenter(PreferencesView view,
            PrefManager prefManager, PreferencesHandler preferencesHandler, Resources resources,
            PlayerManager playerManager, ButterUpdateManager updateManager) {
        return new PreferencesPresenterImpl(view, prefManager, preferencesHandler, resources, playerManager,
                updateManager);
    }

    @Module public interface PreferencesBindModule {

        @Binds PreferencesView bindView(PreferencesActivity activity);

    }
}
