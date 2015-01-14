package pct.droid.tv.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import pct.droid.tv.R;

public class PTVMainActivity extends Activity {

	public static Intent startActivity(Activity activity) {
		Intent intent = new Intent(activity, PTVMainActivity.class);
		activity.startActivity(intent);
		return intent;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

	@Override
	public boolean onSearchRequested() {
		startActivity(new Intent(this, PTVSearchActivity.class));
		return true;
	}
}
