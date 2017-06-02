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

package butter.droid.ui.media.detail;

import android.support.annotation.StringRes;
import butter.droid.base.torrent.StreamInfo;
import butter.droid.base.torrent.TorrentHealth;
import butter.droid.provider.base.Media;
import butter.droid.provider.base.Movie;
import butter.droid.provider.base.Show;

public interface MediaDetailView {
    void initMediaLayout(Media media);

    void displayMovie(Movie media);

    void displayShow(Show media);

    void displayDialog(@StringRes int title, @StringRes int message);

    void playStream(StreamInfo streamInfo);

    void openVideoPlayer(StreamInfo streamInfo);

    void openYouTube(Media media, String url);

    void displayHealthInfo(TorrentHealth health, int seeds, int peers);
}
