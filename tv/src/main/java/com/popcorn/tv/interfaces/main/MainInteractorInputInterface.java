package com.popcorn.tv.interfaces.main;

import android.content.Context;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.HeaderItem;
import android.support.v17.leanback.widget.Row;

public interface MainInteractorInputInterface
{
    public int getNumberOfSections(Context context);
    public HeaderItem getSectionHeaderAtIndex(int index, Context context);
    public ArrayObjectAdapter getSectionAdapterAtIndex(int index, Context context);
    public int getRightItemsNextTo(Object item, Row row);
    public void getMore(Row row);
}
