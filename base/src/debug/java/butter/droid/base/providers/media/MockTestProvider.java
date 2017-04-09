package butter.droid.base.providers.media;

import android.support.annotation.Nullable;
import butter.droid.base.R;
import butter.droid.base.providers.media.MediaProvider.Filters.Order;
import butter.droid.base.providers.media.MediaProvider.Filters.Sort;
import butter.droid.base.providers.media.models.Media;
import butter.droid.base.providers.media.models.Movie;
import butter.droid.base.providers.subs.SubsProvider;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.koushikdutta.async.http.server.AsyncHttpServer;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MockTestProvider extends MediaProvider {

    public static final String URL = "http://localhost:5000/";

    private AsyncHttpServer server;

    public MockTestProvider(final AsyncHttpServer server, final OkHttpClient client, final Gson gson,
            @Nullable final SubsProvider subsProvider) {
        super(client, gson, subsProvider);
        this.server = server;
    }

    @Override
    public Call getList(final ArrayList<Media> currentList, final Filters filters, final Callback callback) {
        Request request = new Request.Builder().url(URL + "list").build();
        return enqueue(request, new okhttp3.Callback() {
            @Override
            public void onFailure(final Call call, final IOException e) {
                if (call.isCanceled()) {
                    return;
                }
                callback.onFailure(new IOException("Couldn't fetch from mockwebserver!", e));
            }

            @Override
            public void onResponse(final Call call, final Response response) throws IOException {
                if (call.isCanceled()) {
                    return;
                }
                final InputStream stream = response.body().byteStream();
                final InputStreamReader reader = new InputStreamReader(stream, "UTF-8");
                final List<Movie> movies = mGson.fromJson(reader, new TypeToken<List<Movie>>() {
                }.getType());
                callback.onSuccess(filters, new ArrayList<Media>(movies), true);
            }
        });
    }

    @Override
    public Call getDetail(final ArrayList<Media> currentList, final Integer index, final Callback callback) {
        ArrayList<Media> returnList = new ArrayList<>();
        returnList.add(currentList.get(index));
        callback.onSuccess(null, returnList, true);
        return null;
    }

    @Override
    public List<NavInfo> getNavigation() {
        final List<NavInfo> tabs = new ArrayList<>();
        final NavInfo info = new NavInfo(R.id.yts_filter_a_to_z, Sort.ALPHABET, Order.ASC, R.string.a_to_z, R.drawable.yts_filter_a_to_z);
        tabs.add(info);
        return tabs;
    }

}
