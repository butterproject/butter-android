package butter.droid.ui.trailer;

import butter.droid.base.providers.media.models.Media;
import butter.droid.base.torrent.StreamInfo;

public class TrailerPresenterImpl implements TrailerPresenter {

  private final TrailerView view;

  private Media media;
  private String youtubeUrl;
  private StreamInfo streamInfo;

  public TrailerPresenterImpl(TrailerView view) {
    this.view = view;
  }

  @Override
  public void onCreate(Media media, String youtubeUrl, StreamInfo streamInfo) {
    this.media = media;
    this.youtubeUrl = youtubeUrl;
    this.streamInfo = streamInfo;

    view.onDisableVideoPlayerSubsButton();
    view.onExecuteQueryYoutubeTask(youtubeUrl);
  }

  @Override
  public StreamInfo getStreamInfo() {
    return streamInfo;
  }
}
