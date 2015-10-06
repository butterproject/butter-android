package pct.droid.adapters;

import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.PresenterSelector;

public class SeasonAdapter extends ArrayObjectAdapter {
    private EpisodeAdapter episodeAdapter;

    public SeasonAdapter(PresenterSelector presenter) {
        super(presenter);
    }

    @Override
    public Object get(int position) {
        if (position < this.size()) {
            // Return season as adapter first object
            if (position == 0) {
                return super.get(position);
            }

            // Return episode as adapter other object
            return episodeAdapter.get(position - 1);
        }

        throw new IllegalArgumentException("Position %s is invalid in SeasonAdapter object array");
    }

    @Override
    public int size() {
        // Adapter size is season row + episodes
        if (episodeAdapter != null) {
            return episodeAdapter.size() + 1;
        }
        else return 0;
    }

    public void setEpisodeAdapter(EpisodeAdapter episodeAdapter) {
        this.episodeAdapter = episodeAdapter;
        if (episodeAdapter.size() > 0) notifyChanged();
    }
}
