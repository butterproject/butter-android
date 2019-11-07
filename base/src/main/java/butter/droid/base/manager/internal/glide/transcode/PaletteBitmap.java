package butter.droid.base.manager.internal.glide.transcode;

import android.graphics.Bitmap;
import androidx.annotation.NonNull;
import androidx.palette.graphics.Palette;

public class PaletteBitmap {

    @NonNull private final Palette palette;
    @NonNull private final Bitmap bitmap;

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
