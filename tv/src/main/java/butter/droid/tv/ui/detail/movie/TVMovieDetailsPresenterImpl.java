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

package butter.droid.tv.ui.detail.movie;

import butter.droid.base.manager.internal.provider.ProviderManager;
import butter.droid.base.providers.media.models.Media;
import butter.droid.base.providers.media.models.Media.Torrent;
import butter.droid.base.providers.media.models.Movie;
import butter.droid.base.utils.StringUtils;
import butter.droid.tv.R;
import butter.droid.tv.ui.detail.base.TVBaseDetailsPresenterImpl;
import java.util.ArrayList;
import java.util.List;

public class TVMovieDetailsPresenterImpl extends TVBaseDetailsPresenterImpl implements TVMovieDetailsPresenter {

    private final TVMovieDetailsView view;

    private Movie item;

    public TVMovieDetailsPresenterImpl(final TVMovieDetailsView view, final ProviderManager providerManager) {
        super(view, providerManager);

        this.view = view;
    }

    @Override public void onCreate(final Movie item) {
        super.onCreate(item);

        this.item = item;
    }

    @Override protected void detailsLoaded(final Media media) {
        super.detailsLoaded(media);
        this.item = (Movie) media;

        addActions(item);
    }

    @Override public void actionClicked(final long actionId) {
        if (actionId == ACTION_TRAILER) {
            view.startTrailer(item, item.trailer);
        } else {
            List<String> qualities = new ArrayList<>(item.torrents.keySet());
            String quality = qualities.get((int) actionId);
            Torrent torrent = item.torrents.get(quality);
            view.startMovie(item, torrent, quality);
        }
    }

    private void addActions(Movie item) {

        if (!StringUtils.isEmpty(item.trailer)) {
            view.addAction(ACTION_TRAILER, R.string.watch, R.string.trailer);
        }

        List<String> qualities = new ArrayList<>(item.torrents.keySet());
        for (int i = 0; i < qualities.size(); i++) {
            String quality = qualities.get(i);

            view.addAction(i, R.string.watch, quality);
        }
    }

}
