package pct.droid.providers.subs;

import java.util.HashMap;
import pct.droid.providers.BaseProvider;

public abstract class SubsProvider extends BaseProvider {

    public void getList(String imdbId, Callback callback) {
        getList(new String[] { imdbId }, callback);
    }

    public abstract void getList(String[] imdbIds, Callback callback);

    public interface Callback {
        public void onSuccess(HashMap<String, HashMap<String, String>> items);
        public void onFailure(Exception e);
    }

}
