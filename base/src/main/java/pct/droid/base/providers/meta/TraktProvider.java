package pct.droid.base.providers.meta;


import com.uwetrottmann.trakt.v2.TraktV2;
import com.uwetrottmann.trakt.v2.entities.Movie;
import com.uwetrottmann.trakt.v2.enums.Extended;
import com.uwetrottmann.trakt.v2.services.Movies;

import retrofit.RetrofitError;

public class TraktProvider extends MetaProvider {

    private static final String API_KEY = "c7e20abc718e46fc75399dd6688afca9ac83cd4519c9cb1fba862b37b8640e89";
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