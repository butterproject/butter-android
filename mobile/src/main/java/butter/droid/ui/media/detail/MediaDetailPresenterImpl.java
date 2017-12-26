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

package butter.droid.ui.media.detail;

import android.support.annotation.NonNull;
import butter.droid.R;
import butter.droid.base.content.preferences.PreferencesHandler;
import butter.droid.base.manager.network.NetworkManager;
import butter.droid.base.providers.media.model.MediaWrapper;
import butter.droid.base.providers.media.model.StreamInfo;
import butter.droid.base.providers.subs.model.SubtitleWrapper;
import butter.droid.base.torrent.TorrentHealth;
import butter.droid.provider.base.model.Movie;
import butter.droid.provider.base.model.Torrent;

public class MediaDetailPresenterImpl implements MediaDetailPresenter {

    private final MediaDetailView view;
    private final PreferencesHandler preferencesHandler;
    private final NetworkManager networkManager;

    private MediaWrapper media;
    private SubtitleWrapper subtitle = new SubtitleWrapper(); // By default load subs with default language
    private Torrent selectedTorrent;

    public MediaDetailPresenterImpl(MediaDetailView view, PreferencesHandler preferencesHandler,
            NetworkManager networkManager) {
        this.view = view;
        this.preferencesHandler = preferencesHandler;
        this.networkManager = networkManager;
    }

    @Override public void onCreate(@NonNull MediaWrapper media) {
        this.media = media;

        view.initMediaLayout(media);

        if (media.isStreamable()) {
            view.displayStreamable(media);
        } else if (media.isShow()) {
            view.displayShow(media);
        } else if (media.isSeason()) {
            view.displaySeason(media);
        } else {
            throw new IllegalStateException("Unknown show type");
        }
    }

    @Override public void playMediaClicked() {
        if (preferencesHandler.wifiOnly() && !networkManager.isWifiConnected() && !networkManager.isEthernetConnected()
                && networkManager.isNetworkConnected()) {
            view.displayDialog(R.string.wifi_only, R.string.wifi_only_message);
        } else {
            StreamInfo streamInfo = new StreamInfo(selectedTorrent, media, null, subtitle);
            view.playStream(streamInfo);
        }

    }

    @Override public void selectTorrent(Torrent torrent) {
        selectedTorrent = torrent;
    }

    @Override public void openVideoPlayer(StreamInfo streamInfo) {
        view.openVideoPlayer(streamInfo);
    }

    @Override public void openYouTube(String url) {
        view.openYouTube(media, url);
    }

    @Override public void selectSubtitle(SubtitleWrapper subtitle) {
        this.subtitle = subtitle;
    }

    @Override public void healthClicked() {
        Torrent torrent;
        if (selectedTorrent != null) {
            torrent = selectedTorrent;
        } else {
            Movie movie = (Movie) this.media.getMedia();
            torrent = movie.getTorrents()[0];
        }

        int seeds = torrent.getSeeds();
        int peers = torrent.getPeers();
        TorrentHealth health = TorrentHealth.calculate(seeds, peers);

        view.displayHealthInfo(health, seeds, peers);
    }

}
