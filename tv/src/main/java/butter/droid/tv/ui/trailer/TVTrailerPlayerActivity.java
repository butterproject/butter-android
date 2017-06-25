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

package butter.droid.tv.ui.trailer;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import butter.droid.provider.base.module.Media;
import butter.droid.tv.TVButterApplication;
import butter.droid.tv.ui.TVBaseActivity;
import org.parceler.Parcels;

public class TVTrailerPlayerActivity extends TVBaseActivity {

    private static final String EXTRA_URI = "butter.droid.tv.ui.trailer.TVTrailerPlayerActivity.EXTRA_URI";
    private static final String EXTRA_MEDIA = "butter.droid.tv.ui.trailer.TVTrailerPlayerActivity.EXTRA_MEDIA";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        TVButterApplication.getAppContext()
                .getComponent()
                .inject(this);

        super.onCreate(savedInstanceState, 0);

        final Intent intent = getIntent();
        final Media media = Parcels.unwrap(intent.getParcelableExtra(EXTRA_MEDIA));
        final String youtubeUrl = intent.getStringExtra(EXTRA_URI);

        if (savedInstanceState == null) {
            TVTrailerPlayerFragment fragment = TVTrailerPlayerFragment.newInstance(media, youtubeUrl);
            getSupportFragmentManager().beginTransaction()
                    .add(android.R.id.content, fragment)
                    .commit();
        }

    }

    public static Intent getIntent(final Context context, final Media media, final String url) {
        final Intent intent = new Intent(context, TVTrailerPlayerActivity.class);
        intent.putExtra(EXTRA_MEDIA, Parcels.wrap(media));
        intent.putExtra(EXTRA_URI, url);
        return intent;
    }
}
