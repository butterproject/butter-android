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

import android.os.Bundle;
import android.support.annotation.Nullable;
import butter.droid.R;
import butter.droid.provider.base.filter.Filter;
import butter.droid.ui.media.list.base.BaseMediaListFragment;
import javax.inject.Inject;

public class SearchFragment extends BaseMediaListFragment implements SearchView {

    @Inject SearchPresenter presenter;

    @Override public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        emptyView.setText(R.string.no_search_results);
    }

    public void triggerSearch(String query) {
        presenter.triggerSearch(query);
    }

    public static SearchFragment newInstance(final int providerId, final Filter filter) {
        Bundle args = new Bundle();
        args.putInt(EXTRA_PROVIDER, providerId);
        args.putParcelable(EXTRA_FILTER, filter);

        SearchFragment fragment = new SearchFragment();
        fragment.setArguments(args);
        return fragment;
    }


}
