package butter.droid.base.providers.media.response;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import butter.droid.base.content.preferences.Prefs;
import butter.droid.base.providers.media.MediaProvider;
import butter.droid.base.providers.media.models.Media;
import butter.droid.base.providers.media.response.models.Response;
import butter.droid.base.providers.media.response.models.shows.Show;
import butter.droid.base.providers.subs.SubsProvider;
import butter.droid.base.utils.PrefUtils;

public class TVResponse extends Response<Show> {

    public TVResponse(List<Show> responseItems) {
        super(responseItems);
    }

    public ArrayList<Media> formatListForPopcorn(Context context, ArrayList<Media> existingList, MediaProvider mediaProvider, SubsProvider subsProvider) {
        for (Show item : responseItems) {
            butter.droid.base.providers.media.models.Show show = new butter.droid.base.providers.media.models.Show();

            show.title = item.getTitle();
            show.videoId = item.getImdbId();
            show.seasons = item.getNumSeasons();
            show.tvdbId = item.getTvdbId();
            show.year = item.getYear();
            if (item.getImages().getPoster() != null && !item.getImages().getPoster().contains("images/posterholder.png")) {
                show.image = item.getImages().getPoster();
                show.fullImage = item.getImages().getPoster().replace("w500", "w1280");
            }
            if (item.getImages().getFanart() != null && item.getImages().getFanart().contains("images/posterholder.png")) {
                show.headerImage = item.getImages().getFanart().replace("w500", "original");
            }

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

            existingList.add(show);
        }
        return existingList;
    }
}