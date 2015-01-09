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
import pct.droid.activities.AboutActivity;
import pct.droid.activities.PreferencesActivity;
import pct.droid.adapters.NavigationAdapter;
import pct.droid.base.preferences.Prefs;
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
		navItems.add(new NavDrawerItem(getString(R.string.title_movies), R.drawable.ic_nav_movies));
		navItems.add(new NavDrawerItem(getString(R.string.title_shows), R.drawable.ic_nav_tv));
		navItems.add(new NavDrawerItem(getString(R.string.settings), R.drawable.ic_nav_settings, mOnSettingsClickListener));
		navItems.add(new NavDrawerItem(getString(R.string.about), R.drawable.ic_nav_about, mOnAboutClickListener));

		mAdapter = new NavigationAdapter(getActivity(), this, navItems);
		mAdapter.setOnItemClickListener(this);

		mRecyclerView.setHasFixedSize(true);
		mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
		mRecyclerView.setAdapter(mAdapter);
	}

	private View.OnClickListener mOnAboutClickListener = new View.OnClickListener() {
		@Override public void onClick(View v) {
			AboutActivity.startActivity(getActivity());
		}
	};

	private View.OnClickListener mOnSettingsClickListener = new View.OnClickListener() {
		@Override public void onClick(View v) {
			PreferencesActivity.startActivity(getActivity());
			mDrawerLayout.closeDrawer(mNavigationDrawerContainer);
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

	public int getSelectedPosition() {
		return mCurrentSelectedPosition;
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

		//ensure the toolbar displays the home icon (will be overriden with the burger icon)
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
		if (null != item.getClickListener()) {
			item.getClickListener().onClick(v);
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
		void onNavigationDrawerItemSelected(int position);
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
			mCallbacks.onNavigationDrawerItemSelected(position);
		}

		mAdapter.notifyDataSetChanged();
	}

	/**
	 * Describes an item to be displayed in the navigation list
	 */
	public static class NavDrawerItem {
		private View.OnClickListener clickListener;
		public boolean isHeader = false;
		private String title;
		private int icon;

		public NavDrawerItem(String title, int icon) {
			this(title, icon, null);
		}

		public NavDrawerItem(String title, int icon, View.OnClickListener listener) {
			this.title = title;
			this.icon = icon;
			this.clickListener = listener;
		}

		public NavDrawerItem(boolean isHeader) {
			this.isHeader = true;
		}

		public String getTitle() {
			return title;
		}

		public int getIcon() {
			return icon;
		}

		public View.OnClickListener getClickListener() {
			return clickListener;
		}
	}


}
