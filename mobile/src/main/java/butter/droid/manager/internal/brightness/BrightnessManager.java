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

package butter.droid.manager.internal.brightness;

import android.app.Activity;
import android.content.ContentResolver;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.provider.Settings.System;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import butter.droid.base.utils.VersionUtils;
import dagger.Reusable;
import javax.inject.Inject;
import timber.log.Timber;

@Reusable
public class BrightnessManager {

    private final Activity activity;
    private final ContentResolver contentResolver;

    private boolean inited;
    private boolean revertToAuto;

    @Inject public BrightnessManager(final Activity activity, final ContentResolver contentResolver) {
        this.activity = activity;
        this.contentResolver = contentResolver;
    }

    public void increaseBrightness(float deltaFraction) {
        if (!inited) {
            initBrightnessTouch();
        }

        setBrightnessDelta(deltaFraction);
    }

    public boolean canChangeBrightness() {
        return !isAutomaticBrightness() || canWriteSettings();
    }

    public void restoreBrightness() {
        if (inited) {
            if (revertToAuto && canWriteSettings()) {
                Settings.System.putInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS_MODE, System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
            }

            setBrightnessRaw(LayoutParams.BRIGHTNESS_OVERRIDE_NONE);
            inited = false;
        }
    }

    private void initBrightnessTouch() {
        if (isAutomaticBrightness()) {
            if (canWriteSettings()) {
                Settings.System.putInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS_MODE,
                        Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
                revertToAuto = true;
            } else {
                throw new IllegalStateException("Cannot change automatic brightnes.");
            }
        }

        inited = true;
    }

    private void setBrightnessDelta(float deltaFraction) {
        float newValue = Math.min(Math.max(getCurrentBrightness() + deltaFraction, 0), 1);
        setBrightnessRaw(newValue);
    }

    private void setBrightnessRaw(float value) {
        Window window = activity.getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.screenBrightness = value;
        window.setAttributes(lp);
        Timber.d("Brightness %f", value);
    }

    private boolean canWriteSettings() {
        return !VersionUtils.isMarshmallow() || System.canWrite(activity);
    }

    private boolean isAutomaticBrightness() {
        try {
            return System.getInt(contentResolver, System.SCREEN_BRIGHTNESS_MODE) == System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC;
        } catch (SettingNotFoundException e) {
            return false;
        }
    }

    private float getCurrentBrightness() {
        WindowManager.LayoutParams lp = activity.getWindow().getAttributes();

        if (lp.screenBrightness < 0) {
            try {
                return System.getInt(contentResolver, System.SCREEN_BRIGHTNESS) / 255f;
            } catch (SettingNotFoundException e) {
                return  .6f; // Sane default
            }
        } else {
            return lp.screenBrightness;
        }

    }

}
