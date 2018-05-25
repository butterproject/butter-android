package butter.droid.base.manager.internal.glide.transcode;

import androidx.annotation.NonNull;

import com.bumptech.glide.load.engine.Resource;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.util.Util;

public class PaletteBitmapResource implements Resource<PaletteBitmap> {
    private final PaletteBitmap paletteBitmap;
    private final BitmapPool bitmapPool;

    public PaletteBitmapResource(@NonNull PaletteBitmap paletteBitmap, @NonNull BitmapPool bitmapPool) {
        this.paletteBitmap = paletteBitmap;
        this.bitmapPool = bitmapPool;
    }

    @NonNull @Override public Class<PaletteBitmap> getResourceClass() {
        return PaletteBitmap.class;
    }

    @NonNull @Override public PaletteBitmap get() {
        return paletteBitmap;
    }

    @Override public int getSize() {
        return Util.getBitmapByteSize(paletteBitmap.getBitmap());
    }

    @Override public void recycle() {
        bitmapPool.put(paletteBitmap.getBitmap());
    }
}
