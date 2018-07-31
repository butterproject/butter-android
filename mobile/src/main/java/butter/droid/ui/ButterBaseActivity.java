/*
 * This file is part of Butter.
 *
 * Butter is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Butter is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Butter. If not, see <http://www.gnu.org/licenses/>.
 */

package butter.droid.ui;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import javax.inject.Inject;

import androidx.core.app.NavUtils;
import androidx.core.app.TaskStackBuilder;
import butter.droid.R;
import butter.droid.base.content.preferences.PreferencesHandler;
import butter.droid.base.manager.internal.beaming.BeamManager;
import butter.droid.base.utils.LocaleUtils;
import butter.droid.base.utils.VersionUtils;
import butter.droid.ui.beam.fragment.dialog.BeamDeviceSelectorDialogFragment;

public class ButterBaseActivity extends TorrentBaseActivity implements BeamManager.BeamListener {

    @Inject BeamManager beamManager;
    @Inject PreferencesHandler preferencesHandler;

    protected Boolean showCasting = false;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!VersionUtils.isUsingCorrectBuild()) {
            new AlertDialog.Builder(this)
                    .setMessage(butter.droid.base.R.string.wrong_abi)
                    .setCancelable(false)
                    .show();
        }
    }

    @Override
    protected void onResume() {
        String language = preferencesHandler.getLocale();
        LocaleUtils.setCurrent(this, LocaleUtils.toLocale(language));
        super.onResume();

        beamManager.addListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        beamManager.removeListener(this);
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
        getMenuInflater().inflate(R.menu.activity_base, menu);
        return true;
    }

    @Override public boolean onPrepareOptionsMenu(Menu menu) {
        Boolean castingVisible = showCasting && beamManager.hasCastDevices();
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
                BeamDeviceSelectorDialogFragment.show(getSupportFragmentManager());
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void updateBeamIcon() {
        supportInvalidateOptionsMenu();
    }

    public void setShowCasting(boolean show) {
        if (show != showCasting) {
            showCasting = show;
            supportInvalidateOptionsMenu();
        }
    }

}
