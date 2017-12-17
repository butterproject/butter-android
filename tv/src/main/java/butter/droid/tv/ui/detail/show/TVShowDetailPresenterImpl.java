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

package butter.droid.tv.ui.detail.show;

import butter.droid.base.content.preferences.PreferencesHandler;
import butter.droid.base.manager.internal.provider.ProviderManager;
import butter.droid.base.providers.media.model.MediaWrapper;
import butter.droid.base.providers.media.model.StreamInfo;
import butter.droid.provider.base.model.Episode;
import butter.droid.provider.base.model.Season;
import butter.droid.provider.base.model.Show;
import butter.droid.provider.base.model.Torrent;
import butter.droid.tv.ui.detail.base.TVBaseDetailsPresenterImpl;

public class TVShowDetailPresenterImpl extends TVBaseDetailsPresenterImpl implements TVShowDetailsPresenter {

    private final TVShowDetailsView view;
    private final PreferencesHandler preferencesHandler;

    public TVShowDetailPresenterImpl(final TVShowDetailsView view, final ProviderManager providerManager,
            final PreferencesHandler preferencesHandler) {
        super(view, providerManager);

        this.view = view;
        this.preferencesHandler = preferencesHandler;
    }

    @Override public void onCreate(final MediaWrapper item) {
        super.onCreate(item);
    }

    @Override protected void detailsLoaded(final MediaWrapper media) {
        super.detailsLoaded(media);

        if (media.isShow()) {
            view.showSeasons(((Show) media.getMedia()).getSeasons());
        } else if (media.isSeason()) {
            view.showSeasons(new Season[] { ((Season) media.getMedia()) });
        } else {
            throw new IllegalStateException("Unsupported media type");
        }
    }

    @Override public void episodeClicked(final Episode episode) {
        Torrent[] torrents = episode.getTorrents();
        if (torrents.length == 1) {
            torrentSelected(episode, torrents[0]);
        } else {
            view.pickTorrent(episode, torrents);
        }
    }

    @Override public void torrentSelected(final Episode episode, final Torrent torrent) {
        // TODO: 7/30/17 We should probalby fech detail information
        startTorrent(new MediaWrapper(episode, item.getProviderId(), item.getColor()), torrent);
    }

    private void startTorrent(final MediaWrapper episode, final Torrent torrent) {
        String subtitleLanguage = preferencesHandler.getSubtitleDefaultLanguage();

        // TODO subs
        StreamInfo info = new StreamInfo(torrent, episode, item, null);

        view.torrentSelected(info);
    }
}
