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

import butter.droid.base.manager.internal.media.MediaDisplayManager;
import butter.droid.base.manager.internal.provider.ProviderManager;
import butter.droid.base.providers.media.model.MediaWrapper;
import butter.droid.base.utils.StringUtils;
import butter.droid.provider.base.module.Movie;
import butter.droid.provider.base.module.Torrent;
import butter.droid.tv.R;
import butter.droid.tv.ui.detail.base.TVBaseDetailsPresenterImpl;


public class TVMovieDetailsPresenterImpl extends TVBaseDetailsPresenterImpl implements TVMovieDetailsPresenter {

    private final TVMovieDetailsView view;
    private final MediaDisplayManager mediaDisplayManager;

    public TVMovieDetailsPresenterImpl(final TVMovieDetailsView view, final ProviderManager providerManager,
            final MediaDisplayManager mediaDisplayManager) {
        super(view, providerManager);

        this.view = view;
        this.mediaDisplayManager = mediaDisplayManager;
    }

    @Override public void onCreate(final MediaWrapper item) {
        super.onCreate(item);
    }

    @Override protected void detailsLoaded(final MediaWrapper media) {
        super.detailsLoaded(media);

        addActions((Movie) media.getMedia());
    }

    @Override public void actionClicked(final long actionId) {
        Movie movie = (Movie) item.getMedia();
        if (actionId == ACTION_TRAILER) {
            view.startTrailer(item, movie.getTrailer());
        } else {
            Torrent torrent = movie.getTorrents()[(int) actionId];
            view.startMovie(item, torrent, mediaDisplayManager.getFormatDisplayName(torrent.getFormat()));
        }
    }

    private void addActions(Movie item) {

        if (!StringUtils.isEmpty(item.getTrailer())) {
            view.addAction(ACTION_TRAILER, R.string.watch, R.string.trailer);
        }

        Torrent[] torrents = item.getTorrents();
        for (int i = 0; i < torrents.length; i++) {
            Torrent torrent = torrents[i];
            String text = mediaDisplayManager.getFormatDisplayName(torrent.getFormat());
            view.addAction(i, R.string.watch, text);
        }
    }

}
