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

package butter.droid.base.ui.trailer;

import android.net.Uri;
import android.os.AsyncTask;
import butter.droid.base.content.preferences.PreferencesHandler;
import butter.droid.base.manager.internal.phone.PhoneManager;
import butter.droid.base.manager.internal.vlc.VlcPlayer;
import butter.droid.base.manager.internal.youtube.YouTubeManager;
import butter.droid.base.manager.network.NetworkManager;
import butter.droid.base.ui.player.base.BaseVideoPlayerPresenterImpl;
import butter.droid.provider.base.module.Media;
import java.net.URLDecoder;
import timber.log.Timber;

public class BaseTrailerPlayerPresenterImpl extends BaseVideoPlayerPresenterImpl implements BaseTrailerPlayerPresenter {

    private final BaseTrailerPlayerView view;
    private final YouTubeManager youTubeManager;
    private final NetworkManager networkManager;
    private final PhoneManager phoneManager;

    private QueryYouTubeTask queryYouTubeTask;

    public BaseTrailerPlayerPresenterImpl(final BaseTrailerPlayerView view, final PreferencesHandler preferencesHandler,
            final VlcPlayer player, final YouTubeManager youTubeManager,
            final NetworkManager networkManager, final PhoneManager phoneManager) {
        super(view, preferencesHandler, player);

        this.view = view;
        this.youTubeManager = youTubeManager;
        this.networkManager = networkManager;
        this.phoneManager = phoneManager;
    }

    @Override public void onCreate(final Media media, final String trailerUri, long resumePosition) {
        super.onCreate(resumePosition);

        if (youTubeManager.isYouTubeUrl(trailerUri)) {
            queryYouTubeTask = new QueryYouTubeTask(youTubeManager, networkManager, phoneManager);
            queryYouTubeTask.execute(youTubeManager.getYouTubeVideoId(trailerUri));
        } else {
            loadMedia(Uri.parse(trailerUri));
        }
    }

    @Override public void onDestroy() {
        super.onDestroy();

        if (queryYouTubeTask != null) {
            queryYouTubeTask.cancel(true);
            queryYouTubeTask = null;
        }
    }

    @Override public void onViewCreated() {
        view.setupControls(media.getTitle());
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
            if (result != null) {
                final String videoUrl = URLDecoder.decode(result.toString());
                loadMedia(Uri.parse(videoUrl));
            } else {
                view.onErrorEncountered();
            }
        }

    }

}
