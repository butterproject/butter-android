package pct.droid.tv.presenters;

import android.support.v17.leanback.widget.Presenter;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import pct.droid.tv.R;

public class MoreViewHolder extends Presenter.ViewHolder implements View.OnFocusChangeListener {

    @Bind(R.id.text)
    TextView textview;
    @Bind(R.id.icon)
    ImageView imageview;

    public MoreViewHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);

        view.setOnFocusChangeListener(this);
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (hasFocus) {
            textview.setVisibility(View.VISIBLE);
        } else {
            textview.setVisibility(View.GONE);
        }
    }
}
