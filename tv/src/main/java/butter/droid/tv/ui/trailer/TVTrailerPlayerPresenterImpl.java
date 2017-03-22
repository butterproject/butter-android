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

package butter.droid.tv.ui.trailer;

import butter.droid.base.providers.media.models.Media;
import butter.droid.base.torrent.StreamInfo;

public class TVTrailerPlayerPresenterImpl implements TVTrailerPlayerPresenter {

  private final TVTrailerPlayerView view;

  private StreamInfo streamInfo;

  private boolean errorDialogDisplayed;

  public TVTrailerPlayerPresenterImpl(TVTrailerPlayerView view) {
    this.view = view;
  }

  @Override
  public void onCreate(Media media, String youtubeUrl) {
    this.streamInfo = new StreamInfo(media, null, null, null, null, null);
    view.onDisableVideoPlayerSubsButton();
    view.onExecuteQueryYoutubeTask(youtubeUrl);
  }

  @Override
  public StreamInfo getStreamInfo() {
    return streamInfo;
  }

  @Override
  public void onVideoUrlObtained(String videoUrl) {
    streamInfo.setVideoLocation(videoUrl);
    view.onNotifyMediaReady();
  }

  @Override
  public void onErrorObtainingVideoUrl() {
    if (!errorDialogDisplayed) {
      errorDialogDisplayed = true;
      view.onDisplayErrorVideoDialog();
    }
  }
}
