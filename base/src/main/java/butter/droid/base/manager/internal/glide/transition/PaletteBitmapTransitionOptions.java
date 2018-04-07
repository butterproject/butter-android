//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package butter.droid.base.manager.internal.glide.transition;

import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;

import com.bumptech.glide.TransitionOptions;
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory;
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory.Builder;
import com.bumptech.glide.request.transition.TransitionFactory;

import butter.droid.base.manager.internal.glide.transcode.PaletteBitmap;

public final class PaletteBitmapTransitionOptions extends TransitionOptions<PaletteBitmapTransitionOptions, PaletteBitmap> {
    public PaletteBitmapTransitionOptions() {
    }

    @NonNull
    public static PaletteBitmapTransitionOptions withCrossFade() {
        return (new PaletteBitmapTransitionOptions()).crossFade();
    }

    @NonNull
    public static PaletteBitmapTransitionOptions withCrossFade(int duration) {
        return (new PaletteBitmapTransitionOptions()).crossFade(duration);
    }

    @NonNull
    public static PaletteBitmapTransitionOptions withCrossFade(@NonNull DrawableCrossFadeFactory drawableCrossFadeFactory) {
        return (new PaletteBitmapTransitionOptions()).crossFade(drawableCrossFadeFactory);
    }

    @NonNull
    public static PaletteBitmapTransitionOptions withCrossFade(@NonNull Builder builder) {
        return (new PaletteBitmapTransitionOptions()).crossFade(builder);
    }

    @NonNull
    public static PaletteBitmapTransitionOptions withWrapped(@NonNull TransitionFactory<Drawable> drawableCrossFadeFactory) {
        return (new PaletteBitmapTransitionOptions()).transitionUsing(drawableCrossFadeFactory);
    }

    @NonNull
    public static PaletteBitmapTransitionOptions with(@NonNull TransitionFactory<PaletteBitmap> transitionFactory) {
        return (new PaletteBitmapTransitionOptions()).transition(transitionFactory);
    }

    @NonNull
    public PaletteBitmapTransitionOptions crossFade() {
        return this.crossFade(new Builder());
    }

    @NonNull
    public PaletteBitmapTransitionOptions crossFade(int duration) {
        return this.crossFade(new Builder(duration));
    }

    @NonNull
    public PaletteBitmapTransitionOptions crossFade(@NonNull DrawableCrossFadeFactory drawableCrossFadeFactory) {
        return this.transitionUsing(drawableCrossFadeFactory);
    }

    @NonNull
    public PaletteBitmapTransitionOptions transitionUsing(@NonNull TransitionFactory<Drawable> drawableCrossFadeFactory) {
        return this.transition(new PaletteBitmapTransitionFactory(drawableCrossFadeFactory));
    }

    @NonNull
    public PaletteBitmapTransitionOptions crossFade(@NonNull Builder builder) {
        return this.transitionUsing(builder.build());
    }
}
