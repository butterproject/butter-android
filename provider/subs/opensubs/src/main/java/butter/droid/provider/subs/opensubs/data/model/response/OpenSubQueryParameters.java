/*
 * This file is part of Butter.
 *
 * Butter is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Butter is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Butter. If not, see <http://www.gnu.org/licenses/>.
 */

package butter.droid.provider.subs.opensubs.data.model.response;

import com.google.gson.annotations.SerializedName;

public class OpenSubQueryParameters {

    /**
     * (number)
     */
    @SerializedName("episode") //
    private String episode;

    /**
     * (always format it as sprintf("%07d", $imdb) - when using imdb you can add
     * /tags-hdtv for example.
     */
    @SerializedName("imdbid") //
    private String imdbid;

    /**
     * (number)
     */
    @SerializedName("moviebytesize") //
    private String moviebytesize;

    /**
     * (should be always 16 character, must be together with moviebytesize)
     */
    @SerializedName("moviehash") //
    private String moviehash;

    /**
     * (use url_encode, make sure " " is converted to "%20")
     */
    @SerializedName("query") //
    private String query;

    /**
     * (number)
     */
    @SerializedName("season") //
    private String season;

    /**
     * (if ommited, all languages are returned)
     */
    @SerializedName("sublanguageid") //
    private String sublanguageid;

    /**
     * (use url_encode, make sure " " is converted to "%20")
     */
    @SerializedName("tag") //
    private String tag;

    public String getEpisode() {
        return episode;
    }

    public void setEpisode(String episode) {
        this.episode = episode;
    }

    public String getImdbid() {
        return imdbid;
    }

    public void setImdbid(String imdbid) {
        this.imdbid = imdbid;
    }

    public String getMoviebytesize() {
        return moviebytesize;
    }

    public void setMoviebytesize(String moviebytesize) {
        this.moviebytesize = moviebytesize;
    }

    public String getMoviehash() {
        return moviehash;
    }

    public void setMoviehash(String moviehash) {
        this.moviehash = moviehash;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getSeason() {
        return season;
    }

    public void setSeason(String season) {
        this.season = season;
    }

    public String getSublanguageid() {
        return sublanguageid;
    }

    public void setSublanguageid(String sublanguageid) {
        this.sublanguageid = sublanguageid;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }
    
    

}
