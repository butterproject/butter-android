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
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import javax.inject.Inject;

import butter.droid.MobileButterApplication;
import butter.droid.R;
import butter.droid.activities.BeamPlayerActivity;
import butter.droid.activities.SearchActivity;
import butter.droid.activities.TrailerPlayerActivity;
import butter.droid.activities.VideoPlayerActivity;
import butter.droid.base.Constants;
import butter.droid.base.PlayerTestConstants;
import butter.droid.base.manager.beaming.BeamPlayerNotificationService;
import butter.droid.base.manager.beaming.server.BeamServerService;
import butter.droid.base.manager.provider.ProviderManager;
import butter.droid.base.manager.provider.ProviderManager.OnProviderChangeListener;
import butter.droid.base.manager.provider.ProviderManager.ProviderType;
import butter.droid.base.providers.media.models.Movie;
import butter.droid.base.torrent.StreamInfo;
import butter.droid.base.utils.ProviderUtils;
import butter.droid.fragments.MediaContainerFragment;
import butter.droid.ui.ButterBaseActivity;
import butter.droid.ui.loading.StreamLoadingActivity;
import butter.droid.ui.main.navigation.NavigationDrawerFragment;
import butter.droid.ui.preferences.PreferencesActivity;
import butter.droid.ui.terms.TermsActivity;
import butter.droid.utils.ToolbarUtils;
import butter.droid.widget.ScrimInsetsFrameLayout;
import butterknife.BindView;
import timber.log.Timber;

/**
 * The main activity that houses the navigation drawer, and controls navigation between fragments
 */
public class MainActivity extends ButterBaseActivity implements MainView, OnProviderChangeListener {

    private static final int REQUEST_CODE_TERMS = 1;
    private static final int PERMISSIONS_REQUEST_STORAGE = 1;

