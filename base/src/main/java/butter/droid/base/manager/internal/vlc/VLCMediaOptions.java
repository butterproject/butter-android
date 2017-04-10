package butter.droid.base.manager.internal.vlc;

import android.net.Uri;
import dagger.internal.Preconditions;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;

public class VLCMediaOptions {

    public static final int HW_ACCELERATION_AUTOMATIC = -1;
    public static final int HW_ACCELERATION_DISABLED = 0;
    public static final int HW_ACCELERATION_DECODING = 1;
    public static final int HW_ACCELERATION_FULL = 2;
    
    public static final String NO_VIDEO = ":no-video";
    public static final String START_PAUSED = ":start-paused";

    public static Builder builder(LibVLC libVLC, Uri videoLocation) {
        return new Builder(libVLC, videoLocation);
    }

    public static class Builder {

        private Media media;

        public Builder(LibVLC libVLC, Uri videoLocation) {
            Preconditions.checkNotNull(libVLC, "libVLC == null");
            Preconditions.checkNotNull(videoLocation, "videoLocation == null");
            this.media = new Media(libVLC, videoLocation);
        }

        public Builder withNoVideo() {
            this.media.addOption(NO_VIDEO);
            return this;
        }

        public Builder withStartPaused() {
            this.media.addOption(START_PAUSED);
            return this;
        }

        public Builder withHardwareAcceleration(int flag) {
            if (flag == HW_ACCELERATION_DISABLED) {
                media.setHWDecoderEnabled(false, false);
            } else if (flag == HW_ACCELERATION_FULL) {
                media.setHWDecoderEnabled(true, true);
            } else if (flag == HW_ACCELERATION_DECODING) {
                media.setHWDecoderEnabled(true, true);
                media.addOption(":no-mediacodec-dr");
                media.addOption(":no-omxil-dr");
            } /* else automatic: use default options */
            return this;
        }

        public Media build() {
            return media;
        }
    }

}
