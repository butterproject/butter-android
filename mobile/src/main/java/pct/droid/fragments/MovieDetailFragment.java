package pct.droid.fragments;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Layout;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.Arrays;
import java.util.Locale;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.Optional;
import pct.droid.R;
import pct.droid.activities.TrailerPlayerActivity;
import pct.droid.activities.VideoPlayerActivity;
import pct.droid.base.preferences.Prefs;
import pct.droid.base.providers.media.models.Movie;
import pct.droid.base.utils.LocaleUtils;
import pct.droid.base.utils.PixelUtils;
import pct.droid.base.utils.PrefUtils;
import pct.droid.base.utils.StringUtils;
import pct.droid.base.utils.VersionUtils;
import pct.droid.base.youtube.YouTubeData;
import pct.droid.dialogfragments.SynopsisDialogFragment;
import pct.droid.widget.OptionSelector;

public class MovieDetailFragment extends BaseDetailFragment {

    private Movie mMovie;
    private String mSelectedSubtitleLanguage, mSelectedQuality;

    @InjectView(R.id.play_button)
    ImageButton mPlayButton;
    @InjectView(R.id.title)
    TextView mTitle;
    @InjectView(R.id.meta)
    TextView mMeta;
    @InjectView(R.id.synopsis)
    TextView mSynopsis;
    @InjectView(R.id.read_more)
    TextView mReadMore;
    @InjectView(R.id.watch_trailer)
    TextView mWatchTrailer;
    @InjectView(R.id.rating)
    RatingBar mRating;
    @InjectView(R.id.subtitles)
    OptionSelector mSubtitles;
    @InjectView(R.id.quality)
    OptionSelector mQuality;
    @Optional
    @InjectView(R.id.cover_image)
    ImageView mCoverImage;

    public static MovieDetailFragment newInstance(Movie movie, int color) {
        Bundle b = new Bundle();
        b.putParcelable(DATA, movie);
        b.putInt(COLOR, color);
        MovieDetailFragment movieDetailFragment = new MovieDetailFragment();
        movieDetailFragment.setArguments(b);
        return movieDetailFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMovie = getArguments().getParcelable(DATA);
        mPaletteColor = getArguments().getInt(COLOR, getResources().getColor(R.color.primary));
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mRoot = inflater.inflate(R.layout.fragment_moviedetail, container, false);
        mRoot.setMinimumHeight(container.getMinimumHeight());
        ButterKnife.inject(this, mRoot);

        if(VersionUtils.isJellyBean()) {
            mPlayButton.setBackgroundDrawable(PixelUtils.changeDrawableColor(mPlayButton.getContext(), R.drawable.play_button_circle, mPaletteColor));
        } else {
            mPlayButton.setBackground(PixelUtils.changeDrawableColor(mPlayButton.getContext(), R.drawable.play_button_circle, mPaletteColor));
        }

        Double rating = Double.parseDouble(mMovie.rating);
        mTitle.setText(mMovie.title);
        mRating.setProgress(rating.intValue());

        String metaDataStr = mMovie.year;
        if (!TextUtils.isEmpty(mMovie.runtime)) {
            metaDataStr += " • ";
            metaDataStr += mMovie.runtime + " " + getString(R.string.minutes);
        }

        if (!TextUtils.isEmpty(mMovie.genre)) {
            metaDataStr += " • ";
            metaDataStr += mMovie.genre;
        }

        mMeta.setText(metaDataStr);

        if (!TextUtils.isEmpty(mMovie.synopsis)) {
            mSynopsis.setText(mMovie.synopsis);
            mSynopsis.post(new Runnable() {
                @Override
                public void run() {
                    boolean ellipsized = false;
                    Layout layout = mSynopsis.getLayout();
                    if(layout == null) return;
                    int lines = layout.getLineCount();
                    if(lines > 0) {
                        int ellipsisCount = layout.getEllipsisCount(lines-1);
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

        if (mMovie.trailer == null) {
            mWatchTrailer.setVisibility(View.GONE);
        }

        mSubtitles.setFragmentManager(getFragmentManager());
        mQuality.setFragmentManager(getFragmentManager());
        mSubtitles.setTitle(R.string.subtitles);
        mQuality.setTitle(R.string.quality);

        String[] languages = mMovie.subtitles.keySet().toArray(new String[mMovie.subtitles.size()]);
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
        mSubtitles.setData(readableNames);

        final String[] qualities = mMovie.torrents.keySet().toArray(new String[mMovie.torrents.size()]);
        Arrays.sort(qualities);
        mQuality.setData(qualities);

        mSubtitles.setListener(new OptionSelector.SelectorListener() {
            @Override
            public void onSelectionChanged(int position, String value) {
                onSubtitleLanguageSelected(adapterLanguages[position]);
            }
        });
        mQuality.setListener(new OptionSelector.SelectorListener() {
            @Override
            public void onSelectionChanged(int position, String value) {
                mSelectedQuality = value;
            }
        });

        mSelectedQuality = qualities[qualities.length - 1];
        mQuality.setText(mSelectedQuality);
        mQuality.setDefault(qualities.length - 1);

        String defaultSubtitle = PrefUtils.get(mSubtitles.getContext(), Prefs.SUBTITLE_DEFAULT, null);
        if (mMovie.subtitles.containsKey(defaultSubtitle)) {
            onSubtitleLanguageSelected(defaultSubtitle);
            mSubtitles.setDefault(Arrays.asList(adapterLanguages).indexOf(defaultSubtitle));
        }

        if(mCoverImage != null) {
            Picasso.with(mCoverImage.getContext()).load(mMovie.image).into(mCoverImage);
        }

        return mRoot;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof FragmentListener)
            mCallback = (FragmentListener) activity;
    }

    @OnClick(R.id.read_more)
    public void openReadMore(View v) {
        if (getFragmentManager().findFragmentByTag("overlay_fragment") != null)
            return;
        SynopsisDialogFragment synopsisDialogFragment = new SynopsisDialogFragment();
        Bundle b = new Bundle();
        b.putString("text", mMovie.synopsis);
        synopsisDialogFragment.setArguments(b);
        synopsisDialogFragment.show(mActivity.getFragmentManager(), "overlay_fragment");
    }

    @OnClick(R.id.watch_trailer)
    public void openTrailer(View v) {
        Intent trailerIntent = new Intent(mActivity, TrailerPlayerActivity.class);
        if (!YouTubeData.isYouTubeUrl(mMovie.trailer)) {
            trailerIntent = new Intent(mActivity, VideoPlayerActivity.class);
        }
        trailerIntent.putExtra(TrailerPlayerActivity.DATA, mMovie);
        trailerIntent.putExtra(TrailerPlayerActivity.LOCATION, mMovie.trailer);
        startActivity(trailerIntent);
    }

    @OnClick(R.id.play_button)
    public void play() {
        String streamUrl = mMovie.torrents.get(mSelectedQuality).url;
        StreamLoadingFragment.StreamInfo streamInfo = new StreamLoadingFragment.StreamInfo(mMovie, streamUrl, mSelectedSubtitleLanguage, mSelectedQuality);
        mCallback.playStream(streamInfo);
    }

    private void onSubtitleLanguageSelected(String language) {
        mSelectedSubtitleLanguage = language;
        if (!language.equals("no-subs")) {
            Locale locale = LocaleUtils.toLocale(language);
            mSubtitles.setText(StringUtils.uppercaseFirst(locale.getDisplayName(locale)));
        } else {
            mSubtitles.setText(R.string.no_subs);
        }
    }
}
