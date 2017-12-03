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

package butter.droid.tv.ui.preferences;

import android.content.Context;
import android.content.res.Resources;

import butter.droid.base.content.preferences.PreferencesHandler;
import butter.droid.base.manager.prefs.PrefManager;
import butter.droid.base.manager.internal.updater.ButterUpdateManager;
import butter.droid.base.manager.internal.vlc.PlayerManager;
import butter.droid.base.ui.FragmentScope;
import butter.droid.tv.ui.preferences.TVPreferencesModule.TVPreferencesBindModule;
import dagger.Binds;
import dagger.Module;
import dagger.Provides;

@Module(includes = TVPreferencesBindModule.class)
public class TVPreferencesModule {

    @FragmentScope @Provides TVPreferencesPresenter providePresenter(TVPreferencesView view, Context context,
            PreferencesHandler preferencesHandler, Resources resources, PrefManager prefManager,
            PlayerManager playerManager, ButterUpdateManager updateManager) {
        return new TVPreferencesPresenterImpl(view, context, preferencesHandler, resources, prefManager,
                playerManager, updateManager);
    }

    @Module
    public interface TVPreferencesBindModule {
        @Binds TVPreferencesView bindView(TVPreferencesFragment fragment);
    }

}
