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

package butter.droid.provider.subs.opensubs;

import android.content.Context;
import butter.droid.provider.base.ProviderScope;
import butter.droid.provider.subs.opensubs.data.OpenSubsService;
import dagger.Module;
import dagger.Provides;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import retrofit2.CallAdapter;
import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@Module
public class OpenSubsModule {

    @Provides @ProviderScope @OpenSubsQualifier HttpUrl providerUrl() {
        return HttpUrl.parse("https://rest.opensubtitles.org/");
    }

    @Provides @ProviderScope @OpenSubsQualifier Converter.Factory provideGsonConverterFactory() {
        return GsonConverterFactory.create();
    }

    @Provides @ProviderScope @OpenSubsQualifier Retrofit provideRetrofit(OkHttpClient client, @OpenSubsQualifier HttpUrl url,
            CallAdapter.Factory callAdapter, @OpenSubsQualifier Converter.Factory converter) {
        return new Retrofit.Builder()
                .client(client)
                .baseUrl(url)
                .addCallAdapterFactory(callAdapter)
                .addConverterFactory(converter)
                .build();
    }

    @Provides @ProviderScope OpenSubsService provideOpensubsService(@OpenSubsQualifier Retrofit retrofit) {
        return retrofit.create(OpenSubsService.class);
    }

    @Provides @ProviderScope OpenSubsProvider provideOpenSubsProvider(final OpenSubsService service, final Context context) {
        return new OpenSubsProvider(service, context);
    }

}
