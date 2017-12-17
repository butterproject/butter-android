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

import android.graphics.Color;
import butter.droid.provider.base.model.Season;
import java.util.Arrays;

public class ShowDetailSeasonPresenterImpl implements ShowDetailSeasonPresenter {

    private final ShowDetailSeasonView view;

    private Season season;

    public ShowDetailSeasonPresenterImpl(ShowDetailSeasonView view) {
        this.view = view;
    }


    @Override public void onCreate(Season season) {
        if (season == null) {
            throw new IllegalStateException("Season not present");
        }

        this.season = season;
    }

    @Override public void onViewCreated() {
        // TODO do someting with color
        view.displayData(Color.TRANSPARENT, Arrays.asList(season.getEpisodes()));
    }

    @Override public void episodeSelected(int position) {
        // TODO show info? do we need it
        view.showEpisodeDialog(null, season.getEpisodes()[position]);
    }

}
