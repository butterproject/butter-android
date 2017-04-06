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

package butter.droid.base.ui.player.fragment;

import static android.R.attr.value;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import butter.droid.base.R;
import butter.droid.base.content.preferences.PreferencesHandler;
import butter.droid.base.manager.beaming.BeamDeviceListener;
import butter.droid.base.manager.beaming.BeamManager;
import butter.droid.base.manager.prefs.PrefManager;
import butter.droid.base.manager.provider.ProviderManager;
import butter.droid.base.manager.vlc.PlayerManager;
import butter.droid.base.manager.vlc.VLCOptions;
import butter.droid.base.providers.media.models.Media;
import butter.droid.base.providers.subs.SubsProvider;
import butter.droid.base.subs.Caption;
import butter.droid.base.subs.SubtitleDownloader;
import butter.droid.base.subs.SubtitleDownloader.ISubtitleDownloaderListener;
import butter.droid.base.subs.TimedTextObject;
import butter.droid.base.torrent.StreamInfo;
import butter.droid.base.utils.LocaleUtils;
import com.connectsdk.device.ConnectableDevice;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;
import org.videolan.libvlc.IVLCVout;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.LibVLC.HardwareAccelerationError;
import org.videolan.libvlc.MediaPlayer;
import org.videolan.libvlc.MediaPlayer.Event;
import org.videolan.libvlc.MediaPlayer.EventListener;
import timber.log.Timber;

