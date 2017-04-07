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

package butter.droid.ui.beam;

import butter.droid.base.manager.internal.beaming.BeamManager;
import butter.droid.base.torrent.StreamInfo;

public class BeamPlayerActivityPresenterImpl implements BeamPlayerActivityPresenter {

    private final BeamPlayerActivityView view;
    private final BeamManager beamManager;

    public BeamPlayerActivityPresenterImpl(final BeamPlayerActivityView view, final BeamManager beamManager) {
        this.view = view;
        this.beamManager = beamManager;
    }

    @Override public void closePlayer() {
        view.closePlayer();
    }

    @Override public void fallbackToVideoPlayer(final StreamInfo streamInfo, final int resumePosition) {
        view.fallbackToVideoPlayer(streamInfo, resumePosition);
    }

    @Override public void stopVideo() {
        beamManager.stopVideo();
        closePlayer();
    }
}
