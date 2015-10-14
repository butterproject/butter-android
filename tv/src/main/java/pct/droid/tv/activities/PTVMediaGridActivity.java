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

import pct.droid.base.providers.media.MediaProvider;
import pct.droid.tv.R;
import pct.droid.tv.activities.base.PTVBaseActivity;
import pct.droid.tv.fragments.PTVMediaGridFragment;

public class PTVMediaGridActivity extends PTVBaseActivity implements PTVMediaGridFragment.Callback {

    public static final String EXTRA_TITLE = "extra_title";
    public static final String EXTRA_SORT = "extra_sort";
    public static final String EXTRA_ORDER = "extra_order";
    public static final String EXTRA_GENRE = "extra_genre";
    public static final String EXTRA_PROVIDER_TYPE = "extra_provider_type";

    public enum ProviderType {
        SHOW, MOVIE
    }


    private final MediaProvider.Filters mFilter = new MediaProvider.Filters();
    private MediaProvider.Filters.Order mDefOrder;
    private MediaProvider.Filters.Sort mSort;
    private String mGenre;
    private ProviderType mType;

    public static Intent startActivity(Activity activity,String title, ProviderType type, MediaProvider.Filters.Sort sort, MediaProvider.Filters.Order defOrder, String genre) {
        Intent intent = new Intent(activity, PTVMediaGridActivity.class);
        intent.putExtra(EXTRA_TITLE, title);
        intent.putExtra(EXTRA_GENRE, genre);
        intent.putExtra(EXTRA_ORDER, defOrder);
        intent.putExtra(EXTRA_SORT, sort);
        intent.putExtra(EXTRA_PROVIDER_TYPE, type);
        activity.startActivity(intent);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, R.layout.activity_movie_media_grid);

        mSort = (MediaProvider.Filters.Sort) getIntent().getExtras().getSerializable(EXTRA_SORT);
        mDefOrder = (MediaProvider.Filters.Order) getIntent().getExtras().getSerializable(EXTRA_ORDER);
        mGenre = getIntent().getExtras().getString(EXTRA_GENRE);
        mType = (ProviderType) getIntent().getExtras().getSerializable(EXTRA_PROVIDER_TYPE);
        String title = getIntent().getExtras().getString(EXTRA_TITLE);
        setTitle(title);

        mFilter.sort = mSort;
        mFilter.order = mDefOrder;
        mFilter.genre = mGenre;

        //add media fragment
        getFragmentManager().beginTransaction().replace(R.id.fragment, PTVMediaGridFragment.newInstance()).commit();
    }

    @Override
    public MediaProvider.Filters getFilters() {
        return mFilter;
    }

    @Override
    public ProviderType getType() {
        return mType;
    }
}
