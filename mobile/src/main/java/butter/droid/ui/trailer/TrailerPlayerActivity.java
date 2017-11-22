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

package butter.droid.ui.trailer;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import butter.droid.MobileButterApplication;
import butter.droid.base.providers.media.model.MediaWrapper;
import butter.droid.ui.ButterBaseActivity;
import org.parceler.Parcels;

public class TrailerPlayerActivity extends ButterBaseActivity {

    private static final String EXTRA_URI = "butter.droid.ui.trailer.TrailerPlayerActivity.uri";
    private static final String EXTRA_MEDIA = "butter.droid.ui.trailer.TrailerPlayerActivity.media";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        MobileButterApplication.getAppContext()
                .getComponent()
                .inject(this);
        super.onCreate(savedInstanceState, 0);

        final Intent intent = getIntent();
        final MediaWrapper media = Parcels.unwrap(intent.getParcelableExtra(EXTRA_MEDIA));
        final String youtubeUrl = intent.getStringExtra(EXTRA_URI);

        if (savedInstanceState == null) {
            TrailerPlayerFragment fragment = TrailerPlayerFragment.newInstance(media, youtubeUrl);
            getSupportFragmentManager().beginTransaction()
                    .add(android.R.id.content, fragment)
                    .commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public static Intent getIntent(final Context context, final MediaWrapper media, final String url) {
        final Intent intent = new Intent(context, TrailerPlayerActivity.class);
        intent.putExtra(TrailerPlayerActivity.EXTRA_MEDIA, Parcels.wrap(media));
        intent.putExtra(TrailerPlayerActivity.EXTRA_URI, url);
        return intent;
    }
}
