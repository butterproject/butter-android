/*
 * This file is part of Popcorn Time.
 *
 * Popcorn Time is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Popcorn Time is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Popcorn Time. If not, see <http://www.gnu.org/licenses/>.
 */

package pct.droid.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.connectsdk.device.ConnectableDevice;
import com.connectsdk.discovery.DiscoveryManager;
import com.connectsdk.discovery.DiscoveryManagerListener;
import com.connectsdk.service.command.ServiceCommandError;

import butterknife.ButterKnife;
import pct.droid.R;
import pct.droid.base.PopcornApplication;
import pct.droid.base.connectsdk.BeamManager;
import pct.droid.base.preferences.Prefs;
import pct.droid.base.utils.LocaleUtils;
import pct.droid.base.utils.PrefUtils;
import pct.droid.dialogfragments.CastDeviceSelectorDialogFragment;

public class BaseActivity extends ActionBarActivity implements DiscoveryManagerListener {

	protected Handler mHandler;
    private Boolean mShowCasting = false, mCastingVisible = false;

	public void onCreate(Bundle savedInstanceState, int layoutId) {
        String language = PrefUtils.get(this, Prefs.LOCALE, PopcornApplication.getSystemLanguage());
        LocaleUtils.setCurrent(this, LocaleUtils.toLocale(language));
		super.onCreate(savedInstanceState);
		setContentView(layoutId);
		ButterKnife.inject(this);
		mHandler = new Handler(getMainLooper());
	}

	@Override
	protected void onResume() {
        String language = PrefUtils.get(this, Prefs.LOCALE, PopcornApplication.getSystemLanguage());
        LocaleUtils.setCurrent(this, LocaleUtils.toLocale(language));
		super.onResume();
        BeamManager.getInstance(this).addListener(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
        BeamManager.getInstance(this).removeListener(this);
	}

	@Override
	public void setContentView(int layoutResID) {
		String language = PrefUtils.get(this, Prefs.LOCALE, PopcornApplication.getSystemLanguage());
		LocaleUtils.setCurrent(this, LocaleUtils.toLocale(language));
		super.setContentView(layoutResID);
	}

    public void setShowCasting(boolean b) {
        mShowCasting = b;
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
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.activity_base, menu);

        mCastingVisible = mShowCasting && BeamManager.getInstance(this).hasCastDevices();
        menu.findItem(R.id.action_casting).setVisible(mCastingVisible);

        return true;
    }

    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				onHomePressed();
				return true;
            case R.id.action_casting:
                CastDeviceSelectorDialogFragment.show(getFragmentManager());
                break;
		}
		return super.onOptionsItemSelected(item);
	}

	protected PopcornApplication getApp() {
		return (PopcornApplication) getApplication();
	}

    @Override
    public void onDeviceAdded(DiscoveryManager manager, ConnectableDevice device) {
        if(mShowCasting && !mCastingVisible) {
            supportInvalidateOptionsMenu();
        }
    }

    @Override
    public void onDeviceUpdated(DiscoveryManager manager, ConnectableDevice device) {

    }

    @Override
    public void onDeviceRemoved(DiscoveryManager manager, ConnectableDevice device) {
        if(mShowCasting && mCastingVisible) {
            supportInvalidateOptionsMenu();
        }
    }

    @Override
    public void onDiscoveryFailed(DiscoveryManager manager, ServiceCommandError error) {

    }
}