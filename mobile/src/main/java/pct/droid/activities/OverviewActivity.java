package pct.droid.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;

import butterknife.InjectView;
import pct.droid.BuildConfig;
import pct.droid.R;
import pct.droid.base.Constants;
import pct.droid.base.preferences.Prefs;
import pct.droid.base.providers.media.YTSProvider;
import pct.droid.base.providers.media.models.Media;
import pct.droid.base.providers.media.models.Movie;
import pct.droid.base.providers.subs.SubsProvider;
import pct.droid.base.utils.PrefUtils;
import pct.droid.base.youtube.YouTubeData;
import pct.droid.fragments.MediaListFragment;
import pct.droid.fragments.NavigationDrawerFragment;
import pct.droid.fragments.StreamLoadingFragment;
import pct.droid.utils.ToolbarUtils;
import pct.droid.widget.ScrimInsetsFrameLayout;

/**
 * The main activity that houses the navigation drawer, and controls navigation between fragments
 */
public class OverviewActivity extends BaseActivity implements NavigationDrawerFragment.Callbacks {

	//    private OverviewActivityTaskFragment mTaskFragment;

	@InjectView(R.id.toolbar) Toolbar mToolbar;
	@InjectView(R.id.navigation_drawer_container) ScrimInsetsFrameLayout mNavigationDrawerContainer;
	NavigationDrawerFragment mNavigationDrawerFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState, R.layout.activity_overview);
		FragmentManager.enableDebugLogging(BuildConfig.DEBUG);

		setSupportActionBar(mToolbar);

		ToolbarUtils.updateToolbarHeight(this, mToolbar);


		// Set up the drawer.
		DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		drawerLayout.setStatusBarBackgroundColor(getResources().getColor(R.color.primary_dark));

		mNavigationDrawerFragment =
				(NavigationDrawerFragment) getSupportFragmentManager().findFragmentById(R.id.navigation_drawer_fragment);

		mNavigationDrawerFragment.initialise(mNavigationDrawerContainer, drawerLayout);


		/* view a magnet link directly */
		String action = getIntent().getAction();
		Uri data = getIntent().getData();
		if (action != null && action.equals(Intent.ACTION_VIEW) && data != null) {
			String streamUrl = data.toString();
			try {
				streamUrl = URLDecoder.decode(streamUrl, "utf-8");
				StreamLoadingActivity.startActivity(this, new StreamLoadingFragment.StreamInfo(streamUrl));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}

		if (null != savedInstanceState) return;//dont reselect item if saved state exists
		int providerId = PrefUtils.get(this, Prefs.DEFAULT_VIEW, 0);
		mNavigationDrawerFragment.selectItem(providerId);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_overview, menu);

		MenuItem playerTestMenuItem = menu.findItem(R.id.action_playertests);
		playerTestMenuItem.setVisible(Constants.DEBUG_ENABLED);

		return super.onCreateOptionsMenu(menu);
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
				SearchActivity.startActivity(this, mNavigationDrawerFragment.getSelectedPosition());
				break;
		}
		return super.onOptionsItemSelected(item);
	}


	@Override public void onNavigationDrawerItemSelected(int position) {
		// update the main content by replacing fragments
		FragmentManager fragmentManager = getSupportFragmentManager();
		String tag = Integer.toString(position);

		//		Fragment fragment = mFragmentCache.get(position);
		Fragment fragment = fragmentManager.findFragmentByTag(tag);
		if (null == fragment) {
			fragment = MediaListFragment.newInstance(MediaListFragment.Mode.NORMAL, position); //create new fragment instance
		}
		fragmentManager.beginTransaction().replace(R.id.container, fragment, tag).commit();
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
					final EditText dialogInput = new EditText(OverviewActivity.this);
					AlertDialog.Builder builder = new AlertDialog.Builder(OverviewActivity.this)
							.setView(dialogInput)
							.setPositiveButton("Start", new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									Movie media = new YTSProvider.YTSMovie();

									media.videoId = "dialogtestvideo";
									media.title = "User input test video";

									VideoPlayerActivity.startActivity(OverviewActivity.this, dialogInput.getText().toString(), media);
								}
							});
				}
				if (YouTubeData.isYouTubeUrl(location)) {
					Intent i = new Intent(OverviewActivity.this, TrailerPlayerActivity.class);
					Media media = new YTSProvider.YTSMovie();
					media.title = file_types[index];
					i.putExtra(TrailerPlayerActivity.DATA, media);
					i.putExtra(TrailerPlayerActivity.LOCATION, location);
					startActivity(i);
				} else {
					final Movie media = new YTSProvider.YTSMovie();
					media.videoId = "bigbucksbunny";
					media.title = file_types[index];
					media.subtitles = new HashMap<String, String>();
					media.subtitles.put("en", "http://popcorn.sv244.cf/bbb-subs.srt");

					SubsProvider.download(OverviewActivity.this, media, "en", new Callback() {
						@Override
						public void onFailure(Request request, IOException e) {
							VideoPlayerActivity.startActivity(OverviewActivity.this, location, media, null, null, 0);
						}

						@Override
						public void onResponse(Response response) throws IOException {
							VideoPlayerActivity.startActivity(OverviewActivity.this, location, media, null, "en", 0);
						}
					});
				}
			}
		});

		builder.show();
	}
}