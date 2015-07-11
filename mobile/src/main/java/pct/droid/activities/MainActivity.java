/*
 * This file is part of Popcorn Time.
 *
 * Popcorn Time is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Popcorn Time is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Popcorn Time. If not, see <http://www.gnu.org/licenses/>.
 */

package pct.droid.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;

import butterknife.Bind;
import android.support.annotation.Nullable;
import pct.droid.BuildConfig;
import pct.droid.R;
import pct.droid.activities.base.PopcornBaseActivity;
import pct.droid.base.Constants;
import pct.droid.base.beaming.BeamManager;
import pct.droid.base.preferences.Prefs;
import pct.droid.base.providers.media.YTSProvider;
import pct.droid.base.providers.media.models.Movie;
import pct.droid.base.providers.subs.SubsProvider;
import pct.droid.base.providers.subs.YSubsProvider;
import pct.droid.base.torrent.StreamInfo;
import pct.droid.base.utils.PrefUtils;
import pct.droid.base.vpn.VPNManager;
import pct.droid.base.youtube.YouTubeData;
import pct.droid.fragments.MediaContainerFragment;
import pct.droid.fragments.NavigationDrawerFragment;
import pct.droid.utils.ToolbarUtils;
import pct.droid.widget.ScrimInsetsFrameLayout;
import timber.log.Timber;

/**
 * The main activity that houses the navigation drawer, and controls navigation between fragments
 */
public class MainActivity extends PopcornBaseActivity implements NavigationDrawerFragment.Callbacks {

    private Fragment mCurrentFragment;

