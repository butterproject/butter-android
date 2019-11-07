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
import androidx.annotation.IntDef;
import androidx.annotation.NonNull;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public interface UiShowDetailItem {

    @ShowDetailItemType int getType();

    String getTitle(@NonNull Context context);

    // region IntDef

    @IntDef({SHOW_DETAIL_ABOUT, SHOW_DETAIL_SPECIAL, SHOW_DETAIL_SEASON})
    @Retention(RetentionPolicy.SOURCE)
    @interface ShowDetailItemType { }

    int SHOW_DETAIL_ABOUT = 0;
    int SHOW_DETAIL_SPECIAL = 1;
    int SHOW_DETAIL_SEASON = 2;

    // endregion IntDef

}
