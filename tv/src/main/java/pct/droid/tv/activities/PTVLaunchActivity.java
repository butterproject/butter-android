package pct.droid.tv.activities;

import android.app.Activity;
import android.os.Bundle;

import pct.droid.base.preferences.Prefs;
import pct.droid.base.utils.PrefUtils;

public class PTVLaunchActivity extends Activity {


	@Override protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Boolean firstRun = PrefUtils.get(this, Prefs.FIRST_RUN, true);
		if (firstRun) {
			//run the welcome wizard
			PTVWelcomeActivity.startActivity(this);
		} else
			PTVMainActivity.startActivity(this);
	}
}
