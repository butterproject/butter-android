package butter.droid.base.manager.internal.glide;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.module.AppGlideModule;

import butter.droid.base.manager.internal.glide.transcode.PaletteBitmap;
import butter.droid.base.manager.internal.glide.transcode.PaletteBitmapTranscoder;

@GlideModule
public class ButterGlideModule extends AppGlideModule {
    @Override
    public void registerComponents(@Nullable Context context, @Nullable Glide glide, @Nullable Registry registry) {
        if (glide != null && registry != null) {
            registry.register(Bitmap.class, PaletteBitmap.class, new PaletteBitmapTranscoder(glide.getBitmapPool()));
        }
    }
}
