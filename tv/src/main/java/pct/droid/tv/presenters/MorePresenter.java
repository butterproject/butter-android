package pct.droid.tv.presenters;

import android.content.Context;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v17.leanback.widget.Presenter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import pct.droid.base.providers.media.MediaProvider;
import pct.droid.tv.R;

public class MorePresenter extends Presenter {

	private Context mContext;

	public MorePresenter(Context context) {
		mContext = context.getApplicationContext();
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent) {
		View view = LayoutInflater.from(mContext).inflate(R.layout.more_row, parent, false);
		return new MoreViewHolder(view);
	}

	@Override
	public void onBindViewHolder(ViewHolder viewHolder, Object item) {
		MoreItem moreItem = (MoreItem) item;
		MoreViewHolder vh = (MoreViewHolder) viewHolder;
		vh.textview.setText(moreItem.mTitle);
		vh.imageview.setImageResource(moreItem.mIcon);
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
