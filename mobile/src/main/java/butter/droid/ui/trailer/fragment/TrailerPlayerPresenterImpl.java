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

package butter.droid.ui.trailer.fragment;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import butter.droid.base.content.preferences.PreferencesHandler;
import butter.droid.base.manager.internal.beaming.BeamManager;
import butter.droid.base.manager.internal.phone.PhoneManager;
import butter.droid.base.manager.internal.provider.ProviderManager;
import butter.droid.base.manager.internal.vlc.PlayerManager;
import butter.droid.base.manager.internal.vlc.VlcPlayer;
import butter.droid.base.manager.internal.youtube.YouTubeManager;
import butter.droid.base.manager.network.NetworkManager;
import butter.droid.base.manager.prefs.PrefManager;
import butter.droid.base.torrent.StreamInfo;
import butter.droid.manager.internal.audio.AudioManager;
import butter.droid.manager.internal.brightness.BrightnessManager;
import butter.droid.ui.player.abs.AbsPlayerPresenterImpl;
import butter.droid.ui.player.stream.PlayerPresenter;
import butter.droid.ui.player.stream.PlayerView;
import butter.droid.ui.player.abs.VideoPlayerTouchHandler;
import butter.droid.ui.player.abs.VideoPlayerTouchHandler.OnVideoTouchListener;
import java.net.URLDecoder;
import timber.log.Timber;

public class TrailerPlayerPresenterImpl extends AbsPlayerPresenterImpl implements PlayerPresenter, OnVideoTouchListener {

    private final YouTubeManager youTubeManager;
    private final NetworkManager networkManager;
    private final PhoneManager phoneManager;

    private final VlcPlayer player;

    public TrailerPlayerPresenterImpl(final PlayerView view, final Context context, final PrefManager prefManager,
            final PreferencesHandler preferencesHandler, final ProviderManager providerManager, final PlayerManager playerManager,
            final BeamManager beamManager, final BrightnessManager brightnessManager, final AudioManager audioManager,
            final VideoPlayerTouchHandler touchHandler, final VlcPlayer player, final YouTubeManager youTubeManager,
            final NetworkManager networkManager, final PhoneManager phoneManager) {
        super(view, context, prefManager, preferencesHandler, providerManager, playerManager, beamManager, brightnessManager, audioManager,
                touchHandler, player);

        this.player = player;

        this.youTubeManager = youTubeManager;
        this.networkManager = networkManager;
        this.phoneManager = phoneManager;
    }

    @Override public void onCreate(final StreamInfo streamInfo, final long resumePosition) {
//        super.onCreate(streamInfo, resumePosition);

        if (!player.initialize()) {
            // TODO: 4/2/17 Stop activity & maybe show error
            return;
        }

        player.setCallback(this);

        new QueryYouTubeTask(youTubeManager, networkManager, phoneManager).execute(youTubeManager.getYouTubeVideoId("https://www.youtube.com/watch?v=uOFDmbUlrT4"));
    }

    @Override public void onResume() {
        super.onResume();
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
                loadMedia(Uri.parse(videoUrl));
//                onVideoUrlObtained(videoUrl);
            } else {
//                onErrorObtainingVideoUrl();
            }
        }

    }

}
