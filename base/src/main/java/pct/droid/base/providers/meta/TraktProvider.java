package pct.droid.base.providers.meta;

import com.uwetrottmann.trakt.v2.TraktV2;
import com.uwetrottmann.trakt.v2.entities.Movie;
import com.uwetrottmann.trakt.v2.enums.Extended;
import com.uwetrottmann.trakt.v2.services.Movies;

import retrofit.RetrofitError;

public class TraktProvider extends MetaProvider {

    private static final String API_KEY = "8712a855abab180fc954c8c0ced2e8405e80d86cc9189bbdab349d760963e766"; // Temporary API key.
    private static TraktV2 TRAKT = new TraktV2();
    private static Movies MOVIES;

    static {
        TRAKT.setApiKey(API_KEY);
        MOVIES = TRAKT.movies();
    }

    /**
     * Get metadata from Trakt
     *
     * @param imdbId IMDb id to get metadata for
     * @return MetaData
     */
    public MetaData getSummary(String imdbId) {
        try {
            Movie m = MOVIES.summary(imdbId, Extended.FULLIMAGES);

            MetaData metaData = new MetaData();
            metaData.certification = m.certification;
            metaData.genres = m.genres.toArray(new String[m.genres.size()]);
            metaData.imdb_id = imdbId;
            metaData.overview = m.overview;
            metaData.released = m.released;
            metaData.year = m.year;
            metaData.runtime = m.runtime;
            metaData.trailer = m.trailer;
            metaData.tagline = m.tagline;
            metaData.title = m.title;
            metaData.images = new MetaData.Images(m.images.poster.full, m.images.fanart.full);

            return metaData;
        } catch (RetrofitError e) {
            e.printStackTrace();
        }

        return new MetaData();
    }

}