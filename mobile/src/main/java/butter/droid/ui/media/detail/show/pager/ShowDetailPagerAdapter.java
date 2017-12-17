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

package butter.droid.ui.media.detail.show.pager;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import butter.droid.base.providers.media.model.MediaWrapper;
import butter.droid.ui.media.detail.show.about.ShowDetailAboutFragment;
import butter.droid.ui.media.detail.show.pager.model.UiShowDetailItem;
import butter.droid.ui.media.detail.show.pager.model.UiShowDetailSeason;
import butter.droid.ui.media.detail.show.season.ShowDetailSeasonFragment;
import java.util.List;

public class ShowDetailPagerAdapter extends FragmentPagerAdapter {

    private MediaWrapper show;
    private List<UiShowDetailItem> items;
    private Context context;

    public ShowDetailPagerAdapter(Context context, FragmentManager fm) {
        super(fm);
        this.context = context;
    }

    public void setData(@NonNull MediaWrapper show, List<UiShowDetailItem> items) {
        this.show = show;
        this.items = items;
        notifyDataSetChanged();
    }

    @Override
    public Fragment getItem(int position) {
        UiShowDetailItem item = items.get(position);
        switch (item.getType()) {
            case UiShowDetailItem.SHOW_DETAIL_ABOUT:
                return ShowDetailAboutFragment.newInstance(show);
            case UiShowDetailItem.SHOW_DETAIL_SPECIAL:
            case UiShowDetailItem.SHOW_DETAIL_SEASON:
                return ShowDetailSeasonFragment.newInstance(show.getMediaMeta(), ((UiShowDetailSeason) item).getSeason());
            default:
                throw new IllegalStateException("Unknown item type.");
        }
    }

    @Override
    public int getCount() {
        if (items == null) {
            return 0;
        } else {
            return items.size();
        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return items.get(position).getTitle(context);
    }

}
