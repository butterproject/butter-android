package butter.droid.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import androidx.appcompat.widget.AppCompatTextView;
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
        Context context = getContext();

        Resources resources;
        if (context == null) {
            resources = Resources.getSystem();
        } else {
            resources = context.getResources();
        }

        strokeWidth = TypedValue.applyDimension(unit, size, resources.getDisplayMetrics());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        ColorStateList states = getTextColors();
        modifyPaint();
        super.onDraw(canvas);

        setTextColor(states);
        getPaint().setStyle(Paint.Style.FILL);
        super.onDraw(canvas);
    }

    private void modifyPaint() {
        getPaint().setStyle(Paint.Style.STROKE);
        getPaint().setStrokeWidth(strokeWidth);
        setTextColor(strokeColor);
    }

}
