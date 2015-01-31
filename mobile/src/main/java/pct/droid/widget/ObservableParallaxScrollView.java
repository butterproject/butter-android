package pct.droid.widget;

import android.content.Context;
import android.util.AttributeSet;

import com.nirhart.parallaxscroll.views.ParallaxScrollView;

public class ObservableParallaxScrollView extends ParallaxScrollView {

    private Listener mListener = null;
    public enum Direction {UP, DOWN};

    public ObservableParallaxScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public ObservableParallaxScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ObservableParallaxScrollView(Context context) {
        super(context);
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        if (mListener != null) {
            Direction d = t > oldt ? Direction.DOWN : Direction.UP;
            mListener.onScroll(t, d);
        }
    }

    public void setListener(Listener listener) {
        mListener = listener;
    }

    public interface Listener {
        public void onScroll(int scrollY, Direction direction);
    }

    private boolean mIsOverScrollEnabled = true;

    public void setOverScrollEnabled(boolean enabled) {
        mIsOverScrollEnabled = enabled;
    }

    public boolean isOverScrollEnabled() {
        return mIsOverScrollEnabled;
    }

    @Override
    protected boolean overScrollBy(int deltaX, int deltaY, int scrollX, int scrollY, int scrollRangeX, int scrollRangeY,
                                   int maxOverScrollX, int maxOverScrollY, boolean isTouchEvent) {
        return super.overScrollBy(
                deltaX,
                deltaY,
                scrollX,
                scrollY,
                scrollRangeX,
                scrollRangeY,
                mIsOverScrollEnabled ? maxOverScrollX : 0,
                mIsOverScrollEnabled ? maxOverScrollY : 0,
                isTouchEvent);
    }

}
