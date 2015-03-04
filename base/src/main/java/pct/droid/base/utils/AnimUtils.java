package pct.droid.base.utils;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import pct.droid.base.PopcornApplication;

public class AnimUtils {

    public static void fadeIn(View v) {
        Animation fadeInAnim = AnimationUtils.loadAnimation(PopcornApplication.getAppContext(), android.R.anim.fade_in);
        v.setVisibility(View.VISIBLE);
        v.startAnimation(fadeInAnim);
        v.requestLayout(); // Force redraw
    }

    public static void fadeOut(View v) {
        Animation fadeOutAnim = AnimationUtils.loadAnimation(PopcornApplication.getAppContext(), android.R.anim.fade_out);
        v.startAnimation(fadeOutAnim);
        v.setVisibility(View.INVISIBLE);
        v.requestLayout(); // Force redraw
    }

}
