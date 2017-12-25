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
import static butter.droid.base.ui.player.base.BaseVideoPlayerPresenter.SURFACE_FIT_SCREEN;
import static butter.droid.base.ui.player.base.BaseVideoPlayerPresenter.SURFACE_ORIGINAL;

import android.net.Uri;
import android.support.annotation.Nullable;
import android.view.SurfaceView;
import butter.droid.base.ui.player.base.BaseVideoPlayerPresenter.SizePolicy;
import org.videolan.libvlc.IVLCVout;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.Media.Slave.Type;
import org.videolan.libvlc.MediaPlayer;
import org.videolan.libvlc.MediaPlayer.Event;

public class VlcPlayer implements MediaPlayer.EventListener, IVLCVout.Callback, IVLCVout.OnNewVideoLayoutListener {

    @Nullable private final LibVLC libVLC;

    private final LayoutHolder layoutHolder;

    private MediaPlayer mediaPlayer;

    private PlayerCallback callback;

    public VlcPlayer(@Nullable final LibVLC libVLC) {
        this.libVLC = libVLC;
        layoutHolder = new LayoutHolder();
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

    public void loadSubs(Uri uri) {
        mediaPlayer.addSlave(Type.Subtitle, uri, true);
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

    public void attachToSurface(SurfaceView videoSurface, SurfaceView subsSurface) {
        final IVLCVout vlcVout = mediaPlayer.getVLCVout();

        if (!vlcVout.areViewsAttached()) {
            vlcVout.setVideoView(videoSurface);
            vlcVout.setSubtitlesView(subsSurface);
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
                // nothing to do
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
                break;
            case MediaPlayer.Event.PositionChanged:
                // nothing to do
                break;
            default:
                // nothing to do
                break;
        }

    }

    @Override public void onNewVideoLayout(final IVLCVout vlcVout, final int width, final int height, final int visibleWidth,
            final int visibleHeight, final int sarNum, final int sarDen) {

        layoutHolder.height = height;
        layoutHolder.width = width;
        layoutHolder.visibleHeight = visibleHeight;
        layoutHolder.visibleWidth = visibleWidth;
        layoutHolder.sarNum = sarNum;
        layoutHolder.sarDen = sarDen;

        updateSurfaceSize();
    }

    private void updateSurfaceSize() {

        // sanity check
        if (!layoutHolder.isValid()) {
            // TODO surface may still be 0
            changeMediaPlayerLayout(); // Uses OpenGL use media library API for scaling
            return;
        }

        double displayWidth = layoutHolder.surfaceWidth;
        double displayHeight = layoutHolder.surfaceHeight;

        // compute the aspect ratio
        double aspectRatio;
        double visibleWidth;
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
            case SURFACE_FIT_SCREEN:
                if (displayAspectRatio >= aspectRatio) {
                    displayHeight = displayWidth / aspectRatio;
                } else {
                    displayWidth = displayHeight * aspectRatio;
                }
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
            default:
                // nothing to do
                break;
        }

        // set display size
        int finalWidth = (int) Math.ceil(displayWidth * layoutHolder.width / layoutHolder.visibleWidth);
        int finalHeight = (int) Math.ceil(displayHeight * layoutHolder.height / layoutHolder.visibleHeight);

        if (callback != null) {
            callback.updateSurfaceSize(finalWidth, finalHeight);
        }

    }

    private void changeMediaPlayerLayout() {
        /* Change the video placement using MediaPlayer API */
        switch (layoutHolder.sizePolicy) {
            case SURFACE_BEST_FIT:
                mediaPlayer.setAspectRatio(null);
                mediaPlayer.setScale(0);
                break;
            case SURFACE_FIT_SCREEN:
            case SURFACE_FILL: {
                Media.VideoTrack vtrack = mediaPlayer.getCurrentVideoTrack();
                if (vtrack == null) {
                    return;
                }
                final boolean videoSwapped = vtrack.orientation == Media.VideoTrack.Orientation.LeftBottom
                        || vtrack.orientation == Media.VideoTrack.Orientation.RightTop;
                if (layoutHolder.sizePolicy == SURFACE_FIT_SCREEN) {
                    int videoW = vtrack.width;
                    int videoH = vtrack.height;

                    if (videoSwapped) {
                        int swap = videoW;
                        videoW = videoH;
                        videoH = swap;
                    }
                    if (vtrack.sarNum != vtrack.sarDen) {
                        videoW = videoW * vtrack.sarNum / vtrack.sarDen;
                    }

                    float ar = videoW / (float) videoH;
                    float dar = layoutHolder.surfaceWidth / (float) layoutHolder.surfaceHeight;

                    float scale;
                    if (dar >= ar) {
                        scale = layoutHolder.surfaceWidth / (float) videoW; /* horizontal */
                    } else {
                        scale = layoutHolder.surfaceHeight / (float) videoH; /* vertical */
                    }
                    mediaPlayer.setScale(scale);
                    mediaPlayer.setAspectRatio(null);
                } else {
                    mediaPlayer.setScale(0);
                    mediaPlayer.setAspectRatio(!videoSwapped ? "" + layoutHolder.surfaceWidth + ":" + layoutHolder.surfaceHeight
                            : "" + layoutHolder.surfaceHeight + ":" + layoutHolder.surfaceWidth);
                }
                break;
            }
            case SURFACE_16_9:
                mediaPlayer.setAspectRatio("16:9");
                mediaPlayer.setScale(0);
                break;
            case SURFACE_4_3:
                mediaPlayer.setAspectRatio("4:3");
                mediaPlayer.setScale(0);
                break;
            case SURFACE_ORIGINAL:
                mediaPlayer.setAspectRatio(null);
                mediaPlayer.setScale(1);
                break;
        }
    }


    @Override public void onSurfacesCreated(final IVLCVout vlcVout) {
        // nothing to do
    }

    @Override public void onSurfacesDestroyed(final IVLCVout vlcVout) {
        // nothing to do
    }

    public void surfaceChanged(final int width, final int height) {
        mediaPlayer.getVLCVout().setWindowSize(width, height);
        layoutHolder.setSurfaceSize(width, height);
    }

    private class LayoutHolder {

        private int width;
        private int height;
        private int visibleWidth;
        private int visibleHeight;
        private int surfaceWidth;
        private int surfaceHeight;
        private int sarNum;
        private int sarDen;
        @SizePolicy private int sizePolicy = SURFACE_BEST_FIT;

        private void setSurfaceSize(int width, int height) {
            if (width < height) {
                width += height;
                height = width - height;
                width -= height;
            }
            surfaceWidth = width;
            surfaceHeight = height;
        }

        private boolean isValid() {
            return surfaceWidth * surfaceHeight > 0 && width * height > 0;
        }

        private double getDisplayAspectRation() {
            return surfaceWidth / surfaceHeight;
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
