package pct.droid.tv.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import pct.droid.base.content.preferences.Prefs;
import pct.droid.base.utils.PrefUtils;
import pct.droid.tv.service.RecommendationService;

public class PTVLaunchActivity extends Activity {


	@Override protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent recommendationIntent = new Intent(this, RecommendationService.class);
		startService(recommendationIntent);

		Boolean firstRun = PrefUtils.get(this, Prefs.FIRST_RUN, true);


		if (firstRun) {
			//run the welcome wizard
			PTVWelcomeActivity.startActivity(this);
		} else
			PTVMainActivity.startActivity(this);
	}
}
