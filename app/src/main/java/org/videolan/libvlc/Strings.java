package org.videolan.libvlc;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

public class Strings {
    public final static String TAG = "VLC/Util/Strings";

    public static String stripTrailingSlash(String s) {
        if( s.endsWith("/") && s.length() > 1 )
            return s.substring(0, s.length() - 1);
        return s;
    }

    static boolean StartsWith(String[] array, String text) {
        for (String item : array)
            if (text.startsWith(item))
                return true;
        return false;
    }

    /**
     * Convert time to a string
     * @param millis e.g.time/length from file
     * @return formated string (hh:)mm:ss
     */
    public static String millisToString(long millis)
    {
        return Strings.millisToString(millis, false);
    }

    /**
     * Convert time to a string
     * @param millis e.g.time/length from file
     * @return formated string "[hh]h[mm]min" / "[mm]min[s]s"
     */
    public static String millisToText(long millis)
    {
        return Strings.millisToString(millis, true);
    }

    static String millisToString(long millis, boolean text) {
        boolean negative = millis < 0;
        millis = java.lang.Math.abs(millis);

        millis /= 1000;
        int sec = (int) (millis % 60);
        millis /= 60;
        int min = (int) (millis % 60);
        millis /= 60;
        int hours = (int) millis;

        String time;
        DecimalFormat format = (DecimalFormat)NumberFormat.getInstance(Locale.US);
        format.applyPattern("00");
        if (text) {
            if (millis > 0)
                time = (negative ? "-" : "") + hours + "h" + format.format(min) + "min";
            else if (min > 0)
                time = (negative ? "-" : "") + min + "min";
            else
                time = (negative ? "-" : "") + sec + "s";
        }
        else {
            if (millis > 0)
                time = (negative ? "-" : "") + hours + ":" + format.format(min) + ":" + format.format(sec);
            else
                time = (negative ? "-" : "") + min + ":" + format.format(sec);
        }
        return time;
    }

    /**
     * equals() with two strings where either could be null
     */
    public static boolean nullEquals(String s1, String s2) {
        return (s1 == null ? s2 == null : s1.equals(s2));
    }

    /**
     * Get the formatted current playback speed in the form of 1.00x
     */
    public static String formatRateString(float rate) {
        return String.format(java.util.Locale.US, "%.2fx", rate);
    }
}