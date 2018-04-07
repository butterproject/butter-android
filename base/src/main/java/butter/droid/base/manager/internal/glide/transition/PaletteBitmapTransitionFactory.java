package butter.droid.base.manager.internal.glide.transition;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import com.bumptech.glide.request.transition.BitmapContainerTransitionFactory;
import com.bumptech.glide.request.transition.TransitionFactory;

import butter.droid.base.manager.internal.glide.transcode.PaletteBitmap;

public class PaletteBitmapTransitionFactory extends BitmapContainerTransitionFactory<PaletteBitmap> {

    public PaletteBitmapTransitionFactory(TransitionFactory<Drawable> realFactory) {
        super(realFactory);
    }

    @Override protected Bitmap getBitmap(PaletteBitmap current) {
        return current.getBitmap();
    }
}
