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

package butter.droid.base.ui.player.stream;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import butter.droid.base.R;
import butter.droid.base.content.preferences.PreferencesHandler;
import butter.droid.base.manager.internal.beaming.BeamDeviceListener;
import butter.droid.base.manager.internal.beaming.BeamManager;
import butter.droid.base.manager.internal.provider.ProviderManager;
import butter.droid.base.manager.internal.vlc.PlayerManager;
import butter.droid.base.manager.internal.vlc.VlcPlayer;
import butter.droid.base.manager.prefs.PrefManager;
import butter.droid.base.providers.subs.SubsProvider;
import butter.droid.base.subs.Caption;
import butter.droid.base.subs.SubtitleDownloader;
import butter.droid.base.subs.TimedTextObject;
import butter.droid.base.torrent.StreamInfo;
import butter.droid.base.ui.player.base.BaseVideoPlayerPresenterImpl;
import butter.droid.base.utils.LocaleUtils;
import com.connectsdk.device.ConnectableDevice;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;
import timber.log.Timber;

public abstract class StreamPlayerPresenterImpl extends BaseVideoPlayerPresenterImpl implements StreamPlayerPresenter {

    private final StreamPlayerView view;
    private final Context context;
    private final PreferencesHandler preferencesHandler;
    private final VlcPlayer player;
    private final ProviderManager providerManager;
    private final PlayerManager playerManager;
    private final BeamManager beamManager;

    protected StreamInfo streamInfo;

    private String currentSubsLang = SubsProvider.SUBTITLE_LANGUAGE_NONE;
    private File subsFile;
    private TimedTextObject subs;
    private Caption lastSubs;
    private int subtitleOffset;

    public StreamPlayerPresenterImpl(final StreamPlayerView view, final Context context, final PrefManager prefManager,
            final PreferencesHandler preferencesHandler, final ProviderManager providerManager, final PlayerManager playerManager,
            final BeamManager beamManager, final VlcPlayer player) {
        super(view, context, prefManager, preferencesHandler, providerManager, playerManager, beamManager, player);
        this.view = view;
        this.context = context;
        this.preferencesHandler = preferencesHandler;
        this.player = player;
        this.providerManager = providerManager;
        this.playerManager = playerManager;
        this.beamManager = beamManager;
    }

    @Override public void onCreate(final StreamInfo streamInfo, final long resumePosition) {
        if (streamInfo == null) {
            throw new IllegalStateException("Stream info was not provided");
        }

        super.onCreate(streamInfo.getMedia(), resumePosition);

        this.streamInfo = streamInfo;

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
        super.onResume();

        beamManager.addDeviceListener(deviceListener);

        loadMedia();
    }

    @Override public void onPause() {
        super.onPause();

        beamManager.removeDeviceListener(deviceListener);
    }

    @Override protected void onHardwareAccelerationError() {
        view.onHardwareAccelerationError();
        disableHardwareAcceleration();
    }

    @Override public void onViewCreated() {
        updateSubtitleSize(preferencesHandler.getSubtitleSize());
        view.setupControls(streamInfo.getTitle());
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

    @Override public void onSubtitleDownloadCompleted(final boolean isSuccessful, final TimedTextObject subtitleFile) {
        onSubtitleEnabledStateChanged(isSuccessful);
        subs = subtitleFile;
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

    @Override public void onSubsFileSelected(final File f) {
        if (!f.getPath().endsWith(".srt")) {
//            Snackbar.make(getView(), R.string.unknown_error,
//                    Snackbar.LENGTH_SHORT).show();
            return;
        } else {
            subsFile = f;
        }
    }

    @Override protected void seek(final int delta) {
        super.seek(delta);

        lastSubs = null;
    }

    @Override public void showCustomSubsPicker() {
        view.showSubsFilePicker();
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

    @Override public void onSubsTimingChanged(final int value) {
        subtitleOffset = value;
        view.showTimedCaptionText(null);
    }

    @Override public void onSubsClicked() {
        if (media != null && media.subtitles != null) {
            view.showSubsSelectorDialog();
        }
    }

    protected abstract void startBeamPlayerActivity();

    protected void setLastSubtitleCaption(Caption sub) {
        lastSubs = sub;
    }

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

    private void updateSubtitleSize(final int size) {
        view.updateSubtitleSize(size);
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

    protected void loadMedia() {
        String videoLocation = streamInfo.getVideoLocation();
        if (TextUtils.isEmpty(videoLocation)) {
//            Toast.makeText(getActivity(), "Error loading media", Toast.LENGTH_LONG).show();
//            getActivity().finish();
            return;
        }

        videoLocation = streamInfo.getVideoLocation();
        if (!videoLocation.startsWith("file://") && !videoLocation.startsWith(
                "http://") && !videoLocation.startsWith("https://")) {
            videoLocation = "file://" + videoLocation;
        }

        loadMedia(Uri.parse(videoLocation));
    }

    private final BeamDeviceListener deviceListener = new BeamDeviceListener() {

        @Override
        public void onDeviceReady(ConnectableDevice device) {
            startBeamPlayerActivity();

//            getActivity().finish();
        }
    };

}
