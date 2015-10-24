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

import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ScrollView;

public class BottomSheetScrollView extends ScrollView {

    private final static int CHECK_INTERVAL = 100;
    private int mInitPosition;
    private Boolean mTouchDown = false, mIsScrolling = false;
    private Listener mListener = null;

    public enum Direction {UP, DOWN}

    ;

    public BottomSheetScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setFadingEdgeLength(0);
    }

    public BottomSheetScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setFadingEdgeLength(0);
    }

    public BottomSheetScrollView(Context context) {
        super(context);
        setFadingEdgeLength(0);
    }

    public void animateScrollTo(int targetScrollY, int duration) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            ValueAnimator realSmoothScrollAnimation = ValueAnimator.ofInt(getScrollY(), targetScrollY);
            realSmoothScrollAnimation.setDuration(duration);
            realSmoothScrollAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    int scrollTo = (Integer) animation.getAnimatedValue();
                    scrollTo(0, scrollTo);
                }
            });

            realSmoothScrollAnimation.start();
        } else {
            smoothScrollTo(0, targetScrollY);
        }
    }

    @Override
    protected void onScrollChanged(int x, int y, int oldX, int oldY) {
        super.onScrollChanged(x, y, oldX, oldY);
        if (!mIsScrolling) {
            if (mListener != null)
                mListener.onScrollStart();
        }
        mIsScrolling = true;

        if (mListener != null) {
            Direction d = y > oldY ? Direction.DOWN : Direction.UP;
            mListener.onScroll(y, d);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (mListener != null && !mTouchDown)
                    mListener.onTouch(true);
                mTouchDown = true;
                break;
            case MotionEvent.ACTION_UP:
                if (mListener != null && mTouchDown)
                    mListener.onTouch(false);
                mTouchDown = false;
                mScrollerTask.run();
                break;
        }
        return super.onTouchEvent(ev);
    }

    public void setListener(Listener listener) {
        mListener = listener;
    }

    public interface Listener {
        public void onScroll(int scrollY, Direction direction);

        public void onTouch(boolean touching);

        public void onScrollStart();

        public void onScrollEnd();
    }

    @Override
    protected boolean overScrollBy(int deltaX, int deltaY, int scrollX, int scrollY, int scrollRangeX, int scrollRangeY, int maxOverScrollX, int maxOverScrollY, boolean isTouchEvent) {
        return super.overScrollBy(
                deltaX,
                deltaY,
                scrollX,
                scrollY,
                scrollRangeX,
                scrollRangeY,
                0,
                0,
                isTouchEvent);
    }

    Runnable mScrollerTask = new Runnable() {
        public void run() {
            int newPosition = getScrollY();
            if (mInitPosition - newPosition == 0) {
                if (mListener != null) {
                    mListener.onScrollEnd();
                }
            } else {
                mInitPosition = getScrollY();
                BottomSheetScrollView.this.postDelayed(mScrollerTask, CHECK_INTERVAL);
            }
        }
    };

}
