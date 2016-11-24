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

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;

import java.util.Locale;

import butter.droid.base.compat.Compatibility;

public class LocaleUtils extends Compatibility{

    public static String getCurrentAsString() {
        return getLanguageCode(getCurrent());
    }

    @SuppressWarnings("deprecation")
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static void setCurrent(Context context, Locale locale) {
        Locale.setDefault(locale);
        Configuration config = context.getResources().getConfiguration();
        if (hasApi(Build.VERSION_CODES.JELLY_BEAN_MR1)) {
            config.setLocale(locale);
        } else {
            config.locale = locale;
        }
        context.getResources().updateConfiguration(config, context.getResources().getDisplayMetrics());

    }

    public static Locale getCurrent() {
        return Locale.getDefault();
    }

    private static String getLanguageCode(Locale locale) {
        String languageCode = locale.getLanguage();
        if (!locale.getCountry().isEmpty()) {
            languageCode += "-" + locale.getCountry();
        }
        return languageCode;
    }

    public static Locale toLocale(String languageCode) {
        String[] language = languageCode.split("-");
        if (language.length > 1) {
            return new Locale(language[0], language[1]);
        }
        return new Locale(language[0]);
    }

    public static boolean currentLocaleIsRTL() {
        return isRTL(toLocale(getCurrentAsString()));
    }

    public static boolean isRTL(Locale locale) {
        final int directionality = Character.getDirectionality(locale.getDisplayName().charAt(0));
        return directionality == Character.DIRECTIONALITY_RIGHT_TO_LEFT || directionality == Character.DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC;
    }

}
