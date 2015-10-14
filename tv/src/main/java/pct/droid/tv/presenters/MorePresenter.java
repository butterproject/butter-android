/*
 * This file is part of Popcorn Time.
 *
 * Popcorn Time is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Popcorn Time is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Popcorn Time. If not, see <http://www.gnu.org/licenses/>.
 */

package pct.droid.tv.presenters;

import android.content.Context;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.v17.leanback.widget.Presenter;
import android.view.ViewGroup;

import pct.droid.base.providers.media.MediaProvider;
import pct.droid.base.utils.LocaleUtils;
import pct.droid.base.utils.StringUtils;

public class MorePresenter extends Presenter {

	private Context mContext;

	public MorePresenter(Context context) {
		mContext = context.getApplicationContext();
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent) {
		final MoreCardView cardView = new MoreCardView(mContext);
		return new ViewHolder(cardView);
	}

	@Override
	public void onBindViewHolder(ViewHolder viewHolder, Object item) {
		MoreItem moreItem = (MoreItem) item;
		MoreCardView cardView = (MoreCardView) viewHolder.view;

		cardView.setTitleText(StringUtils.capWords(moreItem.mTitle.toLowerCase()));
		cardView.setImageResource(moreItem.mIcon);
	}

	@Override
	public void onUnbindViewHolder(ViewHolder viewHolder) {
	}

	public static class MoreItem {

		private final String mTitle;
		private final int mIcon;
		private final int mId;
		private MediaProvider.NavInfo mNavInfo;

		public MoreItem(int id, String text, @DrawableRes int iconResId, @Nullable MediaProvider.NavInfo info) {
			mId = id;
			mIcon = iconResId;
			mTitle = text.toUpperCase(LocaleUtils.getCurrent());
			this.mNavInfo = info;
		}

		public int getId() {
			return mId;
		}

		public MediaProvider.NavInfo getNavInfo() {
			return mNavInfo;
		}
	}


}
