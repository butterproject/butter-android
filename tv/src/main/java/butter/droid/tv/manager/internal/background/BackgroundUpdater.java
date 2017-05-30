/*
 * This file is part of Butter.
 *
 * Butter is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Butter is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Butter. If not, see <http://www.gnu.org/licenses/>.
 */

package butter.droid.tv.manager.internal.background;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.v17.leanback.app.BackgroundManager;
import butter.droid.base.utils.ThreadUtils;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import dagger.Reusable;
import java.util.Timer;
import java.util.TimerTask;
import javax.inject.Inject;

@Reusable
public class BackgroundUpdater {

    private static final int BACKGROUND_UPDATE_DELAY = 300;

    private final BackgroundManager backgroundManager;
    private final Target backgroundImageTarget;
    private final Picasso picasso;

    private int defaultBackground;
    private Timer backgroundTimer;
    private String backgroundUrl;

    @Inject
    public BackgroundUpdater(final BackgroundManager backgroundManager, final Target backgroundImageTarget,
            final Picasso picasso) {
        this.backgroundManager = backgroundManager;
        this.backgroundImageTarget = backgroundImageTarget;
        this.picasso = picasso;
    }

    public void initialise(Activity activity, @DrawableRes int defaultBackground) {
        backgroundManager.attach(activity.getWindow());
        this.defaultBackground = defaultBackground;
    }

    /**
     * Updates the background asynchronously with the given image url, after a short delay.
     *
     * @param url
     */
    public void updateBackgroundAsync(String url) {
        backgroundUrl = url;
        if (null != backgroundTimer) {
            backgroundTimer.cancel();
        }
        backgroundTimer = new Timer();
        backgroundTimer.schedule(new UpdateBackgroundTask(), BACKGROUND_UPDATE_DELAY);
    }


    public void updateBackground(final String uri) {
        if (null != backgroundTimer) {
            backgroundTimer.cancel();
        }

        //load default background image
        if (null == uri) {
            picasso.load(defaultBackground).into(backgroundImageTarget);
            return;
        }

        //load actual background image
        picasso
                .load(uri)
                .error(defaultBackground)
                .into(backgroundImageTarget);
    }

    /**
     * Updates the background immediately with a drawable
     *
     * @param drawable
     */
    public void updateBackground(Drawable drawable) {
        backgroundManager.setDrawable(drawable);
    }

//    protected void setDefaultBackground(Drawable background) {
//        defaultBackground = background;
//    }

    protected void setDefaultBackground(int resourceId) {
        defaultBackground = resourceId;
    }

    /**
     * Clears the background immediately
     */
    public void clearBackground() {
        backgroundManager.setThemeDrawableResourceId(defaultBackground);
    }

    private class UpdateBackgroundTask extends TimerTask {

        @Override
        public void run() {
            ThreadUtils.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (backgroundUrl != null) {
                        updateBackground(backgroundUrl);
                    }

                }
            });
        }
    }

    public void destroy() {
        if (null != backgroundTimer) {
            backgroundTimer.cancel();
        }

        picasso.cancelRequest(backgroundImageTarget);
        backgroundManager.release();
    }

}
