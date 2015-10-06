package pct.droid.tv.presenters.showdetail;

import android.content.Context;
import android.support.v17.leanback.widget.Row;

public class SeasonHeaderRow extends Row {
    private final Context mContext;
    private final int mPosition;
    private final int mSeason;

    public SeasonHeaderRow(Context context, int position, int season) {
        this.mContext = context;
        this.mPosition = position;
        this.mSeason = season;
    }

    public int getSeason() {
        return mSeason;
    }
}
