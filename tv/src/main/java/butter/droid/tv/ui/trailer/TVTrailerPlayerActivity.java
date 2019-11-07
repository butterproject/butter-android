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
import butter.droid.base.providers.media.model.MediaWrapper;
import butter.droid.tv.ui.TVBaseActivity;

public class TVTrailerPlayerActivity extends TVBaseActivity {

    private static final String EXTRA_URI = "butter.droid.tv.ui.trailer.TVTrailerPlayerActivity.EXTRA_URI";
    private static final String EXTRA_MEDIA = "butter.droid.tv.ui.trailer.TVTrailerPlayerActivity.EXTRA_MEDIA";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Intent intent = getIntent();
        final MediaWrapper media = intent.getParcelableExtra(EXTRA_MEDIA);
        final String youtubeUrl = intent.getStringExtra(EXTRA_URI);

        if (savedInstanceState == null) {
            TVTrailerPlayerFragment fragment = TVTrailerPlayerFragment.newInstance(media, youtubeUrl);
            getSupportFragmentManager().beginTransaction()
                    .add(android.R.id.content, fragment)
                    .commit();
        }

    }

    public static Intent getIntent(final Context context, final MediaWrapper media, final String url) {
        final Intent intent = new Intent(context, TVTrailerPlayerActivity.class);
        intent.putExtra(EXTRA_MEDIA, media);
        intent.putExtra(EXTRA_URI, url);
        return intent;
    }
}
