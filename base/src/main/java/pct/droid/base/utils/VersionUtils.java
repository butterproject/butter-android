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

package pct.droid.base.utils;

import android.app.UiModeManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;

import java.util.Locale;

import pct.droid.base.BuildConfig;
import pct.droid.base.PopcornApplication;
import timber.log.Timber;

public class VersionUtils {

    public static boolean isLollipop() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    public static boolean isJellyBean() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
    }

    public static boolean isAndroidTV() {
        UiModeManager uiModeManager = (UiModeManager) PopcornApplication.getAppContext().getSystemService(Context.UI_MODE_SERVICE);
        return uiModeManager.getCurrentModeType() == Configuration.UI_MODE_TYPE_TELEVISION;
    }

    public static boolean isUsingCorrectBuild() {
        String buidAbi = getBuildAbi();
        String deviceAbi;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            deviceAbi = Build.CPU_ABI;
        } else {
            deviceAbi = Build.SUPPORTED_ABIS[0];
        }

        // TODO: if arm64 works remove this
        if(deviceAbi.equalsIgnoreCase("arm64-v8a"))
            deviceAbi = "armeabi-v7a";

        return deviceAbi.equalsIgnoreCase(buidAbi);
    }

    private static String getBuildAbi() {
        PackageManager manager = PopcornApplication.getAppContext().getPackageManager();
        try {
            PackageInfo info = manager.getPackageInfo(PopcornApplication.getAppContext().getPackageName(), 0);
            Integer versionCode = info.versionCode;

            if(versionCode > 4000000) {
                return "x86";
            } else if(versionCode > 3000000) {
                return "arm64-v8a";
            } else if(versionCode > 2000000) {
                return "armeabi-v7a";
            } else if(versionCode > 1000000) {
                return "armeabi";
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return "unsupported";
    }

}
