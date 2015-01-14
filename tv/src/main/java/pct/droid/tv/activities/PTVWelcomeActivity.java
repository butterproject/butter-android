package pct.droid.tv.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import pct.droid.tv.R;

public class PTVWelcomeActivity extends Activity {

	public static Intent startActivity(Activity activity) {
		Intent intent = new Intent(activity, PTVWelcomeActivity.class);
		activity.startActivity(intent);
		return intent;
	}


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_welcome);
	}
}
