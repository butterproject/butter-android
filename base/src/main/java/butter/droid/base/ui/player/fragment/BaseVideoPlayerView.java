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

package butter.droid.base.ui.player.fragment;

import butter.droid.base.subs.Caption;

public interface BaseVideoPlayerView {

    void attachVlcViews();

    void updatePlayPauseState(final boolean playing);

    void showOverlay();

    void setProgressVisible(boolean visible);

    void setKeepScreenOn(boolean keep);

    void onPlaybackEndReached();

    void onErrorEncountered();

    void onProgressChanged(long currentTime, int streamProgress, long duration);

    void showTimedCaptionText(Caption caption);

    void clearFrame();

    void showSubsSelectorDialog();

    void showPickSubsDialog(String[] readableNames, String[] adapterSubtitles, String currentSubsLang);

    void showSubsFilePicker();

    void displaySubsSizeDialog();

    void displaySubsTimingDialog(int subtitleOffset);

    void updateControlsState(boolean playing, long progress, int streamerProgress, long length);

    void updateSurfaceSize(int width, int height);
}
