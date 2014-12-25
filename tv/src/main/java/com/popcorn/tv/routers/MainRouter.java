package com.popcorn.tv.routers;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import com.popcorn.tv.activities.DetailsActivity;
import com.popcorn.tv.fragments.VideoDetailsFragment;
import com.popcorn.tv.interfaces.main.MainRouterInputInterface;
import com.popcorn.tv.models.MainMedia;

public class MainRouter implements MainRouterInputInterface {
    private static String TAG = "MainRouter";

    @Override
    public void openMediaDetail(MainMedia media, Activity fromActivity) {
        Log.v(TAG, "Opening media: "+ media);
        Intent intent = new Intent(fromActivity, DetailsActivity.class);
        intent.putExtra(VideoDetailsFragment.MAIN_MEDIA_KEY, media);
        fromActivity.startActivity(intent);
    }
}
