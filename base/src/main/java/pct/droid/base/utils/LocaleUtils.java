package pct.droid.base.utils;

import android.content.res.Configuration;

import java.util.Locale;

import pct.droid.base.PopcornApplication;

public class LocaleUtils {

    public static String getCurrent() {
        return getLanguageCode(Locale.getDefault());
    }

    public static void setCurrent(Locale locale) {
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;

        PopcornApplication.getAppContext().getResources().updateConfiguration(config, PopcornApplication.getAppContext().getResources().getDisplayMetrics());
    }

    public static String getLanguageCode(Locale locale) {
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

}
