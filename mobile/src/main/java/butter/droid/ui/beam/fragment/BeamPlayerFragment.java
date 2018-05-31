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

package butter.droid.ui.beam.fragment;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import com.google.android.material.snackbar.Snackbar;
import androidx.fragment.app.FragmentActivity;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import org.butterproject.torrentstream.StreamStatus;
import org.butterproject.torrentstream.Torrent;
import org.butterproject.torrentstream.listeners.TorrentListener;

import javax.inject.Inject;

import butter.droid.R;
import butter.droid.base.manager.internal.beaming.BeamPlayerNotificationService;
import butter.droid.base.manager.internal.glide.GlideApp;
import butter.droid.base.providers.media.model.StreamInfo;
import butter.droid.base.utils.PixelUtils;
import butter.droid.base.utils.VersionUtils;
import butter.droid.ui.beam.fragment.dialog.LoadingBeamingDialogFragment;
import butter.droid.ui.player.dialog.OptionDialogFragment;
import butter.droid.widget.SeekBar;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import dagger.android.support.DaggerFragment;

public class BeamPlayerFragment extends DaggerFragment implements BeamPlayerView, TorrentListener {

    private static final String ARG_STREAM_INFO = "butter.droid.ui.beam.fragment.BeamPlayerFragment.streamInfo";
    private static final String ARG_RESUME_POSITION = "butter.droid.ui.beam.fragment.BeamPlayerFragment.resumePosition";

    @Inject BeamPlayerPresenter presenter;

    private Float downloadProgress = 0f;
    private LoadingBeamingDialogFragment loadingDialog;

