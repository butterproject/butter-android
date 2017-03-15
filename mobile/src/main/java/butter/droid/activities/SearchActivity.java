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

package butter.droid.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import butter.droid.MobileButterApplication;
import butter.droid.R;
import butter.droid.base.providers.media.MediaProvider;
import butter.droid.ui.ButterBaseActivity;
import butter.droid.ui.search.SearchFragment;
import butter.droid.utils.ToolbarUtils;
import butterknife.BindView;


/**
 * This activity handles searching a provider.
 * <p/>
 * It must be started with a provider id, which then gets forwarded to the overview fragment
 */
public class SearchActivity extends ButterBaseActivity {

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.searchview) SearchView searchview;

    private SearchFragment fragment;

    @SuppressLint("MissingSuperCall") @Override
    public void onCreate(Bundle savedInstanceState) {
        MobileButterApplication.getAppContext()
                .getComponent()
                .inject(this);

        super.onCreate(savedInstanceState, R.layout.activity_search);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setShowCasting(true);

        ToolbarUtils.updateToolbarHeight(this, toolbar);

        searchview.onActionViewExpanded();
        searchview.setOnQueryTextListener(mSearchListener);

        //dont re add the fragment if it exists
        if (null != savedInstanceState) {
            fragment = (SearchFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);
            return;
        }

        //create and add the media fragment
        fragment = SearchFragment.newInstance(MediaProvider.Filters.Sort.POPULARITY,
                MediaProvider.Filters.Order.DESC);

        getSupportFragmentManager().beginTransaction().replace(R.id.fragment, fragment).commit();
    }

    private SearchView.OnQueryTextListener mSearchListener = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String s) {
            if (null == fragment) return false; //fragment not added yet.
            fragment.triggerSearch(s);
            return true;
        }

        @Override
        public boolean onQueryTextChange(String s) {
            return onQueryTextSubmit(s);
        }
    };


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onHomePressed();
                //to fade out the activity
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    public static Intent getIntent(Context context) {
        return new Intent(context, SearchActivity.class);
    }

    /**
     * @deprecated Use {@link #getIntent(Context)}
     */
    @Deprecated
    public static Intent startActivity(Activity activity) {
        Intent intent = getIntent(activity);
        activity.startActivity(intent);
//		activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out,);
        return intent;
    }

}
