package butter.droid.ui.trailer;

import butter.droid.base.providers.media.models.Media;
import butter.droid.base.torrent.StreamInfo;

public class TrailerPresenterImpl implements TrailerPresenter {

  private final TrailerView view;

  private StreamInfo streamInfo;

  private boolean errorDialogDisplayed;

  public TrailerPresenterImpl(TrailerView view) {
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
