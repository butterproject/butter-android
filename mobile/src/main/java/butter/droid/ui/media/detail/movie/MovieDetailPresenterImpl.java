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

package butter.droid.ui.media.detail.movie;

import android.content.res.Resources;
import android.text.TextUtils;
import butter.droid.R;
import butter.droid.base.content.preferences.PreferencesHandler;
import butter.droid.base.manager.internal.media.MediaDisplayManager;
import butter.droid.base.manager.internal.provider.ProviderManager;
import butter.droid.base.manager.internal.vlc.PlayerManager;
import butter.droid.base.manager.internal.youtube.YouTubeManager;
import butter.droid.base.providers.subs.SubsProvider;
import butter.droid.base.torrent.StreamInfo;
import butter.droid.base.utils.LocaleUtils;
import butter.droid.base.utils.StringUtils;
import butter.droid.provider.base.filter.Genre;
import butter.droid.provider.base.module.Format;
import butter.droid.provider.base.module.Movie;
import butter.droid.provider.base.module.Torrent;
import butter.droid.ui.media.detail.MediaDetailPresenter;
import java.util.Locale;

public class MovieDetailPresenterImpl implements MovieDetailPresenter {

    private final MovieDetailView view;
    private final MediaDetailPresenter parentPresenter;
    private final YouTubeManager youTubeManager;
    private final PreferencesHandler preferencesHandler;
    private final ProviderManager providerManager;
    private final PlayerManager playerManager;
    private final Resources resources;
    private final MediaDisplayManager mediaDisplayManager;

    private Movie movie;
    private String[] subtitleLanguages;

    public MovieDetailPresenterImpl(MovieDetailView view, MediaDetailPresenter parentPresenter,
            YouTubeManager youTubeManager, PreferencesHandler preferencesHandler, ProviderManager providerManager,
            PlayerManager playerManager, Resources resources,
            final MediaDisplayManager mediaDisplayManager) {
        this.view = view;
        this.parentPresenter = parentPresenter;
        this.youTubeManager = youTubeManager;
        this.preferencesHandler = preferencesHandler;
        this.providerManager = providerManager;
        this.playerManager = playerManager;
        this.resources = resources;
        this.mediaDisplayManager = mediaDisplayManager;
    }

    @Override public void onCreate(Movie movie) {
        this.movie = movie;

        if (movie != null) {
            view.initLayout(movie);
            displayMetaData();
            displayRating();
            displaySynopsis();
            displaySubtitles();
            displayQualities();
        } else {
            throw new IllegalStateException("Movie can not be null");
        }
    }

    @Override public void openTrailer() {
        // TODO: 7/29/17 Null trailer
        if (!youTubeManager.isYouTubeUrl(movie.getTrailer())) {
            parentPresenter.openVideoPlayer(new StreamInfo(movie.getTrailer(), movie, null));
        } else {
            parentPresenter.openYouTube(movie.getTrailer());
        }
    }

    @Override public void selectQuality(int position) {
        Torrent torrent = movie.getTorrents()[position];
        parentPresenter.selectTottent(torrent);
        view.renderHealth(torrent);
        view.updateMagnet(torrent);
    }

    @Override public void openReadMore() {
        view.showReadMoreDialog(movie.getSynopsis());
    }

    @Override public void playMediaClicked() {
        parentPresenter.playMediaClicked();
    }

    @Override public void subtitleSelected(int position) {
        String[] languages = this.subtitleLanguages;
        if (languages != null && languages.length > position) {
            String language = languages[position];
            parentPresenter.selectSubtitle(language);
            if (!language.equals(SubsProvider.SUBTITLE_LANGUAGE_NONE)) {
                final Locale locale = LocaleUtils.toLocale(language);
                view.setSubtitleText(StringUtils.uppercaseFirst(locale.getDisplayName(locale)));
            } else {
                view.setSubtitleText(R.string.no_subs);
            }
        }
    }

    @Override public void healthClicked() {
        parentPresenter.healthClicked();
    }

