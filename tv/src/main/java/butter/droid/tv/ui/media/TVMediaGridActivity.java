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
import butter.droid.base.providers.media.MediaProvider;
import butter.droid.tv.R;
import butter.droid.tv.TVButterApplication;
import butter.droid.tv.activities.base.TVBaseActivity;

public class TVMediaGridActivity extends TVBaseActivity {

    private static final String EXTRA_TITLE = "butter.droid.tv.ui.media.TVMediaGridActivity.extra_title";
    private static final String EXTRA_SORT = "butter.droid.tv.ui.media.TVMediaGridActivity.extra_sort";
    private static final String EXTRA_ORDER = "butter.droid.tv.ui.media.TVMediaGridActivity.extra_order";
    private static final String EXTRA_GENRE = "butter.droid.tv.ui.media.TVMediaGridActivity.extra_genre";

    @SuppressLint("MissingSuperCall")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        TVButterApplication.getAppContext()
                .getComponent()
                .inject(this);

        super.onCreate(savedInstanceState, R.layout.activity_movie_media_grid);

        Bundle extras = getIntent().getExtras();
        final MediaProvider.Filters.Sort sort = (MediaProvider.Filters.Sort) extras.getSerializable(EXTRA_SORT);
        final MediaProvider.Filters.Order defOrder = (MediaProvider.Filters.Order) extras.getSerializable(EXTRA_ORDER);
        final String genre = extras.getString(EXTRA_GENRE);
        @StringRes int title = extras.getInt(EXTRA_TITLE);

        //add media fragment
        getFragmentManager().beginTransaction().replace(R.id.fragment, TVMediaGridFragment.newInstance(title, sort, defOrder, genre))
                .commit();
    }

    public static Intent newIntent(Context context, @StringRes int title, MediaProvider.Filters.Sort sort,
            MediaProvider.Filters.Order defOrder, String genre) {
        Intent intent = new Intent(context, TVMediaGridActivity.class);
        intent.putExtra(EXTRA_TITLE, title);
        intent.putExtra(EXTRA_GENRE, genre);
        intent.putExtra(EXTRA_ORDER, defOrder);
        intent.putExtra(EXTRA_SORT, sort);
        return intent;
    }

}
