package pct.droid.base.providers.media;

import java.util.ArrayList;
import java.util.HashMap;

import pct.droid.base.providers.BaseProvider;
import pct.droid.base.providers.media.types.Media;

public abstract class MediaProvider extends BaseProvider {

    public void getList(HashMap<String, String> filters, Callback callback) {
        getList(null, filters, callback);
    }

    public abstract void getList(ArrayList<Media> currentList, HashMap<String, String> filters, Callback callback);
    public abstract void getDetail(String torrentId, Callback callback);

    public interface Callback {
        public void onSuccess(ArrayList<Media> items);
        public void onFailure(Exception e);
    }

}
