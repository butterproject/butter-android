package pct.droid.tv.presenters;

import android.content.Context;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.v17.leanback.widget.Presenter;
import android.view.ViewGroup;

import pct.droid.base.providers.media.MediaProvider;
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

		public MoreItem(int id,String text, @DrawableRes int iconResId,@Nullable MediaProvider.NavInfo info) {
			mId = id;
			mIcon = iconResId;
			mTitle = text;
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
