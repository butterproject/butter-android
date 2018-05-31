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

package butter.droid.widget;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.animation.Interpolator;

import com.google.android.material.appbar.CollapsingToolbarLayout;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.interpolator.view.animation.FastOutLinearInInterpolator;
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator;
import butter.droid.R;

public class BurtterCollapsingToolbarLayout extends CollapsingToolbarLayout {

    @ColorInt private static final int TEXT_COLOR = Color.WHITE;
    @ColorInt private static final int TEXT_COLOR_TRANSPARENT = TEXT_COLOR & 0xFFFFFF;

    private final int toolbarId;
    @Nullable private Toolbar toolbar;
    private boolean scrimsAreShown;

    private ValueAnimator animator;

    public BurtterCollapsingToolbarLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        setTitleEnabled(false);

        TypedArray attr = context.obtainStyledAttributes(attrs,
                R.styleable.CollapsingToolbarLayout, 0,
                R.style.Widget_Design_CollapsingToolbar);

        toolbarId = attr.getResourceId(com.google.android.material.R.styleable.CollapsingToolbarLayout_toolbarId, -1);

        attr.recycle();

        scrimsAreShown = true;

    }

    @Override public void setScrimsShown(final boolean shown, final boolean animate) {
        super.setScrimsShown(shown, animate);

        if (scrimsAreShown != shown) {
            findToolbar();
            if (toolbar != null) {

                if (animator != null) {
                    animator.cancel();
                }

                if (animate) {
                    animator = ValueAnimator.ofFloat(0, 1);
                    animator.setDuration(getScrimAnimationDuration());
                    Interpolator interpolator = shown ? new FastOutLinearInInterpolator() : new LinearOutSlowInInterpolator();
                    animator.setInterpolator(interpolator);

                    final ArgbEvaluator evaluator = new ArgbEvaluator();
                    animator.addUpdateListener(animation -> {
                        float fraction = animation.getAnimatedFraction();
                        int color;
                        if (shown) {
                            color = (int) evaluator.evaluate(fraction, TEXT_COLOR_TRANSPARENT, TEXT_COLOR);
                        } else {
                            color = (int) evaluator.evaluate(fraction, TEXT_COLOR, TEXT_COLOR_TRANSPARENT);
                        }
                        toolbar.setTitleTextColor(color);

                    });
                    animator.start();
                } else {
                    toolbar.setTitleTextColor(shown ? TEXT_COLOR : TEXT_COLOR_TRANSPARENT);
                }

            }

            scrimsAreShown = shown;
        }
    }

    private void findToolbar() {
        if (toolbar == null) {
            toolbar = findViewById(toolbarId);
        }
    }

}
