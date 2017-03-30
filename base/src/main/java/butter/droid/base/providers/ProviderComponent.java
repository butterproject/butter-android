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

package butter.droid.base.providers;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import butter.droid.base.ExposedComponent;
import butter.droid.base.manager.internal.vlc.PlayerManager;
import butter.droid.base.manager.network.NetworkManager;
import butter.droid.base.manager.prefs.PrefManager;
import butter.droid.base.providers.media.VodoProvider;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;
import dagger.Component;
import okhttp3.OkHttpClient;

@Component(
        dependencies = ExposedComponent.class,
        modules = ProviderModule.class
)
@ProviderScope
public interface ProviderComponent {

    Context context();
    Picasso picasso();
    VodoProvider codoProvider();
    OkHttpClient okHttpClient();
    Gson gson();
    PlayerManager playerManager();
    SharedPreferences sharedPreferences();
    Resources resources();
    ConnectivityManager connectivityManager();
    WifiManager wifiManager();
    NetworkManager networkManager();
    PrefManager prefManager();

}
