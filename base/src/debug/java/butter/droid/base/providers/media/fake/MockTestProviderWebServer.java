package butter.droid.base.providers.media.fake;

import android.content.Context;
import android.content.res.AssetManager;
import com.koushikdutta.async.http.server.AsyncHttpServer;
import com.koushikdutta.async.http.server.AsyncHttpServerRequest;
import com.koushikdutta.async.http.server.AsyncHttpServerResponse;
import com.koushikdutta.async.http.server.HttpServerRequestCallback;
import java.io.IOException;
import java.io.InputStream;
import okio.Okio;

public class MockTestProviderWebServer {

    public static AsyncHttpServer create(final Context context) {
        final AsyncHttpServer server = new AsyncHttpServer();
        server.listen(5000);
        server.get("/list", new AssetsResponse(context));
        return server;
    }

    private static class AssetsResponse implements HttpServerRequestCallback {

        private final AssetManager am;

        private boolean firstRequest = true;

        public AssetsResponse(final Context context) {
            this.am = context.getAssets();
        }

        @Override
        public void onRequest(final AsyncHttpServerRequest request, final AsyncHttpServerResponse response) {
            try {
                if (firstRequest) {
                    firstRequest = false;
                    final InputStream stream = am.open("server/fake-test-server-responses.json");
                    final byte[] bytes = Okio.buffer(Okio.source(stream)).readByteArray();
                    response.send("application/json; charset=utf-8", bytes);
                    response.code(200);
                } else {
                    response.send("application/json; charset=utf-8", "[]");
                    response.code(200);
                }
            } catch (IOException e) {
                throw new RuntimeException("Problem loading fake-server-responses.json", e);
            }
        }
    }

}
