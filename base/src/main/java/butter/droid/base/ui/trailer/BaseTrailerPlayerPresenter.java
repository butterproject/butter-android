package butter.droid.base.ui.trailer;

import android.content.Context;
import butter.droid.base.providers.media.models.Media;
import butter.droid.base.torrent.StreamInfo;

public interface BaseTrailerPlayerPresenter {

    void onCreate(Context context, Media media, String youtubeUrl);

    void onDestroy();

    StreamInfo getStreamInfo();

    void onVideoUrlObtained(String videoUrl);

    void onErrorObtainingVideoUrl();

}
