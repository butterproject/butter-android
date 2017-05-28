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

package butter.droid.tv.presenters;

import android.content.Context;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v17.leanback.widget.Presenter;
import android.view.ViewGroup;
import butter.droid.base.providers.media.MediaProvider;
import butter.droid.base.utils.StringUtils;

public class MorePresenter extends Presenter {

    private Context context;

    public MorePresenter(Context context) {
        this.context = context.getApplicationContext();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent) {
        final MoreCardView cardView = new MoreCardView(context);
        return new ViewHolder(cardView);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, Object item) {
        MoreItem moreItem = (MoreItem) item;
        MoreCardView cardView = (MoreCardView) viewHolder.view;

        cardView.setTitleText(StringUtils.capWords(context.getString(moreItem.title).toLowerCase()));
        cardView.setImageResource(moreItem.icon);
    }

    @Override
    public void onUnbindViewHolder(ViewHolder viewHolder) {
    }

    public static class MoreItem {

        @StringRes private final int title;
        private final int icon;
        private final int id;
        private MediaProvider.NavInfo navInfo;

        public MoreItem(@NonNull MediaProvider.NavInfo info) {
            this.id = info.getId();
            icon = info.getIcon();
            title = info.getLabel();
            this.navInfo = info;
        }

        public MoreItem(int id, @StringRes int text, @DrawableRes int iconResId) {
            this.id = id;
            icon = iconResId;
            title = text;
            this.navInfo = null;
        }

        public int getId() {
            return id;
        }

        public MediaProvider.NavInfo getNavInfo() {
            return navInfo;
        }
    }


}
