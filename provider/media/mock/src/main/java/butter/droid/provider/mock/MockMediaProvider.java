
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

package butter.droid.provider.mock;

import android.content.Context;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import butter.droid.provider.AbsMediaProvider;
import butter.droid.provider.base.filter.Filter;
import butter.droid.provider.base.filter.Genre;
import butter.droid.provider.base.filter.Sorter;
import butter.droid.provider.base.model.Episode;
import butter.droid.provider.base.model.Format;
import butter.droid.provider.base.model.Media;
import butter.droid.provider.base.model.Movie;
import butter.droid.provider.base.model.Season;
import butter.droid.provider.base.model.Show;
import butter.droid.provider.base.model.Torrent;
import butter.droid.provider.base.nav.NavItem;
import butter.droid.provider.base.paging.ItemsWrapper;
import butter.droid.provider.base.paging.Paging;
import butter.droid.provider.base.util.Optional;
import butter.droid.provider.filter.Pager;
import butter.droid.provider.mock.model.MockEpisode;
import butter.droid.provider.mock.model.MockMovies;
import butter.droid.provider.mock.model.MockSeason;
import butter.droid.provider.mock.model.MockSeasons;
import butter.droid.provider.mock.model.MockShows;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import okio.BufferedSource;
import okio.Okio;

public class MockMediaProvider extends AbsMediaProvider {

    private final Context context;
    private final Gson gson;

    public MockMediaProvider(Context context, Gson gson) {
        this.context = context;
        this.gson = gson;
    }

    @NonNull @Override public Single<ItemsWrapper> items(@Nullable final Filter filter, @Nullable Pager pager) {
        return parseMovies()
                .concatWith(parseShows())
                .concatWith(parseSeasons())
                .filter(media -> filter == null || filter.getQuery() == null
                        || media.getTitle().toLowerCase().contains(filter.getQuery().toLowerCase()))
                .toList()
                .map(l -> new ItemsWrapper(l, new Paging("", false)));
    }

    @NonNull @Override public Single<Media> detail(Media media) {
        return Single.just(media);
    }

    @NonNull @Override public Maybe<List<Sorter>> sorters() {
        return Maybe.just(Collections.singletonList(new Sorter("alphabet", R.string.sorter_alphabet)));
    }

    @NonNull @Override public Maybe<List<Genre>> genres() {
        return Maybe.just(Arrays.asList(Genre.ACTION, Genre.ADVENTURE, Genre.ANIMATION));
    }

    @NonNull @Override public Maybe<List<NavItem>> navigation() {
        return Maybe.just(Collections.singletonList(new NavItem(R.drawable.ic_nav_movies, R.string.genre_action, null)));
    }

    @NonNull @Override public Single<Optional<Sorter>> getDefaultSorter() {
        return Single.just(Optional.empty());
    }

    private <R> R parseResponse(String fileName, Class<R> tClass) {
        return gson.fromJson(readFile(fileName), tClass);
    }

    private String readFile(String fileName) {
        try {
            return getFile(fileName).readUtf8();
        } catch (IOException e) {
            throw new RuntimeException("Error reading file", e);
        }

    }

    private Observable<Media> parseMovies() {
        return Single.fromCallable(() -> parseResponse("movie_list.json", MockMovies.class))
                .map(MockMovies::getMovies)
                .flattenAsObservable(mockMovies -> mockMovies)
                .map(m -> new Movie(String.valueOf(m.getId()), m.getTitle(), m.getYear(), new Genre[0], -1, m.getPoster(),
                        m.getBackdrop(), m.getSynopsis(),
                        new Torrent[] {
                                new Torrent(m.getTorrent(), new Format(m.getQuality(), Format.FORMAT_NORMAL), 0)
                        }, m.getTrailer(), null));
    }

    private Observable<Media> parseShows() {
        return Single.fromCallable(() -> parseResponse("show_list.json", MockShows.class))
                .map(MockShows::getShow)
                .flattenAsObservable(mockShows -> mockShows)
                .map(s -> new Show(String.valueOf(s.getId()), s.getTitle(), s.getYear(), new Genre[0], -1, s.getPoster(),
                        s.getBackdrop(), s.getSynopsis(), mapSeasons(s.getSeasons()), null));
    }

    private Observable<Media> parseSeasons() {
        return Single.fromCallable(() -> parseResponse("season_list.json", MockSeasons.class))
                .map(MockSeasons::getSeasons)
                .flattenAsObservable(mockSeasons -> mockSeasons)
                .map(this::mapSeason);
    }

    private BufferedSource getFile(String fileName) throws IOException {
        String file = String.format("mock/%s", fileName);
        return Okio.buffer(Okio.source(context.getAssets().open(file)));
    }

    @NonNull
    private Season[] mapSeasons(MockSeason[] mockSeasons) {
        Season[] seasons = new Season[mockSeasons.length];
        for (int i = 0; i < mockSeasons.length; i++) {
            seasons[i] = mapSeason(mockSeasons[i]);
        }
        return seasons;
    }

    @NonNull
    private Episode[] mapEpisodes(MockEpisode[] mockEpisodes, int season) {
        Episode[] episodes = new Episode[mockEpisodes.length];
        for (int i = 0; i < mockEpisodes.length; i++) {
            MockEpisode episode = mockEpisodes[i];
            episodes[i] = new Episode(String.valueOf(episode.getId()), episode.getTitle(), episode.getYear(), new Genre[0],
                    -1, episode.getPoster(), episode.getBackdrop(), episode.getSynopsis(),
                    new Torrent[] {
                            new Torrent(episode.getTorrent(), new Format(episode.getQuality(), Format.FORMAT_NORMAL), 0)
                    }, episode.getEpisdoe(), season, null);
        }
        return episodes;

    }

    @NonNull
    private Season mapSeason(MockSeason season) {
        return new Season(String.valueOf(season.getId()), season.getTitle(), season.getYear(), new Genre[0],
                -1, season.getPoster(), season.getBackdrop(), season.getSynopsis(),
                mapEpisodes(season.getEpisodes(), season.getSeason()), season.getSeason(), null);
    }

}
