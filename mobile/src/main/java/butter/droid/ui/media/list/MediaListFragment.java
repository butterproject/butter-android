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

package butter.droid.ui.media.list;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import butter.droid.provider.base.filter.Filter;
import butter.droid.ui.main.MainActivity;
import butter.droid.ui.media.list.base.BaseMediaListFragment;
import javax.inject.Inject;
import org.parceler.Parcels;

public class MediaListFragment extends BaseMediaListFragment implements MediaListView {

    @Inject MediaListPresenter presenter;

    @Override public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ((MainActivity) getActivity()).getComponent()
                .mediaListComponentBuilder()
                .mediaListModule(new MediaListModule(this))
                .build()
                .inject(this);

        presenter.onCreate();
    }

    @Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        pagingManager.getNextPage();
    }

    @Override public void onDestroy() {
        super.onDestroy();

        presenter.onDestroy();
    }

    public static BaseMediaListFragment newInstance(final int providerId, final Filter filter) {
        Bundle args = new Bundle();
        args.putInt(EXTRA_PROVIDER, providerId);
        args.putParcelable(EXTRA_FILTER, Parcels.wrap(filter));

        MediaListFragment fragment = new MediaListFragment();
        fragment.setArguments(args);
        return fragment;
    }

}
