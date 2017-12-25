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

package butter.droid.tv.service;

import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Intent;
import android.support.annotation.NonNull;
import butter.droid.base.manager.internal.provider.ProviderManager;
import butter.droid.base.providers.media.model.MediaWrapper;
import butter.droid.base.utils.VersionUtils;
import butter.droid.provider.base.model.Episode;
import butter.droid.provider.base.model.Media;
import butter.droid.provider.base.model.Movie;
import butter.droid.provider.base.model.Show;
import butter.droid.tv.R;
import butter.droid.tv.service.recommendation.RecommendationBuilder;
import butter.droid.tv.service.recommendation.RecommendationContentProvider;
import butter.droid.tv.ui.detail.TVMediaDetailActivity;
import dagger.android.DaggerIntentService;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.inject.Inject;
import timber.log.Timber;

// TODO this needs to be rewritten
public class RecommendationService extends DaggerIntentService {

    @Inject ProviderManager providerManager;

    private ArrayList<Media> movies = new ArrayList<>();
    private ArrayList<Media> shows = new ArrayList<>();

    public RecommendationService() {
        super("RecommendationService");
    }

    private static final int MAX_MOVIE_RECOMMENDATIONS = 3;
    private static final int MAX_SHOW_RECOMMENDATIONS = 3;

    private int priority = MAX_MOVIE_RECOMMENDATIONS + MAX_SHOW_RECOMMENDATIONS;
    private int totalCount = 0;

    @Override public void onCreate() {
        super.onCreate();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (!VersionUtils.isAndroidTV()) {
            return;
        }

        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

//        MediaProvider.Filters movieFilter = new MediaProvider.Filters();
//        movieFilter.order = MediaProvider.Filters.Order.DESC;
//        movieFilter.sort = MediaProvider.Filters.Sort.POPULARITY;
//
//        MediaProvider.Filters showsFilter = new MediaProvider.Filters();
//        showsFilter.order = MediaProvider.Filters.Order.DESC;
//        showsFilter.sort = MediaProvider.Filters.Sort.DATE;

        final AtomicBoolean mMoviesCallFinished = new AtomicBoolean(false);
        final AtomicBoolean mShowsCallFinished = new AtomicBoolean(false);

        // TODO: 6/17/17
        //fetch movies
//        if (providerManager.hasProvider(ProviderManager.PROVIDER_TYPE_MOVIE)) {
//            Timber.d("Fetching movies");
//            //noinspection ConstantConditions
//            providerManager.getMediaProvider(ProviderManager.PROVIDER_TYPE_MOVIE)
//                    .getList(movieFilter, new MediaProvider.Callback() {
//                        @Override
//                        public void onSuccess(MediaProvider.Filters filters, ArrayList<Media> items, boolean changed) {
//                            Timber.d(String.format("loaded %s movies", items.size()));
//                            movies.addAll(items);
//                            mMoviesCallFinished.set(true);
//                        }
//
//                        @Override
//                        public void onFailure(Exception ex) {
//                            Timber.d("Failed to fetch movies");
//                            mMoviesCallFinished.set(true);
//                        }
//                    });
//        }

        /*
        Disabled, since no shows provider

        Timber.d("Fetching shows");
        //fetch shows
        mShowProvider.getList(showsFilter, new MediaProvider.Callback() {
            @Override
            public void onSuccess(MediaProvider.Filters filters, ArrayList<Media> items, boolean changed) {
                Timber.d(String.format("loaded %s shows", items.size()));
                shows.addAll(items);
                mShowsCallFinished.set(true);
            }

            @Override
            public void onFailure(Exception e) {
                Timber.d("Failed to fetch shows");
                mShowsCallFinished.set(true);
            }
        });
        */
        mShowsCallFinished.set(true);

        /*

        //wait for callbacks to finish
        while (!mShowsCallFinished.get() || !mMoviesCallFinished.get()) {
            Timber.d("Waiting on callbacks");
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        */

        Timber.d("mShowsCallFinished: " + mShowsCallFinished.get());
        Timber.d("mMoviesCallFinished: " + mMoviesCallFinished.get());
        //process items

        Timber.d("Updating recommendation cards");
        if (movies.size() == 0 && shows.size() == 0) {
            return;
        }

        RecommendationBuilder builder = new RecommendationBuilder()
                .setContext(getApplicationContext())
                .setSmallIcon(R.drawable.header_logo);

        buildMovieRecommendations(builder);
        buildShowRecommendations(builder);
    }

    private void buildMovieRecommendations(@NonNull RecommendationBuilder builder) {
        Timber.d("building movie recommendations");
        try {
            int count = 0;
            for (Media media : movies) {
                Movie movie = (Movie) media;

                Timber.d("Recommendation - " + movie.getTitle());
                priority--;
                totalCount--;
                builder.setBackgroundContentUri(RecommendationContentProvider.CONTENT_URI + URLEncoder.encode(movie.getBackdrop(), "UTF-8"))
                        .setId(totalCount)
                        .setPriority(priority)
                        .setTitle(movie.getTitle())
                        .setDescription(movie.getSynopsis())
                        .setImage(movie.getPoster())
                        // TODO: 6/17/17 Handle provider id
//                        .setIntent(buildPendingIntent(0, movie))
                        .build();

                if (++count >= MAX_MOVIE_RECOMMENDATIONS) {
                    break;
                }

            }
        } catch (IOException e) {
            Timber.e("Unable to update recommendation", e);
        }
    }

    private void buildShowRecommendations(@NonNull RecommendationBuilder builder) {
        Timber.d("building show recommendations");
        try {
            int count = 0;
            for (Media media : shows) {
                Show show = (Show) media;

                Timber.d("Recommendation - " + show.getTitle());

                Episode latestEpisode = findLatestEpisode(show);

                priority--;
                totalCount--;
                builder.setBackgroundContentUri(RecommendationContentProvider.CONTENT_URI + URLEncoder.encode(show.getBackdrop(), "UTF-8"))
                        .setId(totalCount)
                        .setPriority(priority)
                        .setTitle(show.getTitle())
                        .setDescription(latestEpisode == null ? "" : getString(R.string.episode_number_format, latestEpisode.getEpisode()))
                        .setImage(show.getPoster())
                        // TODO: 6/17/17 Handle provider id
//                        .setIntent(buildPendingIntent(show))
                        .build();

                if (++count >= MAX_SHOW_RECOMMENDATIONS) {
                    break;
                }

            }
        } catch (IOException e) {
            Timber.e("Unable to update recommendation", e);
        }
    }

    private Episode findLatestEpisode(Show show) {
//        if (show.getEpisodes().length == 0) {
//            return null;
//        }
//        return show.getEpisodes()[show.getEpisodes().length - 1];
        return null;
    }

    private PendingIntent buildPendingIntent(final MediaWrapper media) {
        // TODO: 6/17/17 Pending intent can not rely on provider position
        Intent detailIntent = TVMediaDetailActivity.getIntent(this, media);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(TVMediaDetailActivity.class);
        stackBuilder.addNextIntent(detailIntent);
        // Ensure a unique PendingIntents, otherwise all recommendations end up with the same
        // PendingIntent
        detailIntent.setAction(media.getMedia().getId());

        PendingIntent intent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        return intent;
    }
}
