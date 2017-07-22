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

package butter.droid.ui.main;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import butter.droid.R;
import butter.droid.base.PlayerTestConstants;
import butter.droid.base.content.preferences.PreferencesHandler;
import butter.droid.base.manager.internal.beaming.BeamManager;
import butter.droid.base.manager.internal.provider.ProviderManager;
import butter.droid.base.manager.internal.youtube.YouTubeManager;
import butter.droid.base.manager.prefs.PrefManager;
import butter.droid.base.torrent.StreamInfo;
import butter.droid.provider.MediaProvider;
import butter.droid.ui.main.genre.list.model.UiGenre;
import butter.droid.ui.main.pager.NavInfo;
import butter.droid.ui.preferences.PreferencesActivity;
import butter.droid.ui.terms.TermsPresenterImpl;
import io.reactivex.Observable;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import java.util.ArrayList;
import java.util.List;

public class MainPresenterImpl implements MainPresenter {

    private final MainView view;
    private final YouTubeManager youTubeManager;
    private final ProviderManager providerManager;
    private final BeamManager beamManager;
    private final Context context;
    private final PreferencesHandler preferencesHandler;
    private final PrefManager prefManager;

    private final List<OnGenreChangeListener> genreListeners = new ArrayList<>();

    private int selectedProviderId;

    public MainPresenterImpl(MainView view, YouTubeManager youTubeManager, ProviderManager providerManager,
            BeamManager beamManager, Context context, PreferencesHandler preferencesHandler, PrefManager prefManager) {
        this.view = view;
        this.youTubeManager = youTubeManager;
        this.providerManager = providerManager;
        this.beamManager = beamManager;
        this.context = context;
        this.preferencesHandler = preferencesHandler;
        this.prefManager = prefManager;
    }

    @Override public void onCreate(final int selectedProviderId) {
        if (selectedProviderId >= 0) {
            this.selectedProviderId = selectedProviderId;
        } else {
            this.selectedProviderId = preferencesHandler.getDefaultProvider();
        }

        view.initProviders(this.selectedProviderId);
    }

    @Override public void onResume() {

        if (!prefManager.contains(TermsPresenterImpl.TERMS_ACCEPTED)) {
            view.showTermsScreen();
        } else if (ContextCompat.checkSelfPermission(context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            view.requestStoragePermissions();
        } else {
            view.checkIntentAction();
        }

        view.setScreenTitle(providerManager.getProvider(selectedProviderId).getName());

        displayProviderData(selectedProviderId);

    }

    @Override public void playerTestClicked() {
        view.showPlayerTestDialog(PlayerTestConstants.FILE_TYPES);
    }

    @Override public void onPlayerTestItemClicked(int index) {

        // TODO: 6/17/17
//        final String file = PlayerTestConstants.FILES[index];
//
//        if (PlayerTestConstants.CUSTOM_FILE.equals(file)) {
//            view.showPlayerTestUrlDialog();
//        } else if (youTubeManager.isYouTubeUrl(file)) {
//            Movie movie = new Movie(PlayerTestConstants.FILE_TYPES[index]);
//            view.showYoutubeVideo(movie, file);
//        } else {
//            final Movie media = new Movie();
//            media.videoId = "bigbucksbunny";
//            media.title = PlayerTestConstants.FILE_TYPES[index];
//            media.subtitles = new HashMap<>();
//            media.subtitles.put("en", PlayerTestConstants.SUBTITLES_URL);
//
//            // TODO: 11/29/16 Show progress while subtitles are loading
//
//            // TODO
//            /*
//            providerManager.getCurrentSubsProvider().download(media, "en", new Callback() {
//                @Override public void onFailure(Call call, IOException ex) {
//                    openStream(new StreamInfo(media, null, null, null, null, file));
//                }
//
//                @Override public void onResponse(Call call, Response response) throws IOException {
//                    openStream(new StreamInfo(media, null, null, "en", null, file));
//                }
//            });
//            */
//        }

    }

    @Override public void openPlayerTestCustomUrl(String location) {
        // TODO
        /*
        Movie media = new Movie();
        media.videoId = "dialogtestvideo";
        media.title = "User input test video";
        StreamInfo info = new StreamInfo(media, null, null, null, null, location);
        openStream(info);
        */
    }

    @Override public void storagePermissionDenied() {
        view.closeScreen();
    }

    @Override public void storagePermissionGranted() {
        view.checkIntentAction();
    }

    @Override public void selectProvider(final int providerId) {
        displayProviderData(providerId);
        view.closeDrawer();
    }

    @Override public void openMenuActivity(Class<? extends Activity> activityClass) {
        if (activityClass == PreferencesActivity.class) {
            view.openPreferenceScreen();
        } else {
            throw new IllegalStateException("Unknown menu activity");
        }

        view.closeDrawer();
    }

    @Override public void onGenreChanged(UiGenre genre) {
        view.onGenreChanged(genre.getGenre());

        if (genreListeners.size() > 0) {
            for (OnGenreChangeListener genreListener : genreListeners) {
                genreListener.onGenreChanged(genre);
            }
        }

        view.showFirsContentScreen();
    }

    @Override public void addGenreListener(OnGenreChangeListener listener) {
        genreListeners.add(listener);
    }

    @Override public void removeGenreListener(OnGenreChangeListener listener) {
        genreListeners.remove(listener);
    }

    @Override public void onSaveInstanceState(@NonNull final Bundle outState) {
        view.writeStateData(outState, selectedProviderId);
    }

    @Override public void searchClicked() {
        view.openSearchScreen(selectedProviderId);

    }

    private void displayProviderData(final int providerId) {
        this.selectedProviderId = providerId;
        MediaProvider provider = providerManager.getProvider(providerId);

        Observable.concat(provider.genres()
                        .filter(genres -> genres != null && genres.size() > 0)
                        .map(g -> new NavInfo(R.id.nav_item_genre, 0, R.string.genres, providerId))
                        .toObservable(),
                provider.navigation()
                        .flatMapObservable(Observable::fromIterable)
                        .map(item -> new NavInfo(item, providerId)))
                .toList()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<List<NavInfo>>() {
                    @Override public void onSubscribe(final Disposable d) {

                    }

                    @Override public void onSuccess(final List<NavInfo> value) {
//                        boolean hasGenres = value.first != null && value.first.size() > 0;
                        view.displayProvider(provider.getName(), value);
                    }

                    @Override public void onError(final Throwable e) {

                    }
                });
    }

    private void openStream(StreamInfo info) {
        if (beamManager.isConnected()) {
            view.showBeamPlayer(info);
        } else {
            view.showVideoPlayer(info);
        }
    }

    public interface OnGenreChangeListener {

        void onGenreChanged(UiGenre genre);
    }

}
