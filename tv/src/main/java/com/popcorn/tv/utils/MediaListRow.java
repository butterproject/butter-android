package com.popcorn.tv.utils;

import android.support.v17.leanback.widget.HeaderItem;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.ObjectAdapter;

public class MediaListRow extends ListRow
{
    private int rowIndex;

    //region Constructors

    public MediaListRow(HeaderItem header, ObjectAdapter adapter) {
        super(header, adapter);
    }

    public MediaListRow(long id, HeaderItem header, ObjectAdapter adapter) {
        super(id, header, adapter);
    }

    public MediaListRow(ObjectAdapter adapter) {
        super(adapter);
    }

    //endregion

    //region Setters/Getters

    public int getRowIndex() {
        return rowIndex;
    }

    public void setRowIndex(int rowIndex) {
        this.rowIndex = rowIndex;
    }

    //endregion
}
