package pct.droid.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import pct.droid.R;
import pct.droid.activities.PreferencesActivity;
import pct.droid.base.preferences.Prefs;
import pct.droid.base.utils.PrefUtils;

public class NavigationDrawerFragment extends Fragment implements AdapterView.OnItemClickListener {

	/**
	 * Remember the position of the selected item.
	 */
	private static final String STATE_SELECTED_POSITION = "selected_navigation_drawer_position";

	/* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	 * views
	 * - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - */

	ListView mListView;
	private DrawerLayout mDrawerLayout;
	private ActionBarDrawerToggle mDrawerToggle;
	private ViewGroup mNavigationDrawerContainer;

	/* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	 * variables
	 * - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - */

	private int mCurrentSelectedPosition = 0;
	private boolean mFromSavedInstanceState;
	private boolean mUserLearnedDrawer;
	private NavDrawAdapter mAdapter;

	/**
	 * A pointer to the current callbacks instance (the Activity).
	 */
	private Callbacks mCallbacks;
	private View mHeaderView;
	private View mFooterView;
	private TextView mPreferencesTextView;

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
		mListView = (ListView) inflater.inflate(R.layout.fragment_navigation_drawer, container, false);
		mHeaderView = inflater.inflate(R.layout.nav_drawer_header, mListView, false);
		mFooterView = inflater.inflate(R.layout.nav_drawer_footer, mListView, false);
		mPreferencesTextView = (TextView) mFooterView.findViewById(R.id.settings_button);
		return mListView;
	}

	@Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		ButterKnife.inject(this, view);

		mListView.setOnItemClickListener(this);

		//todo: make list items dynamic
		mAdapter = new NavDrawAdapter(getActivity(), new NavDrawerItem[]{
				new NavDrawerItem(getString(R.string.title_movies), R.drawable.ic_nav_movies),
				new NavDrawerItem(getString(R.string.title_shows), R.drawable.ic_nav_tv)});

		mListView.addHeaderView(mHeaderView);
		mListView.addFooterView(mFooterView);
		mListView.setAdapter(mAdapter);


		mPreferencesTextView.setOnClickListener(new View.OnClickListener() {
			@Override public void onClick(View v) {
				PreferencesActivity.startActivity(getActivity());
				mDrawerLayout.closeDrawer(mNavigationDrawerContainer);
			}
		});
	}

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

	@Override public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		selectItem(mAdapter.getPosition((NavDrawerItem) parent.getItemAtPosition(position)));
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
		if (mListView != null) {
			mListView.setItemChecked(position, true);
		}
		if (mDrawerLayout != null) {
			mDrawerLayout.closeDrawer(mNavigationDrawerContainer);
		}
		if (mCallbacks != null) {
			mCallbacks.onNavigationDrawerItemSelected(position);
		}
	}

		/* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
		 * List adapter
		 * - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - */

	protected class NavDrawAdapter extends ArrayAdapter<NavDrawerItem> {

		private final int mNormalColor;
		private final int mCheckedColor;
		private final int mNormalIconColor;

		public NavDrawAdapter(Context context, NavDrawerItem[] objects) {
			super(context, R.layout.nav_drawer_row, objects);
			mNormalColor = getContext().getResources().getColor(R.color.text_color);
			mCheckedColor = getContext().getResources().getColor(R.color.primary);
			mNormalIconColor = getContext().getResources().getColor(R.color.secondary_text_color);
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			NavRowWrapper vh = NavRowWrapper.get(convertView, parent);
			NavDrawerItem item = getItem(position);

			vh.title.setText(item.getTitle());

			boolean isSelected = position == mCurrentSelectedPosition;
			vh.title.setTextColor(isSelected ? mCheckedColor : mNormalColor);

			if (item.getIcon() > 0) {
				vh.icon.setImageResource(item.getIcon());
				if (isSelected) vh.icon.setColorFilter(mCheckedColor);
				else vh.icon.setColorFilter(mNormalIconColor);
			}

			return vh.root;
		}
	}

	public static class NavRowWrapper {
		public final View root;
		@InjectView(android.R.id.text1) public TextView title;
		@InjectView(android.R.id.icon1) public ImageView icon;

		private NavRowWrapper(ViewGroup parent) {
			root = LayoutInflater.from(parent.getContext()).inflate(R.layout.nav_drawer_row, parent, false);
			root.setTag(this);
			ButterKnife.inject(this, root);
		}

		public static NavRowWrapper get(View convertView, ViewGroup parent) {
			if (convertView == null) {
				return new NavRowWrapper(parent);
			}
			return (NavRowWrapper) convertView.getTag();
		}
	}

	/**
	 * Describes an item to be displayed in the navigation list
	 */
	public static class NavDrawerItem {
		private final String title;
		private final int icon;

		public NavDrawerItem(String title, int icon) {
			this.title = title;
			this.icon = icon;
		}

		public String getTitle() {
			return title;
		}

		public int getIcon() {
			return icon;
		}
	}

}
