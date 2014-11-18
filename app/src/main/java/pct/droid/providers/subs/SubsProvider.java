package pct.droid.providers.subs;

import com.squareup.okhttp.Call;

import java.util.ArrayList;
import java.util.HashMap;

import pct.droid.providers.BaseProvider;

public abstract class SubsProvider extends BaseProvider {

    public abstract Call getList(String[] imdbIds, Callback callback);

    public Call getList(String imdbId, Callback callback) {
        return getList(new String[] { imdbId }, callback);
    }

    public interface Callback {
        public void onSuccess(HashMap<String, HashMap<String, String>> items);
        public void onFailure(Exception e);
    }

}
