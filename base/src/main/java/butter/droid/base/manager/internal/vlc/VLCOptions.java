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

package butter.droid.base.manager.internal.vlc;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.preference.PreferenceManager;
import butter.droid.base.ButterApplication;
import java.io.File;
import java.util.ArrayList;
import org.videolan.libvlc.util.AndroidUtil;
import org.videolan.libvlc.util.VLCUtil;
import timber.log.Timber;

@SuppressWarnings("unused")
public class VLCOptions {

    public static ArrayList<String> getLibOptions() {
        final Context context = ButterApplication.getAppContext();
        final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);

        ArrayList<String> options = new ArrayList<String>(50);

        final boolean timeStrechingDefault = Build.VERSION.SDK_INT >= VERSION_CODES.KITKAT;
        final boolean timeStreching = pref.getBoolean("enable_time_stretching_audio", timeStrechingDefault);
        final String subtitlesEncoding = pref.getString("subtitle_text_encoding", "");
        final boolean frameSkip = pref.getBoolean("enable_frame_skip", false);
        String chroma = pref.getString("chroma_format", "YV12");
        if (chroma.equals("YV12"))
            chroma = "";
        final boolean verboseMode = pref.getBoolean("enable_verbose_mode", true);

        int deblocking = -1;
        try {
            deblocking = getDeblocking(Integer.parseInt(pref.getString("deblocking", "-1")));
        } catch (NumberFormatException ignored) {}

        int networkCaching = pref.getInt("network_caching_value", 0);
        if (networkCaching > 60000)
            networkCaching = 60000;
        else if (networkCaching < 0)
            networkCaching = 0;

        final String freetypeRelFontsize = pref.getString("subtitles_size", "16");
        final boolean freetypeBold = pref.getBoolean("subtitles_bold", false);
        final String freetypeColor = pref.getString("subtitles_color", "16777215");
        final boolean freetypeBackground = pref.getBoolean("subtitles_background", false);
        final int opengl = Integer.parseInt(pref.getString("opengl", "-1"));

        /* CPU intensive plugin, setting for slow devices */
        options.add(timeStreching ? "--audio-time-stretch" : "--no-audio-time-stretch");
        options.add("--avcodec-skiploopfilter");
        options.add("" + deblocking);
        options.add("--avcodec-skip-frame");
        options.add(frameSkip ? "2" : "0");
        options.add("--avcodec-skip-idct");
        options.add(frameSkip ? "2" : "0");
        options.add("--subsdec-encoding");
        options.add(subtitlesEncoding);
        options.add("--stats");
        /* XXX: why can't the default be fine ? #7792 */
        if (networkCaching > 0)
            options.add("--network-caching=" + networkCaching);
        options.add("--android-display-chroma");
        options.add(chroma);
        options.add("--audio-resampler");
        options.add(getResampler());

        options.add("--freetype-rel-fontsize=" + freetypeRelFontsize);
        if (freetypeBold)
            options.add("--freetype-bold");
        options.add("--freetype-color=" + freetypeColor);
        if (freetypeBackground)
            options.add("--freetype-background-opacity=128");
        else
            options.add("--freetype-background-opacity=0");
        if (opengl == 1)
            options.add("--vout=gles2,none");
        else if (opengl == 0)
            options.add("--vout=android_display,none");

        /* Configure keystore */
        options.add("--keystore");
        if (AndroidUtil.isMarshMallowOrLater)
            options.add("file_crypt,none");
        else
            options.add("file_plaintext,none");
        options.add("--keystore-file");
        options.add(new File(context.getDir("keystore", Context.MODE_PRIVATE), "file").getAbsolutePath());

        options.add(verboseMode ? "-vv" : "-v");

        return options;
    }

    private static String getResampler() {
        final VLCUtil.MachineSpecs m = VLCUtil.getMachineSpecs();
        return (m == null || m.processors > 2) ? "soxr" : "ugly";
    }

    private static int getDeblocking(int deblocking) {
        int ret = deblocking;
        if (deblocking < 0) {
            /**
             * Set some reasonable sDeblocking defaults:
             *
             * Skip all (4) for armv6 and MIPS by default
             * Skip non-ref (1) for all armv7 more than 1.2 Ghz and more than 2 cores
             * Skip non-key (3) for all devices that don't meet anything above
             */
            VLCUtil.MachineSpecs m = VLCUtil.getMachineSpecs();
            if (m == null)
                return ret;
            if ((m.hasArmV6 && !(m.hasArmV7)) || m.hasMips)
                ret = 4;
            else if (m.frequency >= 1200 && m.processors > 2)
                ret = 1;
            else if (m.bogoMIPS >= 1200 && m.processors > 2) {
                ret = 1;
                Timber.d("Used bogoMIPS due to lack of frequency info");
            } else
                ret = 3;
        } else if (deblocking > 4) { // sanity check
            ret = 3;
        }
        return ret;
    }

}