    @Bind(R.id.toolbar)
    Toolbar mToolbar;
    @Bind(R.id.navigation_drawer_container)
    ScrimInsetsFrameLayout mNavigationDrawerContainer;
    @Nullable
    @Bind(R.id.tabs)
    TabLayout mTabs;
    NavigationDrawerFragment mNavigationDrawerFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, R.layout.activity_main);
        FragmentManager.enableDebugLogging(BuildConfig.DEBUG);

        setSupportActionBar(mToolbar);
        setShowCasting(true);

        ToolbarUtils.updateToolbarHeight(this, mToolbar);

        // Set up the drawer.
        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerLayout.setStatusBarBackgroundColor(getResources().getColor(R.color.primary_dark));

        mNavigationDrawerFragment =
                (NavigationDrawerFragment) getSupportFragmentManager().findFragmentById(R.id.navigation_drawer_fragment);

        mNavigationDrawerFragment.initialise(mNavigationDrawerContainer, drawerLayout);

        if (null != savedInstanceState) return;
        int providerId = PrefUtils.get(this, Prefs.DEFAULT_VIEW, 0);
        mNavigationDrawerFragment.selectItem(providerId);
    }

    @Override
    protected void onResume() {
        super.onResume();
        String title = mNavigationDrawerFragment.getCurrentItem().getTitle();
        setTitle(null != title ? title : getString(R.string.app_name));
        supportInvalidateOptionsMenu();
        if (mNavigationDrawerFragment.getCurrentItem() != null && mNavigationDrawerFragment.getCurrentItem().getTitle() != null) {
            setTitle(mNavigationDrawerFragment.getCurrentItem().getTitle());
        }

        mNavigationDrawerFragment.initItems();

        mTabs.setVisibility((mCurrentFragment instanceof MediaContainerFragment) ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onVPNServiceReady() {
        super.onVPNServiceReady();
        mNavigationDrawerFragment.initItems();
    }

    @Override
    public void onVPNStatusUpdate(VPNManager.State state, String message) {
        super.onVPNStatusUpdate(state, message);
        Timber.d("New state: %s", state);
        NavigationDrawerFragment.NavDrawerItem vpnItem = mNavigationDrawerFragment.getVPNItem();
        if(vpnItem != null) {
            if (state.equals(VPNManager.State.DISCONNECTED)) {
                vpnItem.setSwitchValue(false);
                vpnItem.showProgress(false);
            } else if(state.equals(VPNManager.State.CONNECTING)) {
                vpnItem.showProgress(true);
            } else if(state.equals(VPNManager.State.CONNECTED)) {
                vpnItem.setSwitchValue(true);
                vpnItem.showProgress(false);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.activity_overview, menu);

        MenuItem playerTestMenuItem = menu.findItem(R.id.action_playertests);
        playerTestMenuItem.setVisible(Constants.DEBUG_ENABLED);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                /* Override default {@link pct.droid.activities.BaseActivity } behaviour */
                return false;
            case R.id.action_playertests:
                openPlayerTestDialog();
                break;
            case R.id.action_search:
                //start the search activity
                SearchActivity.startActivity(this, mNavigationDrawerFragment.getCurrentItem().getMediaProvider());
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onNavigationDrawerItemSelected(NavigationDrawerFragment.NavDrawerItem item, String title) {
        setTitle(null != title ? title : getString(R.string.app_name));
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();

        String tag = title + "_tag";
        // Fragment fragment = mFragmentCache.get(position);
        mCurrentFragment = fragmentManager.findFragmentByTag(tag);
        if (null == mCurrentFragment && item.hasProvider()) {
            mCurrentFragment = MediaContainerFragment.newInstance(item.getMediaProvider());
        }

        if(mTabs.getTabCount() > 0)
            mTabs.getTabAt(0).select();
        mTabs.removeAllTabs();

        fragmentManager.beginTransaction().replace(R.id.container, mCurrentFragment, tag).commit();
    }

    public void updateTabs(final int position) {
        if(mTabs == null)
            return;

        mTabs.removeAllTabs();
        mTabs.setTabGravity(TabLayout.GRAVITY_CENTER);
        mTabs.setTabMode(TabLayout.MODE_SCROLLABLE);

        if(mCurrentFragment instanceof MediaContainerFragment) {
            MediaContainerFragment containerFragment = (MediaContainerFragment) mCurrentFragment;
            ViewPager viewPager = containerFragment.getViewPager();
            viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mTabs));
            mTabs.setOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(viewPager));

            mTabs.setupWithViewPager(viewPager);
            mTabs.setVisibility(View.VISIBLE);

            mTabs.getTabAt(0).select();
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mTabs.getTabAt(position).select();
                }
            }, 10);

        } else {
            mTabs.setVisibility(View.GONE);
        }
    }

    private void openPlayerTestDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        final String[] file_types = getResources().getStringArray(R.array.file_types);
        final String[] files = getResources().getStringArray(R.array.files);

        builder.setTitle("Player Tests")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                }).setSingleChoiceItems(file_types, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int index) {
                dialogInterface.dismiss();
                final String location = files[index];
                if (location.equals("dialog")) {
                    final EditText dialogInput = new EditText(MainActivity.this);
                    dialogInput.setText("http://download.wavetlan.com/SVV/Media/HTTP/MP4/ConvertedFiles/QuickTime/QuickTime_test13_5m19s_AVC_VBR_324kbps_640x480_25fps_AAC-LCv4_CBR_93.4kbps_Stereo_44100Hz.mp4");
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this)
                            .setView(dialogInput)
                            .setPositiveButton("Start", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Movie media = new Movie(new YTSProvider(), new YSubsProvider());

                                    media.videoId = "dialogtestvideo";
                                    media.title = "User input test video";

                                    String location = dialogInput.getText().toString();

                                    BeamManager bm = BeamManager.getInstance(MainActivity.this);
                                    if (bm.isConnected()) {
                                        BeamPlayerActivity.startActivity(MainActivity.this, new StreamInfo(media, null, null, null, null, location), 0);
                                    } else {
                                        VideoPlayerActivity.startActivity(MainActivity.this, new StreamInfo(media, null, null, null, null, location), 0);
                                    }
                                }
                            });
                    builder.show();
                } else if (YouTubeData.isYouTubeUrl(location)) {
                    Intent i = new Intent(MainActivity.this, TrailerPlayerActivity.class);
                    Movie media = new Movie(new YTSProvider(), new YSubsProvider());
                    media.title = file_types[index];
                    i.putExtra(TrailerPlayerActivity.DATA, media);
                    i.putExtra(TrailerPlayerActivity.LOCATION, location);
                    startActivity(i);
                } else {
                    final Movie media = new Movie(new YTSProvider(), new YSubsProvider());
                    media.videoId = "bigbucksbunny";
                    media.title = file_types[index];
                    media.subtitles = new HashMap<>();
                    media.subtitles.put("en", "http://sv244.cf/bbb-subs.srt");

                    SubsProvider.download(MainActivity.this, media, "en", new Callback() {
                        @Override
                        public void onFailure(Request request, IOException e) {
                            BeamManager bm = BeamManager.getInstance(MainActivity.this);

                            if (bm.isConnected()) {
                                BeamPlayerActivity.startActivity(MainActivity.this, new StreamInfo(media, null, null, null, null, location), 0);
                            } else {
                                VideoPlayerActivity.startActivity(MainActivity.this, new StreamInfo(media, null, null, null, null, location), 0);
                            }
                        }

                        @Override
                        public void onResponse(Response response) throws IOException {
                            BeamManager bm = BeamManager.getInstance(MainActivity.this);
                            if (bm.isConnected()) {
                                BeamPlayerActivity.startActivity(MainActivity.this, new StreamInfo(media, null, null, "en", null, location), 0);
                            } else {
                                VideoPlayerActivity.startActivity(MainActivity.this, new StreamInfo(media, null, null, "en", null, location), 0);
                            }
                        }
                    });
                }
            }
        });

        builder.show();
    }
}