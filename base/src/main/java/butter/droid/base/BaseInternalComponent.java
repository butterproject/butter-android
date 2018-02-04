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
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.PowerManager;
import butter.droid.base.manager.internal.beaming.BeamPlayerNotificationService;
import butter.droid.base.manager.internal.notification.ButterNotificationManager;
import butter.droid.base.manager.internal.provider.model.ProviderWrapper;
import butter.droid.base.manager.internal.vlc.PlayerManager;
import butter.droid.base.torrent.TorrentService;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;
import java.util.Set;
import okhttp3.OkHttpClient;

public interface BaseInternalComponent {

    void inject(BeamPlayerNotificationService service);

    void inject(TorrentService service);

    Context context();

    Picasso picasso();

    Set<ProviderWrapper> providers();

    OkHttpClient okHttpClient();

    Gson gson();

    PlayerManager playerManager();

    SharedPreferences sharedPreferences();

    Resources resources();

    ConnectivityManager connectivityManager();

    WifiManager wifiManager();

    PackageManager packageManager();

    ContentResolver contentResolver();

    AudioManager audioManager();

    PowerManager powerManager();

    ButterNotificationManager butterNotificationManager();

}
