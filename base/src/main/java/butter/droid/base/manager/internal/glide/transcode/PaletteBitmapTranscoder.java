package butter.droid.base.manager.internal.glide.transcode;

import android.graphics.Bitmap;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.palette.graphics.Palette;

import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.engine.Resource;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.transcode.ResourceTranscoder;

public class PaletteBitmapTranscoder implements ResourceTranscoder<Bitmap, PaletteBitmap> {
    private final BitmapPool bitmapPool;

    public PaletteBitmapTranscoder(@NonNull BitmapPool pool) {
        this.bitmapPool = pool;
    }

    @Nullable @Override
    public Resource<PaletteBitmap> transcode(@NonNull Resource<Bitmap> toTranscode, @NonNull Options options) {
        Bitmap bitmap = toTranscode.get();
        Palette palette = new Palette.Builder(bitmap).generate();
        PaletteBitmap result = new PaletteBitmap(bitmap, palette);
        return new PaletteBitmapResource(result, bitmapPool);
    }
}
