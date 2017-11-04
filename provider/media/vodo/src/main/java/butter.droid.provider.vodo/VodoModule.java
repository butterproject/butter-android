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

package butter.droid.provider.vodo;

import butter.droid.provider.base.ProviderScope;
import butter.droid.provider.vodo.api.VodoService;
import com.google.gson.Gson;
import dagger.Module;
import dagger.Provides;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import retrofit2.CallAdapter;
import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

@Module
public class VodoModule {

    @Provides @ProviderScope HttpUrl providerUrl() {
        return HttpUrl.parse("http://butter.vodo.net/");
    }

    @Provides @ProviderScope CallAdapter.Factory provideCallAdapter() {
        return RxJava2CallAdapterFactory.create();
    }

    // TODO: 6/22/17 This should be specific for vodo
//    @Provides @ProviderScope Gson provideGson() {
//        return new Gson();
//    }

    @Provides @ProviderScope Converter.Factory provideConverter(Gson gson) {
        return GsonConverterFactory.create(gson);
    }

    @Provides @ProviderScope Retrofit provideRetrofit(OkHttpClient client, HttpUrl url, CallAdapter.Factory callAdapter,
            Converter.Factory converter) {
        return new Retrofit.Builder()
                .client(client)
                .baseUrl(url)
                .addCallAdapterFactory(callAdapter)
                .addConverterFactory(converter)
                .build();
    }

    @Provides @ProviderScope VodoService provideVodoService(Retrofit retrofit) {
        return retrofit.create(VodoService.class);
    }

    @Provides @ProviderScope VodoProvider provideVodo(VodoService service) {
        return new VodoProvider(service);
    }

}
