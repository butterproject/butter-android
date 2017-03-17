package butter.droid.ui.trailer;

import butter.droid.base.providers.media.models.Media;
import butter.droid.base.torrent.StreamInfo;

public interface TrailerPresenter {

  void onCreate(final Media media, final String youtubeUrl, StreamInfo streamInfo);

  StreamInfo getStreamInfo();
}
