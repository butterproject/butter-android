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

package butter.droid.ui.player.fragment;

import android.app.Activity;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewConfiguration;
import butter.droid.base.ui.FragmentScope;
import butter.droid.manager.brightness.BrightnessManager;
import javax.inject.Inject;

@FragmentScope
public class VideoPlayerTouchHandler implements OnTouchListener {

    private final Activity activity;
    private final BrightnessManager brightnessManager;
    private final DisplayMetrics displayMetrics;
    private final int touchSlop;

    private int surfaceYDisplayRange;
    private float touchX;
    private float touchY;

    private boolean swipeX;
    private boolean swipeY;

    @Nullable private OnVideoTouchListener listener;

    @Inject
    public VideoPlayerTouchHandler(final Activity activity, final BrightnessManager brightnessManager) {
        this.activity = activity;
        this.brightnessManager = brightnessManager;

        ViewConfiguration configuration = ViewConfiguration.get(activity);
        touchSlop = configuration.getScaledTouchSlop();

        displayMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
    }

    public void setListener(@Nullable final OnVideoTouchListener listener) {
        this.listener = listener;
    }

    @Override public boolean onTouch(final View v, final MotionEvent event) {
        if (surfaceYDisplayRange == 0) {
            surfaceYDisplayRange = Math.min(displayMetrics.widthPixels, displayMetrics.heightPixels);
        }


        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchY = event.getRawY();
                touchX = event.getRawX();
                break;
            case MotionEvent.ACTION_MOVE:
                float xDiff = event.getRawX() - touchX;
                float yDiff = event.getRawY() - touchY;

                if (!swipeX && !swipeY) {
                    if (Math.abs(xDiff) > touchSlop) {
                        swipeX = true;
                    } else if (Math.abs(yDiff) > touchSlop) {
                        swipeY = true;
                    }
                }

                if (swipeX) {
                    touchX = event.getRawX();
                    doSeekTouch(xDiff);
                }

                if (swipeY) {
                    touchY = event.getRawY();

                    float halfPoint = displayMetrics.widthPixels / 2f;
                    // right side of screen is for brightens if supported
                    if (touchX < halfPoint && brightnessManager.canChangeBrightness()) {
                        doBrightnessTouch(yDiff);
                    } else { // left side of screen is for volume
                        doVolumeTouch(yDiff);
                    }
                }

                break;
            case MotionEvent.ACTION_UP:

                if (!swipeX && !swipeY) {
                    toggleOverlay();
                }

            case MotionEvent.ACTION_CANCEL:
                swipeX = false;
                swipeY = false;
                break;
        }
        return true;
    }

    private void doBrightnessTouch(final float diff ) {
        // Set delta : 2f is arbitrary for now, it possibly will change in the future
        float delta = -diff / surfaceYDisplayRange;

        if (listener != null) {
            listener.onBrightnessChange(delta);
        }
    }

    private void doSeekTouch(float diff) {

        // Size of the jump, 10 minutes max (600000), with a bi-cubic progression, for a 8cm gesture
        int jump = (int) (Math.signum(diff) * ((600000 * Math.pow((diff / 8), 4)) + 3000));

        if (listener != null) {
            listener.onSeekChange(jump);
        }

    }

    private void doVolumeTouch(float diff) {
        float deltaFraction = -(diff / surfaceYDisplayRange);

        if (listener != null) {
            listener.onVolumeChange(deltaFraction);
        }

    }

    private void toggleOverlay() {
        if (listener != null) {
            listener.onToggleOverlay();
        }
    }

    public interface OnVideoTouchListener {

        void onSeekChange(int jump);

        void onBrightnessChange(float delta);

        void onVolumeChange(float delta);

        void onToggleOverlay();


    }

}
