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

package butter.droid.ui.search;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import butter.droid.R;
import butter.droid.ui.ButterBaseActivity;
import butter.droid.utils.ToolbarUtils;
import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * This activity handles searching a provider.
 * <p/>
 * It must be started with a provider id, which then gets forwarded to the overview fragment
 */
public class SearchActivity extends ButterBaseActivity {

    private static final String EXTRA_PROVIDER = "butter.droid.ui.search.SearchActivity.provider";

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.searchview) SearchView searchview;

    private SearchFragment fragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setShowCasting(true);

        ToolbarUtils.updateToolbarHeight(this, toolbar);

        searchview.onActionViewExpanded();
        searchview.setOnQueryTextListener(searchListener);

        // don't re add the fragment if it exists
        if (null != savedInstanceState) {
            fragment = (SearchFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);
        } else {
            //create and add the media fragment
            // TODO: 6/17/17 Handle null
            Bundle extras = getIntent().getExtras();
            fragment = SearchFragment.newInstance(extras.getInt(EXTRA_PROVIDER), null);

            getSupportFragmentManager().beginTransaction().replace(R.id.fragment, fragment).commit();
        }
    }

    private SearchView.OnQueryTextListener searchListener = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String string) {
            if (null == fragment) {
                return false; //fragment not added yet.
            }
            fragment.triggerSearch(string);
            return true;
        }

        @Override
        public boolean onQueryTextChange(String string) {
            return onQueryTextSubmit(string);
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
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public static Intent getIntent(final Context context, final int providerId) {
        Intent intent = new Intent(context, SearchActivity.class);
        intent.putExtra(EXTRA_PROVIDER, providerId);
        return intent;
    }

}
