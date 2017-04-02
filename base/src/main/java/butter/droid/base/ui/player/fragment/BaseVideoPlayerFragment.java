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

import static butter.droid.base.ui.player.fragment.BaseVideoPlayerPresenter.SURFACE_16_9;
import static butter.droid.base.ui.player.fragment.BaseVideoPlayerPresenter.SURFACE_4_3;
import static butter.droid.base.ui.player.fragment.BaseVideoPlayerPresenter.SURFACE_BEST_FIT;
import static butter.droid.base.ui.player.fragment.BaseVideoPlayerPresenter.SURFACE_FILL;
import static butter.droid.base.ui.player.fragment.BaseVideoPlayerPresenter.SURFACE_FIT_HORIZONTAL;
import static butter.droid.base.ui.player.fragment.BaseVideoPlayerPresenter.SURFACE_FIT_VERTICAL;
import static butter.droid.base.ui.player.fragment.BaseVideoPlayerPresenter.SURFACE_ORIGINAL;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;
import butter.droid.base.R;
import butter.droid.base.R2;
import butter.droid.base.fragments.dialog.FileSelectorDialogFragment;
import butter.droid.base.fragments.dialog.NumberPickerDialogFragment;
import butter.droid.base.fragments.dialog.StringArraySelectorDialogFragment;
import butter.droid.base.torrent.StreamInfo;
import butter.droid.base.torrent.TorrentService;
import butter.droid.base.ui.player.fragment.BaseVideoPlayerPresenter.Surface;
import com.github.sv244.torrentstream.StreamStatus;
import com.github.sv244.torrentstream.Torrent;
import com.github.sv244.torrentstream.listeners.TorrentListener;
import java.io.File;
import java.util.Arrays;
import javax.inject.Inject;
import org.videolan.libvlc.IVLCVout;
import timber.log.Timber;

public abstract class BaseVideoPlayerFragment extends Fragment implements BaseVideoPlayerView, IVLCVout.Callback, TorrentListener {

    private static final String ARG_STREAM_INFO = "butter.droid.base.ui.player.fragment.BaseVideoPlayerFragment.streamInfo";
    private static final String ARG_RESUME_POSITION = "butter.droid.base.ui.player.fragment.BaseVideoPlayerFragment.resumePosition";

    public static final int SUBTITLE_MINIMUM_SIZE = 10;

    @Inject BaseVideoPlayerPresenter presenter;

    protected boolean mShowReload = false;

    private int mVideoHeight;
    private int mVideoWidth;
    private int mVideoVisibleHeight;
    private int mVideoVisibleWidth;
    private int mSarNum;
    private int mSarDen;

