package pct.droid.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;

import butterknife.InjectView;
import pct.droid.R;
import pct.droid.fragments.MediaFragment;
import pct.droid.utils.ToolbarUtils;


/**
 * This activity handles searching a provider.
 *
 * It must be started with a provider id, which then gets forwarded to the overview fragment
 */
public class SearchActivity extends BaseActivity {

	public static final String EXTRA_PROVIDER = "extra_provider";

	@InjectView(R.id.toolbar)
	Toolbar toolbar;

	@InjectView(R.id.searchview)
	SearchView searchview;

	private MediaFragment mFragment;

	public static Intent startActivity(Activity activity, int provider) {
		Intent intent = new Intent(activity, SearchActivity.class);
		intent.putExtra(EXTRA_PROVIDER, provider);
		activity.startActivity(intent);
		activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
		return intent;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState, R.layout.activity_search);
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		ToolbarUtils.updateToolbarHeight(this, toolbar);

		int provider = getIntent().getExtras().getInt(EXTRA_PROVIDER);

		searchview.setOnQueryTextListener(mSearchListener);
		searchview.onActionViewExpanded();

		//dont re add the fragment if it exists
		if (null != savedInstanceState) {
			mFragment = (MediaFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);
			return;
		}

		//create and add the media fragment
		mFragment =
				MediaFragment.newInstance(MediaFragment.Mode.SEARCH, provider);
		getSupportFragmentManager().beginTransaction().replace(R.id.fragment, mFragment).commit();
	}

	private SearchView.OnQueryTextListener mSearchListener = new SearchView.OnQueryTextListener() {
		@Override
		public boolean onQueryTextSubmit(String s) {
			if (null == mFragment) return false;//fragment not added yet.
			mFragment.triggerSearch(s);
			return true;
		}

		@Override
		public boolean onQueryTextChange(String s) {
			if (s.equals("")) {
				onQueryTextSubmit(s);
			}
			return false;
		}
	};
}
