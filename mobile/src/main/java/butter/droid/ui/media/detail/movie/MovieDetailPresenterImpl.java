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

package butter.droid.ui.media.detail.movie;

import butter.droid.base.manager.youtube.YouTubeManager;
import butter.droid.base.providers.media.models.Movie;
import butter.droid.base.torrent.StreamInfo;
import butter.droid.ui.media.detail.MediaDetailPresenter;

public class MovieDetailPresenterImpl implements MovieDetailPresenter {

    private final MovieDetailView view;
    private final MediaDetailPresenter parentPresenter;
    private final YouTubeManager youTubeManager;

    private Movie movie;

    public MovieDetailPresenterImpl(MovieDetailView view, MediaDetailPresenter parentPresenter,
            YouTubeManager youTubeManager) {
        this.view = view;
        this.parentPresenter = parentPresenter;
        this.youTubeManager = youTubeManager;
    }

    @Override public void onCreate(Movie movie) {
        this.movie = movie;

        if (movie != null) {
            view.initLayout(movie);
        }
    }

    @Override public void openTrailer() {
        if (!youTubeManager.isYouTubeUrl(movie.trailer)) {
            parentPresenter.openVideoPlayer(new StreamInfo(movie, null, null, null, null, movie.trailer));
        } else {
            parentPresenter.openYouTube(movie.trailer);
        }
    }

    @Override public void selectQuality(String quality) {
        parentPresenter.selectQuality(quality);
        view.renderHealth(movie, quality);
        view.updateMagnet(movie, quality);
    }

    @Override public void openReadMore() {
        view.showReadMoreDialog(movie.synopsis);
    }

    @Override public void playMediaClicked() {
        parentPresenter.playMediaClicked();
    }
}
