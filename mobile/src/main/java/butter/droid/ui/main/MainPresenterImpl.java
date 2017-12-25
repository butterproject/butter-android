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
import butter.droid.base.content.preferences.PreferencesHandler;
import butter.droid.base.manager.internal.provider.ProviderManager;
import butter.droid.base.manager.internal.provider.model.ProviderWrapper;
import butter.droid.base.manager.prefs.PrefManager;
import butter.droid.base.utils.rx.KeyDisposable;
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
    private final ProviderManager providerManager;
    private final Context context;
    private final PreferencesHandler preferencesHandler;
    private final PrefManager prefManager;

    private final List<OnGenreChangeListener> genreListeners = new ArrayList<>();

    private final KeyDisposable providerDataDisposable = new KeyDisposable();

    private int selectedProviderId;

    public MainPresenterImpl(MainView view, ProviderManager providerManager, Context context, PreferencesHandler preferencesHandler,
            PrefManager prefManager) {
        this.view = view;
        this.providerManager = providerManager;
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

        view.setScreenTitle(providerManager.getProvider(selectedProviderId).getDisplayName());

        displayProviderData(selectedProviderId);

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

    @Override public void onDestroy() {
        providerDataDisposable.dispose();
    }

    private void displayProviderData(final int providerId) {
        this.selectedProviderId = providerId;
        final ProviderWrapper provider = providerManager.getProvider(providerId);
        final MediaProvider mediaProvider = provider.getMediaProvider();

        unsubscribeProviderId(providerId);
        Observable.concat(mediaProvider.genres()
                        .filter(genres -> genres != null && genres.size() > 0)
                        .map(g -> new NavInfo(R.id.nav_item_genre, 0, R.string.genres, providerId))
                        .toObservable(),
                mediaProvider.navigation()
                        .flatMapObservable(Observable::fromIterable)
                        .map(item -> new NavInfo(item, providerId)))
                .toList()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<List<NavInfo>>() {
                    @Override public void onSubscribe(final Disposable d) {
                        providerDataDisposable.add(providerId, d);
                    }

                    @Override public void onSuccess(final List<NavInfo> value) {
                        // TODO: 8/5/17 Do we need this
//                        boolean hasGenres = value.first != null && value.first.size() > 0;
                        view.displayProvider(provider.getDisplayName(), value);
                    }

                    @Override public void onError(final Throwable e) {
                        // TODO: 8/5/17 Display error
                    }
                });
    }

    private void unsubscribeProviderId(final int providerId) {
        providerDataDisposable.disposeSingle(providerId);
    }

    public interface OnGenreChangeListener {

        void onGenreChanged(UiGenre genre);
    }

}
