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

package butter.droid.ui.media.detail.streamable;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.FloatingActionButton;
import android.text.Layout;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;

import javax.inject.Inject;

import butter.droid.R;
import butter.droid.base.manager.internal.glide.GlideApp;
import butter.droid.base.providers.media.model.MediaWrapper;
import butter.droid.base.torrent.Magnet;
import butter.droid.base.torrent.TorrentHealth;
import butter.droid.provider.base.model.Media;
import butter.droid.provider.base.model.Movie;
import butter.droid.provider.base.model.Torrent;
import butter.droid.provider.subs.model.Subtitle;
import butter.droid.ui.media.detail.dialog.quality.QualityPickerDialog;
import butter.droid.ui.media.detail.dialog.quality.QualityPickerDialog.QualityPickerCallback;
import butter.droid.ui.media.detail.dialog.quality.model.UiQuality;
import butter.droid.ui.media.detail.dialog.subs.SubsPickerDialog;
import butter.droid.ui.media.detail.streamable.dialog.SynopsisDialogFragment;
import butter.droid.widget.OptionPreview;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Optional;
import dagger.android.support.DaggerFragment;

public class StreamableDetailFragment extends DaggerFragment implements StreamableDetailView,
        QualityPickerCallback {

    private static final String EXTRA_MOVIE = "butter.droid.ui.media.detail.movie.StreamableDetailFragment.movie";

    @Inject StreamableDetailPresenter presenter;

    private Magnet magnet;

    @BindView(R.id.fab) @Nullable FloatingActionButton fab;
    @BindView(R.id.title) TextView title;
    @BindView(R.id.health) ImageView health;
    @BindView(R.id.meta) TextView meta;
    @BindView(R.id.synopsis) TextView synopsis;
    @BindView(R.id.read_more) Button readMore;
    @BindView(R.id.watch_trailer) Button watchTrailer;
    @BindView(R.id.magnet) ImageButton openMagnet;
    @BindView(R.id.rating) RatingBar rating;
    @BindView(R.id.subtitles) OptionPreview subtitlesPreview;
    @BindView(R.id.quality) OptionPreview qualityPreview;
    @Nullable @BindView(R.id.cover_image) ImageView coverImage;

    private BottomSheetDialogFragment pickerDialog;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_moviedetail, container, false);
    }

    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ButterKnife.bind(this, view);

        subtitlesPreview.setOnClickListener(v -> presenter.onSubtitlesClicked());
        qualityPreview.setOnClickListener(v -> presenter.onQualityClicked());

        MediaWrapper movie = getArguments().getParcelable(EXTRA_MOVIE);
        presenter.onCreate(movie);
    }

    @Override public void onDestroy() {
        presenter.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override public void renderHealth(Torrent torrent) {
        int seeds = torrent.getSeeds();
        int peers = torrent.getPeers();
        if (seeds >= 0 && peers >= 0) {
            TorrentHealth health = TorrentHealth.calculate(seeds, peers);
            this.health.setImageResource(health.getImageResource());
            this.health.setVisibility(View.VISIBLE);
        } else {
            health.setVisibility(View.GONE);
        }
    }

    @Override public void updateMagnet(Torrent torrent) {
        if (magnet == null) {
            magnet = new Magnet(getContext(), torrent.getUrl());
        } else {
            magnet.setUrl(torrent.getUrl());
        }

        if (!magnet.canOpen()) {
            openMagnet.setVisibility(View.GONE);
        } else {
            openMagnet.setVisibility(View.VISIBLE);
        }
    }

    @Override public void showReadMoreDialog(String synopsis) {
        if (getFragmentManager().findFragmentByTag("overlay_fragment") != null) {
            return;
        }

        SynopsisDialogFragment.newInstance(synopsis)
                .show(getFragmentManager(), "overlay_fragment");
    }

    @Override public void hideRating() {
        rating.setVisibility(View.GONE);
    }

    @Override public void displayRating(int rating) {
        this.rating.setProgress(rating);
        this.rating.setContentDescription(String.format(Locale.US, "Rating: %d out of 10", rating));
        this.rating.setVisibility(View.VISIBLE);
    }

    @Override public void displayMetaData(CharSequence metaData) {
        meta.setText(metaData);
    }

    @Override public void displaySynopsis(String synopsis) {
        this.synopsis.setText(synopsis);
        this.synopsis.post(() -> {
            boolean ellipsized = false;
            Layout layout = StreamableDetailFragment.this.synopsis.getLayout();
            int lines = layout.getLineCount();
            if (lines > 0) {
                int ellipsisCount = layout.getEllipsisCount(lines - 1);
                if (ellipsisCount > 0) {
                    ellipsized = true;
                }
            }
            readMore.setVisibility(ellipsized ? View.VISIBLE : View.GONE);
        });

        readMore.setVisibility(View.GONE);
    }

    @Override public void hideSynopsis() {
        readMore.setVisibility(View.GONE);
    }

    @Override public void setSubtitleText(@StringRes int subtitleText) {
        subtitlesPreview.setText(subtitleText);
    }

    @Override public void subtitleVisibility(boolean visible) {
        subtitlesPreview.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    @Override public void setSubtitleText(String subtitleText) {
        subtitlesPreview.setText(subtitleText);
    }

    @Override public void displaySubsPicker(@NonNull final MediaWrapper mediaWrapper, @Nullable Subtitle subtitle) {
        hideDialog();

        SubsPickerDialog dialog = SubsPickerDialog.newInstance(mediaWrapper, subtitle);
        dialog.show(getChildFragmentManager(), "dialog");
        pickerDialog = dialog;
    }

    @Override public void displayQualityPicker(ArrayList<UiQuality> qualities) {
        hideDialog();

        QualityPickerDialog dialog = QualityPickerDialog.newInstance(qualities);
        dialog.show(getChildFragmentManager(), "dialog");
        pickerDialog = dialog;
    }

    @Override public void hideDialog() {
        BottomSheetDialogFragment dialog = pickerDialog;
        if (dialog != null) {
            dialog.dismiss();
            pickerDialog = null;
        }
    }

    @Override public void displayQuality(String quality) {
        qualityPreview.setText(quality);
    }

    @OnClick(R.id.read_more) public void openReadMore() {
        presenter.openReadMore();
    }

    @OnClick(R.id.watch_trailer)
    public void openTrailer() {
        presenter.openTrailer();
    }

    @Optional @OnClick(R.id.fab) public void play() {
        presenter.playMediaClicked();
    }

    @OnClick(R.id.magnet)
    public void openMagnet() {
        magnet.open(getActivity());
    }

    @OnClick(R.id.health)
    public void clickHealth() {
        presenter.healthClicked();
    }

    @Override public void initLayout(MediaWrapper mediaWrapper) {
        Media media = mediaWrapper.getMedia();

        title.setText(media.getTitle());

        if (fab != null && mediaWrapper.hasColor()) {
            fab.setBackgroundTintList(ColorStateList.valueOf(mediaWrapper.getColor()));
        }

        if (mediaWrapper.isMovie()) {
            watchTrailer.setVisibility(TextUtils.isEmpty(((Movie) media).getTrailer()) ? View.GONE : View.VISIBLE);
        }

        if (coverImage != null) {
            GlideApp.with(this).load(media.getBackdrop()).into(coverImage);
        }
    }

    @Override public void onQualityItemSelected(int position) {
        presenter.selectQuality(position);
    }

    public static StreamableDetailFragment newInstance(MediaWrapper movie) {
        Bundle args = new Bundle();
        args.putParcelable(EXTRA_MOVIE, movie);

        StreamableDetailFragment fragment = new StreamableDetailFragment();
        fragment.setArguments(args);
        return fragment;
    }

}
