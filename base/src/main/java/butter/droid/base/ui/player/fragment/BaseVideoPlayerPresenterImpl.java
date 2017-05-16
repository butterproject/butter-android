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
import static butter.droid.base.manager.internal.vlc.VLCOptions.HW_ACCELERATION_DISABLED;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.CallSuper;
import android.text.TextUtils;
import butter.droid.base.R;
import butter.droid.base.content.preferences.PreferencesHandler;
import butter.droid.base.manager.internal.beaming.BeamDeviceListener;
import butter.droid.base.manager.internal.beaming.BeamManager;
import butter.droid.base.manager.internal.provider.ProviderManager;
import butter.droid.base.manager.internal.vlc.PlayerManager;
import butter.droid.base.manager.internal.vlc.VlcPlayer;
import butter.droid.base.manager.internal.vlc.VlcPlayer.PlayerCallback;
import butter.droid.base.manager.prefs.PrefManager;
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
import timber.log.Timber;

public abstract class BaseVideoPlayerPresenterImpl implements BaseVideoPlayerPresenter, ISubtitleDownloaderListener, PlayerCallback {

    private static final String PREF_RESUME_POSITION = "resume_position";

    private final BaseVideoPlayerView view;
    private final Context context;
    private final PrefManager prefManager;
    private final PreferencesHandler preferencesHandler;
    private final ProviderManager providerManager;
    private final PlayerManager playerManager;
    private final BeamManager beamManager;
    private final VlcPlayer player;

    protected StreamInfo streamInfo;
    private long resumePosition;
    protected Media media;

    private String currentSubsLang = SubsProvider.SUBTITLE_LANGUAGE_NONE;
    private File subsFile;
    private TimedTextObject subs;
    private Caption lastSubs;

    // probably required when hardware acceleration selection during playback is implemented
    private boolean disabledHardwareAcceleration;

    private int streamerProgress;
    private boolean videoEnded;
    private int subtitleOffset;

    public BaseVideoPlayerPresenterImpl(final BaseVideoPlayerView view, final Context context, final PrefManager prefManager,
            final PreferencesHandler preferencesHandler, final ProviderManager providerManager, final PlayerManager playerManager,
            final BeamManager beamManager, final VlcPlayer player) {
        this.view = view;
        this.context = context;
        this.prefManager = prefManager;
        this.preferencesHandler = preferencesHandler;
        this.providerManager = providerManager;
        this.playerManager = playerManager;
        this.beamManager = beamManager;
        this.player = player;
    }

    @Override public void onCreate(final StreamInfo streamInfo, final long resumePosition) {

        if (streamInfo == null) {
            throw new IllegalStateException("Stream info was not provided");
        }

        if (!player.initialize()) {
            // TODO: 4/2/17 Stop activity & maybe show error
            return;
        }

        player.setCallback(this);

        this.streamInfo = streamInfo;
        this.resumePosition = resumePosition;
        this.media = streamInfo.getMedia();

        prefManager.save(PREF_RESUME_POSITION, resumePosition);

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

    }

    @Override public void onResume() {
        beamManager.addDeviceListener(deviceListener);

        prepareVlcVout();
        loadMedia();
    }

    @CallSuper @Override public void onViewCreated() {
        updateSubtitleSize(preferencesHandler.getSubtitleSize());

        view.setupControls(streamInfo);
    }

    @Override public void onPause() {
        saveVideoCurrentTime();
        player.stop();

        player.detachFromSurface();
        view.detachMediaSession();

        beamManager.removeDeviceListener(deviceListener);
    }

    @Override public void onDestroy() {
        prefManager.save(PREF_RESUME_POSITION, 0);

        player.release();
        player.setCallback(null);
    }

    @Override public void play() {
        player.play();
    }

    @Override public void pause() {
        player.pause();
    }

    @Override public void onSubtitleDownloadCompleted(final boolean isSuccessful, final TimedTextObject subtitleFile) {
        onSubtitleEnabledStateChanged(isSuccessful);
        subs = subtitleFile;
    }

    @Override public void streamProgressUpdated(final float progress) {
        int newProgress = (int) ((player.getLength() / 100) * progress);
        if (streamerProgress < newProgress) {
            streamerProgress = newProgress;
        }
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
        int currentPolicy = player.getSizePolicy();
        if (currentPolicy < SURFACE_ORIGINAL) {
            currentPolicy++;
        } else {
            currentPolicy = SURFACE_BEST_FIT;
        }

        player.setSizePolicy(currentPolicy);
        view.showOverlay();
    }

    @Override public void onSubsClicked() {
        if (media != null && media.subtitles != null) {
            view.showSubsSelectorDialog();
        }
    }

    @Override public void seekForwardClick() {
        seek(10000);
    }

