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

package butter.droid.ui.beam.fragment;

import butter.droid.base.torrent.StreamInfo;

public interface BeamPlayerPresenter {

    void onCreate(StreamInfo streamInfo, long resumePosition);

    void onResume();

    void onPause();

    void playPauseClicked();

    void forwardClicked();

    void backwardClicked();

    void closePlayer();

    void onUserVolumeChanged(int progress);

    void seek(int progress);

    void beamVideo();

    void onViewCreated();
}
