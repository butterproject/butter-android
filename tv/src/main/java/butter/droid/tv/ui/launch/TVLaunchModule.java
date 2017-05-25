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

package butter.droid.tv.ui.launch;

import android.content.Context;
import butter.droid.base.manager.prefs.PrefManager;
import butter.droid.base.ui.ActivityScope;
import dagger.Module;
import dagger.Provides;

@Module
public class TVLaunchModule {

    private final TVLaunchView view;

    public TVLaunchModule(final TVLaunchView view) {
        this.view = view;
    }

    @Provides @ActivityScope public TVLaunchView provideView() {
        return view;
    }

    @Provides @ActivityScope public TVLaunchPresenter providePresneter(TVLaunchView view, Context context, PrefManager prefManager) {
        return new TVLaunchPresenterImpl(view, context, prefManager);
    }
}