    @Override public void seekBackwardClick() {
        seek(-10000);
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

//        if (mediaPlayer.getMedia() == null) {
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

            int ha;
            if (disabledHardwareAcceleration) {
                ha = HW_ACCELERATION_DISABLED;
            } else {
                ha = preferencesHandler.getHwAcceleration();
            }

            player.loadMedia(Uri.parse(videoLocation), ha);

            long resumeFrom = prefManager.get(PREF_RESUME_POSITION, resumePosition);
            if (resumeFrom > 0) {
                player.setTime(resumeFrom);
            }
//        }

//        videoDuration = mediaPlayer.getLength();
        player.play();
        videoEnded = false;
    }

    protected void disableHardwareAcceleration() {
        disabledHardwareAcceleration = true;
        saveVideoCurrentTime();
    }

    protected long getCurrentTime() {
        return player.getTime();
    }

    protected void seek(int delta) {
        if (player.getLength() <= 0) {
            return;
        }

        long position = player.getTime() + delta;
        if (position < 0) {
            position = 0;
        }

        setCurrentTime(position);
        view.showOverlay();
//        view.onProgressChanged(getCurrentTime(), getStreamerProgress(), player.getLength()); // TODO: 4/2/17 Is this already handled by vlc event?
        lastSubs = null;
    }

    protected void setCurrentTime(long time) {
        if (time / player.getLength() * 100 <= getStreamerProgress()) {
            player.setTime(time);
        }
    }

    protected int getStreamerProgress() {
        return streamerProgress;
    }

    private void saveVideoCurrentTime() {
        long currentTime = player.getTime();
        prefManager.save(PREF_RESUME_POSITION, currentTime);
    }

    /**
     * Is a video currently playing with VLC
     *
     * @return true if video is played using VLC
     */
    protected boolean isPlaying() {
        return player.isPlaying();
    }

    protected void setLastSubtitleCaption(Caption sub) {
        lastSubs = sub;
    }

    protected abstract void startBeamPlayerActivity();

    protected final void progressSubtitleCaption() {
        if (player.isPlaying() && subs != null) {
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
        player.stop();
        onHardwareAccelerationError();
    }

    private void prepareVlcVout() {
       view.attachVlcViews();
    }

    private void resumeVideo() {
        long resumePosition = prefManager.get(PREF_RESUME_POSITION, 0);
        if (player.getLength() > resumePosition && resumePosition > 0) {
            setCurrentTime(resumePosition);
            prefManager.save(PREF_RESUME_POSITION, 0);
        }
    }

    @Override public void updateSurfaceSize(final int width, final int height) {
        view.updateSurfaceSize(width, height);
    }

    @Override public void progressChanged(final long progress) {
        updateControls();
    }

    @Override public void playing() {
        updateControls();
    }

    @Override public void paused() {
        updateControls();
    }

    @Override public void stopped() {

    }

    private void updateControls() {
        view.updateControlsState(player.isPlaying(), getCurrentTime(), getStreamerProgress(), player.getLength());
    }

//            switch (event.type) {
//        case MediaPlayer.Event.Playing:
//            videoDuration = player.getLength();
//            view.setKeepScreenOn(true);
//            resumeVideo();
//            view.setProgressVisible(false);
//            view.showOverlay();
//            view.updatePlayPauseState(true);
//            break;
//        case MediaPlayer.Event.Paused:
//            view.setKeepScreenOn(false);
//            saveVideoCurrentTime();
//            view.updatePlayPauseState(false);
//            break;
//        case MediaPlayer.Event.Stopped:
//            view.setKeepScreenOn(false);
//            view.updatePlayPauseState(false);
//            break;
//        case MediaPlayer.Event.EndReached:
//            endReached();
//            view.updatePlayPauseState(false);
//            break;
//        case MediaPlayer.Event.EncounteredError:
//            view.onErrorEncountered();
//            view.updatePlayPauseState(false);
//            break;
//        case MediaPlayer.Event.Opening:
//            view.setProgressVisible(true);
//            videoDuration = player.getLength();
////                mediaPlayer.play(); // should be handled by auto plau
//            break;
//        case MediaPlayer.Event.TimeChanged:
//        case MediaPlayer.Event.PositionChanged:
//            view.onProgressChanged(getCurrentTime(), getStreamerProgress(), getDuration());
//            progressSubtitleCaption();
//            break;

    @Override public void endReached() {
        videoEnded = true;
        view.onPlaybackEndReached();
    }

    @Override public void playerError() {
        handleHardwareAccelerationError();
    }

    private final BeamDeviceListener deviceListener = new BeamDeviceListener() {

        @Override
        public void onDeviceReady(ConnectableDevice device) {
            startBeamPlayerActivity();

//            getActivity().finish();
        }
    };

}
