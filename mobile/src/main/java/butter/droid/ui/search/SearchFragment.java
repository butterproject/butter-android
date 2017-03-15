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

import javax.inject.Inject;

import butter.droid.MobileButterApplication;
import butter.droid.base.providers.media.MediaProvider;
import butter.droid.ui.medialist.base.BaseMediaListFragment;

public class SearchFragment extends BaseMediaListFragment implements SearchView {

    @Inject SearchPresenter presenter;

    @Override public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MobileButterApplication.getAppContext()
                .getComponent()
                .searchComponentBuilder()
                .searchModule(new SearchModule(this))
                .build()
                .inject(this);
    }

    @Override public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


//        mEmptyView.setText(getString(R.string.no_search_results));
//        updateUI();
    }

    public void triggerSearch(String query) {
        presenter.triggerSearch(query);
    }

    public static SearchFragment newInstance(MediaProvider.Filters.Sort filter, MediaProvider.Filters.Order defOrder) {
        Bundle args = new Bundle();
        args.putSerializable(EXTRA_SORT, filter);
        args.putSerializable(EXTRA_ORDER, defOrder);

        SearchFragment fragment = new SearchFragment();
        fragment.setArguments(args);
        return fragment;
    }


}
