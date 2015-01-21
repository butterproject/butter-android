package pct.droid.activities;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.util.Pair;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.RelativeSizeSpan;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nirhart.parallaxscroll.views.ParallaxScrollView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import butterknife.InjectView;
import pct.droid.R;
import pct.droid.base.preferences.Prefs;
import pct.droid.base.providers.media.models.Movie;
import pct.droid.base.utils.AnimUtils;
import pct.droid.base.utils.LocaleUtils;
import pct.droid.base.utils.NetworkUtils;
import pct.droid.base.utils.PixelUtils;
import pct.droid.base.utils.PrefUtils;
import pct.droid.base.utils.StringUtils;
import pct.droid.base.utils.VersionUtil;
import pct.droid.base.youtube.YouTubeData;
import pct.droid.dialogfragments.MessageDialogFragment;
import pct.droid.dialogfragments.StringArraySelectorDialogFragment;
import pct.droid.dialogfragments.SynopsisDialogFragment;
import pct.droid.fragments.StreamLoadingFragment;
import pct.droid.utils.ActionBarBackground;

public class MovieDetailActivity extends BaseActivity {

    private Movie mItem;
    private Drawable mPlayButtonDrawable;
    private Integer mLastScrollLocation = 0, mPaletteColor, mOpenBarPos, mHeaderHeight, mToolbarHeight, mParallaxHeight;
    private Boolean mTransparentBar = true, mOpenBar = true;
    private String mSelectedQuality, mSubLanguage = "no-subs";

    @InjectView(R.id.popcornLogo)
    View mPopcornLogo;
    @InjectView(R.id.toolbar)
    Toolbar mToolbar;
    @InjectView(R.id.scrollview)
    ParallaxScrollView mScrollView;
    @InjectView(R.id.parallax)
    RelativeLayout mParallaxLayout;
    @InjectView(R.id.cover_image)
    ImageView mCoverImage;
    @InjectView(R.id.header_progress)
    ProgressBar mHeaderProgressCircle;
    @InjectView(R.id.base_info_block)
    RelativeLayout mainInfoBlock;
    @InjectView(R.id.playButton)
    ImageButton mPlayFab;
    @InjectView(R.id.title)
    TextView mTitle;
    @InjectView(R.id.meta)
    TextView mMeta;
    @InjectView(R.id.synopsis)
    TextView mSynopsis;
    @InjectView(R.id.rating)
    RatingBar mRating;
    @InjectView(R.id.watch_trailer)
    Button mWatchTrailer;
    @InjectView(R.id.read_more)
    Button mReadMore;
    @InjectView(R.id.quality)
    LinearLayout mQuality;
    @InjectView(R.id.quality_text)
    TextView mQualityText;
    @InjectView(R.id.subtitles)
    LinearLayout mSubtitles;
    @InjectView(R.id.subtitles_lang)
    TextView mSubtitlesLang;

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onClick(View v) {
            Bundle b;
            switch (v.getId()) {
                case R.id.read_more:
                case R.id.synopsis:
                    if (getFragmentManager().findFragmentByTag("overlay_fragment") != null)
                        return;
                    SynopsisDialogFragment synopsisDialogFragment = new SynopsisDialogFragment();
                    b = new Bundle();
                    b.putString("text", mItem.synopsis);
                    synopsisDialogFragment.setArguments(b);
                    synopsisDialogFragment.show(getFragmentManager(), "overlay_fragment");
                    break;
                case R.id.quality:
                    if (getFragmentManager().findFragmentByTag("overlay_fragment") != null)
                        return;
                    final String[] qualities = mItem.torrents.keySet().toArray(new String[mItem.torrents.size()]);
                    Arrays.sort(qualities);
                    StringArraySelectorDialogFragment
                            .showSingleChoice(getFragmentManager(), R.string.quality, qualities, Arrays.asList(qualities).indexOf(mSelectedQuality),
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int position) {
                                            onQualitySelected(qualities[position]);
                                            dialog.dismiss();
                                        }
                                    });
                    break;
                case R.id.subtitles:
                    if (getFragmentManager().findFragmentByTag("overlay_fragment") != null)
                        return;
                    String[] languages = mItem.subtitles.keySet().toArray(new String[mItem.subtitles.size()]);
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

