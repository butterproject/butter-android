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

package butter.droid.ui.main

import android.app.Activity
import android.os.Bundle
import butter.droid.R
import butter.droid.base.content.preferences.PreferencesHandler
import butter.droid.base.manager.internal.provider.ProviderManager
import butter.droid.base.manager.prefs.PrefManager
import butter.droid.base.utils.rx.KeyDisposable
import butter.droid.ui.main.genre.list.model.UiGenre
import butter.droid.ui.main.pager.NavInfo
import butter.droid.ui.preferences.PreferencesActivity
import butter.droid.ui.terms.TermsPresenterImpl
import io.reactivex.Observable
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.*

class MainPresenterImpl(private val view: MainView,
                        private val providerManager: ProviderManager,
                        private val preferencesHandler: PreferencesHandler,
                        private val prefManager: PrefManager) : MainPresenter {

    private val genreListeners = ArrayList<OnGenreChangeListener>()
    private val providerDataDisposable = KeyDisposable()
    private var selectedProviderId: Int = 0

    override fun onCreate(selectedProviderId: Int) {
        if (selectedProviderId >= 0) {
            this.selectedProviderId = selectedProviderId
        } else {
            this.selectedProviderId = preferencesHandler.defaultProvider
        }

        view.initProviders(this.selectedProviderId)
    }

    override fun onResume() {
        if (!prefManager.contains(TermsPresenterImpl.TERMS_ACCEPTED)) {
            view.showTermsScreen()
        } else {
            view.checkIntentAction()
        }

        view.setScreenTitle(providerManager.getProvider(selectedProviderId).displayName)

        displayProviderData(selectedProviderId)
    }

    override fun selectProvider(providerId: Int) {
        displayProviderData(providerId)
        view.closeDrawer()
    }

    override fun openMenuActivity(activityClass: Class<out Activity>) {
        if (activityClass == PreferencesActivity::class.java) {
            view.openPreferenceScreen()
        } else {
            throw IllegalStateException("Unknown menu activity")
        }

        view.closeDrawer()
    }

    override fun onGenreChanged(genre: UiGenre) {
        view.onGenreChanged(genre.genre)
        genreListeners.forEach { it.onGenreChanged(genre) }
        view.showFirsContentScreen()
    }

    override fun addGenreListener(listener: OnGenreChangeListener) {
        genreListeners.add(listener)
    }

    override fun removeGenreListener(listener: OnGenreChangeListener) {
        genreListeners.remove(listener)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        view.writeStateData(outState, selectedProviderId)
    }

    override fun searchClicked() {
        view.openSearchScreen(selectedProviderId)
    }

    override fun onDestroy() {
        providerDataDisposable.dispose()
    }

    private fun displayProviderData(providerId: Int) {
        this.selectedProviderId = providerId
        val provider = providerManager.getProvider(providerId)
        val mediaProvider = provider.mediaProvider

        unsubscribeProviderId(providerId)
        Observable.concat(mediaProvider.genres()
                .filter { genres -> genres.isNotEmpty() }
                .map({ NavInfo(R.id.nav_item_genre, 0, R.string.genres, providerId) })
                .toObservable(),
                mediaProvider.navigation()
                        .flatMapObservable({ Observable.fromIterable(it) })
                        .map { NavInfo(it, providerId) })
                .toList()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : SingleObserver<List<NavInfo>> {
                    override fun onSubscribe(d: Disposable) {
                        providerDataDisposable.add(providerId, d)
                    }

                    override fun onSuccess(value: List<NavInfo>) {
                        // TODO: 8/5/17 Do we need this
                        //                        boolean hasGenres = value.first != null && value.first.size() > 0;
                        view.displayProvider(provider.displayName, value)
                    }

                    override fun onError(e: Throwable) {
                        // TODO: 8/5/17 Display error
                    }
                })
    }

    private fun unsubscribeProviderId(providerId: Int) {
        providerDataDisposable.disposeSingle(providerId)
    }

    interface OnGenreChangeListener {
        fun onGenreChanged(genre: UiGenre)
    }

}
