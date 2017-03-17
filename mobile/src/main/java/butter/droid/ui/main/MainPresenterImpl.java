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

import java.io.IOException;
import java.util.HashMap;

import butter.droid.base.PlayerTestConstants;
import butter.droid.base.content.preferences.PreferencesHandler;
import butter.droid.base.content.preferences.Prefs;
import butter.droid.base.manager.beaming.BeamManager;
import butter.droid.base.manager.prefs.PrefManager;
import butter.droid.base.manager.provider.ProviderManager;
import butter.droid.base.manager.provider.ProviderManager.ProviderType;
import butter.droid.base.manager.youtube.YouTubeManager;
import butter.droid.base.providers.media.models.Movie;
import butter.droid.base.torrent.StreamInfo;
import butter.droid.ui.preferences.PreferencesActivity;
import butter.droid.ui.terms.TermsPresenterImpl;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class MainPresenterImpl implements MainPresenter {

    private final MainView view;
    private final YouTubeManager youTubeManager;
    private final ProviderManager providerManager;
    private final BeamManager beamManager;
    private final Context context;
    private final PreferencesHandler preferencesHandler;
    private final PrefManager prefManager;

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

    }

    @Override public void playerTestClicked() {
        view.showPlayerTestDialog(PlayerTestConstants.FILE_TYPES);
    }

    @Override public void onPlayerTestItemClicked(int index) {

        final String file = PlayerTestConstants.FILES[index];

        if (PlayerTestConstants.CUSTOM_FILE.equals(file)) {
            view.showPlayerTestUrlDialog();
        } else if (youTubeManager.isYouTubeUrl(file)) {
            Movie movie = new Movie(PlayerTestConstants.FILE_TYPES[index]);
            view.showYoutubeVideo(movie, file);
        } else {
            final Movie media = new Movie();
            media.videoId = "bigbucksbunny";
            media.title = PlayerTestConstants.FILE_TYPES[index];
            media.subtitles = new HashMap<>();
            media.subtitles.put("en", PlayerTestConstants.SUBTITLES_URL);

            // TODO: 11/29/16 Show progress while subtitles are loading

            providerManager.getCurrentSubsProvider().download(media, "en", new Callback() {
                @Override public void onFailure(Call call, IOException e) {
                    openStream(new StreamInfo(media, null, null, null, null, file));
                }

                @Override public void onResponse(Call call, Response response) throws IOException {
                    openStream(new StreamInfo(media, null, null, "en", null, file));
                }
            });
        }

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

    private void openStream(StreamInfo info) {
        if (beamManager.isConnected()) {
            view.showBeamPlayer(info);
        } else {
            view.showVideoPlayer(info);
        }
    }
}
