package pct.droid.adapters;

import android.content.Context;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.PresenterSelector;

import java.util.List;
import java.util.Map;

import pct.droid.base.providers.media.models.Episode;
import pct.droid.tv.presenters.showdetail.SeasonEpisodeRow;
import pct.droid.tv.presenters.showdetail.SeasonHeaderRow;

public class EpisodeAdapter extends ArrayObjectAdapter {
    private final Context context;
    Map<Integer, List<Episode>> seasons;

    private int mSize = -1;

    public EpisodeAdapter(Context context, PresenterSelector presenter) {
        super(presenter);
        this.context = context;
    }

    public void setSeasons(Map<Integer, List<Episode>> seasons) {
        this.seasons = seasons;
    }

    @Override
    public int size() {
        if (mSize == -1) {
            mSize = calculateSize();
        }
        return mSize;
    }

    private int calculateSize() {
        if (seasons == null) {
            return -1;
        }

        int size = seasons.size(); // the number of headers

        for (Map.Entry<Integer, List<Episode>> entry : seasons.entrySet()) {
            size += entry.getValue().size();
        }

        return size;
    }

    @Override
    public Object get(int position) {
        for (Map.Entry<Integer, List<Episode>> adapter : seasons.entrySet()) {
            // season header
            if (position == 0) {
                return new SeasonHeaderRow(context, position, adapter.getKey());
            }

            position -= 1;

            // episode row
            int size = adapter.getValue().size();

            if (position < size) {
                return new SeasonEpisodeRow(adapter.getValue().get(position));
            }

            position -= size;
        }

        return null;
    }
}
