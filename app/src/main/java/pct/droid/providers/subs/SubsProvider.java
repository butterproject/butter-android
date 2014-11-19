package pct.droid.providers.subs;

import java.util.HashMap;
import pct.droid.providers.BaseProvider;

public abstract class SubsProvider extends BaseProvider {

    public HashMap<String, HashMap<String, String>> getList(String imdbId) {
        return getList(new String[] { imdbId });
    }

    public abstract HashMap<String, HashMap<String, String>> getList(String[] imdbIds);

}
