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

import android.app.Activity.RESULT_CANCELED
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat.startActivityForResult
import butter.droid.base.providers.media.model.MediaWrapper
import butter.droid.base.providers.media.model.StreamInfo
import butter.droid.provider.base.model.Clip
import butter.droid.tv.service.RecommendationService
import butter.droid.tv.ui.loading.TVStreamLoadingActivity
import butter.droid.tv.ui.main.TVMainActivity
import butter.droid.tv.ui.terms.TVTermsActivity
import dagger.android.support.DaggerAppCompatActivity
import java.io.UnsupportedEncodingException
import java.net.URLDecoder
import javax.inject.Inject

class TVLaunchActivity : DaggerAppCompatActivity(), TVLaunchView {

    @Inject
    lateinit var presenter: TVLaunchPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter.onCreate()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_CODE_TERMS -> if (resultCode == RESULT_CANCELED) {
                presenter.termsCanceled()
                finish()
            } else {
                presenter.termsAccepted()
            }
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun close() {
        finish()
    }

    override fun showTermsScreen() {
        startActivityForResult(TVTermsActivity.getIntent(this), REQUEST_CODE_TERMS)
    }

    override fun startRecommendationService() {
        val recommendationIntent = Intent(this, RecommendationService::class.java)
        startService(recommendationIntent)
    }

    override fun navigateForward() {
        val action = intent.action
        val data = intent.data

        if (action != null && action == Intent.ACTION_VIEW && data != null) {
            var streamUrl = data.toString()
            try {
                streamUrl = URLDecoder.decode(streamUrl, "utf-8")
                val media = Clip("0", streamUrl, -1, arrayOfNulls(0), -1f, null, "", "",
                        streamUrl)
                TVStreamLoadingActivity.startActivity(this, StreamInfo(streamUrl, MediaWrapper(media, -1), null))
                finish()
                return
            } catch (e: UnsupportedEncodingException) {
                e.printStackTrace()
            }
        }

        TVMainActivity.startActivity(this)
        finish()
    }

    companion object {
        private const val REQUEST_CODE_TERMS = 1
    }
}
