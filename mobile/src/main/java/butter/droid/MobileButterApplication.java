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

package butter.droid;

import android.content.Context;
import androidx.multidex.MultiDex;
import butter.droid.base.BaseApplicationModule;
import butter.droid.base.ButterApplication;
import butter.droid.base.providers.DaggerProviderComponent;
import butter.droid.base.providers.ProviderComponent;
import dagger.android.AndroidInjector;
import dagger.android.support.DaggerApplication;

public class MobileButterApplication extends ButterApplication {

    private ApplicationComponent appComponent;
    private ProviderComponent providerComponent;

    @Override protected void attachBaseContext(final Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    public static MobileButterApplication getAppContext() {
        return (MobileButterApplication) ButterApplication.getAppContext();
    }

    @Override protected AndroidInjector<? extends DaggerApplication> applicationInjector() {
        if (appComponent == null) {
            appComponent = DaggerApplicationComponent.builder()
                    .baseApplicationModule(new BaseApplicationModule(this))
                    .build();
        }

        if (providerComponent == null) {
            providerComponent = DaggerProviderComponent.builder()
                    .baseApplicationComponent(appComponent)
                    .build();
        }

        return DaggerInternalComponent.builder()
                .providerComponent(providerComponent)
                .create(this);
    }
}
