package com.popcorn.tv.presenters;

import android.graphics.Color;
import android.support.v17.leanback.widget.Presenter;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.TextView;

public class BasicRowPresenter extends Presenter {
	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent) {
		TextView view = new TextView(parent.getContext());
		view.setLayoutParams(new ViewGroup.LayoutParams(200, 200));
		view.setFocusable(true);
		view.setFocusableInTouchMode(true);
		view.setTextColor(Color.WHITE);
		view.setGravity(Gravity.CENTER);
		return new ViewHolder(view);
	}

	@Override
	public void onBindViewHolder(ViewHolder viewHolder, Object item) {
		((TextView) viewHolder.view).setText((String) item);
	}

	@Override
	public void onUnbindViewHolder(ViewHolder viewHolder) {
	}
}
