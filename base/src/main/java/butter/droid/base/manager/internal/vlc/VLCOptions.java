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

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import org.videolan.libvlc.util.VLCUtil;

@SuppressWarnings("unused")
public class VLCOptions {

    private static final String TAG = "VLCConfig";

    public static final String AUDIO_TIME_STRECH_OPTION = "--audio-time-stretch";
    public static final String NO_AUDIO_TIME_STRECH_OPTION = "--no-audio-time-stretch";

    public static final String DEBLOCKING_OPTION = "--avcodec-skiploopfilter";
    public static final String DEBLOCKING_NONE = "0";
    public static final String DEBLOCKING_NON_REF = "1";
    public static final String DEBLOCKING_BIDIR = "2";
    public static final String DEBLOCKING_NON_KEY = "3";
    public static final String DEBLOCKING_ALL = "4";

    public static final String SKIP_FRAME_OPTION = "--avcodec-skip-frame";
    public static final String SKIP_FRAME_NONE = "0";
    public static final String SKIP_FRAME_P_FRAMES = "2";

    public static final String SKIP_IDCT_OPTION = "--avcodec-skip-idct";
    public static final String SKIP_IDCT_NONE = "0";
    public static final String SKIP_IDCT_P_FRAMES = "2";

    public static final String SUBTITLES_ENCODING_OPTION = "--subsdec-encoding";
    public static final String UTF8 = "UTF-8";

    public static final String STATS_OPTION = "--stats";

    public static final String ANDROID_WINDOW_CHROMA_OPTION = "--androidwindow-chroma";
    public static final String ANDROID_WINDOW_CHROMA_RV_32_OPTION = "RV32";
    public static final String ANDROID_WINDOW_CHROMA_RV_16_OPTION = "RV16";
    public static final String ANDROID_WINDOW_CHROMA_YV_12_OPTION = "YV12";

    public static final String NETWORK_CACHING_OPTION = "--network-caching";

    private static final String VERBOSITY_HIGH = "-vvv";
    private static final String VERBOSITY_MEDIUM = "-vv";

    // Complete description of options here: https://wiki.videolan.org/VLC_command-line_help/
    public static class Builder {

        private String audioTimeStreching;
        private String deblocking;
        private String skipFrame;
        private String skipIDCT;
        private String subtitlesEncoding;
        private String stats;
        private String androidWindowChroma;
        private String networkCaching;
        private String verbosityLevel;

        public Builder() {
            this.audioTimeStreching = NO_AUDIO_TIME_STRECH_OPTION;
            this.deblocking = DEBLOCKING_NONE;
            this.skipFrame = SKIP_FRAME_NONE;
            this.skipIDCT = SKIP_IDCT_NONE;
            this.subtitlesEncoding = UTF8;
            this.stats = STATS_OPTION;
            this.androidWindowChroma = ANDROID_WINDOW_CHROMA_RV_32_OPTION;
            this.networkCaching = "0";
        }

        public Builder withAudioTimeStreching(boolean audioTimeStreching) {
            this.audioTimeStreching = audioTimeStreching ? AUDIO_TIME_STRECH_OPTION : NO_AUDIO_TIME_STRECH_OPTION;
            return this;
        }

        public Builder withVideoSkipLoopFilter() {
            final VLCUtil.MachineSpecs m = VLCUtil.getMachineSpecs();
            if (m == null) {
                this.deblocking = DEBLOCKING_NONE;
                return this;
            }
            if ((m.hasArmV6 && !(m.hasArmV7)) || m.hasMips) {
                this.deblocking = DEBLOCKING_ALL;
            } else if ((m.frequency >= 1200 && m.processors > 2) || (m.bogoMIPS >= 1200 && m.processors > 2)) {
                this.deblocking = DEBLOCKING_NON_REF;
            } else {
                this.deblocking = DEBLOCKING_NON_KEY;
            }
            return this;
        }

        public Builder withVideoSkipFrame(boolean skipFrame) {
            this.skipFrame = skipFrame ? SKIP_FRAME_P_FRAMES : SKIP_FRAME_NONE;
            return this;
        }

        public Builder withVideoSkipIDCT(boolean skipIDCT) {
            this.skipIDCT = skipIDCT ? SKIP_IDCT_P_FRAMES : SKIP_IDCT_NONE;
            return this;
        }

        public Builder withSubtitlesEncoding(String encoding) {
            this.subtitlesEncoding = encoding;
            return this;
        }

        public Builder withNetworkCaching(long time, TimeUnit timeUnit) {
            final long millis = timeUnit.toMillis(time);
            return this;
        }

        public Builder withAndroidWindowChroma(String chroma) {
            this.androidWindowChroma = chroma;
            return this;
        }

        public Builder withStats(boolean stats) {
            this.stats = stats ? STATS_OPTION : "";
            return this;
        }

        public Builder withVerbosity(boolean enabled) {
            this.verbosityLevel = enabled ? VERBOSITY_HIGH : VERBOSITY_MEDIUM;
            return this;
        }

        public ArrayList<String> build() {
            final ArrayList<String> options = new ArrayList<>();
            options.add(audioTimeStreching);
            options.add(DEBLOCKING_OPTION);
            options.add(deblocking);
            options.add(SKIP_FRAME_OPTION);
            options.add(skipFrame);
            options.add(SKIP_IDCT_OPTION);
            options.add(skipIDCT);
            options.add(subtitlesEncoding);
            options.add(stats);
            options.add(ANDROID_WINDOW_CHROMA_OPTION);
            options.add(androidWindowChroma);
            options.add(NETWORK_CACHING_OPTION);
            options.add(networkCaching);
            options.add(verbosityLevel);
            return options;
        }
    }

}
