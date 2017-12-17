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

import butter.droid.base.providers.media.model.MediaMeta;
import butter.droid.provider.base.model.Season;
import java.util.Arrays;

public class ShowDetailSeasonPresenterImpl implements ShowDetailSeasonPresenter {

    private final ShowDetailSeasonView view;

    private MediaMeta mediaMeta;
    private Season season;

    public ShowDetailSeasonPresenterImpl(ShowDetailSeasonView view) {
        this.view = view;
    }

    @Override public void onCreate(MediaMeta mediaMeta, Season season) {
        if (mediaMeta == null) {
            throw new IllegalStateException("Media Meta not provided");
        }
        if (season == null) {
            throw new IllegalStateException("Season not present");
        }

        this.mediaMeta = mediaMeta;
        this.season = season;
    }

    @Override public void onViewCreated() {
        view.displayData(mediaMeta.getColor(), Arrays.asList(season.getEpisodes()));
    }

    @Override public void episodeSelected(int position) {
        // TODO media wrapper
        view.showEpisodeDialog(mediaMeta, season.getEpisodes()[position]);
    }

}
