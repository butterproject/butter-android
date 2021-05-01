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

package butter.droid.base.providers;

import android.content.Context;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.inject.Singleton;

import butter.droid.base.providers.media.AnimeProvider;
import butter.droid.base.providers.media.MoviesProvider;
import butter.droid.base.providers.media.TVProvider;
import butter.droid.base.providers.subs.open.OpenSubsProvider;
import dagger.Module;
import dagger.Provides;
import de.timroes.axmlrpc.XMLRPCClient;
import okhttp3.OkHttpClient;

@Module
public class ProviderModule {

    @Provides
    @Singleton
    public OpenSubsProvider provideOpenSubsProvider(Context context, OkHttpClient client, ObjectMapper mapper, XMLRPCClient xmlrpcClient) {
        return new OpenSubsProvider(context, client, mapper, xmlrpcClient);
    }

    @Provides
    @Singleton
    public MoviesProvider provideMoviesProvider(Context context, OkHttpClient client, ObjectMapper mapper,
                                                OpenSubsProvider subsProvider) {
        return new MoviesProvider(context, client, mapper, subsProvider);
    }

    @Provides
    @Singleton
    public TVProvider provideTVProvider(Context context, OkHttpClient client, ObjectMapper mapper,
                                        OpenSubsProvider subsProvider) {
        return new TVProvider(context, client, mapper, subsProvider);
    }

    @Provides
    @Singleton
    public AnimeProvider provideAnimeProvider(Context context, OkHttpClient client, ObjectMapper mapper,
                                              OpenSubsProvider subsProvider) {
        return new AnimeProvider(context, client, mapper, subsProvider);
    }

}
