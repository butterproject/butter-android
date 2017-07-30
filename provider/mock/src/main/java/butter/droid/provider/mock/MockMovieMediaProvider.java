
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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import butter.droid.provider.AbsMediaProvider;
import butter.droid.provider.base.filter.Filter;
import butter.droid.provider.base.filter.Genre;
import butter.droid.provider.base.filter.Sorter;
import butter.droid.provider.base.module.Format;
import butter.droid.provider.base.module.FormatKt;
import butter.droid.provider.base.module.ItemsWrapper;
import butter.droid.provider.base.module.Media;
import butter.droid.provider.base.module.Movie;
import butter.droid.provider.base.module.Paging;
import butter.droid.provider.base.module.Torrent;
import butter.droid.provider.base.nav.NavItem;
import butter.droid.provider.base.util.Optional;
import butter.droid.provider.filter.Pager;
import butter.droid.provider.mock.model.MockMovies;
import com.google.gson.Gson;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import okio.BufferedSource;
import okio.Okio;

public class MockMovieMediaProvider extends AbsMediaProvider {

    private final Context context;
    private final Gson gson;

    public MockMovieMediaProvider(Context context, Gson gson) {
        this.context = context;
        this.gson = gson;
    }

    @NonNull @Override public Single<ItemsWrapper> items(@Nullable final Filter filter, @Nullable Pager pager) {
        return Single.fromCallable(() -> parseResponse("movies_list.json", MockMovies.class))
                .map(MockMovies::getMovies)
                .flatMapObservable(Observable::fromIterable)
                .<Media>map(m -> new Movie(String.valueOf(m.getId()), m.getTitle(), m.getYear(), new Genre[0], null, m.getPoster(),
                        m.getBackdrop(), m.getSynopsis(),
                        new Torrent[]{
                                new Torrent(null, m.getTorrent(), new Format(m.getQuality(), FormatKt.FORMAT_NORMAL), 0, null, null, null)
                        }, m.getTrailer()))
                .toList()
                .map(l -> new ItemsWrapper(l, new Paging("", false)));
    }

    @Override public Single<Media> detail(Media media) {
        return Single.just(media);
    }

    @NonNull @Override public Maybe<List<Sorter>> sorters() {
        return Maybe.just(Arrays.asList(new Sorter("alphabet", R.string.sorter_alphabet)));
    }

    @NonNull @Override public Maybe<List<Genre>> genres() {
        return Maybe.just(Arrays.asList(Genre.ACTION, Genre.ADVENTURE, Genre.ANIMATION));
    }

    @NonNull @Override public Maybe<List<NavItem>> navigation() {
        return Maybe.just(Arrays.asList(new NavItem(0, R.string.genre_action, null)));
    }

    @NonNull @Override public Single<Optional<Sorter>> getDefaultSorter() {
        return Single.just(Optional.<Sorter>empty());
    }

    @Override public int getLoadingMessage() {
        return R.string.loading_movies;
    }

    @Override public int getIcon() {
        return R.drawable.ic_nav_movies;
    }

    @Override public int getName() {
        return R.string.title_movies;
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

    private BufferedSource getFile(String fileName) throws IOException {
        String file = String.format("mock/%s", fileName);
        return Okio.buffer(Okio.source(context.getAssets().open(file)));
    }
}
