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

import static butter.droid.base.ui.player.base.BaseVideoPlayerPresenter.SURFACE_16_9;
import static butter.droid.base.ui.player.base.BaseVideoPlayerPresenter.SURFACE_4_3;
import static butter.droid.base.ui.player.base.BaseVideoPlayerPresenter.SURFACE_BEST_FIT;
import static butter.droid.base.ui.player.base.BaseVideoPlayerPresenter.SURFACE_FILL;
import static butter.droid.base.ui.player.base.BaseVideoPlayerPresenter.SURFACE_FIT_HORIZONTAL;
import static butter.droid.base.ui.player.base.BaseVideoPlayerPresenter.SURFACE_FIT_VERTICAL;
import static butter.droid.base.ui.player.base.BaseVideoPlayerPresenter.SURFACE_ORIGINAL;

import android.graphics.Point;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.view.Display;
import android.view.SurfaceView;
import android.view.WindowManager;
import butter.droid.base.ui.player.base.BaseVideoPlayerPresenter.SizePolicy;
import org.videolan.libvlc.IVLCVout;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;
import org.videolan.libvlc.MediaPlayer.Event;
import timber.log.Timber;

public class VlcPlayer implements MediaPlayer.EventListener, IVLCVout.Callback, IVLCVout.OnNewVideoLayoutListener {

    @Nullable private final LibVLC libVLC;
    private final WindowManager windowManager;

    private final LayoutHolder layoutHolder;

    private MediaPlayer mediaPlayer;

    private PlayerCallback callback;

    public VlcPlayer(@Nullable final LibVLC libVLC, final WindowManager windowManager) {
        this.libVLC = libVLC;
        this.windowManager = windowManager;

        Point point = new Point();
        windowManager.getDefaultDisplay().getSize(point);
        layoutHolder = new LayoutHolder(point);
    }

    public boolean initialize() {
        if (libVLC == null) {
            return false;
        }

        mediaPlayer = new MediaPlayer(libVLC);
        mediaPlayer.setEventListener(this);
        return true;
    }

    public void loadMedia(Uri uri, int hardwareAcceleration) {
        final Media media = VLCMediaOptions.builder(libVLC, uri)
                .withHardwareAcceleration(hardwareAcceleration)
                .build();

        mediaPlayer.setMedia(media);

    }

    public void play() {
        mediaPlayer.play();
    }

    public void pause() {
        mediaPlayer.pause();
    }

    public void stop() {
        mediaPlayer.stop();
    }

    public void setCallback(final PlayerCallback callback) {
        this.callback = callback;
    }

    public void release() {
        mediaPlayer.release();
        if (libVLC != null) {
            libVLC.release();
        }
    }

    public void togglePlayPause() {
        if (isPlaying()) {
            pause();
        } else {
            play();
        }
    }

    public long getLength() {
        return mediaPlayer.getLength();
    }

    public boolean isPlaying() {
        return mediaPlayer.isPlaying();
    }

    public long getTime() {
        return mediaPlayer.getTime();
    }

    public void setTime(long time) {
        mediaPlayer.setTime(time);
    }

    public void fastForward(long rate) {
        mediaPlayer.setRate(rate);
    }

    public void attachToSurface(SurfaceView surface) {
        final IVLCVout vlcVout = mediaPlayer.getVLCVout();

        if (!vlcVout.areViewsAttached()) {
            vlcVout.setVideoView(surface);
            vlcVout.addCallback(this);
            vlcVout.attachViews(this);
        }
    }

    public void detachFromSurface() {
        final IVLCVout vlcVout = mediaPlayer.getVLCVout();
        vlcVout.removeCallback(this);
        vlcVout.detachViews();
    }

    public void setSizePolicy(@SizePolicy final int sizePolicy) {
        if (this.layoutHolder.sizePolicy != sizePolicy) {
            this.layoutHolder.sizePolicy = sizePolicy;
            updateSurfaceSize();
        }
    }

    @SizePolicy public int getSizePolicy() {
        return layoutHolder.sizePolicy;
    }

    @Override public void onEvent(final Event event) {
        switch (event.type) {
            case MediaPlayer.Event.Opening:
                break;
            case MediaPlayer.Event.Playing:
                if (callback != null) {
                    callback.playing();
                }
                break;
            case MediaPlayer.Event.Paused:
                if (callback != null) {
                    callback.paused();
                }
                break;
            case MediaPlayer.Event.Stopped:
                if (callback != null) {
                    callback.stopped();
                }
                break;
            case MediaPlayer.Event.EndReached:
                if (callback != null) {
                    callback.endReached();
                }
                break;
            case MediaPlayer.Event.EncounteredError:
                if (callback != null) {
                    callback.playerError();
                }
                break;
            case MediaPlayer.Event.TimeChanged:
                callback.progressChanged(event.getTimeChanged());
//            case MediaPlayer.Event.PositionChanged:
                break;
        }

    }

