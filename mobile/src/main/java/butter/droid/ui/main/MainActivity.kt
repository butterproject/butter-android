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

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import butter.droid.R
import butter.droid.base.manager.internal.beaming.BeamPlayerNotificationService
import butter.droid.base.manager.internal.beaming.server.BeamServerService
import butter.droid.base.providers.media.model.MediaWrapper
import butter.droid.base.providers.media.model.StreamInfo
import butter.droid.provider.base.filter.Genre
import butter.droid.provider.base.model.Clip
import butter.droid.ui.ButterBaseActivity
import butter.droid.ui.loading.StreamLoadingActivity
import butter.droid.ui.main.navigation.NavigationDrawerFragment
import butter.droid.ui.main.pager.MediaPagerAdapter
import butter.droid.ui.main.pager.NavInfo
import butter.droid.ui.preferences.PreferencesActivity
import butter.droid.ui.search.SearchActivity
import butter.droid.ui.terms.TermsActivity
import butter.droid.utils.ToolbarUtils
import butter.droid.widget.ScrimInsetsFrameLayout
import java.io.UnsupportedEncodingException
import java.net.URLDecoder
import javax.inject.Inject

/**
 * The main activity that houses the navigation drawer, and controls navigation between fragments
 */
class MainActivity : ButterBaseActivity(), MainView {

    @Inject
    lateinit var presenter: MainPresenter

    @BindView(R.id.toolbar)
    lateinit var toolbar: Toolbar
    @BindView(R.id.navigation_drawer_container)
    lateinit var navigationDrawerContainer: ScrimInsetsFrameLayout
    @BindView(R.id.tabs)
    lateinit var tabs: TabLayout
    @BindView(R.id.pager)
    lateinit var viewPager: ViewPager

    private var navigationDrawerFragment: NavigationDrawerFragment? = null
    private var drawerToggle: ActionBarDrawerToggle? = null
    private var drawerLayout: DrawerLayout? = null
    private var adapter: MediaPagerAdapter? = null

