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

import butter.droid.base.providers.media.model.MediaWrapper;
import butter.droid.provider.base.model.Season;
import butter.droid.provider.base.model.Show;
import butter.droid.ui.media.detail.show.pager.model.UiShowDetailAbout;
import butter.droid.ui.media.detail.show.pager.model.UiShowDetailItem;
import butter.droid.ui.media.detail.show.pager.model.UiShowDetailSeason;
import java.util.ArrayList;
import java.util.List;

public class ShowDetailPresenterImpl implements ShowDetailPresenter {

    private final ShowDetailView view;

    private MediaWrapper mediaWrapper;

    public ShowDetailPresenterImpl(ShowDetailView view) {
        this.view = view;
    }

    @Override public void onCreate(MediaWrapper show) {
        if (show == null) {
            throw new IllegalStateException("Show not provided");
        }

        this.mediaWrapper = show;

    }

    @Override public void viewCreated(final boolean isTablet) {

        List<UiShowDetailItem> items = new ArrayList<>();

        if (isTablet) {
            view.displayAboutData(mediaWrapper);
        } else {
            items.add(new UiShowDetailAbout());
        }

        for (Season season : getAvailableSeasons()) {
            items.add(new UiShowDetailSeason(season));
        }

        view.displayData(mediaWrapper, items);

    }

    private Season[] getAvailableSeasons() {
        if (mediaWrapper.isShow()) {
            Show show = (Show) mediaWrapper.getMedia();
            return show.getSeasons();
        } else if (mediaWrapper.isSeason()) {
            return new Season[] { (Season) mediaWrapper.getMedia() };
        } else {
            throw new IllegalStateException("Unsupported media type");
        }
    }

}
