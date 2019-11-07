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

package butter.droid.tv.ui;

import android.app.AlertDialog;
import android.os.Bundle;

import butter.droid.base.utils.VersionUtils;
import butter.droid.tv.ui.search.TVSearchActivity;

public abstract class TVBaseActivity extends TVTorrentBaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!VersionUtils.isUsingCorrectBuild()) {
            new AlertDialog.Builder(this)
                    .setMessage(butter.droid.base.R.string.wrong_abi)
                    .setCancelable(false)
                    .show();
        }
    }

    @Override
    public boolean onSearchRequested() {
        startActivity(TVSearchActivity.newIntent(this));
        return true;
    }

}