    @SuppressLint("MissingSuperCall")
    fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_main)

        setSupportActionBar(toolbar)
        val actionBar = getSupportActionBar()
        actionBar.setDisplayHomeAsUpEnabled(true)
        actionBar.setHomeButtonEnabled(true)

        setShowCasting(true)
        ToolbarUtils.updateToolbarHeight(this, toolbar!!)
        setupDrawer()
        setupTabs()

        adapter = MediaPagerAdapter(getSupportFragmentManager(), this)
        viewPager.setAdapter(adapter)

        var defaultProviderId = -1
        if (savedInstanceState != null) {
            defaultProviderId = savedInstanceState.getInt(EXTRA_PROVIDER, defaultProviderId)
        }

        presenter.onCreate(defaultProviderId)
    }

    override fun onResume() {
        super.onResume()

        presenter.onResume()

        supportInvalidateOptionsMenu()

        if (BeamServerService.getServer() != null) {
            BeamServerService.getServer().stop()
        }

        BeamPlayerNotificationService.cancelNotification()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        getMenuInflater().inflate(R.menu.activity_overview, menu)

        return true
    }

    fun onRequestPermissionsResult(requestCode: Int, @NonNull permissions: Array<String>, @NonNull grantResults: IntArray) {
        when (requestCode) {
            PERMISSIONS_REQUEST_STORAGE -> if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                presenter.storagePermissionDenied()
            } else {
                presenter.storagePermissionGranted()
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (drawerToggle.onOptionsItemSelected(item)) {
            true
        } else {
            when (item.itemId) {
                android.R.id.home ->
                    /* Override default {@link pct.droid.activities.BaseActivity } behaviour */
                    false
                R.id.action_search -> {
                    presenter.searchClicked()
                    true
                }
                else -> super.onOptionsItemSelected(item)
            }
        }
    }

    fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        drawerToggle.onConfigurationChanged(newConfig)
    }

    fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        presenter.onSaveInstanceState(outState)
    }

    fun onDestroy() {
        super.onDestroy()
        presenter.onDestroy()
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        when (requestCode) {
            REQUEST_CODE_TERMS -> if (resultCode == RESULT_CANCELED) {
                finish()
            }
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun showTermsScreen() {
        startActivityForResult(TermsActivity.getIntent(this), REQUEST_CODE_TERMS)
    }

    override fun requestStoragePermissions() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                PERMISSIONS_REQUEST_STORAGE)
    }

    override fun closeScreen() {
        finish()
    }

    override fun checkIntentAction() {
        checkActions()
    }

    override fun initProviders(providerId: Int) {
        navigationDrawerFragment!!.selectProvider(providerId)
    }

    override fun closeDrawer() {
        drawerLayout.closeDrawer(navigationDrawerContainer)
    }

    override fun openPreferenceScreen() {
        startActivity(PreferencesActivity.getIntent(this))
    }

    override fun displayProvider(@StringRes title: Int, navigation: List<NavInfo>) {
        setTitle(title)
        adapter.setData(navigation)

        // TODO: 6/17/17
        //        tabs.getTabAt(hasGenres ? 1 : 0).select();
    }

    override fun onGenreChanged(genre: Genre) {
        adapter.setGenre(genre)
    }

    override fun showFirsContentScreen() {
        tabs.getTabAt(1)?.select() // TODO something should be done here
    }

    override fun writeStateData(@NonNull outState: Bundle, providerId: Int) {
        outState.putInt(EXTRA_PROVIDER, providerId)
    }

    override fun setScreenTitle(@StringRes title: Int) {
        setTitle(title)
    }

    override fun openSearchScreen(providerId: Int) {
        startActivity(SearchActivity.getIntent(this, providerId))
    }

    private fun checkActions() {
        val action = getIntent().getAction()
        val data = getIntent().getData()
        if (action != null && action == Intent.ACTION_VIEW && data != null) {
            var streamUrl = data!!.toString()
            try {
                streamUrl = URLDecoder.decode(streamUrl, "UTF-8")
                // TODO: 7/29/17 Check if actual torrent
                val clip = Clip("0", streamUrl, 0, arrayOfNulls(0), -1f, "", "", "",
                        streamUrl)
                StreamLoadingActivity.startActivity(this, StreamInfo(streamUrl, MediaWrapper(clip, -1), null))
                finish()
            } catch (e: UnsupportedEncodingException) {
                Timber.d("Unknown encoding") // this should never happen
            }

        }
    }

    private fun setupDrawer() {
        // Set up the drawer.
        drawerLayout = findViewById(R.id.drawer_layout)
        drawerLayout!!.setStatusBarBackgroundColor(ContextCompat.getColor(this, R.color.primary_dark))

        navigationDrawerFragment = getSupportFragmentManager().findFragmentById(
                R.id.navigation_drawer_fragment)

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the navigation drawer and the action bar app icon.
        drawerToggle = ActionBarDrawerToggle(this, drawerLayout, R.string.drawer_open, R.string.drawer_close)
        drawerToggle.syncState()
        drawerLayout.addDrawerListener(drawerToggle)

    }

    private fun setupTabs() {
        tabs.setupWithViewPager(viewPager)
        tabs.setTabGravity(TabLayout.GRAVITY_CENTER)
        tabs.setTabMode(TabLayout.MODE_SCROLLABLE)
        tabs.setVisibility(View.VISIBLE)

        viewPager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabs))
        tabs.addOnTabSelectedListener(TabLayout.ViewPagerOnTabSelectedListener(viewPager))

    }

    companion object {

        private const val EXTRA_PROVIDER = "butter.droid.ui.main.MainActivity.providerId"

        private const val REQUEST_CODE_TERMS = 1
        private const val PERMISSIONS_REQUEST_STORAGE = 1
    }

}
