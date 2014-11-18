package pct.droid.widget;

import android.content.Context;
import android.util.AttributeSet;

import pct.droid.utils.CheatSheet;

/**
 * Created by Sebastiaan on 21-09-14.
 */
public class ImageButton extends android.widget.ImageButton {

    public ImageButton(Context context) {
        super(context);
    }

    public ImageButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ImageButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void setContentDescription(CharSequence contentDesc) {
        super.setContentDescription(contentDesc);
        if(contentDesc.length() > 0)
            CheatSheet.setup(this, contentDesc);
    }

}
