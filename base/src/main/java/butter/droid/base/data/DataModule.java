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

package butter.droid.base.data;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import javax.inject.Singleton;

import butter.droid.base.content.preferences.Prefs;
import butter.droid.base.utils.PrefUtils;
import butter.droid.base.utils.StorageUtils;
import dagger.Module;
import dagger.Provides;
import okhttp3.Cache;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.OkHttpClient.Builder;
import okhttp3.Request;
import okhttp3.Response;

@Module
public class DataModule {

    @Provides
    @Singleton
    public Cache provideCache(Context context) {
        int cacheSize = 10 * 1024 * 1024;
        File cacheLocation = new File(
                PrefUtils.get(context, Prefs.STORAGE_LOCATION, StorageUtils.getIdealCacheDirectory(context)
                        .toString()));
        cacheLocation.mkdirs();

        return new Cache(cacheLocation, cacheSize);
    }

    @Provides
    @Singleton
    public OkHttpClient provideOkHttpClient(Context context, Cache cache) {
        return new Builder()
                .addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        Request.Builder builder = chain.request().newBuilder();
                        String version = "";
                        try {
                            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
                            version = info.versionName;
                        } catch (PackageManager.NameNotFoundException e) {
                            version = "unknown";
                        }
                        builder.addHeader("User-Agent", "Popcorn Time Ru Android (" + version + ")");
                        return chain.proceed(builder.build());
                    }
                })
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .cache(cache)
                .build();
    }

    @Provides
    @Singleton
    public Picasso providePicasso(Context context) {
        return new Picasso.Builder(context).build();
    }

    @Provides
    @Singleton
    public ObjectMapper provideObjectMapper() {
        return new ObjectMapper();
    }

}
