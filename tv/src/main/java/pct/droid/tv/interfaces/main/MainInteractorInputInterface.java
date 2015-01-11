package pct.droid.tv.interfaces.main;

import android.content.Context;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.HeaderItem;

import pct.droid.tv.utils.MediaListRow;

public interface MainInteractorInputInterface
{
    public int getNumberOfSections(Context context);
    public HeaderItem getSectionHeaderAtIndex(int index, Context context);
    public ArrayObjectAdapter getSectionAdapterAtIndex(int index, Context context);
    public int getRightItemsNextTo(Object item, MediaListRow row);
    public void getMore(MediaListRow row, Context context);
    public void synchronize(Context context);
}
