package pct.droid.tv.presenters.showdetail;

import android.support.v17.leanback.widget.RowPresenter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import pct.droid.tv.R;

public class EpisodeRowPresenter extends RowPresenter {

    private final Listener mListener;

    public EpisodeRowPresenter(Listener clickListener) {
        this.mListener = clickListener;
        this.setHeaderPresenter(null);
        this.setSelectEffectEnabled(false);
    }

    public ViewHolder createRowViewHolder(ViewGroup parent) {
        return new EpisodeRowViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.episode_row, parent, false));
    }

    public static class EpisodeRowViewHolder extends RowPresenter.ViewHolder {

        public
        @InjectView(R.id.title_textview)
        TextView titleTextView;
        public
        @InjectView(R.id.number_textview)
        TextView numberTextView;
        public
        @InjectView(R.id.duration_textview)
        TextView durationTextView;
        public
        @InjectView(R.id.synopsis_textview)
        TextView synopsisTextView;

        public EpisodeRowViewHolder(View view) {
            super(view);
            ButterKnife.inject(this, view);
        }
    }

    protected void onBindRowViewHolder(ViewHolder viewHolder, Object item) {
        super.onBindRowViewHolder(viewHolder, item);
        final EpisodeRow episodeRow = (EpisodeRow) item;

        final EpisodeRowViewHolder episodeViewHolder = (EpisodeRowViewHolder) viewHolder;

        if (null==episodeRow.getEpisode()){
            return;
        }

        ((EpisodeRowViewHolder) viewHolder).view.setClickable(true);
        ((EpisodeRowViewHolder) viewHolder).view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) mListener.onEpisodeRowClicked(episodeRow);
            }
        });

        String title = episodeRow.getEpisode().title;
        episodeViewHolder.titleTextView.setText(title);
        episodeViewHolder.synopsisTextView.setText(episodeRow.getEpisode().overview);
        episodeViewHolder.numberTextView.setText(String.format("%02d", episodeRow.getEpisode().episode));
    }

    public interface Listener {
        void onEpisodeRowClicked(EpisodeRow row);
    }
}
