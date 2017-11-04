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

package butter.droid.provider.vodo.api.model;

import com.google.gson.annotations.SerializedName;

public class VodoMovie {

    @SerializedName("SizeByte") private long sizeBytes;
    @SerializedName("Rating") private int rating;
    //    @SerializedName("Runtime") private int runtime;
    @SerializedName("MovieUrl") private String movieUrl;
    @SerializedName("ImdbUrl") private String imdbUrl;
    @SerializedName("CoverImage") private String coverImage;
    @SerializedName("Popularity") private int popularity;
    @SerializedName("TorrentUrl") private String torrentUrl;
    @SerializedName("MovieRating") private float movieRating;
    @SerializedName("Synopsis") private String synopsis;
    @SerializedName("Tagline") private String tagline;
    @SerializedName("Genre") private String genre;
    @SerializedName("Size") private String size;
    @SerializedName("Quality") private String quality;
    @SerializedName("MovieTitleClean") private String movieTitleClean;
    @SerializedName("MovieYear") private int movieYear;
    @SerializedName("ImdbCode") private String imdbCode;

    public long getSizeBytes() {
        return sizeBytes;
    }

    public int getRating() {
        return rating;
    }

//    public int getRuntime() {
//        return runtime;
//    }

    public String getMovieUrl() {
        return movieUrl;
    }

    public String getImdbUrl() {
        return imdbUrl;
    }

    public String getCoverImage() {
        return coverImage;
    }

    public int getPopularity() {
        return popularity;
    }

    public String getTorrentUrl() {
        return torrentUrl;
    }

    public float getMovieRating() {
        return movieRating;
    }

    public String getSynopsis() {
        return synopsis;
    }

    public String getTagline() {
        return tagline;
    }

    public String getGenre() {
        return genre;
    }

    public String getSize() {
        return size;
    }

    public String getQuality() {
        return quality;
    }

    public String getMovieTitleClean() {
        return movieTitleClean;
    }

    public int getMovieYear() {
        return movieYear;
    }

    public String getImdbCode() {
        return imdbCode;
    }
}
