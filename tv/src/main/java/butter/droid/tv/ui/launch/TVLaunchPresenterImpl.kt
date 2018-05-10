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

package butter.droid.tv.ui.launch

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.support.v4.content.ContextCompat
import butter.droid.base.manager.prefs.PrefManager
import butter.droid.tv.ui.terms.TVTermsPresenterImpl

class TVLaunchPresenterImpl(private val view: TVLaunchView, private val context: Context, private val prefManager: PrefManager) : TVLaunchPresenter {

    override fun onCreate() {
        initialChecks()
        view.startRecommendationService()
    }

    override fun permissionsGranted() {
        initialChecks()
    }

    override fun permissionsDenied() {
        view.close()
    }

    override fun termsCanceled() {
        view.close()
    }

    override fun termsAccepted() {
        initialChecks()
    }

    private fun initialChecks() {
        if (!hasPermissions()) {
            view.requestPermissions()
        } else if (!hasAcceptedTerms()) {
            view.showTermsScreen()
        } else {
            view.navigateForward()
        }
    }

    private fun hasPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) === PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) === PackageManager.PERMISSION_GRANTED
    }

    private fun hasAcceptedTerms(): Boolean {
        return prefManager.get(TVTermsPresenterImpl.TERMS_ACCEPTED, false)!!
    }

}
