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

package butter.droid.ui.media.detail.show.season;

import butter.droid.base.providers.model.MediaWrapper;
import butter.droid.provider.base.module.Episode;
import butter.droid.provider.base.module.Show;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ShowDetailSeasonPresenterImpl implements ShowDetailSeasonPresenter {

    private final ShowDetailSeasonView view;

    private MediaWrapper show;
    private int season;
    private List<Episode> episodes;

    public ShowDetailSeasonPresenterImpl(ShowDetailSeasonView view) {
        this.view = view;
    }


    @Override public void onCreate(MediaWrapper show, int season) {
        if (show == null) {
            throw new IllegalStateException("Show not provided");
        }

        if (season < 0) {
            throw new IllegalStateException("Season not valid");
        }

        this.show = show;
        this.season = season;
        mapData(show, season);

    }

    @Override public void onViewCreated() {
        view.displayData(show.getColor(), episodes);
    }

    @Override public void episodeSelected(int position) {
        view.showEpisodeDialog(show, episodes.get(position));
    }

    private void mapData(MediaWrapper mediaWrapper, int season) {

        List<Episode> episodes = new ArrayList<>();

        Show show = (Show) mediaWrapper.getMedia();
        for (Episode episode : show.getEpisodes()) {
            if (episode.getSeasion() == season) {
                episodes.add(episode);
            }
        }

        Collections.sort(episodes, (lhs, rhs) -> lhs.getEpisode() - rhs.getEpisode());

        this.episodes = episodes;

    }

}