public abstract class BaseVideoPlayerPresenterImpl implements BaseVideoPlayerPresenter, HardwareAccelerationError, EventListener,
        ISubtitleDownloaderListener {

    private static final String PREF_RESUME_POSITION = "resume_position";

    private final BaseVideoPlayerView view;
    private final Context context;
    private final PrefManager prefManager;
    @Nullable private final LibVLC libVLC;
    private final PreferencesHandler preferencesHandler;
    private final ProviderManager providerManager;
    private final PlayerManager playerManager;
    private final BeamManager beamManager;

    private MediaPlayer mediaPlayer;

    protected StreamInfo streamInfo;
    private long resumePosition;
    protected Media media;

    private String currentSubsLang = SubsProvider.SUBTITLE_LANGUAGE_NONE;
    private File subsFile;
    private TimedTextObject subs;
    private Caption lastSubs;

    // probably required when hardware acceleration selection during playback is implemented
    private boolean disabledHardwareAcceleration;
    private boolean seeking;

    private long videoDuration;
    private int streamerProgress;
    private boolean videoEnded;
    private int subtitleOffset;
    @Surface private int currentSize = SURFACE_BEST_FIT;

    public BaseVideoPlayerPresenterImpl(final BaseVideoPlayerView view, final Context context, final PrefManager prefManager,
            @Nullable final LibVLC libVLC,final PreferencesHandler preferencesHandler, final ProviderManager providerManager,
            final PlayerManager playerManager, final BeamManager beamManager) {
        this.view = view;
        this.context = context;
        this.prefManager = prefManager;
        this.libVLC = libVLC;
        this.preferencesHandler = preferencesHandler;
        this.providerManager = providerManager;
        this.playerManager = playerManager;
        this.beamManager = beamManager;
    }

    @Override public void onCreate(final StreamInfo streamInfo, final long resumePosition) {

        if (libVLC == null) {
            // TODO: 4/2/17 Stop activity & maybe show error
            return;
        }

        if (streamInfo == null) {
            throw new IllegalStateException("Stream info was not provided");
        }

        this.streamInfo = streamInfo;
        this.resumePosition = resumePosition;
        this.media = streamInfo.getMedia();

        prefManager.save(PREF_RESUME_POSITION, resumePosition);

        libVLC.setOnHardwareAccelerationError(this);

        mediaPlayer = new MediaPlayer(libVLC);
        mediaPlayer.setEventListener(this);

        if (streamInfo.getSubtitleLanguage() == null) {
            // Get selected default subtitle
            String defaultSubtitle = preferencesHandler.getSubtitleDefaultLanguage();
            streamInfo.setSubtitleLanguage(defaultSubtitle);
            currentSubsLang = defaultSubtitle;
            Timber.d("Using default subtitle: %s", currentSubsLang);
        }

        if (!streamInfo.getSubtitleLanguage().equals(SubsProvider.SUBTITLE_LANGUAGE_NONE)) {
            Timber.d("Download default subtitle");
            currentSubsLang = streamInfo.getSubtitleLanguage();
            loadOrDownloadSubtitle();
        }

        updateSubtitleSize(preferencesHandler.getSubtitleSize());

    }

    @Override public void onResume() {
        beamManager.addDeviceListener(deviceListener);
        view.onProgressChanged(prefManager.get(PREF_RESUME_POSITION, resumePosition), getStreamerProgress(), videoDuration);


        prepareVlcVout();
        loadMedia();
    }

    @Override public void onPause() {
        saveVideoCurrentTime();
        mediaPlayer.stop();

        final IVLCVout vlcout = mediaPlayer.getVLCVout();
        view.detachVlcViews(vlcout);

        beamManager.removeDeviceListener(deviceListener);
    }

    @Override public void onDestroy() {
        prefManager.save(PREF_RESUME_POSITION, 0);

        mediaPlayer.release();
        if (libVLC != null) {
            libVLC.release();
        }
    }

    @Override public void play() {
        mediaPlayer.play();
    }

    @Override public void pause() {
        mediaPlayer.pause();
    }

    @Override public void togglePlayPause() {
        if (videoEnded) {
            loadMedia();
        }

        if (mediaPlayer.isPlaying()) {
            pause();
        } else {
            play();
        }
    }

    @Override public void eventHardwareAccelerationError() {
        handleHardwareAccelerationError();
    }

    @Override public void onEvent(final Event event) {
        switch (event.type) {
            case MediaPlayer.Event.Playing:
                videoDuration = mediaPlayer.getLength();
                view.setKeepScreenOn(true);
                resumeVideo();
                view.setProgressVisible(false);
                view.showOverlay();
                view.updatePlayPauseState(true);
                break;
            case MediaPlayer.Event.Paused:
                view.setKeepScreenOn(false);
                saveVideoCurrentTime();
                view.updatePlayPauseState(false);
                break;
            case MediaPlayer.Event.Stopped:
                view.setKeepScreenOn(false);
                view.updatePlayPauseState(false);
                break;
            case MediaPlayer.Event.EndReached:
                endReached();
                view.updatePlayPauseState(false);
                break;
            case MediaPlayer.Event.EncounteredError:
                view.onErrorEncountered();
                view.updatePlayPauseState(false);
                break;
            case MediaPlayer.Event.Opening:
                view.setProgressVisible(true);
                videoDuration = mediaPlayer.getLength();
                mediaPlayer.play();
                break;
            case MediaPlayer.Event.TimeChanged:
            case MediaPlayer.Event.PositionChanged:
                view.onProgressChanged(getCurrentTime(), getStreamerProgress(), getDuration());
                progressSubtitleCaption();
                break;
        }

    }

    @Override public void onSubtitleDownloadCompleted(final boolean isSuccessful, final TimedTextObject subtitleFile) {
        onSubtitleEnabledStateChanged(isSuccessful);
        subs = subtitleFile;
    }

    @Override public void vlcNewLayout() {
        view.changeSurfaceSize(mediaPlayer.getVLCVout(), currentSize, false);
    }

    @Override public void streamProgressUpdated(final float progress) {
        int newProgress = (int) ((getDuration() / 100) * progress);
        if (streamerProgress < newProgress) {
            streamerProgress = newProgress;
        }
    }

    @Override public void reloadMedia() {
        mediaPlayer.stop();
        view.clearFrame();
        loadMedia();
    }

    @Override public void showSubsLanguageSettings() {

        String[] subtitles = media.subtitles.keySet().toArray(new String[media.subtitles.size()]);
        Arrays.sort(subtitles);

        final String[] adapterSubtitles = new String[subtitles.length + 2];
        System.arraycopy(subtitles, 0, adapterSubtitles, 1, subtitles.length);

        adapterSubtitles[0] = SubsProvider.SUBTITLE_LANGUAGE_NONE;
        adapterSubtitles[adapterSubtitles.length - 1] = "custom";
        String[] readableNames = new String[adapterSubtitles.length];

        for (int i = 0; i < readableNames.length - 1; i++) {
            String language = adapterSubtitles[i];
            if (language.equals(SubsProvider.SUBTITLE_LANGUAGE_NONE)) {
                readableNames[i] = context.getString(R.string.no_subs);
            } else {
                Locale locale = LocaleUtils.toLocale(language);
                readableNames[i] = locale.getDisplayName(locale);
            }
        }

        readableNames[readableNames.length - 1] = "Custom..";

        view.showPickSubsDialog(readableNames, adapterSubtitles, currentSubsLang);
    }

    @Override public void showCustomSubsPicker() {
        view.showSubsFilePicker();
    }

    @Override public void onSubtitleLanguageSelected(final String language) {
        if (currentSubsLang != null && (language == null || currentSubsLang.equals(language))) {
            return;
        }

        currentSubsLang = language;
        streamInfo.setSubtitleLanguage(language);

        if (currentSubsLang.equals(SubsProvider.SUBTITLE_LANGUAGE_NONE)) {
            subs = null;
            onSubtitleEnabledStateChanged(false);
            return;
        }

        if (media == null || media.subtitles == null || media.subtitles.size() == 0) {
            media = null;
            onSubtitleEnabledStateChanged(false);
            throw new IllegalArgumentException("Media doesn't have subtitle");
        }

        if (!media.subtitles.containsKey(currentSubsLang)) {
            subs = null;
            onSubtitleEnabledStateChanged(false);
            throw new IllegalArgumentException("Media doesn't have subtitle with specified language");
        }

        view.showTimedCaptionText(null);
        loadOrDownloadSubtitle();
    }

    @Override public void onSubsFileSelected(final File f) {
        if (!f.getPath().endsWith(".srt")) {
//            Snackbar.make(getView(), R.string.unknown_error,
//                    Snackbar.LENGTH_SHORT).show();
            return;
        } else {
            subsFile = f;
        }
    }

    @Override public void showSubsSizeSettings() {
        view.displaySubsSizeDialog();
    }

    @Override public void onSubsSizeChanged(final int size) {
        updateSubtitleSize(size);
    }

    @Override public void showSubsTimingSettings() {
        view.displaySubsTimingDialog(subtitleOffset);
    }

    @Override public void onSubsTimingChanged(final int offset) {
        subtitleOffset = value;
        view.showTimedCaptionText(null);
    }

    @Override public void onScaleClicked() {
        if (currentSize < SURFACE_ORIGINAL) {
            currentSize++;
        } else {
            currentSize = SURFACE_BEST_FIT;
        }

        view.changeSurfaceSize(mediaPlayer.getVLCVout(), currentSize, true);
        view.showOverlay();
    }

    @Override public void onSubsClicked() {
        if (media != null && media.subtitles != null) {
            view.showSubsSelectorDialog();
        }
    }

    @Override public void onStartSeeking() {
        seeking = true;
    }

    @Override public void onStopSeeking() {
        seeking = false;
    }

    protected abstract void onHardwareAccelerationError();

    /**
     * Called when subtitle for current media successfully loaded or disabled.
     *
     * @param enabled Whether subtitle is loaded or disabled.
     */
    protected void onSubtitleEnabledStateChanged(boolean enabled) {
        // override if needed
    }

    protected abstract void updateSubtitleSize(int size);

    /**
     * External extras: - position (long) - position of the video to start with (in ms)
     */
    protected void loadMedia() {

        if (mediaPlayer.getMedia() == null) {
            String videoLocation;
            if (TextUtils.isEmpty(streamInfo.getVideoLocation())) {
//            Toast.makeText(getActivity(), "Error loading media", Toast.LENGTH_LONG).show();
//            getActivity().finish();
                return;
            } else {
                videoLocation = streamInfo.getVideoLocation();
                if (!videoLocation.startsWith("file://") && !videoLocation.startsWith(
                        "http://") && !videoLocation.startsWith("https://")) {
                    videoLocation = "file://" + videoLocation;
                }
            }

            int flags = disabledHardwareAcceleration ? VLCOptions.MEDIA_NO_HWACCEL : 0;
            flags = flags | VLCOptions.MEDIA_VIDEO;

            org.videolan.libvlc.Media media = new org.videolan.libvlc.Media(libVLC, Uri.parse(videoLocation));
            VLCOptions.setMediaOptions(media, preferencesHandler, flags);

            mediaPlayer.setMedia(media);

            long resumeFrom = prefManager.get(PREF_RESUME_POSITION, resumePosition);
            if (resumeFrom > 0) {
                mediaPlayer.setTime(resumeFrom);
            }
        }

        videoDuration = mediaPlayer.getLength();
        mediaPlayer.play();
        videoEnded = false;
    }

    protected void disableHardwareAcceleration() {
        disabledHardwareAcceleration = true;
        saveVideoCurrentTime();
    }

    protected long getCurrentTime() {
        return mediaPlayer.getTime();
    }

    protected long getDuration() {
        return videoDuration;
    }

    protected void seek(int delta) {
        if (mediaPlayer.getLength() <= 0 && !seeking) {
            return;
        }

        long position = mediaPlayer.getTime() + delta;
        if (position < 0) {
            position = 0;
        }

        setCurrentTime(position);
        view.showOverlay();
        view.onProgressChanged(getCurrentTime(), getStreamerProgress(), getDuration()); // TODO: 4/2/17 Is this already handled by vlc event?
        lastSubs = null;
    }

    protected void setCurrentTime(long time) {
        if (time / getDuration() * 100 <= getStreamerProgress()) {
            mediaPlayer.setTime(time);
        }
    }

    protected int getStreamerProgress() {
        return streamerProgress;
    }

    private void saveVideoCurrentTime() {
        long currentTime = mediaPlayer.getTime();
        prefManager.save(PREF_RESUME_POSITION, currentTime);
    }

    protected boolean isSeeking() {
        return seeking;
    }

    protected void seekForwardClick() {
        seek(10000);
    }

    protected void seekBackwardClick() {
        seek(-10000);
    }

    /**
     * Is a video currently playing with VLC
     *
     * @return true if video is played using VLC
     */
    protected boolean isPlaying() {
        return mediaPlayer.isPlaying();
    }

    protected void setLastSubtitleCaption(Caption sub) {
        lastSubs = sub;
    }

    protected abstract void startBeamPlayerActivity();

    protected final void progressSubtitleCaption() {
        if (libVLC != null && mediaPlayer != null && mediaPlayer.isPlaying() && subs != null) {
            Collection<Caption> subtitles = subs.captions.values();
            double currentTime = getCurrentTime() - subtitleOffset;
            if (lastSubs != null && currentTime >= lastSubs.start.getMilliseconds() && currentTime <= lastSubs.end.getMilliseconds()) {
                view.showTimedCaptionText(lastSubs);
            } else {
                for (Caption caption : subtitles) {
                    if (currentTime >= caption.start.getMilliseconds() && currentTime <= caption.end.getMilliseconds()) {
                        lastSubs = caption;
                        view.showTimedCaptionText(caption);
                        break;
                    } else if (currentTime > caption.end.getMilliseconds()) {
                        view.showTimedCaptionText(null);
                    }
                }
            }
        }
    }

    private void loadOrDownloadSubtitle() {

        if (media == null) {
            throw new NullPointerException("Media is not available");
        }

        if (currentSubsLang.equals(SubsProvider.SUBTITLE_LANGUAGE_NONE)) {
            return;
        }

        SubtitleDownloader subtitleDownloader = new SubtitleDownloader(providerManager.getCurrentSubsProvider(), streamInfo, playerManager,
                currentSubsLang);
        subtitleDownloader.setSubtitleDownloaderListener(this);

        try {
            subsFile = playerManager.getDownloadedSubtitleFile(media, currentSubsLang);
            if (subsFile != null && subsFile.exists()) {
                subtitleDownloader.parseSubtitle(subsFile);
            }
        } catch (FileNotFoundException e) {
            subtitleDownloader.downloadSubtitle();
        }
    }

    private void handleHardwareAccelerationError() {
        saveVideoCurrentTime();
        mediaPlayer.stop();
        onHardwareAccelerationError();
    }

    private void prepareVlcVout() {
        // TODO: 4/2/17 Check if has to be done in on resume
        final IVLCVout vlcVout = mediaPlayer.getVLCVout();

        if (!vlcVout.areViewsAttached()) {
            view.attachVlcViews(vlcVout);
        }
    }

    private void resumeVideo() {
        long resumePosition = prefManager.get(PREF_RESUME_POSITION, 0);
        videoDuration = mediaPlayer.getLength();
        if (videoDuration > resumePosition && resumePosition > 0) {
            setCurrentTime(resumePosition);
            prefManager.save(PREF_RESUME_POSITION, 0);
        }
    }

    private void endReached() {
        videoEnded = true;
        view.onPlaybackEndReached();
    }

    private final BeamDeviceListener deviceListener = new BeamDeviceListener() {

        @Override
        public void onDeviceReady(ConnectableDevice device) {
            startBeamPlayerActivity();

//            getActivity().finish();
        }
    };

}
