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

package butter.droid.ui.media.detail.movie;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
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
import butter.droid.R;
import butter.droid.base.torrent.Magnet;
import butter.droid.provider.base.module.Movie;
import butter.droid.ui.media.detail.MediaDetailActivity;
import butter.droid.ui.media.detail.movie.dialog.SynopsisDialogFragment;
import butter.droid.widget.OptionSelector;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Optional;
import com.squareup.picasso.Picasso;
import java.util.Arrays;
import java.util.Locale;
import javax.inject.Inject;
import org.parceler.Parcels;

public class MovieDetailFragment extends Fragment implements MovieDetailView {

    private static final String EXTRA_MOVIE = "butter.droid.ui.media.detail.movie.MovieDetailFragment.movie";

    @Inject MovieDetailPresenter presenter;

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
    @BindView(R.id.subtitles) OptionSelector subtitles;
    @BindView(R.id.quality) OptionSelector quality;
    @Nullable @BindView(R.id.cover_image) ImageView coverImage;

    @Override public void onAttach(final Context context) {
        super.onAttach(context);
        ((MediaDetailActivity) context).getComponent()
                .movieDetailComponentBuilder()
                .movieDetailModule(new MovieDetailModule(this))
                .build()
                .inject(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_moviedetail, container, false);
    }

    @Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ButterKnife.bind(this, view);

        Movie movie = Parcels.unwrap(getArguments().getParcelable(EXTRA_MOVIE));
        presenter.onCreate(movie);
    }

    private void setQuality(String quality) {
        presenter.selectQuality(quality);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override public void renderHealth(Movie movie, String quality) {
        if (health.getVisibility() == View.GONE) {
            health.setVisibility(View.VISIBLE);
        }

        // TODO: 6/17/17  
//        TorrentHealth health = TorrentHealth.calculate(movie.getTorrents().get(quality).seeds,
//                movie.getTorrents().get(quality).peers);
//        this.health.setImageResource(health.getImageResource());
    }

    @Override public void updateMagnet(Movie movie, String quality) {
        // TODO: 6/17/17
        //        if (magnet == null) {
//            magnet = new Magnet(getContext(), movie.getTorrents().get(quality).url);
//        }
//        magnet.setUrl(movie.getTorrents().get(quality).url);

//        if (!magnet.canOpen()) {
//            openMagnet.setVisibility(View.GONE);
//        } else {
//            openMagnet.setVisibility(View.VISIBLE);
//        }
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
            Layout layout = MovieDetailFragment.this.synopsis.getLayout();
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
        subtitles.setText(subtitleText);
    }

    @Override public void setSubtitleText(String subtitleText) {
        subtitles.setText(subtitleText);
    }

    @Override public void setSubtitleEnabled(boolean enabled) {
        subtitles.setClickable(enabled);
    }

    @Override public void setSubsData(String[] names, int defaultIndex) {
        subtitles.setData(names);
        subtitles.setDefault(defaultIndex);
        setSubtitleText(names[defaultIndex]);
    }

    @Override public void setQualities(String[] qualities, String quality) {
        this.quality.setData(qualities);
        this.quality.setText(quality);
        this.quality.setDefault(Arrays.asList(qualities).indexOf(quality));
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

    @Override public void initLayout(Movie movie) {

        title.setText(movie.getTitle());

        subtitles.setFragmentManager(getFragmentManager());
        subtitles.setTitle(R.string.subtitles);
        subtitles.setListener((position, value) -> presenter.subtitleSelected(position));

        quality.setFragmentManager(getFragmentManager());
        quality.setTitle(R.string.quality);
        quality.setListener((position, value) -> setQuality(value));

//        if (fab != null) {
//            fab.setBackgroundTintList(ColorStateList.valueOf(movie.color));
//        }

        watchTrailer.setVisibility(TextUtils.isEmpty(movie.getTrailer()) ? View.GONE : View.VISIBLE);

        if (coverImage != null) {
            Picasso.with(coverImage.getContext()).load(movie.getBackdrop()).into(coverImage);
        }
    }

    public static MovieDetailFragment newInstance(Movie movie) {
        Bundle args = new Bundle();
        args.putParcelable(EXTRA_MOVIE, Parcels.wrap(movie));

        MovieDetailFragment fragment = new MovieDetailFragment();
        fragment.setArguments(args);
        return fragment;
    }
}
