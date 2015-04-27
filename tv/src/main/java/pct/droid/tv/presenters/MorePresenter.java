package pct.droid.tv.presenters;

import android.content.Context;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;
import android.support.v17.leanback.widget.Presenter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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

		private final int mTitle;
		private final int mIcon;
		private final int mId;

		public MoreItem(int id,@StringRes int textId, @DrawableRes int iconResId) {
			mId = id;
			mIcon = iconResId;
			mTitle = textId;
		}

		public int getId() {
			return mId;
		}
	}


}
