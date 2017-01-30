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
import java.util.Map;

import javax.inject.Inject;

import butter.droid.R;
import butter.droid.base.content.preferences.PreferencesHandler;
import butter.droid.base.manager.provider.ProviderManager;
import butter.droid.base.manager.vlc.PlayerManager;
import butter.droid.base.providers.media.models.Movie;
import butter.droid.base.providers.subs.SubsProvider;
import butter.droid.base.torrent.Magnet;
import butter.droid.base.torrent.TorrentHealth;
import butter.droid.base.utils.LocaleUtils;
import butter.droid.base.utils.SortUtils;
import butter.droid.base.utils.StringUtils;
import butter.droid.base.utils.ThreadUtils;
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
    @Inject ProviderManager providerManager;
    @Inject PreferencesHandler preferencesHandler;
    @Inject PlayerManager playerManager;

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

    public static MovieDetailFragment newInstance(Movie movie) {
        Bundle args = new Bundle();
        args.putParcelable(EXTRA_MOVIE, movie);

        MovieDetailFragment fragment = new MovieDetailFragment();
        fragment.setArguments(args);
        return fragment;
    }

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
//        int seeds = sMovie.torrents.get(mSelectedQuality).seeds;
//        int peers = sMovie.torrents.get(mSelectedQuality).peers;
//        TorrentHealth health = TorrentHealth.calculate(seeds, peers);
//
//        final Snackbar snackbar = Snackbar.make(mRoot,
//                getString(R.string.health_info, getString(health.getStringResource()), seeds, peers),
//                Snackbar.LENGTH_LONG);
//        snackbar.setAction(R.string.close, new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                snackbar.dismiss();
//            }
//        });
//        snackbar.show();
    }

    private void onSubtitleLanguageSelected(String language) {
//        mSelectedSubtitleLanguage = language;
        if (!language.equals("no-subs")) {
            final Locale locale = LocaleUtils.toLocale(language);
            ThreadUtils.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mSubtitles.setText(StringUtils.uppercaseFirst(locale.getDisplayName(locale)));
                }
            });
        } else {
            ThreadUtils.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mSubtitles.setText(R.string.no_subs);
                }
            });
        }
    }

    @Override public void initLayout(Movie movie) {

        mTitle.setText(movie.title);
        setMetaData(movie);
        setRating(movie);
        setSynopsis(movie);
        setSubtitles(movie);
        setQualities(movie);

        if (fab != null) {
            fab.setBackgroundTintList(ColorStateList.valueOf(movie.color));
        }

        mWatchTrailer.setVisibility(TextUtils.isEmpty(movie.trailer) ? View.GONE : View.VISIBLE);

        if (mCoverImage != null) {
            Picasso.with(mCoverImage.getContext()).load(movie.image).into(mCoverImage);
        }
    }

    private void setMetaData(Movie movie) {
        StringBuilder sb = new StringBuilder(String.valueOf(movie.year));
        if (!TextUtils.isEmpty(movie.runtime)) {
            sb.append(" • ")
                    .append(movie.runtime)
                    .append(' ')
                    .append(getString(R.string.minutes));
        }

        if (!TextUtils.isEmpty(movie.genre)) {
            sb.append(" • ")
                    .append(movie.genre);
        }

        mMeta.setText(sb);
    }

    private void setRating(Movie movie) {
        if (!"-1".equals(movie.rating)) {
            Double rating = Double.parseDouble(movie.rating);
            mRating.setProgress(rating.intValue());
            mRating.setContentDescription("Rating: " + rating.intValue() + " out of 10");
            mRating.setVisibility(View.VISIBLE);
        } else {
            mRating.setVisibility(View.GONE);
        }
    }

    private void setSynopsis(Movie movie) {
        if (!TextUtils.isEmpty(movie.synopsis)) {
            mSynopsis.setText(movie.synopsis);
            mSynopsis.post(new Runnable() {
                @Override
                public void run() {
                    boolean ellipsized = false;
                    Layout layout = mSynopsis.getLayout();
                    if (layout == null) {
                        return;
                    }
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
        } else {
            mSynopsis.setClickable(false);
            mReadMore.setVisibility(View.GONE);
        }
    }

    private void setSubtitles(final Movie movie) {
        mSubtitles.setFragmentManager(getFragmentManager());
        mSubtitles.setTitle(R.string.subtitles);
        mSubtitles.setText(R.string.loading_subs);
        mSubtitles.setClickable(false);

        if (providerManager.hasCurrentSubsProvider()) {
            providerManager.getCurrentSubsProvider().getList(movie, new SubsProvider.Callback() {
                @Override
                public void onSuccess(Map<String, String> subtitles) {
                    if (subtitles == null) {
                        ThreadUtils.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mSubtitles.setText(R.string.no_subs_available);
                            }
                        });
                        return;
                    }

                    movie.subtitles = subtitles;

                    String[] languages = subtitles.keySet().toArray(new String[subtitles.size()]);
                    Arrays.sort(languages);
                    final String[] adapterLanguages = new String[languages.length + 1];
                    adapterLanguages[0] = "no-subs";
                    System.arraycopy(languages, 0, adapterLanguages, 1, languages.length);

                    String[] readableNames = new String[adapterLanguages.length];
                    for (int i = 0; i < readableNames.length; i++) {
                        String language = adapterLanguages[i];
                        if (language.equals("no-subs")) {
                            readableNames[i] = getString(R.string.no_subs);
                        } else {
                            Locale locale = LocaleUtils.toLocale(language);
                            readableNames[i] = locale.getDisplayName(locale);
                        }
                    }

                    mSubtitles.setListener(new OptionSelector.SelectorListener() {
                        @Override
                        public void onSelectionChanged(int position, String value) {
                            onSubtitleLanguageSelected(adapterLanguages[position]);
                        }
                    });
                    mSubtitles.setData(readableNames);
                    ThreadUtils.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mSubtitles.setClickable(true);
                        }
                    });

                    String defaultSubtitle = preferencesHandler.getSubtitleDefaultLanguage();
                    if (subtitles.containsKey(defaultSubtitle)) {
                        onSubtitleLanguageSelected(defaultSubtitle);
                        mSubtitles.setDefault(Arrays.asList(adapterLanguages).indexOf(defaultSubtitle));
                    } else {
                        onSubtitleLanguageSelected(SubsProvider.SUBTITLE_LANGUAGE_NONE);
                        mSubtitles.setDefault(
                                Arrays.asList(adapterLanguages).indexOf(SubsProvider.SUBTITLE_LANGUAGE_NONE));
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    mSubtitles.setData(new String[0]);
                    mSubtitles.setClickable(true);
                }
            });
        } else {
            mSubtitles.setClickable(false);
            mSubtitles.setText(R.string.no_subs_available);
        }
    }

    private void setQualities(Movie movie) {
        mQuality.setFragmentManager(getFragmentManager());
        mQuality.setTitle(R.string.quality);

        if (movie.torrents.size() > 0) {
            final String[] qualities = movie.torrents.keySet().toArray(new String[movie.torrents.size()]);
            SortUtils.sortQualities(qualities);

            mQuality.setData(qualities);
            mQuality.setListener(new OptionSelector.SelectorListener() {
                @Override
                public void onSelectionChanged(int position, String value) {
                    setQuality(value);
                }
            });

            String quality = playerManager.getDefaultQuality(Arrays.asList(qualities));
            int qualityIndex = Arrays.asList(qualities).indexOf(quality);
            mQuality.setText(quality);
            mQuality.setDefault(qualityIndex);

            setQuality(quality);
        }
    }
}
