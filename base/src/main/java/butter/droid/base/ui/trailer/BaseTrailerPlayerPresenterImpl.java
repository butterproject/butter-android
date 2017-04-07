package butter.droid.base.ui.trailer;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import butter.droid.base.R;
import butter.droid.base.manager.internal.youtube.YouTubeManager;
import butter.droid.base.manager.network.NetworkManager;
import butter.droid.base.manager.internal.phone.PhoneManager;
import butter.droid.base.providers.media.models.Media;
import butter.droid.base.torrent.StreamInfo;
import java.net.URLDecoder;
import timber.log.Timber;

public class BaseTrailerPlayerPresenterImpl implements BaseTrailerPlayerPresenter {

    private final Context context;
    private final BaseTrailerPlayerView view;
    private final YouTubeManager youTubeManager;
    private final NetworkManager networkManager;
    private final PhoneManager phoneManager;

    private StreamInfo streamInfo;

    private QueryYouTubeTask queryYouTubeTask;

    public BaseTrailerPlayerPresenterImpl(final Context context, final BaseTrailerPlayerView view, final YouTubeManager youTubeManager,
            final NetworkManager networkManager, final PhoneManager phoneManager) {
        this.context = context;
        this.view = view;
        this.youTubeManager = youTubeManager;
        this.networkManager = networkManager;
        this.phoneManager = phoneManager;
    }

    @Override
    public void onCreate(Media media, String youtubeUrl) {
        media.title += " " + context.getString(R.string.trailer);
        this.streamInfo = new StreamInfo(media, null, null, null, null, null);
        view.onDisableVideoPlayerSubsButton();
        this.queryYouTubeTask = new QueryYouTubeTask(youTubeManager, networkManager, phoneManager);
        queryYouTubeTask.execute(youTubeManager.getYouTubeVideoId(youtubeUrl));
    }

    @Override
    public void onDestroy() {
        if (queryYouTubeTask != null && !queryYouTubeTask.isCancelled()) {
            queryYouTubeTask.cancel(true);
            queryYouTubeTask = null;
        }
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
        view.onDisplayErrorVideoDialog();
    }

    private class QueryYouTubeTask extends AsyncTask<String, Void, Uri> {

        private final YouTubeManager youTubeManager;
        private final NetworkManager networkManager;
        private final PhoneManager phoneManager;

        private QueryYouTubeTask(final YouTubeManager youTubeManager, NetworkManager networkManager, PhoneManager phoneManager) {
            this.youTubeManager = youTubeManager;
            this.networkManager = networkManager;
            this.phoneManager = phoneManager;
        }

        @Override
        protected Uri doInBackground(String... params) {
            final String videoId = params[0];
            try {
                int videoQuality;
                if (networkManager.isWifiConnected() || networkManager.isEthernetConnected()) {
                    videoQuality = YouTubeManager.QUALITY_HIGH_MP4;
                } else if (phoneManager.isPhone() && phoneManager.isConnected() && phoneManager.isHighSpeedConnection()) {
                    videoQuality = YouTubeManager.QUALITY_NORMAL_MP4;
                } else {
                    videoQuality = YouTubeManager.QUALITY_MEDIUM_3GPP;
                }
                // calculate the actual URL of the video, encoded with proper YouTube token
                final String uriStr = youTubeManager.calculateYouTubeUrl(videoQuality, true, videoId);
                return Uri.parse(uriStr);
            } catch (Exception e) {
                Timber.e("Error occurred while retrieving information from YouTube", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(Uri result) {
            if (isCancelled()) {
                return;
            }
            if (result != null) {
                final String videoUrl = URLDecoder.decode(result.toString());
                onVideoUrlObtained(videoUrl);
            } else {
                onErrorObtainingVideoUrl();
            }
        }

    }
}