    @Inject MainPresenter presenter;
    @Inject ProviderManager providerManager;

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.navigation_drawer_container) ScrimInsetsFrameLayout navigationDrawerContainer;
    @Nullable @BindView(R.id.tabs) TabLayout tabs;

    private MainComponent component;
    private NavigationDrawerFragment navigationDrawerFragment;
    private ActionBarDrawerToggle drawerToggle;
    private DrawerLayout drawerLayout;

    @SuppressLint("MissingSuperCall")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        component = MobileButterApplication.getAppContext()
                .getComponent()
                .mainComponentBuilder()
                .mainModule(new MainModule(this))
                .build();
        component.inject(this);

        super.onCreate(savedInstanceState, R.layout.activity_main);

        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        setShowCasting(true);
        ToolbarUtils.updateToolbarHeight(this, toolbar);
        setupDrawer();

        presenter.onCreate(savedInstanceState == null);
    }

    @Override
    protected void onResume() {
        super.onResume();

        presenter.onResume();

        setTitle(ProviderUtils.getProviderTitle(providerManager.getCurrentMediaProviderType()));
        supportInvalidateOptionsMenu();

        if (BeamServerService.getServer() != null) {
            BeamServerService.getServer().stop();
        }

        BeamPlayerNotificationService.cancelNotification();

        providerManager.addProviderListener(this);
    }

    @Override protected void onPause() {
        super.onPause();

        providerManager.removeProviderListener(this);
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
            @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_STORAGE: {
                if (grantResults.length < 1 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    presenter.storagePermissionDenied();
                } else {
                    presenter.storagePermissionGranted();
                }
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        } else {
            switch (item.getItemId()) {
                case android.R.id.home:
                /* Override default {@link pct.droid.activities.BaseActivity } behaviour */
                    return false;
                case R.id.action_playertests:
                    presenter.playerTestClicked();
                    return true;
                case R.id.action_search:
                    //start the search activity
                    SearchActivity.startActivity(this);
                    return true;
                default:
                    return super.onOptionsItemSelected(item);
            }
        }
    }

    @Override public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override public void onProviderChanged(@ProviderType int provider) {
        showProvider(provider);
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_TERMS:
                if (resultCode == RESULT_CANCELED) {
                    finish();
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    public void updateTabs(MediaContainerFragment containerFragment, final int position) {
        if (tabs == null) {
            return;
        }

        if (containerFragment != null) {
            ViewPager viewPager = containerFragment.getViewPager();
            if (viewPager == null) {
                return;
            }

            tabs.setupWithViewPager(viewPager);
            tabs.setTabGravity(TabLayout.GRAVITY_CENTER);
            tabs.setTabMode(TabLayout.MODE_SCROLLABLE);
            tabs.setVisibility(View.VISIBLE);

            viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabs));
            tabs.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(viewPager));

            if (tabs.getTabCount() > 0) {
                tabs.getTabAt(0).select();
                torrentHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (tabs.getTabCount() > position) {
                            tabs.getTabAt(position).select();
                        }
                    }
                }, 10);
            }

        } else {
            tabs.setVisibility(View.GONE);
        }
    }

    @Override public void showTermsScreen() {
        startActivityForResult(TermsActivity.getIntent(this), REQUEST_CODE_TERMS);
    }

    @Override public void showYoutubeVideo(Movie movie, String url) {
        Intent i = TrailerPlayerActivity.getIntent(this, movie, url);
        startActivity(i);
    }

    @Override public void showPlayerTestDialog(String[] fileTypes) {
        new AlertDialog.Builder(this)
                .setTitle("Player Tests")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .setSingleChoiceItems(fileTypes, -1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int index) {
                        dialogInterface.dismiss();
                        presenter.onPlayerTestItemClicked(index);
                    }
                })
                .show();
    }

    @Override public void showPlayerTestUrlDialog() {
        final EditText dialogInput = new EditText(MainActivity.this);
        dialogInput.setText(PlayerTestConstants.DEFAULT_CUSTOM_FILE);

        new AlertDialog.Builder(MainActivity.this)
                .setView(dialogInput)
                .setPositiveButton("Start", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String location = dialogInput.getText().toString();
                        presenter.openPlayerTestCustomUrl(location);
                    }
                })
                .show();
    }

    @Override public void showVideoPlayer(@NonNull StreamInfo info) {
        startActivity(VideoPlayerActivity.getIntent(this, info));
    }

    @Override public void showBeamPlayer(@NonNull StreamInfo info) {
        startActivity(BeamPlayerActivity.getIntent(this, info, 0));
    }

    @Override public void requestStoragePermissions() {
        ActivityCompat.requestPermissions(this, new String[]{ Manifest.permission.WRITE_EXTERNAL_STORAGE },
                PERMISSIONS_REQUEST_STORAGE);
    }

    @Override public void closeScreen() {
        finish();
    }

    @Override public void checkIntentAction() {
        checkActions();
    }

    @Override public void initProviders(@ProviderType int provider) {
        navigationDrawerFragment.selectProvider(provider);
        showProvider(provider);
    }

    @Override public void openDrawer() {
        drawerLayout.openDrawer(navigationDrawerContainer);
    }

    @Override public void closeDrawer() {
        drawerLayout.closeDrawer(navigationDrawerContainer);
    }

    @Override public void openPreferenceScreen() {
        startActivity(PreferencesActivity.getIntent(this));
    }

    public MainComponent getComponent() {
        return component;
    }

    private void checkActions() {
        String action = getIntent().getAction();
        Uri data = getIntent().getData();
        if (action != null && action.equals(Intent.ACTION_VIEW) && data != null) {
            String streamUrl = data.toString();
            try {
                streamUrl = URLDecoder.decode(streamUrl, "UTF-8");
                StreamLoadingActivity.startActivity(this, new StreamInfo(streamUrl));
                finish();
            } catch (UnsupportedEncodingException e) {
                Timber.d("Unknown encoding"); // this should never happen
            }
        }
    }

    private void showProvider(@ProviderType int provider) {
        setTitle(ProviderUtils.getProviderTitle(provider));
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();

        MediaContainerFragment mediaFragment = MediaContainerFragment.newInstance();

        if (tabs.getTabCount() > 0) {
            tabs.getTabAt(0).select();
        }

        fragmentManager.beginTransaction().replace(R.id.container, mediaFragment).commit();
        updateTabs(mediaFragment, mediaFragment.getCurrentSelection());
    }

    private void setupDrawer() {
        // Set up the drawer.
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerLayout.setStatusBarBackgroundColor(ContextCompat.getColor(this, R.color.primary_dark));

        navigationDrawerFragment =
                (NavigationDrawerFragment) getSupportFragmentManager().findFragmentById(
                        R.id.navigation_drawer_fragment);

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the navigation drawer and the action bar app icon.
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);

                presenter.drawerOpened();
            }

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, 0);
            }
        };
        drawerToggle.syncState();
        drawerLayout.addDrawerListener(drawerToggle);

    }
}