    @Override public void onNewVideoLayout(final IVLCVout vlcVout, final int width, final int height, final int visibleWidth,
            final int visibleHeight, final int sarNum, final int sarDen) {
        Display display = windowManager.getDefaultDisplay();

        Point size = new Point();
        display.getSize(size);

        int screenWidth = size.x;
        int screenHeight = size.y;

        vlcVout.setWindowSize(screenWidth, screenHeight);

        layoutHolder.height = height;
        layoutHolder.width = width;
        layoutHolder.visibleHeight = visibleHeight;
        layoutHolder.visibleWidth = visibleWidth;
        layoutHolder.sarNum = sarNum;
        layoutHolder.sarDen = sarDen;

        updateSurfaceSize();
    }

//    @Override
//    public void onNewLayout(final IVLCVout vlcVout, final int width, final int height, final int visibleWidth, final int visibleHeight,
//            final int sarNum, final int sarDen) {
//
//        Display display = windowManager.getDefaultDisplay();
//
//        Point size = new Point();
//        display.getSize(size);
//
//        int screenWidth = size.x;
//        int screenHeight = size.y;
//
//        vlcVout.setWindowSize(screenWidth, screenHeight);
//
//        layoutHolder.height = height;
//        layoutHolder.width = width;
//        layoutHolder.visibleHeight = visibleHeight;
//        layoutHolder.visibleWidth = visibleWidth;
//        layoutHolder.sarNum = sarNum;
//        layoutHolder.sarDen = sarDen;
//
//        updateSurfaceSize();
//
//    }

    private void updateSurfaceSize() {

        // sanity check
        if (!layoutHolder.isValid()) {
            Timber.e("Invalid surface size");
            return;
        }

        double displayWidth = layoutHolder.displaySize.x;
        double displayHeight = layoutHolder.displaySize.y;

        // compute the aspect ratio
        double aspectRatio, visibleWidth;
        if (layoutHolder.sarNum == layoutHolder.sarDen) {
            /* No indication about the density, assuming 1:1 */
            visibleWidth = layoutHolder.visibleWidth;
            aspectRatio = (double) layoutHolder.visibleWidth / (double) layoutHolder.visibleHeight;
        } else {
            /* Use the specified aspect ratio */
            visibleWidth = layoutHolder.visibleWidth * (double) layoutHolder.sarNum / layoutHolder.sarDen;
            aspectRatio = visibleWidth / layoutHolder.visibleHeight;
        }

        // compute the display aspect ratio
        double displayAspectRatio = layoutHolder.getDisplayAspectRation();

        switch (layoutHolder.sizePolicy) {
            case SURFACE_BEST_FIT:
                if (displayAspectRatio < aspectRatio) {
                    displayHeight = displayWidth / aspectRatio;
                } else {
                    displayWidth = displayHeight * aspectRatio;
                }
                break;
            case SURFACE_FIT_HORIZONTAL:
                displayHeight = displayWidth / aspectRatio;
                break;
            case SURFACE_FIT_VERTICAL:
                displayWidth = displayHeight * aspectRatio;
                break;
            case SURFACE_FILL:
                break;
            case SURFACE_16_9:
                aspectRatio = 16.0 / 9.0;
                if (displayAspectRatio < aspectRatio) {
                    displayHeight = displayWidth / aspectRatio;
                } else {
                    displayWidth = displayHeight * aspectRatio;
                }
                break;
            case SURFACE_4_3:
                aspectRatio = 4.0 / 3.0;
                if (displayAspectRatio < aspectRatio) {
                    displayHeight = displayWidth / aspectRatio;
                } else {
                    displayWidth = displayHeight * aspectRatio;
                }
                break;
            case SURFACE_ORIGINAL:
                displayHeight = layoutHolder.visibleHeight;
                displayWidth = visibleWidth;
                break;
        }

        // set display size
        int finalWidth = (int) Math.ceil(displayWidth * layoutHolder.width / layoutHolder.visibleWidth);
        int finalHeight = (int) Math.ceil(displayHeight * layoutHolder.height / layoutHolder.visibleHeight);

        if (callback != null) {
            callback.updateSurfaceSize(finalWidth, finalHeight);
        }

    }

    @Override public void onSurfacesCreated(final IVLCVout vlcVout) {
        // nothing to do
    }

    @Override public void onSurfacesDestroyed(final IVLCVout vlcVout) {
        // nothing to do
    }

//    @Override
//    public void onHardwareAccelerationError(final IVLCVout ivlcVout) {
//        // nothing to do
//    }

    private class LayoutHolder {

        private final Point displaySize;
        private int width;
        private int height;
        private int visibleWidth;
        private int visibleHeight;
        private int sarNum;
        private int sarDen;
        @SizePolicy private int sizePolicy = SURFACE_BEST_FIT;

        public LayoutHolder(final Point displaySize) {
            this.displaySize = displaySize;

            if (displaySize.x < displaySize.y) {
                displaySize.x += displaySize.y;
                displaySize.y = displaySize.x - displaySize.y;
                displaySize.x -= displaySize.y;
            }
        }

        private boolean isValid() {
            return displaySize.x * displaySize.y > 0 && width * height > 0;
        }

        private double getDisplayAspectRation() {
            return displaySize.x / displaySize.y;
        }
    }

    public interface PlayerCallback {

        void updateSurfaceSize(int width, int height);

        void progressChanged(long progress);

        void playing();

        void paused();

        void stopped();

        void endReached();

        void playerError();

    }

}
