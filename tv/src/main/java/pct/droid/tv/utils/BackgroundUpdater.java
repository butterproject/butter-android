package pct.droid.tv.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.v17.leanback.app.BackgroundManager;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.Timer;
import java.util.TimerTask;

import pct.droid.base.utils.PixelUtils;
import pct.droid.base.utils.ThreadUtils;
import pct.droid.tv.R;

public class BackgroundUpdater {


    private int mDisplayWidth;
    private int mDisplayHeight;
    private static int BACKGROUND_UPDATE_DELAY = 300;
    private Drawable mDefaultBackground;
    private Context mContext;
    private Target mBackgroundImageTarget;
    private Timer mBackgroundTimer;
    private BackgroundManager mBackgroundManager;
    private String mBackgroundUrl;

    public void initialise(Activity activity, @DrawableRes int defaultBackground) {
        mContext = activity.getApplicationContext();

        mBackgroundManager = BackgroundManager.getInstance(activity);
        mBackgroundManager.attach(activity.getWindow());
        mBackgroundImageTarget = new PicassoBackgroundManagerTarget(mBackgroundManager);
        mDefaultBackground = mContext.getResources().getDrawable(defaultBackground);
        mDisplayWidth = PixelUtils.getScreenWidth(mContext);
        mDisplayHeight = PixelUtils.getScreenWidth(mContext);
    }

    public BackgroundUpdater() {
    }


    /**
     * Updates the background asynchronously with the given image url, after a short delay.
     *
     * @param url
     */
    public void updateBackgroundAsync(String url) {
        mBackgroundUrl = url;
        if (null != mBackgroundTimer) {
            mBackgroundTimer.cancel();
        }
        mBackgroundTimer = new Timer();
        mBackgroundTimer.schedule(new UpdateBackgroundTask(), BACKGROUND_UPDATE_DELAY);
    }


    public void updateBackground(final String uri) {
        if (null != mBackgroundTimer) {
            mBackgroundTimer.cancel();
        }

        //load default background image
        if (null == uri) {
            Picasso.with(mContext).load(R.drawable.default_background).into(mBackgroundImageTarget);
            return;
        }

        //load actual background image
        Picasso.with(mContext)
                .load(uri.toString())
                .error(mDefaultBackground)
                .into(mBackgroundImageTarget);
    }

    protected void setDefaultBackground(Drawable background) {
        mDefaultBackground = background;
    }


    protected void setDefaultBackground(int resourceId) {
        mDefaultBackground = mContext.getResources().getDrawable(resourceId);
    }

    /**
     * Updates the background immediately with a drawable
     *
     * @param drawable
     */
    public void updateBackground(Drawable drawable) {
        mBackgroundManager.setDrawable(drawable);
    }

    /**
     * Clears the background immediately
     */
    public void clearBackground() {
        mBackgroundManager.setDrawable(mDefaultBackground);
    }

    private class UpdateBackgroundTask extends TimerTask {

        @Override
        public void run() {
            ThreadUtils.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mBackgroundUrl != null) {
                        updateBackground(mBackgroundUrl.toString());
                    }

                }
            });
        }
    }

    public void destroy() {
        if (null != mBackgroundTimer) {
            mBackgroundTimer.cancel();
        }
        mBackgroundManager.release();
    }
}
