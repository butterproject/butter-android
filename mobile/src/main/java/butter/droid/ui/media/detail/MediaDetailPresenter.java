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

import androidx.annotation.NonNull;
import butter.droid.base.providers.media.model.MediaWrapper;
import butter.droid.base.providers.media.model.StreamInfo;
import butter.droid.base.providers.subs.model.SubtitleWrapper;
import butter.droid.provider.base.model.Torrent;

public interface MediaDetailPresenter {
    void onCreate(@NonNull MediaWrapper media);

    void playMediaClicked();

    void selectTorrent(Torrent torrent);

    void openVideoPlayer(StreamInfo streamInfo);

    void openYouTube(String url);

    void selectSubtitle(SubtitleWrapper subtitle);

    void healthClicked();
}
