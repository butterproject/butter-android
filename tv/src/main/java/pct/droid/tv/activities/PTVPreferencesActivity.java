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

package pct.droid.tv.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v17.leanback.app.GuidedStepFragment;

import pct.droid.tv.activities.base.PTVBaseActivity;
import pct.droid.tv.fragments.PTVPreferencesFragment;

public class PTVPreferencesActivity extends PTVBaseActivity {

    public static Intent startActivity(Activity activity) {
        Intent intent = new Intent(activity, PTVPreferencesActivity.class);
        activity.startActivity(intent);
        return intent;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (null == savedInstanceState) {
            GuidedStepFragment.add(getFragmentManager(), new PTVPreferencesFragment());
        }
    }

    @Override
    public void onBackPressed() {
        if(getFragmentManager().getBackStackEntryCount() > 0) {
            getFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }
}
