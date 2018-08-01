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

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;

import com.jakewharton.threetenabp.AndroidThreeTen;

import java.io.File;

import javax.inject.Inject;

import androidx.multidex.MultiDex;
import butter.droid.base.content.preferences.PreferencesHandler;
import butter.droid.base.manager.internal.beaming.BeamManager;
import butter.droid.base.manager.internal.foreground.ForegroundManager;
import butter.droid.base.utils.FileUtils;
import butter.droid.base.utils.LocaleUtils;
import butter.droid.base.utils.StorageUtils;
import dagger.android.support.DaggerApplication;
import timber.log.Timber;

public abstract class ButterApplication extends DaggerApplication {

    private static String sDefSystemLanguage;
    private static ButterApplication sThis;

    @Inject BeamManager beamManager;
    @Inject PreferencesHandler preferencesHandler;
    @Inject ForegroundManager foregroundManager; // inject just so it is initialized

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sThis = this;

        AndroidThreeTen.init(this);

        sDefSystemLanguage = LocaleUtils.getCurrentAsString();

        Constants.DEBUG_ENABLED = false;
        try {
            String packageName = getPackageName();
            PackageInfo packageInfo = getPackageManager().getPackageInfo(packageName, 0);
            int flags = packageInfo.applicationInfo.flags;
            Constants.DEBUG_ENABLED = (flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        // initialise logging
        if (Constants.DEBUG_ENABLED) {
            Timber.plant(new Timber.DebugTree());
        }

        File path = new File(preferencesHandler.getStorageLocation());
        File directory = new File(path, "/torrents/");
        if (preferencesHandler.removeCache()) {
            FileUtils.recursiveDelete(directory);
            FileUtils.recursiveDelete(new File(path + "/subs"));
        } else {
            File statusFile = new File(directory, "status.json");
            statusFile.delete();
        }

        Timber.d("StorageLocations: %s", StorageUtils.getAllStorageLocations());
        Timber.i("Chosen cache location: %s", directory);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        sDefSystemLanguage = LocaleUtils.getCurrentAsString();
    }

    @Override
    public void onTerminate() {
        // Just, so that it exists. Cause it is not executed in production, the whole application is closed anyways on OS level.
        beamManager.onDestroy();
        super.onTerminate();
    }

    public static String getSystemLanguage() {
        return sDefSystemLanguage;
    }

    public static ButterApplication getAppContext() {
        return sThis;
    }
}