    protected Callback callback;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof Callback) {
            callback = (Callback) context;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);
        setProgressVisible(true);

        StreamInfo streamInfo = getArguments().getParcelable(ARG_STREAM_INFO);
        long resumePosition = getArguments().getLong(ARG_RESUME_POSITION);

        presenter.onCreate(streamInfo, resumePosition);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (callback.getService() != null) {
            callback.getService().addListener(this);
        }
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

        if (callback.getService() != null) {
            callback.getService().removeListener(this);
        }
    }

    @Override public void attachVlcViews(@NonNull final IVLCVout vlcVout) {
        vlcVout.setVideoView(getVideoSurface());
        vlcVout.addCallback(this);
        vlcVout.attachViews();
    }

    @Override public void detachVlcViews(@NonNull final IVLCVout vlcout) {
        vlcout.removeCallback(this);
        vlcout.detachViews();
    }

    @Override public void setKeepScreenOn(final boolean keep) {
        getVideoSurface().getHolder().setKeepScreenOn(keep);
    }

    protected abstract void showPlayerInfo(String info);

    protected abstract SurfaceView getVideoSurface();

    @SuppressWarnings("SuspiciousNameCombination") @Override
    public void changeSurfaceSize(final IVLCVout vlcVout, @Surface final int currentSize, final boolean message) {
        int screenWidth = getActivity().getWindow().getDecorView().getWidth();
        int screenHeight = getActivity().getWindow().getDecorView().getHeight();

        vlcVout.setWindowSize(screenWidth, screenHeight);

        double displayWidth = screenWidth;
        double displayHeight = screenHeight;

        if (screenWidth < screenHeight) {
            displayWidth = screenHeight;
            displayHeight = screenWidth;
        }

        // sanity check
        if (displayWidth * displayHeight <= 1 || mVideoWidth * mVideoHeight <= 1) {
            Timber.e("Invalid surface size");
            onErrorEncountered();
            return;
        }

        // compute the aspect ratio
        double aspectRatio, visibleWidth;
        if (mSarDen == mSarNum) {
            /* No indication about the density, assuming 1:1 */
            visibleWidth = mVideoVisibleWidth;
            aspectRatio = (double) mVideoVisibleWidth / (double) mVideoVisibleHeight;
        } else {
            /* Use the specified aspect ratio */
            visibleWidth = mVideoVisibleWidth * (double) mSarNum / mSarDen;
            aspectRatio = visibleWidth / mVideoVisibleHeight;
        }

        // compute the display aspect ratio
        double displayAspectRatio = displayWidth / displayHeight;

        switch (currentSize) {
            case SURFACE_BEST_FIT:
                if (message) {
                    showPlayerInfo(getString(R.string.best_fit));
                }
                if (displayAspectRatio < aspectRatio) {
                    displayHeight = displayWidth / aspectRatio;
                } else {
                    displayWidth = displayHeight * aspectRatio;
                }
                break;
            case SURFACE_FIT_HORIZONTAL:
                displayHeight = displayWidth / aspectRatio;
                if (message) {
                    showPlayerInfo(getString(R.string.fit_horizontal));
                }
                break;
            case SURFACE_FIT_VERTICAL:
                displayWidth = displayHeight * aspectRatio;
                if (message) {
                    showPlayerInfo(getString(R.string.fit_vertical));
                }
                break;
            case SURFACE_FILL:
                if (message) {
                    showPlayerInfo(getString(R.string.fill));
                }
                break;
            case SURFACE_16_9:
                if (message) {
                    showPlayerInfo("16:9");
                }
                aspectRatio = 16.0 / 9.0;
                if (displayAspectRatio < aspectRatio) {
                    displayHeight = displayWidth / aspectRatio;
                } else {
                    displayWidth = displayHeight * aspectRatio;
                }
                break;
            case SURFACE_4_3:
                if (message) {
                    showPlayerInfo("4:3");
                }
                aspectRatio = 4.0 / 3.0;
                if (displayAspectRatio < aspectRatio) {
                    displayHeight = displayWidth / aspectRatio;
                } else {
                    displayWidth = displayHeight * aspectRatio;
                }
                break;
            case SURFACE_ORIGINAL:
                if (message) {
                    showPlayerInfo(getString(R.string.original_size));
                }
                displayHeight = mVideoVisibleHeight;
                displayWidth = visibleWidth;
                break;
        }

        // set display size
        int finalWidth = (int) Math.ceil(displayWidth * mVideoWidth / mVideoVisibleWidth);
        int finalHeight = (int) Math.ceil(displayHeight * mVideoHeight / mVideoVisibleHeight);

        SurfaceHolder holder = getVideoSurface().getHolder();
        holder.setFixedSize(finalWidth, finalHeight);

        ViewGroup.LayoutParams lp = getVideoSurface().getLayoutParams();
        lp.width = finalWidth;
        lp.height = finalHeight;
        getVideoSurface().setLayoutParams(lp);
        getVideoSurface().invalidate();
    }

    /**
     * This callback is called when the native vout call request a new Layout.
     *
     * @param vlcVout vlcVout
     * @param width Frame width
     * @param height Frame height
     * @param visibleWidth Visible frame width
     * @param visibleHeight Visible frame height
     * @param sarNum Surface aspect ratio numerator
     * @param sarDen Surface aspect ratio denominator
     */
    @Override
    public void onNewLayout(IVLCVout vlcVout, int width, int height, int visibleWidth, int visibleHeight, int sarNum, int sarDen) {
        if (width * height <= 0) {
            return;
        }

        // store video size
        mVideoWidth = width;
        mVideoHeight = height;
        mVideoVisibleWidth = visibleWidth;
        mVideoVisibleHeight = visibleHeight;
        mSarNum = sarNum;
        mSarDen = sarDen;

        presenter.vlcNewLayout();

    }

    @Override
    public void onSurfacesCreated(IVLCVout ivlcVout) {
    }

    @Override
    public void onSurfacesDestroyed(IVLCVout ivlcVout) {
    }

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

    public interface Callback {
        TorrentService getService();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.activity_player, menu);
        menu.findItem(R.id.action_reload).setVisible(mShowReload);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        switch (item.getItemId()) {
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
