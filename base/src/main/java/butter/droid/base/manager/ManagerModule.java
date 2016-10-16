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

package butter.droid.base.manager;

import android.content.Context;

import com.google.gson.Gson;
import com.squareup.okhttp.OkHttpClient;

import javax.inject.Singleton;

import butter.droid.base.manager.provider.ProviderManager;
import butter.droid.base.providers.media.VodoProvider;
import butter.droid.base.providers.subs.SubsProvider;
import butter.droid.base.providers.subs.YSubsProvider;
import dagger.Module;
import dagger.Provides;

@Module
public class ManagerModule {

    @Provides @Singleton public ProviderManager provideProviderManager(VodoProvider moviesProvider,
            SubsProvider subsProvider) {
        return new ProviderManager(moviesProvider, null, subsProvider);
    }

    @Provides @Singleton public SubsProvider provideSubsProvider(Context context, OkHttpClient client, Gson gson) {
        return new YSubsProvider(context, client, gson);
    }

    @Provides @Singleton public VodoProvider provideVodoProvider(OkHttpClient client, Gson gson) {
        return new VodoProvider(client, gson);
    }

}
