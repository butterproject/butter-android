package pct.droid.tv.interfaces.main;

import android.support.v17.leanback.widget.Row;

public interface MainPresenterInputInterface
{
    public void onViewCreated();
    public void userDidSelectMedia(Object object);
    public void userDidClickMedia(Object object);
    public void userDidSelectItem(Object item, Row row);
}
