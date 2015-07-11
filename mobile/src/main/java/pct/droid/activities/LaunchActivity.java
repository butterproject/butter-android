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

import com.crashlytics.android.Crashlytics;

import io.fabric.sdk.android.Fabric;
import pct.droid.R;
import pct.droid.activities.base.PopcornBaseActivity;
import pct.droid.base.BuildConfig;
import pct.droid.base.torrent.TorrentService;
import pct.droid.base.utils.PrefUtils;
import pct.droid.base.utils.SignUtils;
import pct.droid.dialogfragments.MessageDialogFragment;

public class LaunchActivity extends PopcornBaseActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        TorrentService.start(this);

        if(SignUtils.checkAppSignature(this) != SignUtils.VALID && !BuildConfig.GIT_BRANCH.equals("local")) {
            MessageDialogFragment.show(getFragmentManager(), R.string.signature_invalid, R.string.possibly_dangerous, false);
            return;
        }

        if (PrefUtils.contains(this, TermsActivity.TERMS_ACCEPTED)) {
            startActivity(new Intent(this, MainActivity.class));
        } else {
            startActivity(new Intent(this, TermsActivity.class));
        }
        finish();
    }

}
