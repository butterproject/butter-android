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

package butter.droid.ui.media.detail.show;

import butter.droid.base.providers.media.models.Episode;
import butter.droid.base.providers.media.models.Show;
import butter.droid.ui.media.detail.show.pager.model.UiShowDetailAbout;
import butter.droid.ui.media.detail.show.pager.model.UiShowDetailItem;
import butter.droid.ui.media.detail.show.pager.model.UiShowDetailSeason;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ShowDetailPresenterImpl implements ShowDetailPresenter {

    private final ShowDetailView view;

    private Show show;

    public ShowDetailPresenterImpl(ShowDetailView view) {
        this.view = view;
    }

    @Override public void onCreate(Show show) {

        if (show == null) {
            throw new IllegalStateException("Show not provided");
        }

        this.show = show;

    }

    @Override public void viewCreated(final boolean isTablet) {

        List<UiShowDetailItem> items = new ArrayList<>();

        if (isTablet) {
            view.displayAboutData(show);
        } else {
            items.add(new UiShowDetailAbout());
        }

        ArrayList<Integer> availableSeasons = getAvailableSeasons();
        for (int season : availableSeasons) {
            items.add(new UiShowDetailSeason(season));
        }

        view.displayData(show, items);

    }

    private ArrayList<Integer> getAvailableSeasons() {
        final ArrayList<Integer> availableSeasons = new ArrayList<>();
        for (Episode episode : show.episodes) {
            if (!availableSeasons.contains(episode.season)) {
                availableSeasons.add(episode.season);
            }
        }
        Collections.sort(availableSeasons);
        return availableSeasons;
    }

}
