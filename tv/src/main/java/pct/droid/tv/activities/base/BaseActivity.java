package pct.droid.tv.activities.base;

import android.app.Activity;

import pct.droid.tv.activities.PTVSearchActivity;

public abstract class BaseActivity extends Activity {

	@Override
	public boolean onSearchRequested() {
		PTVSearchActivity.startActivity(this);
		return true;
	}
}
