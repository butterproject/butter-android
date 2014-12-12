package com.popcorn.tv.interfaces.main;

import android.app.Activity;
import android.content.Context;
import android.support.v17.leanback.widget.ObjectAdapter;

public interface MainViewInputInterface
{
    public Activity getActivity();
    public Context getContext();
    public void setBackgroundWithUri(String uri);
    public void setAdapter(ObjectAdapter adapter);
}
