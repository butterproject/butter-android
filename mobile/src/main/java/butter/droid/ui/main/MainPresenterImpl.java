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
import android.support.v4.content.ContextCompat;
import butter.droid.base.content.preferences.PreferencesHandler;
import butter.droid.base.content.preferences.Prefs;
import butter.droid.base.manager.internal.beaming.BeamManager;
import butter.droid.base.manager.internal.provider.ProviderManager;
import butter.droid.base.manager.internal.provider.ProviderManager.ProviderType;
import butter.droid.base.manager.internal.youtube.YouTubeManager;
import butter.droid.base.manager.prefs.PrefManager;
import butter.droid.base.providers.media.MediaProvider;
import butter.droid.base.providers.media.models.Movie;
import butter.droid.base.torrent.StreamInfo;
import butter.droid.base.utils.ProviderUtils;
import butter.droid.ui.main.genre.list.model.UiGenre;
import butter.droid.ui.preferences.PreferencesActivity;
import butter.droid.ui.terms.TermsPresenterImpl;
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

    private boolean userLearnedDrawer;

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

    @Override public void onCreate(boolean isInitial) {
        if (isInitial) {
            @ProviderType int provider = preferencesHandler.getDefaultProvider();
            view.initProviders(provider);
        }

        userLearnedDrawer = prefManager.get(Prefs.DRAWER_LEARNED, false);
        if (!userLearnedDrawer && isInitial) {
            view.openDrawer();
        }
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

        displayProviderData(providerManager.getCurrentMediaProviderType());

    }

    @Override public void openPlayerTestCustomUrl(String location) {
        Movie media = new Movie();
        media.videoId = "dialogtestvideo";
        media.title = "User input test video";
        StreamInfo info = new StreamInfo(media, null, null, null, null, location);
        openStream(info);
    }

    @Override public void storagePermissionDenied() {
        view.closeScreen();
    }

    @Override public void storagePermissionGranted() {
        view.checkIntentAction();
    }

    @Override public void drawerOpened() {
        if (!userLearnedDrawer) {
            userLearnedDrawer = true;
            prefManager.save(Prefs.DRAWER_LEARNED, true);
        }
    }

    @Override public void selectProvider(@ProviderType int providerType) {
        providerManager.setCurrentProviderType(providerType);
        displayProviderData(providerType);
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
        view.onGenreChanged(genre.getKey());

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

    private void displayProviderData(@ProviderType int providerType) {
        MediaProvider provider = providerManager.getMediaProvider(providerType);
        boolean hasGenres = provider.getGenres() != null && provider.getGenres().size() > 0;
        view.displayProvider(ProviderUtils.getProviderTitle(providerType), hasGenres, provider.getNavigation());
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
