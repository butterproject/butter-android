package pct.droid.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;

import butterknife.InjectView;
import pct.droid.R;
import pct.droid.base.utils.PrefUtils;
import pct.droid.fragments.AboutFragment;
import pct.droid.utils.ToolbarUtils;

public class AboutActivity extends BaseActivity implements AboutFragment.OnFragmentInteractionListener {

	public static String TERMS_ACCEPTED = "terms_accepted";

	@InjectView(R.id.toolbar)
	Toolbar toolbar;

	public static Intent startActivity(Activity activity) {
		Intent intent = new Intent(activity, AboutActivity.class);
		activity.startActivity(intent);
		return intent;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState, R.layout.activity_about);
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		ToolbarUtils.updateToolbarHeight(this, toolbar);
	}

	public void acceptClick(View v) {
		PrefUtils.save(this, TERMS_ACCEPTED, true);
		Intent overviewIntent = new Intent(this, OverviewActivity.class);
		startActivity(overviewIntent);
		finish();
	}

	public void leaveClick(View v) {
		finish();
	}

}
