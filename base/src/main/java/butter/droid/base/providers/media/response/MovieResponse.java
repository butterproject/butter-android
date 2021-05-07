package butter.droid.base.providers.media.response;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butter.droid.base.content.preferences.Prefs;
import butter.droid.base.providers.media.MediaProvider;
import butter.droid.base.providers.media.models.Media;
import butter.droid.base.providers.media.response.models.Response;
import butter.droid.base.providers.media.response.models.common.Locale;
import butter.droid.base.providers.media.response.models.movies.Language;
import butter.droid.base.providers.media.response.models.movies.Movie;
import butter.droid.base.providers.media.response.models.movies.Quality;
import butter.droid.base.providers.subs.SubsProvider;
import butter.droid.base.utils.PrefUtils;
import butter.droid.base.utils.StringUtils;

public class MovieResponse extends Response<Movie> {

    public MovieResponse(List<Movie> responseItems) {
        super(responseItems);
    }

    public ArrayList<Media> formatListForPopcorn(Context context, ArrayList<Media> existingList, MediaProvider mediaProvider, SubsProvider subsProvider) {
        for (Movie item : responseItems) {

            butter.droid.base.providers.media.models.Movie movie = new butter.droid.base.providers.media.models.Movie();

            movie.videoId = item.getImdbId();
            movie.imdbId = movie.videoId;

            movie.title = item.getTitle();
            movie.year = item.getYear();

            List<String> genres = item.getGenres();
            movie.genre = "";
            if (genres.size() > 0) {
                StringBuilder stringBuilder = new StringBuilder();
                for (String genre : genres) {
                    if (stringBuilder.length() > 0) {
                        stringBuilder.append(", ");
                    }
                    stringBuilder.append(StringUtils.capWords(genre));
                }
                movie.genre = stringBuilder.toString();
            }

            movie.rating = Double.toString(item.getRating().getPercentage() / 10);
            movie.trailer = item.getTrailer();
            movie.runtime = item.getRuntime();
            movie.synopsis = item.getSynopsis();
            movie.certification = item.getCertification();

            if (item.getImages().getPoster() != null && !item.getImages().getPoster().contains("images/posterholder.png")) {
                movie.image = item.getImages().getPoster();
                movie.fullImage = item.getImages().getPoster().replace("w500", "w1280");
            }
            if (item.getImages().getFanart() != null && !item.getImages().getFanart().contains("images/posterholder.png")) {
                movie.headerImage = item.getImages().getFanart().replace("w500", "original");
            }

            if (item.getTorrents() != null) {
                for (Map.Entry<String, Language> language : item.getTorrents().getLanguages().entrySet()) {
                    Map<String, Media.Torrent> torrentMap = new HashMap<>();
                    for (Map.Entry<String, Quality> torrentQuality : language.getValue().getQualities().entrySet()) {
                        if (torrentQuality == null) continue;
                        Media.Torrent torrent = new Media.Torrent(torrentQuality.getValue().getUrl(), torrentQuality.getValue().getFile(), torrentQuality.getValue().getSeeds(), torrentQuality.getValue().getPeers());
                        torrentMap.put(torrentQuality.getKey(), torrent);
                    }
                    movie.torrents.put(language.getKey(), torrentMap);
                }
            }

            if (item.getLocale() != null) {
                if (!item.getLocale().getTitle().isEmpty()) {
                    switch (PrefUtils.get(context, Prefs.TRANSLATE_TITLE, "translated-origin")) {
                        case "translated-origin":
                            movie.title2 = movie.title;
                            movie.title = item.getLocale().getTitle();
                            break;
                        case "origin-translated":
                            movie.title2 = item.getLocale().getTitle();
                            break;
                        case "translated":
                            movie.title = item.getLocale().getTitle();
                            break;
                        case "origin":
                            break;
                    }
                }
                if (!item.getLocale().getSynopsis().isEmpty() && PrefUtils.get(context, Prefs.TRANSLATE_SYNOPSIS, true)) {
                    movie.synopsis = item.getLocale().getSynopsis();
                }
                if (!item.getLocale().getPoster().isEmpty() && PrefUtils.get(context, Prefs.TRANSLATE_POSTER, true)) {
                    movie.image = item.getLocale().getPoster();
                    movie.fullImage = item.getLocale().getPoster().replace("w500", "w1280");
                }
                if (!item.getLocale().getFanart().isEmpty() && PrefUtils.get(context, Prefs.TRANSLATE_POSTER, true)) {
                    movie.headerImage = item.getLocale().getFanart().replace("w500", "original");
                }
            }

            existingList.add(movie);
        }
        return existingList;
    }
}