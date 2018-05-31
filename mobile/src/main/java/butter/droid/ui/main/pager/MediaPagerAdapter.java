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

package butter.droid.ui.main.pager;

import android.content.Context;

import java.util.List;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import butter.droid.R;
import butter.droid.base.utils.LocaleUtils;
import butter.droid.provider.base.filter.Filter;
import butter.droid.provider.base.filter.Genre;
import butter.droid.ui.main.genre.GenreSelectionFragment;
import butter.droid.ui.media.list.MediaListFragment;

public class MediaPagerAdapter extends FragmentStatePagerAdapter {

    private final Context context;

    private List<NavInfo> items;
    private Genre genre;

    public MediaPagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        this.context = context;
    }

    @Override
    public int getCount() {
        int cnt = 0;

        if (items != null) {
            cnt += items.size();
        }

        return cnt;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return context.getString(items.get(position).getLabel()).toUpperCase(LocaleUtils.getCurrent());
    }

    @Override public int getItemPosition(final Object object) {
        return POSITION_NONE;
    }

    @Override
    public Fragment getItem(int position) {
        NavInfo navInfo = items.get(position);
        switch (navInfo.getId()) {
            case R.id.nav_item_filter:
                return MediaListFragment.newInstance(navInfo.getProviderId(), new Filter(genre, navInfo.getSorter()));
            case R.id.nav_item_genre:
                return GenreSelectionFragment.newInstance(navInfo.getProviderId());
            default:
                throw new IllegalStateException("Unknown item type");
        }
    }

    public void setGenre(@Nullable Genre genre) {
        this.genre = genre;
    }

    public void setData(List<NavInfo> items) {
        this.items = items;
        notifyDataSetChanged();
    }

}
