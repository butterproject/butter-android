package butter.droid.base.manager.internal;

import android.content.Context;
import android.support.annotation.Nullable;

import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.util.VLCUtil;

import butter.droid.base.content.preferences.PreferencesHandler;
import butter.droid.base.manager.internal.vlc.VLCOptions;
import dagger.Module;
import dagger.Provides;
import dagger.Reusable;
import timber.log.Timber;

@Module
public class BasePlayerModule {

    @Provides @Reusable @Nullable LibVLC provideLibVLC(Context context, PreferencesHandler preferencesHandler) {
        if (!VLCUtil.hasCompatibleCPU(context)) {
            Timber.e(VLCUtil.getErrorMsg());
            return null;
        } else {
            return new LibVLC(context, VLCOptions.getLibOptions(preferencesHandler));
        }
    }

}
