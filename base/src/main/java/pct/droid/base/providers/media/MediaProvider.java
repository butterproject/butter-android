package pct.droid.base.providers.media;

import com.squareup.okhttp.Call;

import java.util.ArrayList;

import pct.droid.base.providers.BaseProvider;
import pct.droid.base.providers.media.models.Media;

/**
 * MediaProvider.java
 * <p/>
 * Base class for all media providers. Any media providers has to extend this class and use the callback defined here.
 */
public abstract class MediaProvider extends BaseProvider {
    public static final String MEDIA_CALL = "media_http_call";

    /**
     * Get a list of Media items from the provider
     *
     * @param filters  Filters the provider can use to sort or search
     * @param callback MediaProvider callback
     */
    public void getList(Filters filters, Callback callback) {
        getList(null, filters, callback);
    }

    /**
     * Get a list of Media items from the provider
     *
     * @param currentList Input the current list so it can be extended
     * @param filters     Filters the provider can use to sort or search
     * @param callback    MediaProvider callback
     * @return Call
     */
    public abstract Call getList(ArrayList<Media> currentList, Filters filters, Callback callback);

    public abstract Call getDetail(String videoId, Callback callback);

	public abstract int getLoadingMessage();

    public interface Callback {
        public void onSuccess(ArrayList<Media> items);

        public void onFailure(Exception e);
    }

    public static class Filters {
        public enum Order {ASC, DESC}

        ;

        public enum Sort {POPULARITY, YEAR, DATE, RATING, ALPHABET}

        public String keywords = null;
        public String genre = null;
        public Order order = Order.DESC;
        public Sort sort = Sort.POPULARITY;
        public Integer page = null;
    }

}
