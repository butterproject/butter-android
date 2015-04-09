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

package pct.droid.fragments;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import pct.droid.R;
import pct.droid.activities.PreferencesActivity;
import pct.droid.adapters.NavigationAdapter;
import pct.droid.adapters.decorators.OneShotDividerDecorator;
import pct.droid.base.Constants;
import pct.droid.base.preferences.Prefs;
import pct.droid.base.providers.media.EZTVProvider;
import pct.droid.base.providers.media.HaruProvider;
import pct.droid.base.providers.media.MediaProvider;
import pct.droid.base.providers.media.YTSProvider;
import pct.droid.base.utils.IntentUtils;
import pct.droid.base.utils.PrefUtils;

public class NavigationDrawerFragment extends Fragment implements NavigationAdapter.Callback,
		NavigationAdapter.OnItemClickListener {

	/**
	 * Remember the position of the selected item.
	 */
	private static final String STATE_SELECTED_POSITION = "selected_navigation_drawer_position";

	/* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	 * views
	 * - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - */

	RecyclerView mRecyclerView;
	private DrawerLayout mDrawerLayout;
	private ActionBarDrawerToggle mDrawerToggle;
	private ViewGroup mNavigationDrawerContainer;

	/* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	 * variables
	 * - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - */

	private int mCurrentSelectedPosition = 0;
	private boolean mFromSavedInstanceState;
	private boolean mUserLearnedDrawer;
	private NavigationAdapter mAdapter;

	/**
	 * A pointer to the current callbacks instance (the Activity).
	 */
	private Callbacks mCallbacks;

	/* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	 * life cycle methods
	 * - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - */

	@Override public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mCallbacks = (Callbacks) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException("Activity must implement NavigationDrawerCallbacks.");
		}
	}

	@Override public void onDetach() {
		super.onDetach();
		mCallbacks = null;
	}

	@Override public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mUserLearnedDrawer = PrefUtils.get(getActivity(), Prefs.DRAWER_LEARNED, false);

		if (savedInstanceState != null) {
			mCurrentSelectedPosition = savedInstanceState.getInt(STATE_SELECTED_POSITION);
			mFromSavedInstanceState = true;
		}
	}

	@Override public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		// Indicate that this fragment would like to influence the set of actions in the action bar.
		setHasOptionsMenu(true);
	}

	@Override public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		mRecyclerView = (RecyclerView) inflater.inflate(R.layout.fragment_navigation_drawer, container, false);
		return mRecyclerView;
	}

	@Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		ButterKnife.inject(this, view);

		//todo: make list items dynamic
		List<NavDrawerItem> navItems = new ArrayList<>();
		navItems.add(new NavDrawerItem(true));
		navItems.add(new NavDrawerItem(getString(R.string.title_movies), R.drawable.ic_nav_movies, new YTSProvider()));
		navItems.add(new NavDrawerItem(getString(R.string.title_shows), R.drawable.ic_nav_tv, new EZTVProvider()));
        navItems.add(new NavDrawerItem(getString(R.string.title_anime), R.drawable.ic_nav_anime, new HaruProvider()));
		navItems.add(new NavDrawerItem(getString(R.string.share), R.drawable.ic_nav_share, mOnShareClickListener));
		navItems.add(new NavDrawerItem(getString(R.string.preferences), R.drawable.ic_nav_settings, mOnSettingsClickListener));

		mAdapter = new NavigationAdapter(getActivity(), this, navItems);
		mAdapter.setOnItemClickListener(this);

		mRecyclerView.addItemDecoration(new OneShotDividerDecorator(getActivity(), 3));
		mRecyclerView.setHasFixedSize(true);
		mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
		mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.requestFocus();
	}

	private View.OnClickListener mOnSettingsClickListener = new View.OnClickListener() {
		@Override public void onClick(View v) {
			PreferencesActivity.startActivity(getActivity());
			mDrawerLayout.closeDrawer(mNavigationDrawerContainer);
		}
	};

	private View.OnClickListener mOnShareClickListener = new View.OnClickListener() {
		@Override public void onClick(View v) {
			String message = getString(R.string.share_message);
			startActivity(IntentUtils.getSendIntent(getActivity(), getString(R.string.share_dialog_title), String.format("%s %s", message,
					Constants.POPCORN_URL)));
		}
	};

	@Override public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(STATE_SELECTED_POSITION, mCurrentSelectedPosition);
	}

	@Override public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		// Forward the new configuration the drawer toggle component.
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

    @Override
    public int getSelectedPosition() {
        return mCurrentSelectedPosition;
    }

	public NavDrawerItem getCurrentItem() {
		return mAdapter.getItem(getSelectedPosition() + 1);
	}

	/* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	 * initialise 
	 * - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - */

	/**
	 * Called by the implementing activity to initialise the navigation drawer
	 *
	 * @param navigationContainer
	 * @param drawerLayout
	 */
	public void initialise(ViewGroup navigationContainer, DrawerLayout drawerLayout) {
		mNavigationDrawerContainer = navigationContainer;
		mDrawerLayout = drawerLayout;

		//ensure the mToolbar displays the home icon (will be overriden with the burger icon)
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setHomeButtonEnabled(true);

		// ActionBarDrawerToggle ties together the the proper interactions
		// between the navigation drawer and the action bar app icon.
		mDrawerToggle = new ActionBarDrawerToggle(getActivity(), mDrawerLayout, R.string.drawer_open, R.string.drawer_close) {
			@Override public void onDrawerClosed(View drawerView) {
				super.onDrawerClosed(drawerView);
				/* do nothing */
			}

			@Override public void onDrawerOpened(View drawerView) {
				super.onDrawerOpened(drawerView);
				if (!isAdded()) return;

				if (!mUserLearnedDrawer) {
					// The user manually opened the drawer; store this flag to prevent auto-showing
					// the navigation drawer automatically in the future.
					mUserLearnedDrawer = true;
					PrefUtils.save(getActivity(), Prefs.DRAWER_LEARNED, true);
				}
			}
		};

		// If the user hasn't 'learned' about the drawer, open it to introduce them to the drawer,
		// per the navigation drawer design guidelines.
		if (!mUserLearnedDrawer && !mFromSavedInstanceState) {
			mDrawerLayout.openDrawer(mNavigationDrawerContainer);
		}

		// Defer code dependent on restoration of previous instance state.
		mDrawerLayout.post(new Runnable() {
			@Override public void run() {
				mDrawerToggle.syncState();
			}
		});

		mDrawerLayout.setDrawerListener(mDrawerToggle);

	}

	private ActionBar getActionBar() {
		return ((ActionBarActivity) getActivity()).getSupportActionBar();
	}

	@Override public boolean onOptionsItemSelected(MenuItem item) {
		//consume the home button press
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override public void onItemClick(View v, NavDrawerItem item, int position) {
		if (null != item.getmOnClickListener()) {
			item.getmOnClickListener().onClick(v);
			return;
		}

		selectItem(mAdapter.getCorrectPosition(position));
	}

    /**
	 * Callbacks interface that all activities using this fragment must implement.
	 */
	public static interface Callbacks {
		/**
		 * Called when an item in the navigation drawer is selected.
		 */
		void onNavigationDrawerItemSelected(NavDrawerItem item, String s);
	}

	/**
	 * Called when a list item is selected.
	 *
	 * Updates the state of the list, closes the drawer, and fowards the event to the parent activity to handle.
	 *
	 * @param position position of the item in the list
	 */
	public void selectItem(int position) {
		mCurrentSelectedPosition = position;

		if (mDrawerLayout != null) {
			mDrawerLayout.closeDrawer(mNavigationDrawerContainer);
		}

		if (mCallbacks != null) {
            NavDrawerItem navDrawerItem = mAdapter.getItem(position + 1);
			mCallbacks.onNavigationDrawerItemSelected(navDrawerItem, null != navDrawerItem ? navDrawerItem.getTitle() : null);
		}

		mAdapter.notifyDataSetChanged();
	}

	/**
	 * Describes an item to be displayed in the navigation list
	 */
	public static class NavDrawerItem {
		private View.OnClickListener mOnClickListener;
		private boolean mIsHeader = false;
		private String mTitle;
		private int mIcon;
        private MediaProvider mMediaProvider;

		public NavDrawerItem(String title, int icon) {
            mTitle = title;
            mIcon = icon;
		}

        public NavDrawerItem(String title, int icon, MediaProvider mediaProvider) {
            this(title, icon);
            mMediaProvider = mediaProvider;
        }

		public NavDrawerItem(String title, int icon, View.OnClickListener listener) {
			this(title, icon);
			mOnClickListener = listener;
		}

		public NavDrawerItem(boolean isHeader) {
			mIsHeader = true;
		}

		public String getTitle() {
			return mTitle;
		}

		public int getIcon() {
			return mIcon;
		}

        public MediaProvider getMediaProvider() {
            return mMediaProvider;
        }

        public boolean isHeader() {
            return mIsHeader;
        }

        public boolean hasProvider() {
            return mMediaProvider != null;
        }

		public View.OnClickListener getmOnClickListener() {
			return mOnClickListener;
		}
	}


}
