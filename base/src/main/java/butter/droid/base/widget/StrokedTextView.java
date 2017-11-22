package butter.droid.base.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;
import android.util.TypedValue;

public class StrokedTextView extends AppCompatTextView {

    private int strokeColor;
    private float strokeWidth;

    public StrokedTextView(Context context) {
        super(context);
    }

    public StrokedTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public StrokedTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setStrokeColor(int color) {
        strokeColor = color;
    }

    public void setStrokeWidth(int unit, int size) {
        Context c = getContext();
        Resources r;

        if (c == null) {
            r = Resources.getSystem();
        } else {
            r = c.getResources();
        }

        strokeWidth = TypedValue.applyDimension(unit, size, r.getDisplayMetrics());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        final ColorStateList states = getTextColors();
        getPaint().setStyle(Paint.Style.STROKE);
        getPaint().setStrokeWidth(strokeWidth);
        setTextColor(strokeColor);
        super.onDraw(canvas);

        getPaint().setStyle(Paint.Style.FILL);
        setTextColor(states);
        super.onDraw(canvas);
    }
}