    private void displayMetaData() {
        StringBuilder sb = new StringBuilder(String.valueOf(movie.getYear()));
        // TODO: 7/30/17 Runtime
        //        if (!TextUtils.isEmpty(movie.runtime)) {
//            sb.append(" • ")
//                    .append(movie.runtime)
//                    .append(' ')
//                    .append(resources.getString(R.string.minutes));
//        }

        Genre[] genres = movie.getGenres();
        if (genres.length > 0) {
            sb.append(" • ");
            for (int i = 0; i < genres.length; i++) {
                if (i != 0) {
                    sb.append(", ");
                }
                sb.append(genres[i].getName());
            }
        }

        view.displayMetaData(sb);
    }

    private void displayRating() {
        if (movie.getRating() != null) {
            view.displayRating((int) (movie.getRating() * 10));
        } else {
            view.hideRating();
        }
    }

    private void displaySynopsis() {
        if (!TextUtils.isEmpty(movie.getSynopsis())) {
            view.displaySynopsis(movie.getSynopsis());
        } else {
            view.hideSynopsis();
        }
    }


    private void displaySubtitles() {
        // TODO
        /*
        if (providerManager.hasCurrentSubsProvider()) {
            view.setSubtitleText(R.string.loading_subs);
            view.setSubtitleEnabled(false);

            providerManager.getCurrentSubsProvider().getList(movie, new SubsProvider.Callback() {
                @Override
                public void onSuccess(Map<String, String> subtitles) {
                    if (subtitles == null || subtitles.isEmpty()) {
                        ThreadUtils.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                view.setSubtitleText(R.string.no_subs_available);
                            }
                        });
                        return;
                    }

                    movie.subtitles = subtitles;

                    String[] languages = subtitles.keySet().toArray(new String[subtitles.size()]);
                    Arrays.sort(languages);
                    final String[] adapterLanguages = new String[languages.length + 1];
                    adapterLanguages[0] = SubsProvider.SUBTITLE_LANGUAGE_NONE;
                    System.arraycopy(languages, 0, adapterLanguages, 1, languages.length);
                    subtitleLanguages = adapterLanguages;

                    final String[] readableNames = new String[adapterLanguages.length];
                    for (int i = 0; i < readableNames.length; i++) {
                        String language = adapterLanguages[i];
                        if (language.equals(SubsProvider.SUBTITLE_LANGUAGE_NONE)) {
                            readableNames[i] = resources.getString(R.string.no_subs);
                        } else {
                            Locale locale = LocaleUtils.toLocale(language);
                            readableNames[i] = locale.getDisplayName(locale);
                        }
                    }

                    String defaultSubtitle = preferencesHandler.getSubtitleDefaultLanguage();
                    final int defaultIndex;
                    if (subtitles.containsKey(defaultSubtitle)) {
                        defaultIndex = Arrays.asList(adapterLanguages).indexOf(defaultSubtitle);
                    } else {
                        defaultIndex = Arrays.asList(adapterLanguages).indexOf(SubsProvider.SUBTITLE_LANGUAGE_NONE);
                    }

                    ThreadUtils.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            view.setSubtitleEnabled(true);
                            view.setSubsData(readableNames, defaultIndex);
                        }
                    });
                }

                @Override
                public void onFailure(Exception ex) {
                    ThreadUtils.runOnUiThread(new Runnable() {
                        @Override public void run() {
                            view.setSubtitleText(R.string.no_subs_available);
                            view.setSubtitleEnabled(false);
                        }
                    });
                }
            });
        } else {
            view.setSubtitleText(R.string.no_subs_available);
            view.setSubtitleEnabled(false);
        }
        */
    }

    private void displayQualities() {
        if (movie.getTorrents().length > 0) {
            final Format[] formats = mediaDisplayManager.getSortedTorrentFormats(movie.getTorrents());

            int defaultFormatIndex = mediaDisplayManager.getDefaultFormatIndex(formats);

            String[] qualities = new String[formats.length];
            for (int i = 0; i < formats.length; i++) {
                qualities[i] = mediaDisplayManager.getFormatDisplayName(formats[i]);
            }

            view.setQualities(qualities, qualities[defaultFormatIndex]);
            selectQuality(defaultFormatIndex);
        }

    }

}
