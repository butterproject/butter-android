/*
 * This file is part of Butter.
 *
 * Butter is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Butter is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Butter. If not, see <http://www.gnu.org/licenses/>.
 */

package butter.droid.base.manager;

import android.content.Context;
import android.support.annotation.Nullable;
import butter.droid.base.Constants;
import butter.droid.base.content.preferences.PreferencesHandler;
import butter.droid.base.manager.internal.provider.ProviderManager;
import butter.droid.base.manager.internal.vlc.VLCOptions;
import butter.droid.base.providers.media.VodoProvider;
import butter.droid.base.providers.subs.SubsProvider;
import dagger.Module;
import dagger.Provides;
import javax.inject.Singleton;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.util.VLCUtil;
import timber.log.Timber;

@Module
public class ManagerModule {

    @Provides
    @Singleton
    public ProviderManager provideProviderManager(VodoProvider moviesProvider, SubsProvider subsProvider) {
        return new ProviderManager(moviesProvider, null);
    }

    @Provides
    @Singleton
    @Nullable
    public LibVLC provideLibVLC(Context context, PreferencesHandler preferencesHandler) {
        if (!VLCUtil.hasCompatibleCPU(context)) {
            Timber.e(VLCUtil.getErrorMsg());
            return null;
        } else {
            String chroma = preferencesHandler.getPixelFormat();
            return new LibVLC(context, VLCOptions.getLibOptions(context, true, "UTF-8", true, chroma, Constants.DEBUG_ENABLED));
        }
    }

}
