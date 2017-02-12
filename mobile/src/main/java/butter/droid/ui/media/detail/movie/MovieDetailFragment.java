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

import android.app.Activity;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
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

import com.squareup.picasso.Picasso;

import java.util.Arrays;
import java.util.Locale;

import javax.inject.Inject;

import butter.droid.R;
import butter.droid.base.providers.media.models.Movie;
import butter.droid.base.torrent.Magnet;
import butter.droid.base.torrent.TorrentHealth;
import butter.droid.fragments.base.BaseDetailFragment;
import butter.droid.fragments.dialog.SynopsisDialogFragment;
import butter.droid.ui.media.detail.MediaDetailActivity;
import butter.droid.widget.OptionSelector;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Optional;

public class MovieDetailFragment extends BaseDetailFragment implements MovieDetailView {

    private static final String EXTRA_MOVIE = "butter.droid.ui.media.detail.movie.MovieDetailFragment.movie";

    @Inject MovieDetailPresenter presenter;

    private Magnet mMagnet;

    @BindView(R.id.fab) @Nullable FloatingActionButton fab;
    @BindView(R.id.title) TextView mTitle;
    @BindView(R.id.health) ImageView mHealth;
    @BindView(R.id.meta) TextView mMeta;
    @BindView(R.id.synopsis) TextView mSynopsis;
    @BindView(R.id.read_more) Button mReadMore;
    @BindView(R.id.watch_trailer) Button mWatchTrailer;
    @BindView(R.id.magnet) ImageButton mOpenMagnet;
    @BindView(R.id.rating) RatingBar mRating;
    @BindView(R.id.subtitles) OptionSelector mSubtitles;
    @BindView(R.id.quality) OptionSelector mQuality;
    @Nullable @BindView(R.id.cover_image) ImageView mCoverImage;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MediaDetailActivity) activity).getComponent()
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

        Movie movie = getArguments().getParcelable(EXTRA_MOVIE);
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
        if (mHealth.getVisibility() == View.GONE) {
            mHealth.setVisibility(View.VISIBLE);
        }

        TorrentHealth health = TorrentHealth.calculate(movie.torrents.get(quality).seeds,
                movie.torrents.get(quality).peers);
        mHealth.setImageResource(health.getImageResource());
    }

    @Override public void updateMagnet(Movie movie, String quality) {
        if (mMagnet == null) {
            mMagnet = new Magnet(mActivity, movie.torrents.get(quality).url);
        }
        mMagnet.setUrl(movie.torrents.get(quality).url);

        if (!mMagnet.canOpen()) {
            mOpenMagnet.setVisibility(View.GONE);
        } else {
            mOpenMagnet.setVisibility(View.VISIBLE);
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
        mRating.setVisibility(View.GONE);
    }

    @Override public void displayRating(int rating) {
        mRating.setProgress(rating);
        mRating.setContentDescription(String.format(Locale.US, "Rating: %d out of 10", rating));
        mRating.setVisibility(View.VISIBLE);
    }

    @Override public void displayMetaData(CharSequence metaData) {
        mMeta.setText(metaData);
    }

    @Override public void displaySynopsis(String synopsis) {
        mSynopsis.setText(synopsis);
        mSynopsis.post(new Runnable() {
            @Override
            public void run() {
                boolean ellipsized = false;
                Layout layout = mSynopsis.getLayout();
                int lines = layout.getLineCount();
                if (lines > 0) {
                    int ellipsisCount = layout.getEllipsisCount(lines - 1);
                    if (ellipsisCount > 0) {
                        ellipsized = true;
                    }
                }
                mReadMore.setVisibility(ellipsized ? View.VISIBLE : View.GONE);
            }
        });

        mReadMore.setVisibility(View.GONE);
    }

    @Override public void hideSynopsis() {
        mReadMore.setVisibility(View.GONE);
    }

    @Override public void setSubtitleText(@StringRes int subtitleText) {
        mSubtitles.setText(subtitleText);
    }

    @Override public void setSubtitleText(String subtitleText) {
        mSubtitles.setText(subtitleText);
    }

    @Override public void setSubtitleEnabled(boolean enabled) {
        mSubtitles.setClickable(enabled);
    }

    @Override public void setSubsData(String[] names, int defaultIndex) {
        mSubtitles.setData(names);
        mSubtitles.setDefault(defaultIndex);
        setSubtitleText(names[defaultIndex]);
    }

    @Override public void setQualities(String[] qualities, String quality) {
        mQuality.setData(qualities);
        mQuality.setText(quality);
        mQuality.setDefault(Arrays.asList(qualities).indexOf(quality));
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
        mMagnet.open(mActivity);
    }

    @OnClick(R.id.health)
    public void clickHealth() {
        presenter.healthClicked();
    }

    @Override public void initLayout(Movie movie) {

        mTitle.setText(movie.title);

        mSubtitles.setFragmentManager(getFragmentManager());
        mSubtitles.setTitle(R.string.subtitles);
        mSubtitles.setListener(new OptionSelector.SelectorListener() {
            @Override
            public void onSelectionChanged(int position, String value) {
                presenter.subtitleSelected(position);
            }
        });

        mQuality.setFragmentManager(getFragmentManager());
        mQuality.setTitle(R.string.quality);
        mQuality.setListener(new OptionSelector.SelectorListener() {
            @Override
            public void onSelectionChanged(int position, String value) {
                setQuality(value);
            }
        });

        if (fab != null) {
            fab.setBackgroundTintList(ColorStateList.valueOf(movie.color));
        }

        mWatchTrailer.setVisibility(TextUtils.isEmpty(movie.trailer) ? View.GONE : View.VISIBLE);

        if (mCoverImage != null) {
            Picasso.with(mCoverImage.getContext()).load(movie.image).into(mCoverImage);
        }
    }

    public static MovieDetailFragment newInstance(Movie movie) {
        Bundle args = new Bundle();
        args.putParcelable(EXTRA_MOVIE, movie);

        MovieDetailFragment fragment = new MovieDetailFragment();
        fragment.setArguments(args);
        return fragment;
    }
}
