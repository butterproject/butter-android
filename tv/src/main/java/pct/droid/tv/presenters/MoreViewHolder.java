package pct.droid.tv.presenters;

import android.support.v17.leanback.widget.Presenter;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import pct.droid.tv.R;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class MoreViewHolder extends Presenter.ViewHolder {

	@InjectView(R.id.text) TextView textview;
	@InjectView(R.id.icon) ImageView imageview;

	public MoreViewHolder(View view) {
		super(view);
		ButterKnife.inject(this, view);
	}
}
