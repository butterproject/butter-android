package com.popcorn.tv.datamanagers;

import com.popcorn.tv.interfaces.main.MainDataManagerCallback;
import com.popcorn.tv.interfaces.main.MainDataManagerInputInterface;
import com.popcorn.tv.models.MainMedia;

import java.util.ArrayList;
import pct.droid.base.providers.media.MediaProvider;
import pct.droid.base.providers.media.YTSProvider;
import pct.droid.base.providers.media.types.Media;

public class YTSDataManager implements MainDataManagerInputInterface
{
    private MediaProvider mProvider = new YTSProvider();

    //region

    @Override
    public void getList(String genre, int page, final MainDataManagerCallback callback) {
        MediaProvider.Filters filters = new MediaProvider.Filters();
        filters.genre = genre;
        mProvider.getList(null, filters, new MediaProvider.Callback() {
            @Override
            public void onSuccess(ArrayList<Media> items) {
                ArrayList<MainMedia>list = new ArrayList<>();
                for (Media item: items) {
                    list.add(new MainMedia(item));
                }
                callback.onSuccess(list);
            }

            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }
        });
    }
    //endregion
}
