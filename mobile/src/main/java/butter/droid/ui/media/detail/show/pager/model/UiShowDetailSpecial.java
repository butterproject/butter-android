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

package butter.droid.ui.media.detail.show.pager.model;

import android.content.Context;
import android.support.annotation.NonNull;

import butter.droid.R;
import butter.droid.provider.base.model.Season;

// TODO should be same as season as only title is different (we can reuse fragments)
public class UiShowDetailSpecial extends UiShowDetailSeason {

    public UiShowDetailSpecial(Season season) {
        super(season);
    }

    @Override public int getType() {
        return SHOW_DETAIL_SPECIAL;
    }

    @Override public String getTitle(@NonNull Context context) {
        return context.getString(R.string.specials);
    }

}
