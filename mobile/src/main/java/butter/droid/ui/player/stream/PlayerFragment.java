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

package butter.droid.ui.player.stream;

import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import org.butterproject.torrentstream.StreamStatus;
import org.butterproject.torrentstream.Torrent;
import org.butterproject.torrentstream.listeners.TorrentListener;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.FragmentActivity;
import butter.droid.R;
import butter.droid.base.fragments.dialog.FileSelectorDialogFragment;
import butter.droid.base.fragments.dialog.NumberPickerDialogFragment;
import butter.droid.base.fragments.dialog.StringArraySelectorDialogFragment;
import butter.droid.base.providers.media.model.MediaWrapper;
import butter.droid.base.providers.media.model.StreamInfo;
import butter.droid.provider.subs.model.Subtitle;
import butter.droid.ui.beam.BeamPlayerActivity;
import butter.droid.ui.media.detail.dialog.subs.SubsPickerDialog;
import butter.droid.ui.player.abs.AbsPlayerFragment;

public class PlayerFragment extends AbsPlayerFragment implements PlayerView, TorrentListener {

    private static final String ARG_STREAM_INFO = "butter.droid.base.ui.player.fragment.BaseVideoPlayerFragment.streamInfo";

    @Inject PlayerPresenter presenter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        StreamInfo streamInfo = getArguments().getParcelable(ARG_STREAM_INFO);
        long resumePosition = getResumePosition(savedInstanceState);

        presenter.onCreate(streamInfo, resumePosition);
    }

    @Override public void displayStreamProgress(final int progress) {
        stateBuilder.setBufferedPosition(progress);
        mediaSession.setPlaybackState(stateBuilder.build());
    }

    @Override public void showErrorMessage(@StringRes final int message) {
        Snackbar.make(getView(), message, Snackbar.LENGTH_SHORT).show();
    }

    @Override public void toggleOverlay() {
        if (overlayVisible) {
            hideOverlay();
        } else {
            showOverlay();
        }
    }

    @Override public void startBeamPlayerActivity(@NonNull final StreamInfo streamInfo, final long currentTime) {
        FragmentActivity activity = requireActivity();
        activity.startActivity(BeamPlayerActivity.getIntent(activity, streamInfo, currentTime));
    }

    @Override public void showSubsSelectorDialog() {
        if (getChildFragmentManager().findFragmentByTag("overlay_fragment") != null) {
            return;
        }

        final String[] subsOptions = {
                getString(R.string.subtitle_language),
                getString(R.string.subtitle_timing)
        };

        StringArraySelectorDialogFragment.show(getChildFragmentManager(), R.string.subtitle_settings, subsOptions,
                -1,
                (dialog, position) -> {
                    switch (position) {
                        case 0:
                            presenter.showSubsLanguageSettings();
                            break;
                        case 1:
                            presenter.showSubsTimingSettings();
                            break;
                        default:
                            throw new IllegalStateException("Unknown position");
                    }
                });
    }

    @Override public void showPickSubsDialog(MediaWrapper mediaWrapper, @Nullable Subtitle subtitle) {
//        hideDialog();

        SubsPickerDialog dialog = SubsPickerDialog.newInstance(mediaWrapper, subtitle);
        dialog.show(getChildFragmentManager(), "dialog");
//        pickerDialog = dialog;
    }

    @Override public void showSubsFilePicker() {
        FileSelectorDialogFragment.show(getChildFragmentManager(),
                file -> {
                    presenter.onSubsFileSelected(file);
                    FileSelectorDialogFragment.hide();
                });
    }

    @Override public void displaySubsTimingDialog(int subtitleOffset) {
        Bundle args = new Bundle();
        args.putString(NumberPickerDialogFragment.TITLE, getString(R.string.subtitle_timing));
        args.putInt(NumberPickerDialogFragment.MAX_VALUE, 3600);
        args.putInt(NumberPickerDialogFragment.MIN_VALUE, -3600);
        args.putInt(NumberPickerDialogFragment.DEFAULT_VALUE, subtitleOffset / 60);
        args.putBoolean(NumberPickerDialogFragment.FOCUSABLE, true);

        NumberPickerDialogFragment dialogFragment = new NumberPickerDialogFragment();
        dialogFragment.setArguments(args);
        dialogFragment.setOnResultListener(value -> presenter.onSubsTimingChanged(value * 60));
        dialogFragment.show(getChildFragmentManager(), "overlay_fragment");
    }

    @Override public void onStreamPrepared(final Torrent torrent) {
        // nothing to do
    }

    @Override public void onStreamStarted(final Torrent torrent) {
        // nothing to do
    }

    @Override public void onStreamError(final Torrent torrent, final Exception ex) {
        // nothing to do
    }

    @Override public void onStreamReady(final Torrent torrent) {
        // nothing to do
    }

    @Override public void onStreamProgress(final Torrent torrent, final StreamStatus streamStatus) {
        presenter.streamProgressUpdated(streamStatus.progress);
    }

    @Override public void onStreamStopped() {
        // nothing to do
    }

    @Override protected boolean onCustomAction(final String action) {
        switch (action) {
            case ACTION_CLOSE_CAPTION:
                presenter.onSubsClicked();
                return true;
            default:
                return super.onCustomAction(action);
        }

    }

    public static PlayerFragment newInstance(final StreamInfo streamInfo, final long resumePosition) {
        Bundle args = new Bundle(2);
        args.putParcelable(ARG_STREAM_INFO, streamInfo);
        args.putLong(ARG_RESUME_POSITION, resumePosition);

        PlayerFragment fragment = new PlayerFragment();
        fragment.setArguments(args);
        return fragment;
    }

}
