/*
 * This file is part of Popcorn Time.
 *
 * Popcorn Time is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Popcorn Time is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Popcorn Time. If not, see <http://www.gnu.org/licenses/>.
 */

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
