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
import android.support.annotation.Nullable;
import android.text.TextUtils;
import butter.droid.base.R;
import butter.droid.base.content.preferences.PreferencesHandler;
import butter.droid.base.manager.internal.provider.ProviderManager;
import butter.droid.base.manager.internal.subtitle.SubtitleManager;
import butter.droid.base.manager.internal.vlc.PlayerManager;
import butter.droid.base.manager.internal.vlc.VlcPlayer;
import butter.droid.base.providers.media.model.MediaWrapper;
import butter.droid.base.providers.media.model.StreamInfo;
import butter.droid.base.providers.subs.model.SubtitleWrapper;
import butter.droid.base.ui.player.base.BaseVideoPlayerPresenterImpl;
import butter.droid.provider.subs.SubsProvider;
import io.reactivex.MaybeObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import java.io.File;
import timber.log.Timber;

public abstract class StreamPlayerPresenterImpl extends BaseVideoPlayerPresenterImpl implements StreamPlayerPresenter {

    private final StreamPlayerView view;
    private final Context context;
    private final PreferencesHandler preferencesHandler;
    private final VlcPlayer player;
    private final ProviderManager providerManager;
    private final PlayerManager playerManager;
    private final SubtitleManager subtitleManager;

    protected StreamInfo streamInfo;

    @Nullable private Disposable subsDisposable;

//    private String currentSubsLang = SubsProvider.SUBTITLE_LANGUAGE_NONE;
    private File subsFile;
    private int subtitleOffset;
    private int streamerProgress;

    public StreamPlayerPresenterImpl(final StreamPlayerView view, final Context context, final PreferencesHandler preferencesHandler,
            final ProviderManager providerManager, final PlayerManager playerManager, final VlcPlayer player,
            final SubtitleManager subtitleManager) {
        super(view, preferencesHandler, player);
        this.view = view;
        this.context = context;
        this.preferencesHandler = preferencesHandler;
        this.player = player;
        this.providerManager = providerManager;
        this.playerManager = playerManager;
        this.subtitleManager = subtitleManager;
    }

    @Override public void onCreate(final StreamInfo streamInfo, final long resumePosition) {
        if (streamInfo == null) {
            throw new IllegalStateException("Stream info was not provided");
        }

        super.onCreate(resumePosition);

        this.streamInfo = streamInfo;

        loadSubtitle();
    }

    @Override public void onResume() {
        super.onResume();

        loadMedia();
    }

    @Override public void onDestroy() {
        super.onDestroy();

        disposeSubs();
    }

    @Override public void onViewCreated() {
        view.setupControls(streamInfo.getFullTitle());
    }

    @Override public void onSubtitleLanguageSelected(final String language) {
//        if (currentSubsLang != null && (language == null || currentSubsLang.equals(language))) {
//            return;
//        }
//
//        currentSubsLang = language;
////        streamInfo.setSubtitleLanguage(language);
//
//        if (currentSubsLang.equals(SubsProvider.SUBTITLE_LANGUAGE_NONE)) {
//            subs = null;
//            onSubtitleEnabledStateChanged(false);
//            return;
//        }

        // TODO subs
        /*
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
        */

        loadSubtitle();
    }

//    @Override public void onSubtitleDownloadCompleted(final boolean isSuccessful, final TimedTextObject subtitleFile) {
//        onSubtitleEnabledStateChanged(isSuccessful);
//        subs = subtitleFile;
//    }

    @Override public void showSubsLanguageSettings() {
        // TODO subs
/*
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
        */
    }

    @Override public void onSubsFileSelected(final File f) {
        if (!f.getPath().endsWith(".srt")) {
            view.showErrorMessage(R.string.unknown_error);
        } else {
            subsFile = f;
        }
    }

    @Override public void showCustomSubsPicker() {
        view.showSubsFilePicker();
    }

    @Override public void showSubsTimingSettings() {
        view.displaySubsTimingDialog(subtitleOffset);
    }

    @Override public void onSubsTimingChanged(final int value) {
        subtitleOffset = value;
        player.setSubsDelay(value);
    }

    @Override public void onSubsClicked() {
        view.showSubsSelectorDialog();
    }

    @Override public void streamProgressUpdated(final float progress) {
        int newProgress = (int) ((player.getLength() / 100) * progress);
        if (streamerProgress < newProgress) {
            streamerProgress = newProgress;
            view.displayStreamProgress(getStreamerProgress());
        }
    }

    @Override protected void setCurrentTime(final long time) {
        if (time / player.getLength() * 100 <= getStreamerProgress()) {
            super.setCurrentTime(time);
        }
    }

    protected int getStreamerProgress() {
        return streamerProgress;
    }

    private void loadSubtitle() {
        SubtitleWrapper subtitle = streamInfo.getSubtitle();
        MediaWrapper media = streamInfo.getMedia();
        SubsProvider provider = providerManager.getSubsProvider(media.getProviderId());
        subtitleManager.downloadSubtitle(provider, media.getMedia(), subtitle)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new MaybeObserver<SubtitleWrapper>() {
                    @Override public void onSubscribe(final Disposable d) {
                        disposeSubs();
                        subsDisposable = d;
                    }

                    @Override public void onSuccess(final SubtitleWrapper subs) {
                        loadSubs(subs.getFileUri());
                    }

                    @Override public void onError(final Throwable e) {
                        // TODO show error loading subs
                        Timber.d("Error loading subs");
                    }

                    @Override public void onComplete() {
                        Timber.d("Maybe empty");
                    }
                });
    }

    private void disposeSubs() {
        Disposable current = subsDisposable;
        if (current != null) {
            current.dispose();
        }
    }

    private void loadMedia() {
        String videoLocation = streamInfo.getStreamUrl();
        if (TextUtils.isEmpty(videoLocation)) {
            // TODO: 7/29/17 Show error
        //            Toast.makeText(getActivity(), "Error loading media", Toast.LENGTH_LONG).show();
//            getActivity().finish();
        } else {
            if (!videoLocation.startsWith("file://") && !videoLocation.startsWith(
                    "http://") && !videoLocation.startsWith("https://")) {
                videoLocation = "file://" + videoLocation;
            }

            loadMedia(Uri.parse(videoLocation));
        }
    }

}
