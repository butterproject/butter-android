package pct.droid.tv.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import pct.droid.tv.R;
import pct.droid.tv.activities.base.PTVBaseActivity;


public class PTVSearchActivity extends PTVBaseActivity {

	public static Intent startActivity(Activity activity) {
		Intent intent = new Intent(activity, PTVSearchActivity.class);
		activity.startActivity(intent);
		return intent;
	}

	/**
	 * Called when the activity is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState,R.layout.activity_search);
	}
}
