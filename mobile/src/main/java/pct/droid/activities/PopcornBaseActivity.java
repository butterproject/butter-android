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
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.view.Menu;
import android.view.MenuItem;

import pct.droid.R;
import pct.droid.base.PopcornApplication;
import pct.droid.base.activities.TorrentBaseActivity;
import pct.droid.base.beaming.BeamManager;
import pct.droid.base.preferences.Prefs;
import pct.droid.base.utils.LocaleUtils;
import pct.droid.base.utils.PrefUtils;
import pct.droid.dialogfragments.BeamDeviceSelectorDialogFragment;

public class PopcornBaseActivity extends TorrentBaseActivity implements BeamManager.BeamListener {

    protected Boolean mShowCasting = false;

    @Override
    protected void onResume() {
        String language = PrefUtils.get(this, Prefs.LOCALE, PopcornApplication.getSystemLanguage());
        LocaleUtils.setCurrent(this, LocaleUtils.toLocale(language));
        super.onResume();
        BeamManager.getInstance(this).setListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        BeamManager.getInstance(this).setListener(null);
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

        BeamManager beamManager = BeamManager.getInstance(this);
        Boolean castingVisible = mShowCasting && beamManager.hasCastDevices();
        MenuItem item = menu.findItem(R.id.action_casting);
        item.setVisible(castingVisible);
        item.setIcon(beamManager.isConnected() ? R.drawable.ic_av_beam_connected : R.drawable.ic_av_beam_disconnected);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onHomePressed();
                return true;
            case R.id.action_casting:
                BeamDeviceSelectorDialogFragment.show(getFragmentManager());
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void updateBeamIcon() {
        supportInvalidateOptionsMenu();
    }

    public void setShowCasting(boolean b) {
        mShowCasting = b;
    }

}