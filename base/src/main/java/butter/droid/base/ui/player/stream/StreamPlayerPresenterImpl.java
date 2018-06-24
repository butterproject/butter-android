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

import android.net.Uri;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.io.File;

import butter.droid.base.R;
import butter.droid.base.content.preferences.PreferencesHandler;
import butter.droid.base.manager.internal.provider.ProviderManager;
import butter.droid.base.manager.internal.subtitle.SubtitleManager;
import butter.droid.base.manager.internal.vlc.VlcPlayer;
import butter.droid.base.providers.media.model.MediaWrapper;
import butter.droid.base.providers.media.model.StreamInfo;
import butter.droid.base.providers.subs.model.SubtitleWrapper;
import butter.droid.base.ui.player.base.BaseVideoPlayerPresenterImpl;
import butter.droid.provider.subs.SubsProvider;
import butter.droid.provider.subs.model.Subtitle;
import io.reactivex.MaybeObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import timber.log.Timber;

public abstract class StreamPlayerPresenterImpl extends BaseVideoPlayerPresenterImpl implements StreamPlayerPresenter {

    public static final int PLAYER_ACTION_CC = 0x1;
    public static final int PLAYER_ACTION_PIP = 0x1 << 1;
    public static final int PLAYER_ACTION_SKIP_PREVIOUS = 0x1 << 2;
    public static final int PLAYER_ACTION_SKIP_NEXT = 0x1 << 3;
    public static final int PLAYER_ACTION_SCALE = 0x1 << 4;

    private final StreamPlayerView view;
    private final VlcPlayer player;
    private final ProviderManager providerManager;
    private final SubtitleManager subtitleManager;

    protected StreamInfo streamInfo;

    @Nullable private Disposable subsDisposable;

    // TODO subs from file
    private File subsFile;
    private int subtitleOffset;
    private int streamerProgress;

    public StreamPlayerPresenterImpl(final StreamPlayerView view, final PreferencesHandler preferencesHandler,
            final ProviderManager providerManager, final VlcPlayer player, final SubtitleManager subtitleManager) {
        super(view, preferencesHandler, player);
        this.view = view;
        this.player = player;
        this.providerManager = providerManager;
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
        // TODO subs actions
        view.setupControls(streamInfo.getFullTitle(), getPlayerActions());
    }

    @Override public void showSubsLanguageSettings() {
        // TODO custom subs
        final SubtitleWrapper subtitleWrapper = streamInfo.getSubtitle();
        final Subtitle subtitle;
        if (subtitleWrapper != null) {
            subtitle = subtitleWrapper.getSubtitle();
        } else {
            subtitle = null;
        }

        view.showPickSubsDialog(streamInfo.getMedia(), subtitle);
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
        player.setSubsDelay(value * 10);
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

    protected int getPlayerActions() {
        int actions = PLAYER_ACTION_SCALE;

        if (providerManager.hasSubsProvider(streamInfo.getMedia().getProviderId())) {
            actions |= PLAYER_ACTION_CC;
        }

        return actions;
    }

    protected int getStreamerProgress() {
        return streamerProgress;
    }

    protected void loadSubtitle() {
        final SubtitleWrapper subtitle = streamInfo.getSubtitle();
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
                        streamInfo.setSubtitle(subs);
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
