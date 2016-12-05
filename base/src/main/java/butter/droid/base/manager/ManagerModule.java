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

import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.util.VLCUtil;

import javax.inject.Singleton;

import butter.droid.base.Constants;
import butter.droid.base.content.preferences.Prefs;
import butter.droid.base.manager.prefs.PrefManager;
import butter.droid.base.manager.provider.ProviderManager;
import butter.droid.base.providers.media.VodoProvider;
import butter.droid.base.providers.subs.SubsProvider;
import butter.droid.base.manager.vlc.VLCOptions;
import dagger.Module;
import dagger.Provides;
import timber.log.Timber;

@Module
public class ManagerModule {

    @Provides @Singleton public ProviderManager provideProviderManager(VodoProvider moviesProvider,
            SubsProvider subsProvider) {
        return new ProviderManager(moviesProvider, null);
    }

    @Provides @Singleton @Nullable LibVLC provideLibVLC(Context context, PrefManager prefManager) {
        if(!VLCUtil.hasCompatibleCPU(context)) {
            Timber.e(VLCUtil.getErrorMsg());
            return null;
        } else {
            String chroma = prefManager.get(Prefs.PIXEL_FORMAT, null);
            return new LibVLC(VLCOptions.getLibOptions(context, true, "UTF-8", true, chroma, Constants.DEBUG_ENABLED));
        }
    }

}
