package pct.droid.tv.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import pct.droid.tv.R;
import pct.droid.tv.activities.base.PTVBaseActivity;

public class PTVWelcomeActivity extends PTVBaseActivity {

	public static Intent startActivity(Activity activity) {
		Intent intent = new Intent(activity, PTVWelcomeActivity.class);
		activity.startActivity(intent);
		return intent;
	}


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState,R.layout.activity_welcome);
	}
}
