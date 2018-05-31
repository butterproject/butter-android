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

package butter.droid.base.manager.internal;

import android.content.Context;
import androidx.annotation.Nullable;
import butter.droid.base.Internal;
import butter.droid.base.manager.internal.provider.ProviderManager;
import butter.droid.base.manager.internal.provider.model.ProviderWrapper;
import butter.droid.base.manager.internal.vlc.VLCOptions;
import dagger.Module;
import dagger.Provides;
import java.util.Set;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.util.VLCUtil;
import timber.log.Timber;

@Module
public class InternalBaseManagerModule {

    @Provides @Internal ProviderManager provideProviderManager(final Set<ProviderWrapper> providers) {
        ProviderWrapper[] array = new ProviderWrapper[providers.size()];
        providers.toArray(array);
        return new ProviderManager(array);
    }

    @Provides @Internal @Nullable LibVLC provideLibVLC(Context context) {
        if (!VLCUtil.hasCompatibleCPU(context)) {
            Timber.e(VLCUtil.getErrorMsg());
            return null;
        } else {
            return new LibVLC(context, VLCOptions.getLibOptions());
        }
    }

}
