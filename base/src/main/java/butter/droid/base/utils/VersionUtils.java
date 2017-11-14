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

package butter.droid.base.utils;

import android.app.UiModeManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;

import butter.droid.base.BuildConfig;
import butter.droid.base.ButterApplication;

public class VersionUtils {

    public static boolean isMarshmallow() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    public static boolean isLollipop() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    public static boolean isAndroidTV() {
        UiModeManager uiModeManager = (UiModeManager) ButterApplication.getAppContext().getSystemService(Context.UI_MODE_SERVICE);
        return uiModeManager.getCurrentModeType() == Configuration.UI_MODE_TYPE_TELEVISION;
    }

    public static boolean isUsingCorrectBuild() {
        String buildAbi = getBuildAbi();
        if(BuildConfig.GIT_BRANCH.equalsIgnoreCase("local"))
            return true;

        String deviceAbi;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            deviceAbi = Build.CPU_ABI;
        } else {
            deviceAbi = Build.SUPPORTED_ABIS[0];
        }

        return deviceAbi.equalsIgnoreCase(buildAbi);
    }

    private static String getBuildAbi() {
        PackageManager manager = ButterApplication.getAppContext().getPackageManager();
        try {
            PackageInfo info = manager.getPackageInfo(ButterApplication.getAppContext().getPackageName(), 0);
            Integer versionCode = info.versionCode;

            if(info.versionName != null && info.versionName.contains("local"))
                return "local";

            if(versionCode > 50000000) {
                return "x86_64";
            } else if(versionCode > 40000000) {
                return "x86";
            } else if(versionCode > 30000000) {
                return "arm64-v8a";
            } else if(versionCode > 20000000) {
                return "armeabi-v7a";
            } else if(versionCode > 10000000) {
                return "armeabi";
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return "unsupported";
    }

}
