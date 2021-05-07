package butter.droid.base.providers.media.response;

import android.content.Context;

import java.util.ArrayList;
import java.util.Map;

import butter.droid.base.content.preferences.Prefs;
import butter.droid.base.providers.media.MediaProvider;
import butter.droid.base.providers.media.models.Media;
import butter.droid.base.providers.media.response.models.DetailsResponse;
import butter.droid.base.providers.media.response.models.common.Quality;
import butter.droid.base.providers.media.response.models.shows.Episode;
import butter.droid.base.providers.media.response.models.shows.ShowDetails;
import butter.droid.base.providers.subs.SubsProvider;
import butter.droid.base.utils.PrefUtils;
import butter.droid.base.utils.StringUtils;

public class TVDetailsReponse extends DetailsResponse<ShowDetails> {

    public TVDetailsReponse() {
    }

    public ArrayList<Media> formatDetailForPopcorn(Context context, ShowDetails item, MediaProvider mediaProvider, SubsProvider subsProvider) {
        ArrayList<Media> list = new ArrayList<>();
        try {
            butter.droid.base.providers.media.models.Show show = new butter.droid.base.providers.media.models.Show();

            show.title = item.getTitle();
            show.videoId = item.getImdbId();
            show.imdbId = item.getImdbId();
            show.tvdbId = item.getTvdbId();
            show.seasons = item.getNumSeasons();
            show.year = item.getYear();

            if (item.getImages().getPoster() != null && !item.getImages().getPoster().contains("images/posterholder.png")) {
                show.image = item.getImages().getPoster().replace("/posters/", "/_cache/posters/");
                show.fullImage = item.getImages().getPoster();
            }
            if (item.getImages().getFanart() != null && !item.getImages().getFanart().contains("images/posterholder.png")) {
                show.headerImage = item.getImages().getFanart().replace("/original/", "/medium/");
            }

            if (item.getStatus() != null) {
                String status = item.getStatus();
                if (status.equalsIgnoreCase("ended")) {
                    show.status = butter.droid.base.providers.media.models.Show.Status.ENDED;
                } else if (status.equalsIgnoreCase("returning series")) {
                    show.status = butter.droid.base.providers.media.models.Show.Status.CONTINUING;
                } else if (status.equalsIgnoreCase("in production")) {
                    show.status = butter.droid.base.providers.media.models.Show.Status.CONTINUING;
                } else if (status.equalsIgnoreCase("canceled")) {
                    show.status = butter.droid.base.providers.media.models.Show.Status.CANCELED;
                }
            }

            show.country = item.getCountry();
            show.network = item.getNetwork();
            show.synopsis = item.getSynopsis();
            show.runtime = item.getRuntime();
            show.airDay = item.getAirDay();
            show.airTime = item.getAirTime();
            show.rating = Double.toString(item.getRating().getPercentage() / 10);

            show.genre = "";
            StringBuilder stringBuilder = new StringBuilder();
            for (String genre : item.getGenres()) {
                if (stringBuilder.length() > 0) {
                    stringBuilder.append(", ");
                }
                stringBuilder.append(StringUtils.capWords(genre));
            }
            show.genre = stringBuilder.toString();

            if (item.getLocale() != null) {
                if (!item.getLocale().getTitle().isEmpty()) {
                    switch (PrefUtils.get(context, Prefs.TRANSLATE_TITLE, "translated-origin")) {
                        case "translated-origin":
                            show.title2 = show.title;
                            show.title = item.getLocale().getTitle();
                            break;
                        case "origin-translated":
                            show.title2 = item.getLocale().getTitle();
                            break;
                        case "translated":
                            show.title = item.getLocale().getTitle();
                            break;
                        case "origin":
                            break;
                    }
                }
                if (!item.getLocale().getSynopsis().isEmpty() && PrefUtils.get(context, Prefs.TRANSLATE_SYNOPSIS, true)) {
                    show.synopsis = item.getLocale().getSynopsis();
                }
                if (!item.getLocale().getPoster().isEmpty() && PrefUtils.get(context, Prefs.TRANSLATE_POSTER, true)) {
                    show.image = item.getLocale().getPoster();
                    show.fullImage = item.getLocale().getPoster().replace("w500", "w1280");
                }
                if (!item.getLocale().getFanart().isEmpty() && PrefUtils.get(context, Prefs.TRANSLATE_POSTER, true)) {
                    show.headerImage = item.getLocale().getFanart().replace("w500", "original");
                }
            }

            for (Episode episode : item.getEpisodes()) {
                try {
                    butter.droid.base.providers.media.models.Episode episodeObject = new butter.droid.base.providers.media.models.Episode();

                    if (episode.getTorrents() != null) {
                        for (Map.Entry<String, Quality> entry : episode.getTorrents().getQualities().entrySet()) {
                            if (!entry.getKey().equals("0")) {
                                Media.Torrent torrent = new Media.Torrent(entry.getValue().getUrl(), entry.getValue().getFile(), entry.getValue().getSeeds(), entry.getValue().getPeers());
                                episodeObject.torrents.put(entry.getKey(), torrent);
                            }
                        }
                    }

                    episodeObject.showName = show.title;
                    episodeObject.dateBased = episode.isDateBased();
                    episodeObject.aired = episode.getFirstAired();
                    episodeObject.title = episode.getTitle();
                    episodeObject.overview = episode.getOverview();
                    episodeObject.season = episode.getSeason();
                    episodeObject.episode = episode.getEpisode();
                    episodeObject.videoId = show.videoId + episodeObject.season + episodeObject.episode;
                    episodeObject.imdbId = show.imdbId;
                    episodeObject.image = episodeObject.fullImage = episodeObject.headerImage = show.headerImage;

                    if (episode.getLocale() != null) {
                        if (!episode.getLocale().getTitle().isEmpty() && PrefUtils.get(context, Prefs.TRANSLATE_EPISODES, true)) {
                            episodeObject.title = episode.getLocale().getTitle();
                        }
                        if (!item.getLocale().getSynopsis().isEmpty() && PrefUtils.get(context, Prefs.TRANSLATE_SYNOPSIS, true)) {
                            episodeObject.overview = episode.getLocale().getOverview();
                        }
                    }

                    show.episodes.add(episodeObject);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            list.add(show);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }
}