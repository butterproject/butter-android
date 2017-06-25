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

package butter.droid.tv.ui.detail.show;

import butter.droid.base.torrent.StreamInfo;
import butter.droid.provider.base.module.Episode;
import butter.droid.provider.base.module.Show;
import butter.droid.provider.base.module.Torrent;
import butter.droid.tv.ui.detail.base.TVBaseDetailView;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public interface TVShowDetailsView extends TVBaseDetailView {

    void showSeasons(TreeMap<Integer, List<Episode>> seasons);

    void torrentSelected(Show show, StreamInfo streaminfo);

    void pickTorrent(Episode episode, Map<String, Torrent> torrents);
}
