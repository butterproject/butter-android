package pct.droid.tv.service;

import android.app.IntentService;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Intent;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.squareup.okhttp.Call;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import pct.droid.base.PopcornApplication;
import pct.droid.base.providers.media.EZTVProvider;
import pct.droid.base.providers.media.MediaProvider;
import pct.droid.base.providers.media.YTSProvider;
import pct.droid.base.providers.media.models.Episode;
import pct.droid.base.providers.media.models.Media;
import pct.droid.base.providers.media.models.Movie;
import pct.droid.base.providers.media.models.Show;
import pct.droid.tv.R;
import pct.droid.tv.activities.PTVMediaDetailActivity;
import pct.droid.tv.fragments.PTVMovieDetailsFragment;
import timber.log.Timber;

public class RecommendationService extends IntentService {

    private ArrayList<Media> mMovies = new ArrayList<>();
    private ArrayList<Media> mShows = new ArrayList<>();

    public RecommendationService() {
        super("RecommendationService");
    }

    private static final int MAX_MOVIE_RECOMMENDATIONS = 3;
    private static final int MAX_SHOW_RECOMMENDATIONS = 3;

    private EZTVProvider mShowProvider = new EZTVProvider();
    private YTSProvider mMovieProvider = new YTSProvider();

    private int PRIORITY = MAX_MOVIE_RECOMMENDATIONS + MAX_SHOW_RECOMMENDATIONS;
    private int TOTAL_COUNT=0;


    @Override
    protected void onHandleIntent(Intent intent) {
        MediaProvider.Filters movieFilter = new MediaProvider.Filters();
        movieFilter.order = MediaProvider.Filters.Order.DESC;
        movieFilter.sort = MediaProvider.Filters.Sort.POPULARITY;

        MediaProvider.Filters showsFilter = new MediaProvider.Filters();
        showsFilter.order = MediaProvider.Filters.Order.DESC;
        showsFilter.sort = MediaProvider.Filters.Sort.DATE;

        final AtomicBoolean mMoviesCallFinished = new AtomicBoolean(false);
        final AtomicBoolean mShowsCallFinished = new AtomicBoolean(false);


        Timber.d("Fetching movies");
        //fetch movies
        mMovieProvider.getList(movieFilter, new MediaProvider.Callback() {
            @Override
            public void onSuccess(MediaProvider.Filters filters, ArrayList<Media> items, boolean changed) {
                Timber.d(String.format("loaded %s movies", items.size()));
                mMovies.addAll(items);
                mMoviesCallFinished.set(true);
            }

            @Override
            public void onFailure(Exception e) {
                Timber.d("Failed to fetch movies");
                mMoviesCallFinished.set(true);
            }
        });

        Timber.d("Fetching shows");
        //fetch shows
        mShowProvider.getList(showsFilter, new MediaProvider.Callback() {
            @Override
            public void onSuccess(MediaProvider.Filters filters, ArrayList<Media> items, boolean changed) {
                Timber.d(String.format("loaded %s shows", items.size()));
                mShows.addAll(items);
                mShowsCallFinished.set(true);
            }

            @Override
            public void onFailure(Exception e) {
                Timber.d("Failed to fetch shows");
                mShowsCallFinished.set(true);
            }
        });

        //wait for callbacks to finish
        while (!mShowsCallFinished.get() || !mMoviesCallFinished.get()) {
            Timber.d("Waiting on callbacks");
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        Timber.d("mShowsCallFinished: " + mShowsCallFinished.get());
        Timber.d("mMoviesCallFinished: " + mMoviesCallFinished.get());
        //process items

        Timber.d("Updating recommendation cards");
        if (mMovies.size() == 0 && mShows.size() == 0)
            return;


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
            for (Media media : mMovies) {
                Movie movie = (Movie) media;

                Timber.d("Recommendation - " + movie.title);
                PRIORITY--;
                TOTAL_COUNT--;
                builder.setBackground(movie.image)
                        .setId(TOTAL_COUNT)
                        .setPriority(PRIORITY)
                        .setTitle(movie.title)
                        .setDescription(movie.synopsis)
                        .setImage(movie.image)
                        .setIntent(buildPendingIntent(movie))
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
            for (Media media : mShows) {
                Show show = (Show) media;

                Timber.d("Recommendation - " + show.title);

                Episode latestEpisode = findLatestEpisode(show);

                PRIORITY--;
                TOTAL_COUNT--;
                builder.setBackground(show.image)
                        .setId(TOTAL_COUNT)
                        .setPriority(PRIORITY)
                        .setTitle(show.title)
                        .setDescription(latestEpisode == null ? "" : getString(R.string.episode_number_format, latestEpisode.episode))
                        .setImage(show.image)
                        .setIntent(buildPendingIntent(show))
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
        if (show.episodes == null || show.episodes.size() == 0) return null;
        return show.episodes.get(show.episodes.size());
    }

    private PendingIntent buildPendingIntent(Media media) {
        Intent detailIntent = PTVMediaDetailActivity.buildIntent(this, media, media.headerImage, media.image);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(PTVMediaDetailActivity.class);
        stackBuilder.addNextIntent(detailIntent);
        // Ensure a unique PendingIntents, otherwise all recommendations end up with the same
        // PendingIntent
        detailIntent.setAction(media.videoId);

        PendingIntent intent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        return intent;
    }
}
