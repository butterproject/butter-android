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
import android.graphics.Bitmap;

import com.bumptech.glide.request.target.Target;

import java.util.Timer;
import java.util.TimerTask;

import javax.inject.Inject;

import androidx.annotation.DrawableRes;
import androidx.leanback.app.BackgroundManager;
import butter.droid.base.manager.internal.glide.GlideApp;
import butter.droid.base.manager.internal.glide.GlideRequests;
import butter.droid.base.utils.ThreadUtils;
import dagger.Reusable;

@Reusable
public class BackgroundUpdater {

    private static final int BACKGROUND_UPDATE_DELAY = 300;

    private final BackgroundManager backgroundManager;
    private final Target<Bitmap> backgroundImageTarget;

    private int defaultBackground;
    private Timer backgroundTimer;
    private String backgroundUrl;
    private GlideRequests glide;

    @Inject
    public BackgroundUpdater(final BackgroundManager backgroundManager, final Target<Bitmap> backgroundImageTarget) {
        this.backgroundManager = backgroundManager;
        this.backgroundImageTarget = backgroundImageTarget;
    }

    public void initialise(Activity activity, @DrawableRes int defaultBackground) {
        backgroundManager.attach(activity.getWindow());
        this.defaultBackground = defaultBackground;

        glide = GlideApp.with(activity);
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
//        backgroundTimer = new Timer();
//        backgroundTimer.schedule(new UpdateBackgroundTask(), BACKGROUND_UPDATE_DELAY);
    }


    public void updateBackground(final String uri) {
        if (glide == null) {
            return;
        }

        if (backgroundTimer != null) {
            backgroundTimer.cancel();
        }

        // TODO resolve recycling issue
        //load default background image
//        if (uri == null) {
//            glide.asBitmap()
//                    .load(defaultBackground)
//                    .into(backgroundImageTarget);
//        } else {
//            //load actual background image
//            glide.asBitmap()
//                    .load(uri)
//                    .error(defaultBackground)
//                    .into(backgroundImageTarget);
//        }
    }

    private class UpdateBackgroundTask extends TimerTask {

        @Override
        public void run() {
            ThreadUtils.runOnUiThread(() -> {
                if (backgroundUrl != null) {
                    updateBackground(backgroundUrl);
                }

            });
        }
    }

    public void destroy() {
        if (null != backgroundTimer) {
            backgroundTimer.cancel();
        }

        glide.clear(backgroundImageTarget);
        backgroundManager.release();
    }

}
