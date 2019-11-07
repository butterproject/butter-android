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

import android.os.Bundle
import androidx.annotation.StringRes
import butter.droid.provider.base.filter.Genre
import butter.droid.ui.main.pager.NavInfo

interface MainView {
    fun showTermsScreen()

    fun requestStoragePermissions()

    fun closeScreen()

    fun checkIntentAction()

    fun initProviders(providerId: Int)

    fun closeDrawer()

    fun openPreferenceScreen()

    fun displayProvider(@StringRes title: Int, navigation: List<NavInfo>)

    fun onGenreChanged(genre: Genre)

    fun showFirsContentScreen()

    fun writeStateData(outState: Bundle, selectedProviderId: Int)

    fun setScreenTitle(@StringRes title: Int)

    fun openSearchScreen(providerId: Int)
}
