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

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.TypedValue;
import butter.droid.R;
import butter.droid.base.fragments.dialog.FileSelectorDialogFragment;
import butter.droid.base.fragments.dialog.NumberPickerDialogFragment;
import butter.droid.base.fragments.dialog.StringArraySelectorDialogFragment;
import butter.droid.base.subs.Caption;
import butter.droid.base.torrent.StreamInfo;
import butter.droid.ui.beam.BeamPlayerActivity;
import butter.droid.ui.player.VideoPlayerActivity;
import butter.droid.ui.player.abs.AbsPlayerFragment;
import butter.droid.widget.StrokedRobotoTextView;
import butterknife.BindView;
import butterknife.OnClick;
import com.github.se_bastiaan.torrentstream.StreamStatus;
import com.github.se_bastiaan.torrentstream.Torrent;
import com.github.se_bastiaan.torrentstream.listeners.TorrentListener;
import java.io.File;
import java.util.Arrays;
import javax.inject.Inject;

public class PlayerFragment extends AbsPlayerFragment implements PlayerView, TorrentListener {

    private static final String ARG_STREAM_INFO = "butter.droid.base.ui.player.fragment.BaseVideoPlayerFragment.streamInfo";

    private static final String ACTION_CLOSE_CAPTION = "butter.droid.tv.ui.player.video.action.CLOSE_CAPTION";

    public static final int SUBTITLE_MINIMUM_SIZE = 10;

    @Inject PlayerPresenter presenter;

    @BindView(R.id.subtitle_text) StrokedRobotoTextView subtitleText;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        VideoPlayerActivity activity = (VideoPlayerActivity) getActivity();
        activity.getComponent()
                .playerComponentBuilder()
                .playerModule(new PlayerModule(this, activity))
                .build()
                .inject(this);

        super.onCreate(savedInstanceState);

        StreamInfo streamInfo = getArguments().getParcelable(ARG_STREAM_INFO);
        long resumePosition = getResumePosition(savedInstanceState);

        stateBuilder.addCustomAction(ACTION_CLOSE_CAPTION, getString(R.string.subtitles), R.drawable.ic_av_subs);

        presenter.onCreate(streamInfo, resumePosition);
    }

    @Override public void displayStreamProgress(final int progress) {
        stateBuilder.setBufferedPosition(progress);
        mediaSession.setPlaybackState(stateBuilder.build());
    }

    @Override public void showErrorMessage(@StringRes final int message) {
        Snackbar.make(getView(), message, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void setupSubtitles(@ColorInt final int color, final int size, @ColorInt final int strokeColor, final int strokeWidth) {
        subtitleText.setTextColor(color);
        subtitleText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, size);
        subtitleText.setStrokeColor(strokeColor);
        subtitleText.setStrokeWidth(TypedValue.COMPLEX_UNIT_DIP, strokeWidth);
    }

    @Override public void toggleOverlay() {
        if (overlayVisible) {
            hideOverlay();
        } else {
            showOverlay();
        }
    }

    @Override public void showTimedCaptionText(final Caption text) {
        if (text == null) {
            if (subtitleText.getText().length() > 0) {
                subtitleText.setText("");
            }
            return;
        }
        SpannableStringBuilder styledString = (SpannableStringBuilder) Html.fromHtml(text.content);

        ForegroundColorSpan[] toRemoveSpans = styledString.getSpans(0, styledString.length(), ForegroundColorSpan.class);
        for (ForegroundColorSpan remove : toRemoveSpans) {
            styledString.removeSpan(remove);
        }

        if (!subtitleText.getText().toString().equals(styledString.toString())) {
            subtitleText.setText(styledString);
        }
    }

    @Override public void startBeamPlayerActivity(@NonNull final StreamInfo streamInfo, final long currentTime) {
        getActivity().startActivity(BeamPlayerActivity.getIntent(getActivity(), streamInfo, currentTime));
    }

    @Override public void updateSubtitleSize(int size) {
        subtitleText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, size);
    }

    @OnClick(R.id.subs_button) void onSubsClick() {
        mediaController.getTransportControls().sendCustomAction(ACTION_CLOSE_CAPTION, null);
    }

    @Override public void showSubsSelectorDialog() {
        if (getChildFragmentManager().findFragmentByTag("overlay_fragment") != null) {
            return;
        }

        final String[] subsOptions = {
                getString(R.string.subtitle_language),
                getString(R.string.subtitle_size),
                getString(R.string.subtitle_timing)
        };

        StringArraySelectorDialogFragment.show(getChildFragmentManager(), R.string.subtitle_settings, subsOptions,
                -1,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int position) {
                        switch (position) {
                            case 0:
                                presenter.showSubsLanguageSettings();
                                break;
                            case 1:
                                presenter.showSubsSizeSettings();
                                break;
                            case 2:
                                presenter.showSubsTimingSettings();
                                break;
                            default:
                                throw new IllegalStateException("Unknown position");
                        }
                    }
                });
    }

    @Override public void showPickSubsDialog(final String[] readableNames, final String[] adapterSubtitles, final String currentSubsLang) {
        StringArraySelectorDialogFragment.showSingleChoice(
                getChildFragmentManager(),
                R.string.subtitles,
                readableNames,
                Arrays.asList(adapterSubtitles).indexOf(currentSubsLang),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, int position) {
                        if (position == adapterSubtitles.length - 1) {
                            presenter.showCustomSubsPicker();
                        } else {
                            presenter.onSubtitleLanguageSelected(adapterSubtitles[position]);
                        }
                        dialog.dismiss();
                    }
                });
    }

    @Override public void showSubsFilePicker() {
        FileSelectorDialogFragment.show(getChildFragmentManager(),
                new FileSelectorDialogFragment.Listener() {
                    @Override
                    public void onFileSelected(File file) {
                        presenter.onSubsFileSelected(file);
                        FileSelectorDialogFragment.hide();
                    }
                });
    }

    @Override public void displaySubsSizeDialog() {
        Bundle args = new Bundle();
        args.putString(NumberPickerDialogFragment.TITLE, getString(R.string.subtitle_size));
        args.putInt(NumberPickerDialogFragment.MAX_VALUE, 60);
        args.putInt(NumberPickerDialogFragment.MIN_VALUE, SUBTITLE_MINIMUM_SIZE);
        args.putInt(NumberPickerDialogFragment.DEFAULT_VALUE, 16);

        NumberPickerDialogFragment dialogFragment = new NumberPickerDialogFragment();
        dialogFragment.setArguments(args);
        dialogFragment.setOnResultListener(new NumberPickerDialogFragment.ResultListener() {
            @Override
            public void onNewValue(int value) {
                presenter.onSubsSizeChanged(value);
            }
        });
        dialogFragment.show(getChildFragmentManager(), "overlay_fragment");
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
        dialogFragment.setOnResultListener(new NumberPickerDialogFragment.ResultListener() {
            @Override
            public void onNewValue(int value) {
                presenter.onSubsTimingChanged(value * 60);
            }
        });
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
