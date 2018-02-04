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

package butter.droid.base;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.PowerManager;
import android.telephony.TelephonyManager;
import dagger.Module;
import dagger.Provides;
import javax.inject.Singleton;

@Module
public class BaseAndroidModule {

    @Provides @Singleton ConnectivityManager provideConnectivityManager(Context context) {
        return (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    @SuppressLint("WifiManagerPotentialLeak") @Provides @Singleton WifiManager provideWifiManager(Context context) {
        return (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
    }

    @Provides @Singleton TelephonyManager provideTelephonyManager(Context context) {
        return (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
    }

    @Provides @Singleton PackageManager providePackageManager(Context context) {
        return context.getPackageManager();
    }

    @Provides @Singleton ContentResolver provideContentResolver(Context context) {
        return context.getContentResolver();
    }

    @Provides @Singleton AudioManager provideAudioManager(Context context) {
        return (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    }

    @Provides @Singleton PowerManager providePowerManager(Context context) {
        return (PowerManager) context.getSystemService(Context.POWER_SERVICE);
    }

}
