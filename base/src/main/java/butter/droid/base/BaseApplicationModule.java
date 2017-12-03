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

package butter.droid.base;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import butter.droid.base.content.ObscuredSharedPreferences;
import butter.droid.base.data.DataModule;
import dagger.Module;
import dagger.Provides;
import javax.inject.Singleton;

@Module(
        includes = {
                BaseApplicationBindModule.class,
                BaseAndroidModule.class,
                DataModule.class,
                TypeModule.class
        }
)
public class BaseApplicationModule {

    // TODO remove
    private final ButterApplication application;

    public BaseApplicationModule(ButterApplication application) {
        this.application = application;
    }

    @Provides @Singleton public ButterApplication provdeButterApplication() {
        return application;
    }

    @Provides @Singleton public SharedPreferences provideSharedPreferences(Context context) {
        return new ObscuredSharedPreferences(context,
                context.getSharedPreferences(Constants.PREFS_FILE, Context.MODE_PRIVATE));
    }

    @Provides @Singleton public Resources provideResources(Context context) {
        return context.getResources();
    }

}
