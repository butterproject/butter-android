package butter.droid.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.TypedValue;

import com.devspark.robototextview.widget.RobotoTextView;

public class StrokedRobotoTextView extends RobotoTextView {

    private int mStrokeColor;
    private float mStrokeWidth;

    public StrokedRobotoTextView(Context context) {
        super(context);
    }

    public StrokedRobotoTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public StrokedRobotoTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setStrokeColor(int color) {
        mStrokeColor = color;
    }

    public void setStrokeWidth(int unit, int size) {
        Context c = getContext();
        Resources r;

        if (c == null)
            r = Resources.getSystem();
        else
            r = c.getResources();

        mStrokeWidth = TypedValue.applyDimension(unit, size, r.getDisplayMetrics());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        ColorStateList states = getTextColors();
        getPaint().setStyle(Paint.Style.STROKE);
        getPaint().setStrokeWidth(mStrokeWidth);
        setTextColor(mStrokeColor);
        super.onDraw(canvas);

        getPaint().setStyle(Paint.Style.FILL);
        setTextColor(states);
        super.onDraw(canvas);
    }
}
