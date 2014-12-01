package pct.droid.base.providers.meta;

import java.util.HashMap;

import pct.droid.base.providers.BaseProvider;

public abstract class MetaProvider extends BaseProvider {
    public class MetaData {
        public String title;
        public Integer year;
        public String imdb_id;
        public Integer released;
        public String trailer;
        public Integer runtime;
        public String tagline;
        public String overview;
        public String certification;
        public HashMap<String, String> images;
        public String[] genres;
    }
}
