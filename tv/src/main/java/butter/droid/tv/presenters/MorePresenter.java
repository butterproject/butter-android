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
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v17.leanback.widget.Presenter;
import android.view.ViewGroup;
import butter.droid.base.utils.StringUtils;
import butter.droid.provider.base.filter.Sorter;
import butter.droid.provider.base.nav.NavItem;
import butter.droid.tv.R;

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
        @Nullable private Sorter sorter;
        private final int providerId;

        public MoreItem(@NonNull NavItem nav, final int providerId) {
            this.id = R.id.more_item_filter;
            this.icon = nav.getIcon();
            this.title = nav.getLabel();
            this.sorter = nav.getSorter();
            this.providerId = providerId;
        }

        public MoreItem(int id, @StringRes int text, @DrawableRes int iconResId) {
            if (id == R.id.more_item_filter) {
                throw new IllegalStateException("Filter item requires filter field to be set");
            }

            this.id = id;
            this.icon = iconResId;
            this.title = text;
            this.sorter = null;
            this.providerId = -1;
        }

        public int getId() {
            return id;
        }

        public int getTitle() {
            return title;
        }

        @Nullable public Sorter getSorter() {
            return sorter;
        }

        public int getProviderId() {
            return providerId;
        }
    }


}
