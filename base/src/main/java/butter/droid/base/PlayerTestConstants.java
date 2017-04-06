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

public final class PlayerTestConstants {

    public static final String CUSTOM_FILE = "dialog";
    public static final String DEFAULT_CUSTOM_FILE
            = "http://download.wavetlan.com/SVV/Media/HTTP/MP4/ConvertedFiles/QuickTime/QuickTime_test13_5m19s_AVC_VBR_324kbps_640x480_25fps_AAC-LCv4_CBR_93.4kbps_Stereo_44100Hz.mp4";
    public static final String SUBTITLES_URL = "http://sv244.cf/bbb-subs.srt";

    public static final String[] FILE_TYPES = new String[]{
            "TASM2, MP4 720p h264 High 3.1 4062 Kbps 24 FPS AC3 (5.1) 640 Kbps",
            "TASM2, MP4 720p h264 High 3.1 4062 Kbps 24 FPS MP3 167 Kbps",
            "TASM2, MP4 1080p h264 High 4.0 11.6 Mbps 24 FPS AC3 (5.1) 640 Kbps",
            "BBB, MP4 1080p h264 High 4.1 3 Mbps 30 FPS AAC 356 Kbps",
            "BBB, MP4 1080p h264 High 4.1 3 Mbps 30 FPS MP3 160 Kbps",
            "BBB, MP4 1080p h264 High 4.1 3 Mbps 30 FPS AC3 (5.1) 320 Kbps",
            "BBB, MP4 1080p h264 High 4.1 3 Mbps 30 FPS AC3 (2.0) 192 Kbps",
            "BBB, MP4 1080p h264 High 4.1 3 Mbps 30 FPS 1: AAC 2: AC3 (5.1) 1: 353 Kbps (AAC) 2: 320 Kbps (AC3",
            "BBB, MP4 1080p h264 High 4.1 3 Mbps 30 FPS 1: AC3 (5.1) 2: AAC 1: 320 Kbps (AC3) 2: 353 Kbps (AAC",
            "BBB, MP4 1080p mpeg4 Simple L1 5408 Kbps 30 FPS MP3 160 Kbps",
            "BBB, MKV 1080p h264 High 4.1 3 Mbps 30 FPS AAC 356 Kbps",
            "BBB, MKV 1080p h264 High 4.1 3 Mbps 30 FPS MP3 160 Kbps",
            "BBB, MKV 1080p h264 High 4.1 3 Mbps 30 FPS AC3 (5.1) 320 Kbps",
            "BBB, MP4 1080p h265 High 4.1 3 Mbps 30 FPS AAC 356 Kbps",
            "BBB, MP4 1080p h265 High 4.1 3 Mbps 30 FPS AC3 (5.1) 320 Kbps",
            "BBB, MKV 1080p h265 High 4.1 3 Mbps 30 FPS AAC 356 Kbps",
            "BBB, AVI 1080p XviD 4376 Kbps 30 FPS MP3 160 Kbps",
            "BBB, WebM 1080p VP8 917 Kbps 30 FPS Vorbis 324 Kbps",
            "BBB, WebM 1080p VP9 1064 Kbps 30 FPS Vorbis 128 Kbps",
            "BBB, MP4 2K h265 High 5.1 8.9 Mbps 60 FPS MP3 160 Kbps",
            "BBB, MP4 2K h264 High 5.1 8.9 Mbps 60 FPS MP3 160 Kbps",
            "BBB, MP4 4K h264 High 5.1 12.4 Mbps 60 FPS MP3 160 Kbps",
            "YouTube, Cinemates - Thug Life Hollywood Compilation",
            "YouTube, Cash Cash - Surrender (Official Lyric Video) ~ Copyright Protected",
            "Open dialog"
    };

    public final static String[] FILES = new String[]{
            "http://download.blender.org/peach/bigbuckbunny_movies/BigBuckBunny_320x180.mp4",
//            "https://get.butterproject.org/nwtests/Spider-Man_2_720p_24fps_surround_ac3.mp4",
            "https://get.butterproject.org/nwtests/Spider-Man_2_720p_24fps_mp3.mp4",
            "https://get.butterproject.org/nwtests/Spider-Man_2_1080p_24fps_surround_ac3.mp4",
            "https://get.butterproject.org/nwtests/bbb_sunflower_1080p_30fps_normal_aac.mp4",
            "https://get.butterproject.org/nwtests/bbb_sunflower_1080p_30fps_normal_mp3.mp4",
            "https://get.butterproject.org/nwtests/bbb_sunflower_1080p_30fps_normal_ac3.mp4",
            "https://get.butterproject.org/nwtests/bbb_sunflower_1080p_30fps_normal_ac3_stereo.mp4",
            "https://get.butterproject.org/nwtests/bbb_sunflower_1080p_30fps_normal_aac_ac3.mp4",
            "https://get.butterproject.org/nwtests/bbb_sunflower_1080p_30fps_normal_ac3_aac.mp4",
            "https://get.butterproject.org/nwtests/bbb_sunflower_1080p_30fps_normal_mpeg4_mp3.mp4",
            "https://get.butterproject.org/nwtests/bbb_sunflower_1080p_30fps_normal_aac.mkv",
            "https://get.butterproject.org/nwtests/bbb_sunflower_1080p_30fps_normal_mp3.mkv",
            "https://get.butterproject.org/nwtests/bbb_sunflower_1080p_30fps_normal_ac3.mkv",
            "https://get.butterproject.org/nwtests/bbb_sunflower_h265_1080p_30fps_normal_aac.mp4",
            "https://get.butterproject.org/nwtests/bbb_sunflower_h265_1080p_30fps_normal_ac3.mp4",
            "https://get.butterproject.org/nwtests/bbb_sunflower_h265_1080p_30fps_normal_aac.mkv",
            "https://get.butterproject.org/nwtests/bbb_sunflower_1080p_30fps_normal_mp3.avi",
            "https://get.butterproject.org/nwtests/bbb_sunflower_1080p_30fps_normal_vorbis.webm",
            "https://get.butterproject.org/nwtests/bbb_sunflower_1080p_30fps_normal_vp9_vorbis.webm",
            "https://get.butterproject.org/nwtests/bbb_sunflower_h265_2160p_60fps_normal_mp3.mp4",
            "https://get.butterproject.org/nwtests/bbb_sunflower_2160p_60fps_normal_mp3.mp4",
            "https://get.butterproject.org/nwtests/bbb_sunflower_native_60fps_normal_mp3.mp4",
            "https://www.youtube.com/watch?v=SvDMZFfwmgo",
            "http://youtu.be/K22v4xolOsc",
            CUSTOM_FILE
    };

    private PlayerTestConstants() {
        // no instances
    }

}
