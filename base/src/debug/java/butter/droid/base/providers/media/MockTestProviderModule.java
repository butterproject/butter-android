package butter.droid.base.providers.media;

import android.content.Context;
import butter.droid.base.manager.internal.vlc.PlayerManager;
import butter.droid.base.providers.ProviderScope;
import butter.droid.base.providers.media.fake.MockTestProviderWebServer;
import butter.droid.base.providers.subs.SubsProvider;
import butter.droid.base.providers.subs.YSubsProvider;
import com.google.gson.Gson;
import com.koushikdutta.async.http.server.AsyncHttpServer;
import dagger.Module;
import dagger.Provides;
import javax.inject.Named;
import okhttp3.OkHttpClient;

@Module
public class MockTestProviderModule {

    @Provides
    @ProviderScope
    public SubsProvider provideSubsProvider(OkHttpClient client, Gson gson, PlayerManager playerManager) {
        return new YSubsProvider(client, gson, playerManager);
    }

    @Provides
    @ProviderScope
    @Named("test.provider.mock.server")
    AsyncHttpServer provideMockAsyncHttpServer(final Context context) {
        return MockTestProviderWebServer.create(context);
    }

    @Provides
    @ProviderScope
    public MediaProvider provideMediaProvider(@Named("test.provider.mock.server") final AsyncHttpServer server, final OkHttpClient
            client, final Gson gson, final SubsProvider subsProvider) {
        return new MockTestProvider(server, client, gson, subsProvider);
    }

}
