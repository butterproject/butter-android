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

import butter.droid.provider.base.filter.Genre;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.List;

import butter.droid.provider.AbsMediaProvider;
import butter.droid.provider.base.Media;
import butter.droid.provider.base.Movie;
import butter.droid.provider.base.Torrent;
import butter.droid.provider.mock.model.MockMovies;
import io.reactivex.Observable;
import io.reactivex.Single;
import okio.BufferedSource;
import okio.Okio;

public class MockMovieMediaProvider extends AbsMediaProvider {

    private final Context context;
    private final Gson gson;

    public MockMovieMediaProvider(Context context, Gson gson) {
        this.context = context;
        this.gson = gson;
    }

    @NonNull @Override public Single<List<Media>> items() {
        return Single.fromCallable(() -> parseResponse("movies_list.json", MockMovies.class))
                .map(MockMovies::getMovies)
                .flatMapObservable(Observable::fromIterable)
                .<Media>map(m -> new Movie(String.valueOf(m.getId()), m.getTitle(), m.getYear(), new Genre[0], 0, m.getPoster(),
                        m.getBackdrop(), m.getSynopsis(),
                        new Torrent[]{
                                new Torrent(m.getTorrent(), 0L, 0, null, null, null)
                        }, m.getTrailer()))
                .toList();
    }

    @Override public Single<Media> detail(Media media) {
        return Single.just(media);
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