                    StringArraySelectorDialogFragment.showSingleChoice(getFragmentManager(), R.string.subtitles, readableNames,
                            Arrays.asList(adapterLanguages).indexOf(mSubLanguage), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int position) {
                                    onSubtitleLanguageSelected(adapterLanguages[position]);
                                    dialog.dismiss();
                                }
                            });
                    break;
                case R.id.watch_trailer:
                    Intent trailerIntent = new Intent(MovieDetailActivity.this, TrailerPlayerActivity.class);
                    if (!YouTubeData.isYouTubeUrl(mItem.trailer)) {
                        trailerIntent = new Intent(MovieDetailActivity.this, VideoPlayerActivity.class);
                    }
                    trailerIntent.putExtra(TrailerPlayerActivity.DATA, mItem);
                    trailerIntent.putExtra(TrailerPlayerActivity.LOCATION, mItem.trailer);
                    startActivity(trailerIntent);
                    break;
                case R.id.playButton:
                    final String streamUrl = mItem.torrents.get(mSelectedQuality).url;
                    if (PrefUtils.get(MovieDetailActivity.this, Prefs.WIFI_ONLY, true) &&
                            !NetworkUtils.isWifiConnected(MovieDetailActivity.this) &&
                            NetworkUtils
                                    .isNetworkConnected(MovieDetailActivity.this)) {
                        MessageDialogFragment.show(getFragmentManager(), R.string.wifi_only, R.string.wifi_only_message);
                    } else {
                        StreamLoadingFragment.StreamInfo streamInfo =
                                new StreamLoadingFragment.StreamInfo(mItem, streamUrl, mSubLanguage, mSelectedQuality);


                        Intent i = new Intent(MovieDetailActivity.this, StreamLoadingActivity.class);
                        i.putExtra(StreamLoadingActivity.EXTRA_INFO, streamInfo);


                        if (VersionUtil.isLollipop())
                            StreamLoadingActivity.startActivity(MovieDetailActivity.this, streamInfo, Pair.create((View) mCoverImage, mCoverImage.getTransitionName()));
                        else
                            StreamLoadingActivity.startActivity(MovieDetailActivity.this, streamInfo);
                    }
                    break;
            }

        }
    };

    private ViewTreeObserver.OnScrollChangedListener mOnScrollListener = new ViewTreeObserver.OnScrollChangedListener() {
        @Override
        public void onScrollChanged() {
            if (mToolbarHeight == 0) {
                mToolbarHeight = mToolbar.getHeight();
                mHeaderHeight = mParallaxHeight - mToolbarHeight;
            }

            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mToolbar.getLayoutParams();

            if (mScrollView.getScrollY() > mHeaderHeight) {
                if (mLastScrollLocation > mScrollView.getScrollY()) {
                    // scroll up
                    if ((mOpenBarPos == null || !mOpenBar) && layoutParams.topMargin <= -mToolbarHeight)
                        mOpenBarPos = mScrollView.getScrollY() - mToolbarHeight;
                    mOpenBar = true;
                } else if (mLastScrollLocation < mScrollView.getScrollY()) {
                    // scroll down
                    if (mOpenBarPos == null || mOpenBar)
                        mOpenBarPos = mScrollView.getScrollY();
                    mOpenBar = false;
                }

                if (layoutParams.topMargin <= 0) {
                    layoutParams.topMargin = mOpenBarPos - mScrollView.getScrollY();
                }

                if (layoutParams.topMargin > 0) {
                    layoutParams.topMargin = 0;
                }
            }

            if (layoutParams.topMargin < 0) {
                mScrollView.setOverScrollMode(View.OVER_SCROLL_NEVER);
            } else {
                mScrollView.setOverScrollMode(View.OVER_SCROLL_IF_CONTENT_SCROLLS);
            }

                /* Fade out when over header */
            if (mParallaxHeight - mScrollView.getScrollY() < 0) {
                if (mTransparentBar) {
                    mTransparentBar = false;
                    ActionBarBackground.changeColor(MovieDetailActivity.this, mPaletteColor, false);
                }
            } else {
                if (!mTransparentBar) {
                    mTransparentBar = true;
                    ActionBarBackground.fadeOut(MovieDetailActivity.this);
                }
            }

            mToolbar.setLayoutParams(layoutParams);

            mLastScrollLocation = mScrollView.getScrollY();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        super.onCreate(savedInstanceState, R.layout.activity_moviedetail);
        setSupportActionBar(mToolbar);

        // Set transparent toolbar
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ActionBarBackground.fadeOut(this);

        Drawable playButtonDrawable = PixelUtils.changeDrawableColor(this, R.drawable.play_button_circle, getResources().getColor(R.color.primary));
        if (mPlayButtonDrawable == null) mPlayFab.setBackgroundDrawable(playButtonDrawable);

        mPlayFab.setOnClickListener(mOnClickListener);
        mSynopsis.setOnClickListener(mOnClickListener);
        mWatchTrailer.setOnClickListener(mOnClickListener);
        mReadMore.setOnClickListener(mOnClickListener);
        mSubtitles.setOnClickListener(mOnClickListener);
        mQuality.setOnClickListener(mOnClickListener);

        mParallaxHeight = (PixelUtils.getScreenHeight(this) / 3) * 2;
        mParallaxLayout.getLayoutParams().height = mParallaxHeight;

        mToolbarHeight = mToolbar.getHeight();
        mHeaderHeight = mParallaxHeight - mToolbarHeight;
        mScrollView.getViewTreeObserver().addOnScrollChangedListener(mOnScrollListener);

        Intent intent = getIntent();
        mItem = intent.getParcelableExtra("item");

        mPaletteColor = intent.getIntExtra("palette", getResources().getColor(R.color.primary));
        mPlayButtonDrawable = PixelUtils.changeDrawableColor(MovieDetailActivity.this, R.drawable.play_button_circle, mPaletteColor);
        mPlayFab.setBackgroundDrawable(mPlayButtonDrawable);

        Double rating = Double.parseDouble(mItem.rating);
        mTitle.setText(mItem.title);
        mRating.setProgress(rating.intValue());

        String metaDataStr = mItem.year;
        List<int[]> spanDatas = new ArrayList<>();
        if (!TextUtils.isEmpty(mItem.runtime)) {
            int spanData[] = new int[2];
            spanData[0] = metaDataStr.length() + 1;
            metaDataStr += " ● ";
            spanData[1] = metaDataStr.length() - 1;
            spanDatas.add(spanData);
            metaDataStr += mItem.runtime + " " + getString(R.string.minutes);
        }

        if (!TextUtils.isEmpty(mItem.genre)) {
            int spanData[] = new int[2];
            spanData[0] = metaDataStr.length() + 1;
            metaDataStr += " ● ";
            spanData[1] = metaDataStr.length() - 1;
            spanDatas.add(spanData);
            metaDataStr += mItem.genre;
        }

        SpannableString metaDataSpan =  new SpannableString(metaDataStr);
        for(int[] spanData : spanDatas) {
            metaDataSpan.setSpan(new RelativeSizeSpan(0.7f), spanData[0], spanData[1], Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        mMeta.setText(metaDataSpan);

        if (!TextUtils.isEmpty(mItem.synopsis)) {
            mSynopsis.setText(mItem.synopsis);
        } else {
            mSynopsis.setClickable(false);
        }

        if (mItem.trailer == null) {
            mWatchTrailer.setVisibility(View.GONE);
        }

        if (mItem.subtitles.containsKey(PrefUtils.get(this, Prefs.SUBTITLE_DEFAULT, "no-subs"))) {
            onSubtitleLanguageSelected(PrefUtils.get(this, Prefs.SUBTITLE_DEFAULT, "no-subs"));
        }

        Picasso.with(this).load(mItem.image).into(mCoverImage, new Callback() {
            @Override
            public void onSuccess() {
                int oldColor = mPaletteColor;
                if (mPaletteColor == getResources().getColor(R.color.primary)) {
                    Palette palette = Palette.generate(((BitmapDrawable) mCoverImage.getDrawable()).getBitmap());

                    int vibrantColor = palette.getVibrantColor(-1);
                    if (vibrantColor == -1) {
                        mPaletteColor = palette.getMutedColor(getResources().getColor(R.color.primary));
                    } else {
                        mPaletteColor = vibrantColor;
                    }
                }

                final ObjectAnimator mainInfoBlockColorFade =
                        ObjectAnimator.ofObject(mainInfoBlock, "backgroundColor", new ArgbEvaluator(), oldColor, mPaletteColor);
                mainInfoBlockColorFade.setDuration(500);
                Drawable oldDrawable = PixelUtils.changeDrawableColor(MovieDetailActivity.this, R.drawable.play_button_circle, oldColor);
                mPlayButtonDrawable = PixelUtils.changeDrawableColor(MovieDetailActivity.this, R.drawable.play_button_circle, mPaletteColor);

                final TransitionDrawable td = new TransitionDrawable(new Drawable[]{oldDrawable, mPlayButtonDrawable});

                // Delay to make sure transition is smooth
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mPlayFab.setBackgroundDrawable(td);
                        td.startTransition(500);
                        AnimUtils.fadeIn(mCoverImage);
                        mPopcornLogo.setVisibility(View.GONE);
                    }
                }, 1000);
            }

            @Override
            public void onError() {
                mHeaderProgressCircle.setVisibility(View.GONE);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mSelectedQuality == null) {
            String[] keys = mItem.torrents.keySet().toArray(new String[mItem.torrents.size()]);
            onQualitySelected(keys[0]);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mScrollView.getViewTreeObserver().removeOnScrollChangedListener(mOnScrollListener);
    }

    public void onQualitySelected(String quality) {
        mSelectedQuality = quality;
        mQualityText.setText(mSelectedQuality);
    }

    private void onSubtitleLanguageSelected(String language) {
        mSubLanguage = language;
        if (!mSubLanguage.equals("no-subs")) {
            Locale locale = LocaleUtils.toLocale(mSubLanguage);
            mSubtitlesLang.setText(StringUtils.uppercaseFirst(locale.getDisplayName(locale)));
        } else {
            mSubtitlesLang.setText(R.string.no_subs);
        }
    }
}