    private View rootView;
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.sliding_layout) SlidingUpPanelLayout panel;
    @BindView(R.id.play_button) ImageButton playButton;
    @BindView(R.id.cover_image) ImageView coverImage;
    @BindView(R.id.seekbar) SeekBar seekBar;
    @BindView(R.id.volumebar) SeekBar volumeBar;

    private boolean isUserSeeking = false;

    @Override public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        loadingDialog = LoadingBeamingDialogFragment.newInstance();
        loadingDialog.setOnCancelListener(dialogInterface -> presenter.closePlayer());
        loadingDialog.show(getChildFragmentManager(), "overlay_fragment");

        Bundle arguments = getArguments();
        StreamInfo streamInfo = arguments.getParcelable(ARG_STREAM_INFO);
        long resumePosition = getArguments().getLong(ARG_RESUME_POSITION);
        presenter.onCreate(streamInfo, resumePosition);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return rootView = inflater.inflate(R.layout.fragment_beamplayer, container, false);
    }

    @Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        setupToolbar((AppCompatActivity) getActivity());

        seekBar.setOnSeekBarChangeListener(seekBarChangeListener);
        volumeBar.setOnSeekBarChangeListener(volumeBarChangeListener);

        presenter.onViewCreated();
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

        Intent intent = new Intent(getActivity(), BeamPlayerNotificationService.class);
        getActivity().stopService(intent);
    }

    @OnClick(R.id.play_button) public void playPauseClick() {
        presenter.playPauseClicked();
    }

    @OnClick(R.id.forward_button) public void forwardClick() {
        presenter.forwardClicked();
    }

    @OnClick(R.id.backward_button) public void backwardClick() {
        presenter.backwardClicked();
    }

    @Override
    public void onStreamStarted(Torrent torrent) {
        // nothing to do
    }

    @Override
    public void onStreamPrepared(Torrent torrent) {
        // nothing to do
    }

    @Override
    public void onStreamError(Torrent torrent, Exception exception) {
        // nothing to do
    }

    @Override
    public void onStreamReady(Torrent torrent) {
        // nothing to do
    }

    @Override
    public void onStreamProgress(Torrent torrent, StreamStatus status) {
        downloadProgress = seekBar.getMax() / 100 * status.progress;
        seekBar.setSecondaryProgress(0); // hack to make the secondary progress appear on Android 5.0
        seekBar.setSecondaryProgress(downloadProgress.intValue());
    }

    @Override
    public void onStreamStopped() {
        // nothing to do
    }

    @Override public void tintProgress(@ColorInt int paletteColor) {
        if (paletteColor == Color.TRANSPARENT) {
            paletteColor = ContextCompat.getColor(getContext(), R.color.primary);
        }

        tintProgressDrawable(paletteColor);
        tintVolumeDrawable(paletteColor);

        playButton.setBackground(PixelUtils.changeDrawableColor(playButton.getContext(), R.drawable.play_button_circle, paletteColor));
    }

    @Override public void loadCoverImage(@NonNull final String imageUrl) {
        GlideApp.with(this)
                .asDrawable()
                .load(imageUrl)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(coverImage);
    }

    @Override public void hideSeekBar() {
        seekBar.setVisibility(View.INVISIBLE);
    }

    @Override public void disableVolumePanel() {
        panel.setEnabled(false);
        panel.setTouchEnabled(false);
    }

    @Override public void disablePlayButton() {
        playButton.setEnabled(false);
    }

    @Override public void showErrorMessage(@StringRes int errorMessage) {
        Snackbar.make(rootView, errorMessage, Snackbar.LENGTH_SHORT).show();
    }

    @Override public void closeScreen() {
        getActivity().finish();
    }

    @Override public void setVolume(final int volume) {
        volumeBar.setProgress(volume);
    }

    @Override public void displayProgress(final int progress) {
        if (!isUserSeeking) {
            seekBar.setProgress(progress);
            seekBar.setSecondaryProgress(0); // hack to make the secondary progress appear on Android 5.0
            seekBar.setSecondaryProgress(downloadProgress.intValue());
        }
    }

    @Override public void setDuration(final int duration) {
        seekBar.setMax(duration);
    }

    @Override public void hideLoadingDialog() {
        if (loadingDialog.isVisible()) {
            loadingDialog.dismiss();
        }
    }

    @Override public void showBeamFailedDialog() {
        OptionDialogFragment
                .show(getActivity(), getChildFragmentManager(), R.string.unknown_error, R.string.beaming_failed, android.R.string.yes,
                        android.R.string.no, new OptionDialogFragment.Listener() {
                            @Override
                            public void onSelectionPositive() {
                                presenter.beamVideo();
                            }

                            @Override
                            public void onSelectionNegative() {
                                presenter.closePlayer();
                            }
                        });
    }

    @Override public void updatePlayButton(@DrawableRes final int icon, @StringRes final int cd) {
        playButton.setImageResource(icon);
        playButton.setContentDescription(getString(cd));
    }

    @Override public void startNotificationService(final boolean isPlaying) {
        FragmentActivity activity = requireActivity();
        activity.startService(BeamPlayerNotificationService.getIntent(activity, isPlaying));
    }

    private void tintProgressDrawable(@ColorInt final int paletteColor) {

        LayerDrawable progressDrawable;

        if (!VersionUtils.isLollipop()) {
            progressDrawable = (LayerDrawable) ContextCompat.getDrawable(getContext(), R.drawable.scrubber_progress_horizontal_bigtrack);
        } else {
            progressDrawable = (LayerDrawable) ContextCompat.getDrawable(getContext(), R.drawable.progress_horizontal_material);
        }

        progressDrawable.findDrawableByLayerId(android.R.id.background)
                .setColorFilter(ContextCompat.getColor(getContext(), R.color.beamplayer_seekbar_track), PorterDuff.Mode.SRC_IN);
        progressDrawable.findDrawableByLayerId(android.R.id.progress)
                .setColorFilter(paletteColor, PorterDuff.Mode.SRC_IN);
        progressDrawable.findDrawableByLayerId(android.R.id.secondaryProgress)
                .setColorFilter(paletteColor, PorterDuff.Mode.SRC_IN);

        seekBar.setProgressDrawable(progressDrawable);
        seekBar.getThumbDrawable().setColorFilter(paletteColor, PorterDuff.Mode.SRC_IN);

    }

    private void tintVolumeDrawable(@ColorInt final int paletteColor) {
        LayerDrawable volumeDrawable;

        if (!VersionUtils.isLollipop()) {
            volumeDrawable = (LayerDrawable) getResources().getDrawable(R.drawable.scrubber_progress_horizontal);
        } else {
            if (volumeBar.getProgressDrawable() instanceof StateListDrawable) {
                StateListDrawable stateListDrawable = (StateListDrawable) volumeBar.getProgressDrawable();
                volumeDrawable = (LayerDrawable) stateListDrawable.getCurrent();
            } else {
                volumeDrawable = (LayerDrawable) volumeBar.getProgressDrawable();
            }
        }

        volumeDrawable.findDrawableByLayerId(android.R.id.background)
                .setColorFilter(ContextCompat.getColor(getContext(), R.color.beamplayer_seekbar_track), PorterDuff.Mode.SRC_IN);
        volumeDrawable.findDrawableByLayerId(android.R.id.progress).setColorFilter(paletteColor, PorterDuff.Mode.SRC_IN);
        volumeDrawable.findDrawableByLayerId(android.R.id.secondaryProgress).setColorFilter(paletteColor, PorterDuff.Mode.SRC_IN);

        volumeBar.setProgressDrawable(volumeDrawable);
        volumeBar.getThumbDrawable().setColorFilter(paletteColor, PorterDuff.Mode.SRC_IN);
    }

    private void setupToolbar(AppCompatActivity activity) {
        activity.setSupportActionBar(toolbar);
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        activity.getSupportActionBar().setTitle("");

        toolbar.getBackground().setAlpha(0);
        toolbar.setNavigationIcon(R.drawable.abc_ic_clear_material);
    }

    public static BeamPlayerFragment newInstance(StreamInfo info, long resumePosition) {
        Bundle args = new Bundle(2);
        args.putParcelable(ARG_STREAM_INFO, info);
        args.putLong(ARG_RESUME_POSITION, resumePosition);

        BeamPlayerFragment fragment = new BeamPlayerFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private final SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(android.widget.SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser && progress <= downloadProgress && isUserSeeking) {
                if (progress <= downloadProgress) {
                    presenter.seek(progress);
                }
            }
        }

        @Override
        public void onStartTrackingTouch(android.widget.SeekBar seekBar) {
            isUserSeeking = true;
        }

        @Override
        public void onStopTrackingTouch(android.widget.SeekBar seekBar) {
            isUserSeeking = false;
        }
    };

    public final SeekBar.OnSeekBarChangeListener volumeBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onStopTrackingTouch(android.widget.SeekBar seekBar) {
        }

        @Override
        public void onStartTrackingTouch(android.widget.SeekBar seekBar) {
        }

        @Override
        public void onProgressChanged(android.widget.SeekBar seekBar, int position, boolean fromUser) {
            if (fromUser) {
                presenter.onUserVolumeChanged(volumeBar.getProgress());
            }
        }
    };

}
