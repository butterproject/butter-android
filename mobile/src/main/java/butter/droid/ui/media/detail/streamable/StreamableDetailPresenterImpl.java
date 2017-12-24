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
import butter.droid.R;
import butter.droid.base.content.preferences.PreferencesHandler;
import butter.droid.base.manager.internal.media.MediaDisplayManager;
import butter.droid.base.manager.internal.provider.ProviderManager;
import butter.droid.base.manager.internal.vlc.PlayerManager;
import butter.droid.base.manager.internal.youtube.YouTubeManager;
import butter.droid.base.providers.media.model.MediaWrapper;
import butter.droid.base.providers.media.model.StreamInfo;
import butter.droid.provider.base.filter.Genre;
import butter.droid.provider.base.model.Format;
import butter.droid.provider.base.model.Movie;
import butter.droid.provider.base.model.Streamable;
import butter.droid.provider.base.model.Torrent;
import butter.droid.ui.media.detail.MediaDetailPresenter;
import butter.droid.ui.media.detail.model.UiSubItem;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import java.util.Collections;
import java.util.List;

public class StreamableDetailPresenterImpl implements StreamableDetailPresenter {

    private final StreamableDetailView view;
    private final MediaDetailPresenter parentPresenter;
    private final YouTubeManager youTubeManager;
    private final PreferencesHandler preferencesHandler;
    private final ProviderManager providerManager;
    private final PlayerManager playerManager;
    private final Resources resources;
    private final MediaDisplayManager mediaDisplayManager;

    private MediaWrapper mediaWrapper;

    // TODO: 11/5/17 Saved instance state
    private List<UiSubItem> subtitleList;
    private UiSubItem selectedSub;

    public StreamableDetailPresenterImpl(StreamableDetailView view, MediaDetailPresenter parentPresenter,
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
        Torrent torrent = ((Streamable) mediaWrapper.getMedia()).getTorrents()[position];
        parentPresenter.selectTorrent(torrent);
        view.renderHealth(torrent);
        view.updateMagnet(torrent);
    }

    @Override public void openReadMore() {
        view.showReadMoreDialog(mediaWrapper.getMedia().getSynopsis());
    }

    @Override public void playMediaClicked() {
        parentPresenter.playMediaClicked();
    }

    @Override public void subtitleSelected(UiSubItem item) {
        UiSubItem selectedSub = this.selectedSub;
        if (selectedSub != null) {
            selectedSub.setSelected(false);
        }

        this.selectedSub = item;
        item.setSelected(true);

        String language = item.getLanguage();
        parentPresenter.selectSubtitle(item.getSubtitle());

        if (language == null) {
            view.setSubtitleText(R.string.no_subs);
        } else {
            view.setSubtitleText(item.getName());
        }
        view.hideDialog();
    }

    @Override public void healthClicked() {
        parentPresenter.healthClicked();
    }

    @Override public void onSubtitlesClicked() {
        List<UiSubItem> subtitleList = this.subtitleList;
        if (subtitleList != null && !subtitleList.isEmpty()) {
            view.displaySubsPicker(subtitleList);
        } // else ignore click (TODO maybe show error)
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
        Float rating = mediaWrapper.getMedia().getRating();
        if (rating != null) {
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
        if (providerManager.hasCurrentSubsProvider()) {
            view.setSubtitleText(R.string.loading_subs);
            view.setSubtitleEnabled(false);

            providerManager.getCurrentSubsProvider().list(mediaWrapper.getMedia())
                    .flatMap(subs -> {
                        if (subs.isEmpty()) {
                            return Single.<List<UiSubItem>>just(Collections.EMPTY_LIST);
                        } else {
                            final String defaultSubtitle = preferencesHandler.getSubtitleDefaultLanguage();
                            return Observable.fromIterable(subs)
                                    .map(sub -> new UiSubItem(sub, defaultSubtitle.equals(sub.getLanguage())))
                                    .startWith(new UiSubItem(null, defaultSubtitle == null))
                                    .toList();
                        }
                    })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SingleObserver<List<UiSubItem>>() {
                        @Override public void onSubscribe(final Disposable d) {

                        }

                        @Override public void onSuccess(final List<UiSubItem> subs) {
                            if (subs.isEmpty()) {
                                view.setSubtitleText(R.string.no_subs_available);
                                subtitleList = null;
                            } else {
                                view.setSubtitleEnabled(true);
                                subtitleList = subs;

                                UiSubItem selectedItem = null;
                                for (final UiSubItem sub : subs) {
                                    if (sub.isSelected()) {
                                        selectedItem = sub;
                                        String name = sub.getName();
                                        if (TextUtils.isEmpty(name)) {
                                            view.setSubtitleText(R.string.no_subs);
                                        } else {
                                            view.setSubtitleText(name);
                                        }
                                        break;
                                    }
                                }
                                if (selectedItem == null) {
                                    selectedItem = subs.get(0);
                                }

                                selectedSub = selectedItem;
                                parentPresenter.selectSubtitle(selectedItem.getSubtitle());
                            }
                        }

                        @Override public void onError(final Throwable e) {
                            subtitleList = null;
                            view.setSubtitleText(R.string.no_subs_available);
                            view.setSubtitleEnabled(false);
                        }
                    });
        } else {
            view.setSubtitleText(R.string.no_subs_available);
            view.setSubtitleEnabled(false);
        }
    }

    private void displayQualities() {
        Torrent[] torrents = ((Streamable) mediaWrapper.getMedia()).getTorrents();
        if (torrents.length > 0) {
            final Format[] formats = mediaDisplayManager.getSortedTorrentFormats(torrents);

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
