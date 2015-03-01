package pct.droid.base.providers.meta;

import org.joda.time.DateTime;
import pct.droid.base.providers.BaseProvider;

public abstract class MetaProvider extends BaseProvider {
    public static final String META_CALL = "meta_http_call";

    public static class MetaData {
        public String title;
        public Integer year;
        public String imdb_id;
        public DateTime released;
        public String trailer;
        public Integer runtime;
        public String tagline;
        public String overview;
        public String certification;
        public Images images;
        public String[] genres;

        public static class Images {
            public String poster;
            public String backdrop;

            public Images(String poster, String backdrop) {
                this.poster = poster;
                this.backdrop = backdrop;
            }
        }
    }
}
