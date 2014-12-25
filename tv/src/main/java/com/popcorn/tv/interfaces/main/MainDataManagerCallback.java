package com.popcorn.tv.interfaces.main;

import com.popcorn.tv.models.MainMedia;
import java.util.ArrayList;

public interface MainDataManagerCallback
{
    public void onSuccess(ArrayList<MainMedia> items);
    public void onFailure(Exception e);
}
