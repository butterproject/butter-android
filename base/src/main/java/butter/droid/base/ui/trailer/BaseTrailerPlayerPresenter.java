package butter.droid.base.ui.trailer;

import butter.droid.base.torrent.StreamInfo;

public interface BaseTrailerPlayerPresenter {

    void onDestroy();

    StreamInfo getStreamInfo();

    void onVideoUrlObtained(String videoUrl);

    void onErrorObtainingVideoUrl();

}
