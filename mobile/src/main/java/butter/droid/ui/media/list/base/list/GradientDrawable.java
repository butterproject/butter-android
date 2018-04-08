package butter.droid.ui.media.list.base.list;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class GradientDrawable extends Drawable {

    @Override public void draw(@NonNull Canvas canvas) {
        // Code borrowed from https://stackoverflow.com/questions/23657811/how-to-mask-bitmap-with-lineargradient-shader-properly
        int width = canvas.getWidth();
        int height = canvas.getHeight();

        Paint paint = new Paint();
        float gradientHeight = height / 2f;
        LinearGradient shader = new LinearGradient(0, height - gradientHeight, 0, height,
                0xFFFFFFFF, 0x00FFFFFF,
                Shader.TileMode.CLAMP);
        paint.setShader(shader);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        canvas.drawRect(0, height - gradientHeight, width, height, paint);
    }

    @Override public void setAlpha(int alpha) {
        // TODO
    }

    @Override public void setColorFilter(@Nullable ColorFilter colorFilter) {
        // TODO
    }

    @Override public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }
}
