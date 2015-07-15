package pct.droid.tv.presenters.showdetail;

import android.support.v17.leanback.widget.RowPresenter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import pct.droid.tv.R;

public class SeasonHeaderRowPresenter extends RowPresenter {

    public SeasonHeaderRowPresenter() {
        this.setHeaderPresenter(null);
        this.setSelectEffectEnabled(false);
    }

    public ViewHolder createRowViewHolder(ViewGroup parent) {
        return new SeasonHeaderRowViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.season_header_row, parent, false));
    }

    public static class SeasonHeaderRowViewHolder extends ViewHolder {

        public
        @Bind(R.id.title_textview)
        TextView titleTextView;

        public SeasonHeaderRowViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    protected void onBindRowViewHolder(ViewHolder viewHolder, Object item) {
        super.onBindRowViewHolder(viewHolder, item);
        final SeasonHeaderRow episodeRow = (SeasonHeaderRow) item;

        final SeasonHeaderRowViewHolder seasonViewHolder = (SeasonHeaderRowViewHolder) viewHolder;

        seasonViewHolder.titleTextView.setText(String.format("Season %d", episodeRow.getSeason()));
    }
}
