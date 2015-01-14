package pct.droid.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;

import com.bugsnag.android.Bugsnag;

import butterknife.ButterKnife;
import pct.droid.base.PopcornApplication;
import pct.droid.base.preferences.Prefs;
import pct.droid.base.utils.LocaleUtils;
import pct.droid.base.utils.PrefUtils;

public class BaseActivity extends ActionBarActivity {

	protected Handler mHandler;

	public void onCreate(Bundle savedInstanceState, int layoutId) {
		super.onCreate(savedInstanceState);
		Bugsnag.onActivityCreate(this);
		setContentView(layoutId);
		ButterKnife.inject(this);
		mHandler = new Handler(getMainLooper());
	}

	@Override
	protected void onResume() {
		super.onResume();
		Bugsnag.onActivityResume(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		Bugsnag.onActivityPause(this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Bugsnag.onActivityDestroy(this);
	}

	@Override
	public void setContentView(int layoutResID) {
		String language = PrefUtils.get(this, Prefs.LOCALE, PopcornApplication.getSystemLanguage());
		LocaleUtils.setCurrent(LocaleUtils.toLocale(language));
		super.setContentView(layoutResID);
	}

	protected void onHomePressed() {
		Intent upIntent = NavUtils.getParentActivityIntent(this);
		if (upIntent != null && NavUtils.shouldUpRecreateTask(this, upIntent)) {
			// This activity is NOT part of this app's task, so create a new task
			// when navigating up, with a synthesized back stack.
			TaskStackBuilder.create(this)
					// Add all of this activity's parents to the back stack
					.addNextIntentWithParentStack(upIntent)
							// Navigate up to the closest parent
					.startActivities();
		} else {
			finish();
		}
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				onHomePressed();
				return true;

		}
		return super.onOptionsItemSelected(item);
	}

	protected PopcornApplication getApp() {
		return (PopcornApplication) getApplication();
	}

}