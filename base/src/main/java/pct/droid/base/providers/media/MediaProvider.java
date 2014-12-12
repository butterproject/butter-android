package pct.droid.base.providers.media;

import com.squareup.okhttp.Call;

import java.util.ArrayList;
import java.util.HashMap;

import pct.droid.base.providers.BaseProvider;
import pct.droid.base.providers.media.types.Media;

public abstract class MediaProvider extends BaseProvider {

    public void getList(HashMap<String, String> filters, Callback callback) {
        getList(null, filters, callback);
    }

    public abstract Call getList(ArrayList<Media> currentList, HashMap<String, String> filters, Callback callback);
    public abstract Call getDetail(String torrentId, Callback callback);

    public interface Callback {
        public void onSuccess(ArrayList<Media> items);
        public void onFailure(Exception e);
    }

}
