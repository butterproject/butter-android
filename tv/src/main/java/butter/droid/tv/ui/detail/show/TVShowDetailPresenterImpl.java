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
import butter.droid.base.torrent.StreamInfo;
import butter.droid.provider.base.module.Episode;
import butter.droid.provider.base.module.Media;
import butter.droid.provider.base.module.Show;
import butter.droid.provider.base.module.Torrent;
import butter.droid.tv.ui.detail.base.TVBaseDetailsPresenterImpl;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;

public class TVShowDetailPresenterImpl extends TVBaseDetailsPresenterImpl implements TVShowDetailsPresenter {

    private final TVShowDetailsView view;
    private final PreferencesHandler preferencesHandler;

    private Show item;

    public TVShowDetailPresenterImpl(final TVShowDetailsView view, final ProviderManager providerManager,
            final PreferencesHandler preferencesHandler) {
        super(view, providerManager);

        this.view = view;
        this.preferencesHandler = preferencesHandler;
    }

    @Override public void onCreate(final int providerId, final Show item) {
        super.onCreate(providerId, item);

        this.item = item;
    }

    @Override protected void detailsLoaded(final Media media) {
        super.detailsLoaded(media);
        this.item = (Show) media;

        addSeasons(item);
    }

    private void addSeasons(Show show) {
        final TreeMap<Integer, List<Episode>> seasons = new TreeMap<>((me, other) -> me - other);

        for (Episode episode : show.getEpisodes()) {
            // create list of season if does not exists
            if (!seasons.containsKey(episode.getSeasion())) {
                seasons.put(episode.getSeasion(), new ArrayList<>());
            }

            // add episode to the list
            final List<Episode> seasonEpisodes = seasons.get(episode.getSeasion());
            seasonEpisodes.add(episode);
        }

        for (Integer seasonKey : seasons.descendingKeySet()) {
            Collections.sort(seasons.get(seasonKey), (me, other) -> other.getEpisode() - me.getEpisode());
        }

        view.showSeasons(seasons);

    }

    @Override public void episodeClicked(final Episode episode) {
        Torrent[] torrents = episode.getTorrents();
        if (torrents.length == 1) {
            startTorrent(episode, torrents[0]);
        } else {
            view.pickTorrent(episode, torrents);
        }
    }

    @Override public void torrentSelected(final Episode episode, final Torrent torrent) {
        startTorrent(episode, torrent);
    }

    private void startTorrent(final Episode episode, final Torrent torrent) {
        String subtitleLanguage = preferencesHandler.getSubtitleDefaultLanguage();

        StreamInfo info = new StreamInfo(torrent, episode, item);

        view.torrentSelected(info);
    }
}
