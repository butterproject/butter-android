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

import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SurfaceView;
import butter.droid.base.R;
import butter.droid.base.R2;
import butter.droid.base.fragments.dialog.FileSelectorDialogFragment;
import butter.droid.base.fragments.dialog.NumberPickerDialogFragment;
import butter.droid.base.fragments.dialog.StringArraySelectorDialogFragment;
import butter.droid.base.manager.internal.vlc.VlcPlayer;
import butter.droid.base.torrent.StreamInfo;
import com.github.se_bastiaan.torrentstream.StreamStatus;
import com.github.se_bastiaan.torrentstream.Torrent;
import com.github.se_bastiaan.torrentstream.listeners.TorrentListener;
import java.io.File;
import java.util.Arrays;
import javax.inject.Inject;

public abstract class BaseVideoPlayerFragment extends Fragment implements BaseVideoPlayerView, TorrentListener {

    private static final String ARG_STREAM_INFO = "butter.droid.base.ui.player.fragment.BaseVideoPlayerFragment.streamInfo";
    private static final String ARG_RESUME_POSITION = "butter.droid.base.ui.player.fragment.BaseVideoPlayerFragment.resumePosition";

    public static final int SUBTITLE_MINIMUM_SIZE = 10;

    @Inject BaseVideoPlayerPresenter presenter;
    @Inject VlcPlayer player;

    protected boolean mShowReload = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);

        StreamInfo streamInfo = getArguments().getParcelable(ARG_STREAM_INFO);
        long resumePosition = getArguments().getLong(ARG_RESUME_POSITION);

        presenter.onCreate(streamInfo, resumePosition);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setProgressVisible(true);
    }

    @Override
    public void onResume() {
        super.onResume();

        presenter.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();

        presenter.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        presenter.onDestroy();
    }

    @Override public void attachVlcViews() {
        player.attachToSurface(getVideoSurface());
    }

    @Override public void setKeepScreenOn(final boolean keep) {
        getVideoSurface().getHolder().setKeepScreenOn(keep);
    }

    protected abstract void showPlayerInfo(String info);

    protected abstract SurfaceView getVideoSurface();

    @Override
    public void onStreamPrepared(Torrent torrent) {
    }

    @Override
    public void onStreamStarted(Torrent torrent) {
    }

    @Override
    public void onStreamStopped() {
    }

    @Override
    public void onStreamError(Torrent torrent, Exception e) {
    }

    @Override
    public void onStreamReady(Torrent torrent) {
    }

    @Override
    public final void onStreamProgress(Torrent torrent, StreamStatus streamStatus) {
        presenter.streamProgressUpdated(streamStatus.progress);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.activity_player, menu);
        menu.findItem(R.id.action_reload).setVisible(mShowReload);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R2.id.action_reload:
                presenter.reloadMedia();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override public void clearFrame() {
        Canvas canvas = new Canvas();
        canvas.drawColor(0, PorterDuff.Mode.CLEAR);
        getVideoSurface().draw(canvas);
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
                    public void onFileSelected(File f) {
                        presenter.onSubsFileSelected(f);
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

    protected static Bundle newInstanceArgs(final StreamInfo streamInfo, final long resumePosition) {
        Bundle args = new Bundle(2);
        args.putParcelable(ARG_STREAM_INFO, streamInfo);
        args.putLong(ARG_RESUME_POSITION, resumePosition);
        return args;
    }

}
