/*
 * This file is part of Popcorn Time.
 *
 * Popcorn Time is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Popcorn Time is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Popcorn Time. If not, see <http://www.gnu.org/licenses/>.
 */

package pct.droid.base;

import android.app.Notification;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.support.multidex.MultiDex;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;

import org.videolan.vlc.VLCApplication;

import java.io.File;
import java.io.IOException;

import pct.droid.base.beaming.BeamManager;
import pct.droid.base.preferences.Prefs;
import pct.droid.base.torrent.TorrentService;
import pct.droid.base.updater.PopcornUpdater;
import pct.droid.base.utils.FileUtils;
import pct.droid.base.utils.LocaleUtils;
import pct.droid.base.utils.PrefUtils;
import pct.droid.base.utils.StorageUtils;
import timber.log.Timber;

public class PopcornApplication extends VLCApplication {

    private static OkHttpClient sHttpClient;
    private static String sDefSystemLanguage;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sDefSystemLanguage = LocaleUtils.getCurrent();

        Constants.DEBUG_ENABLED = false;
        int versionCode = 0;
        try {
            String packageName = getPackageName();
            PackageInfo packageInfo = getPackageManager().getPackageInfo(packageName, 0);
            int flags = packageInfo.applicationInfo.flags;
            versionCode = packageInfo.versionCode;
            Constants.DEBUG_ENABLED = (flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        //initialise logging
        if (Constants.DEBUG_ENABLED) {
            Timber.plant(new Timber.DebugTree());
        }

        TorrentService.start(this);

        File path = new File(PrefUtils.get(this, Prefs.STORAGE_LOCATION, StorageUtils.getIdealCacheDirectory(this).toString()));
        File directory = new File(path, "/torrents/");
        if (PrefUtils.get(this, Prefs.REMOVE_CACHE, true)) {
            FileUtils.recursiveDelete(directory);
            FileUtils.recursiveDelete(new File(path + "/subs"));
        } else {
            File statusFile = new File(directory, "status.json");
            statusFile.delete();
        }

        Timber.d("StorageLocations: " + StorageUtils.getAllStorageLocations());
        Timber.i("Chosen cache location: " + directory);


        if (PrefUtils.get(this, Prefs.INSTALLED_VERSION, 0) < versionCode) {
            PrefUtils.save(this, Prefs.INSTALLED_VERSION, versionCode);
            FileUtils.recursiveDelete(new File(StorageUtils.getIdealCacheDirectory(this) + "/backend"));
        }

        Picasso.Builder builder = new Picasso.Builder(getAppContext());
        OkHttpDownloader downloader = new OkHttpDownloader(getHttpClient());
        builder.downloader(downloader);
        Picasso.setSingletonInstance(builder.build());

        PopcornUpdater.getInstance(this).checkUpdates(false);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        sDefSystemLanguage = LocaleUtils.getCurrent();
    }

    @Override
    public void onTerminate() {
        BeamManager.getInstance(getAppContext()).onDestroy();
        super.onTerminate();
    }

    public static String getSystemLanguage() {
        return sDefSystemLanguage;
    }

    public static OkHttpClient getHttpClient() {
        if (sHttpClient == null) {
            sHttpClient = new OkHttpClient();

            int cacheSize = 10 * 1024 * 1024;
            File cacheLocation = new File(PrefUtils.get(PopcornApplication.getAppContext(), Prefs.STORAGE_LOCATION, StorageUtils.getIdealCacheDirectory(PopcornApplication.getAppContext()).toString()));
            cacheLocation.mkdirs();
            com.squareup.okhttp.Cache cache = null;
            try {
                cache = new com.squareup.okhttp.Cache(cacheLocation, cacheSize);
            } catch (Exception e) {
                e.printStackTrace();
            }
            sHttpClient.setCache(cache);
        }
        return sHttpClient;
    }

    public static String getStreamDir() {
        File path = new File(PrefUtils.get(getAppContext(), Prefs.STORAGE_LOCATION, StorageUtils.getIdealCacheDirectory(getAppContext()).toString()));
        File directory = new File(path, "/torrents/");
        return directory.toString();
    }

}
