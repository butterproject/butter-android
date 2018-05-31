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

import androidx.annotation.ColorInt;
import butter.droid.base.providers.media.model.MediaMeta;
import butter.droid.provider.base.model.Episode;
import java.util.List;

public interface ShowDetailSeasonView {
    void displayData(@ColorInt int color, List<Episode> episodes);

    void showEpisodeDialog(MediaMeta mediaMeta, Episode episode);
}
