package com.popcorn.tv.interfaces.main;

import android.app.Activity;
import com.popcorn.tv.models.MainMedia;

public interface MainRouterInputInterface
{
    public void openMediaDetail(MainMedia media, Activity fromActivity);
}
