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

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;
import android.view.WindowManager;
import butter.droid.base.manager.network.NetworkManager;
import butter.droid.base.manager.prefs.PrefManager;
import com.google.gson.Gson;
import okhttp3.OkHttpClient;

public interface BaseApplicationComponent {

    Context context();

    OkHttpClient okHttpClient();

    Gson gson();

    SharedPreferences sharedPreferences();

    Resources resources();

    ConnectivityManager connectivityManager();

    WifiManager wifiManager();

    NetworkManager networkManager();

    PrefManager prefManager();

    TelephonyManager telephonyManager();

    ContentResolver contentResolver();

    AudioManager audioManager();

    WindowManager windowManager();

}
