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

package butter.droid.tv.ui.media;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.StringRes;
import butter.droid.provider.base.filter.Filter;
import butter.droid.tv.R;
import butter.droid.tv.TVButterApplication;
import butter.droid.tv.ui.TVBaseActivity;
import org.parceler.Parcels;

public class TVMediaGridActivity extends TVBaseActivity {

    private static final String EXTRA_TITLE = "butter.droid.tv.ui.media.TVMediaGridActivity.extra_title";
    private static final String EXTRA_FILTER = "butter.droid.tv.ui.media.TVMediaGridActivity.extra_filter";
    private static final String EXTRA_PROVIDER = "butter.droid.tv.ui.media.TVMediaGridActivity.extra_provider";

    @SuppressLint("MissingSuperCall")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        TVButterApplication.getAppContext()
                .getInternalComponent()
                .inject(this);

        super.onCreate(savedInstanceState, R.layout.activity_movie_media_grid);

        Bundle extras = getIntent().getExtras();
        final Filter filter = Parcels.unwrap(extras.getParcelable(EXTRA_FILTER));
        @StringRes int title = extras.getInt(EXTRA_TITLE);
        final int providerId = extras.getInt(EXTRA_PROVIDER);

        //add media fragment
        getFragmentManager().beginTransaction().replace(R.id.fragment, TVMediaGridFragment.newInstance(providerId, title, filter))
                .commit();
    }

    public static Intent newIntent(Context context, final int providerId, @StringRes int title, Filter filter) {
        Intent intent = new Intent(context, TVMediaGridActivity.class);
        intent.putExtra(EXTRA_TITLE, title);
        intent.putExtra(EXTRA_FILTER, Parcels.wrap(filter));
        intent.putExtra(EXTRA_PROVIDER, providerId);
        return intent;
    }

}
