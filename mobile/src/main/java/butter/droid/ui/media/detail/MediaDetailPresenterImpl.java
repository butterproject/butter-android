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
import butter.droid.base.providers.model.MediaWrapper;
import butter.droid.base.providers.model.StreamInfo;
import butter.droid.base.torrent.TorrentHealth;
import butter.droid.provider.base.module.Movie;
import butter.droid.provider.base.module.Torrent;

public class MediaDetailPresenterImpl implements MediaDetailPresenter {

    private final MediaDetailView view;
    private final PreferencesHandler preferencesHandler;
    private final NetworkManager networkManager;

    private MediaWrapper media;
    private String selectedSubtitleLanguage;
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

        if (media.isMovie()) {
            view.displayMovie(media);
        } else if (media.isShow()) {
            view.displayShow(media);
        } else { // TODO: 7/30/17 Support season and episode
            throw new IllegalStateException("Unknown show type");
        }
    }

    @Override public void playMediaClicked() {
        if (preferencesHandler.wifiOnly() && !networkManager.isWifiConnected() && !networkManager.isEthernetConnected()
                && networkManager.isNetworkConnected()) {
            view.displayDialog(R.string.wifi_only, R.string.wifi_only_message);
        } else {
            StreamInfo streamInfo = new StreamInfo(selectedTorrent, media, null);
            view.playStream(streamInfo);
        }

    }

    @Override public void selectTottent(Torrent torrent) {
        selectedTorrent = torrent;
    }

    @Override public void openVideoPlayer(StreamInfo streamInfo) {
        view.openVideoPlayer(streamInfo);
    }

    @Override public void openYouTube(String url) {
        view.openYouTube(media, url);
    }

    @Override public void selectSubtitle(String language) {
        selectedSubtitleLanguage = language;
    }

    @Override public void healthClicked() {
        Torrent torrent;
        if (selectedTorrent != null) {
            torrent = selectedTorrent;
        } else {
            Movie movie = (Movie) this.media.getMedia();
            torrent = movie.getTorrents()[0];
        }

        Integer seeds = torrent.getSeeds();
        Integer peers = torrent.getPeers();
        TorrentHealth health = TorrentHealth.calculate(seeds, peers);

        view.displayHealthInfo(health, seeds, peers);
    }

}
