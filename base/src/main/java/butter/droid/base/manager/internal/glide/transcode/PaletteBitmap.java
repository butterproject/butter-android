package butter.droid.base.manager.internal.glide.transcode;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v7.graphics.Palette;

public class PaletteBitmap {

    private final Palette palette;
    private final Bitmap bitmap;

    public PaletteBitmap(@NonNull Bitmap bitmap, @NonNull Palette palette) {
        this.bitmap = bitmap;
        this.palette = palette;
    }

    public Palette getPalette() {
        return palette;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

}
