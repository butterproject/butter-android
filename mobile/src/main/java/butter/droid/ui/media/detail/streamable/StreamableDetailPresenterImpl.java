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

package butter.droid.ui.media.detail.streamable;

import android.content.res.Resources;
import android.text.TextUtils;

import java.util.ArrayList;

import javax.inject.Inject;

import butter.droid.R;
import butter.droid.base.content.preferences.PreferencesHandler;
import butter.droid.base.manager.internal.media.MediaDisplayManager;
import butter.droid.base.manager.internal.provider.ProviderManager;
import butter.droid.base.manager.internal.youtube.YouTubeManager;
import butter.droid.base.providers.media.model.MediaWrapper;
import butter.droid.base.providers.media.model.StreamInfo;
import butter.droid.base.providers.subs.model.SubtitleWrapper;
import butter.droid.base.ui.FragmentScope;
import butter.droid.provider.base.filter.Genre;
import butter.droid.provider.base.model.Movie;
import butter.droid.provider.base.model.Streamable;
import butter.droid.provider.base.model.Torrent;
import butter.droid.provider.subs.model.Subtitle;
import butter.droid.ui.media.detail.MediaDetailPresenter;
import butter.droid.ui.media.detail.dialog.quality.model.UiQuality;
import butter.droid.ui.media.detail.dialog.subs.SubsPickerParent;
import io.reactivex.MaybeObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

@FragmentScope
public class StreamableDetailPresenterImpl implements StreamableDetailPresenter, SubsPickerParent {

    private final StreamableDetailView view;
    private final MediaDetailPresenter parentPresenter;
    private final YouTubeManager youTubeManager;
    private final PreferencesHandler preferencesHandler;
    private final ProviderManager providerManager;
    private final Resources resources;
    private final MediaDisplayManager mediaDisplayManager;

    private MediaWrapper mediaWrapper;

    private Subtitle selectedSub;

    // TODO: 11/5/17 Saved instance state
    private Torrent[] sortedTorrents;
    private int selectedQuality;

    private Disposable subtitlesRequest;

    @Inject
    public StreamableDetailPresenterImpl(StreamableDetailView view, MediaDetailPresenter parentPresenter,
            YouTubeManager youTubeManager, PreferencesHandler preferencesHandler, ProviderManager providerManager,
            final Resources resources, final MediaDisplayManager mediaDisplayManager) {
        this.view = view;
        this.parentPresenter = parentPresenter;
        this.youTubeManager = youTubeManager;
        this.preferencesHandler = preferencesHandler;
        this.providerManager = providerManager;
        this.resources = resources;
        this.mediaDisplayManager = mediaDisplayManager;
    }

    @Override public void onCreate(MediaWrapper movie) {
        this.mediaWrapper = movie;

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

    @Override public void onDestroy() {
        if (subtitlesRequest != null) {
            subtitlesRequest.dispose();
            subtitlesRequest = null;
        }
    }

    @Override public void openTrailer() {
        // TODO: 7/29/17 Null trailer
        Movie movie = (Movie) this.mediaWrapper.getMedia();
        if (!youTubeManager.isYouTubeUrl(movie.getTrailer())) {
            parentPresenter.openVideoPlayer(new StreamInfo(movie.getTrailer(), mediaWrapper, null));
        } else {
            parentPresenter.openYouTube(movie.getTrailer());
        }
    }

    @Override public void selectQuality(int position) {
        selectedQuality = position;
        Torrent torrent = sortedTorrents[position];
        parentPresenter.selectTorrent(torrent);
        view.renderHealth(torrent);
        view.updateMagnet(torrent);
        view.displayQuality(mediaDisplayManager.getFormatDisplayName(torrent.getFormat()));
        view.hideDialog();
    }

    @Override public void openReadMore() {
        view.showReadMoreDialog(mediaWrapper.getMedia().getSynopsis());
    }

    @Override public void playMediaClicked() {
        parentPresenter.playMediaClicked();
    }

    @Override public void subtitleSelected(Subtitle subtitle) {
        this.selectedSub = subtitle;
        parentPresenter.selectSubtitle(new SubtitleWrapper(subtitle));

        if (subtitle.getLanguage() == null) {
            view.setSubtitleText(R.string.no_subs);
        } else {
            view.setSubtitleText(subtitle.getName());
        }
    }

    @Override public void healthClicked() {
        parentPresenter.healthClicked();
    }

    @Override public void onSubtitlesClicked() {
        view.displaySubsPicker(mediaWrapper, selectedSub);
    }

    @Override public void onQualityClicked() {
        Torrent[] torrents = sortedTorrents;
        if (torrents.length > 0) {
            ArrayList<UiQuality> qualities = new ArrayList<>(torrents.length);
            for (int i = 0; i < torrents.length; i++) {
                qualities.add(new UiQuality(selectedQuality == i,
                        mediaDisplayManager.getFormatDisplayName(torrents[i].getFormat())));
            }

            view.displayQualityPicker(qualities);
        }
    }

    private void displayMetaData() {
        StringBuilder sb = new StringBuilder(String.valueOf(mediaWrapper.getMedia().getYear()));
        // TODO: 7/30/17 Runtime
        //        if (!TextUtils.isEmpty(movie.runtime)) {
//            sb.append(" • ")
//                    .append(movie.runtime)
//                    .append(' ')
//                    .append(resources.getString(R.string.minutes));
//        }

        Genre[] genres = mediaWrapper.getMedia().getGenres();
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
        float rating = mediaWrapper.getMedia().getRating();
        if (rating > -1) {
            view.displayRating((int) (rating * 10));
        } else {
            view.hideRating();
        }
    }

    private void displaySynopsis() {
        String synopsis = mediaWrapper.getMedia().getSynopsis();
        if (!TextUtils.isEmpty(synopsis)) {
            view.displaySynopsis(synopsis);
        } else {
            view.hideSynopsis();
        }
    }

    private void displaySubtitles() {
        if (providerManager.hasSubsProvider(mediaWrapper.getProviderId())) {
            view.subtitleVisibility(true);
            view.setSubtitleText(R.string.loading_subs);

            final String subsLanguage = preferencesHandler.getSubtitleDefaultLanguage();
            if (subsLanguage != null) {
                providerManager.getSubsProvider(mediaWrapper.getProviderId())
                        .getSubtitle(mediaWrapper.getMedia(), subsLanguage)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new MaybeObserver<Subtitle>() {
                            @Override public void onSubscribe(Disposable d) {
                                subtitlesRequest = d;
                            }

                            @Override public void onSuccess(Subtitle subtitle) {
                                subtitleSelected(subtitle);
                            }

                            @Override public void onError(Throwable e) {

                            }

                            @Override public void onComplete() {
                                view.setSubtitleText(R.string.no_subs);
                                parentPresenter.selectSubtitle(new SubtitleWrapper());
                            }
                        });
            } else {
                view.setSubtitleText(R.string.no_subs);
            }
        } else {
            view.subtitleVisibility(false);
        }
    }

    private void displayQualities() {
        Torrent[] torrents = mediaDisplayManager.getSortedTorrents(
                ((Streamable) mediaWrapper.getMedia()).getTorrents());
        sortedTorrents = torrents;
        if (torrents.length > 0) {
            int defaultFormatIndex = mediaDisplayManager.getDefaultFormatIndex(torrents);
            view.displayQuality(mediaDisplayManager.getFormatDisplayName(torrents[defaultFormatIndex].getFormat()));
            selectQuality(defaultFormatIndex);
        }

    }

}
