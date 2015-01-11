package pct.droid.activities;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nirhart.parallaxscroll.views.ParallaxScrollView;
import com.squareup.picasso.Callback;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;

import butterknife.InjectView;
import pct.droid.R;
import pct.droid.base.PopcornApplication;
import pct.droid.base.preferences.Prefs;
import pct.droid.base.providers.media.types.Media;
import pct.droid.base.providers.media.types.Show;
import pct.droid.base.utils.AnimUtils;
import pct.droid.base.utils.LogUtils;
import pct.droid.base.utils.NetworkUtils;
import pct.droid.base.utils.PixelUtils;
import pct.droid.base.utils.PrefUtils;
import pct.droid.dialogfragments.MessageDialogFragment;
import pct.droid.dialogfragments.StringArraySelectorDialogFragment;
import pct.droid.dialogfragments.SynopsisDialogFragment;
import pct.droid.utils.ActionBarBackground;

public class ShowDetailActivity extends BaseActivity {

    private Show mItem;
    private Drawable mPlayButtonDrawable;
    private Integer mLastScrollLocation = 0, mPaletteColor, mOpenBarPos, mHeaderHeight, mToolbarHeight, mParallaxHeight;
    private Boolean mTransparentBar = true, mOpenBar = true, mIsFavourited = false;
    private String mQuality, mSubLanguage;

    @InjectView(R.id.toolbar)
    Toolbar toolbar;
    @InjectView(R.id.scrollView)
    ParallaxScrollView scrollView;
    @InjectView(R.id.parallax)
    RelativeLayout parallax;
    @InjectView(R.id.coverImage)
    ImageView coverImage;
    @InjectView(R.id.headerProgress)
    ProgressBar headerProgress;
    @InjectView(R.id.mainInfoBlock)
    RelativeLayout mainInfoBlock;
    @InjectView(R.id.playButton)
    ImageButton playButton;
    @InjectView(R.id.titleText)
    TextView titleText;
    @InjectView(R.id.yearText)
    TextView yearText;
    @InjectView(R.id.runtimeText)
    TextView runtimeText;
    @InjectView(R.id.ratingText)
    TextView ratingText;
    @InjectView(R.id.synopsisText)
    TextView synopsisText;
    //@InjectView(R.id.favouriteText)
    //TextView favouriteText;
    @InjectView(R.id.synopsisBlock)
    LinearLayout synopsisBlock;
    @InjectView(R.id.qualityBlock)
    LinearLayout qualityBlock;
    @InjectView(R.id.qualityText)
    TextView qualityText;
    //@InjectView(R.id.favouriteBlock)
    //LinearLayout favouriteBlock;
    @InjectView(R.id.trailerBlock)
    LinearLayout trailerBlock;
    @InjectView(R.id.subtitlesBlock)
    LinearLayout subtitlesBlock;
    @InjectView(R.id.subtitlesText)
    TextView subtitlesText;

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Bundle b;
            switch (v.getId()) {
                case R.id.synopsisBlock:
                    if (getFragmentManager().findFragmentByTag("overlay_fragment") != null)
                        return;
                    SynopsisDialogFragment synopsisDialogFragment = new SynopsisDialogFragment();
                    b = new Bundle();
                    b.putString("text", mItem.synopsis);
                    synopsisDialogFragment.setArguments(b);
                    synopsisDialogFragment.show(getFragmentManager(), "overlay_fragment");
                    break;
                /*
                case R.id.qualityBlock:
                    if(getFragmentManager().findFragmentByTag("overlay_fragment") != null) return;
                    QualitySelectorDialogFragment qualitySelectorDialogFragment = new QualitySelectorDialogFragment();
                    b = new Bundle();
                    b.putStringArray(QualitySelectorDialogFragment.QUALITIES, mItem.torrents.keySet().toArray(new String[mItem.torrents.size()]));
                    qualitySelectorDialogFragment.setArguments(b);
                    qualitySelectorDialogFragment.show(getFragmentManager(), "overlay_fragment");
                    break;

                case R.id.subtitlesBlock:
                    if (getFragmentManager().findFragmentByTag("overlay_fragment") != null)
                        return;
                    SubtitleSelectorDialogFragment subtitleSelectorDialogFragment = new SubtitleSelectorDialogFragment();
                    b = new Bundle();
                    Iterator<String> it = mItem.episodes.keySet().iterator();
                    String name = it.next();
                    Show.Episode episode = mItem.episodes.get(name);
                    if (episode.subtitles != null) {
                        b.putStringArray(SubtitleSelectorDialogFragment.LANGUAGES, episode.subtitles.keySet().toArray(new String[episode.subtitles.size()]));
                        subtitleSelectorDialogFragment.setArguments(b);
                        subtitleSelectorDialogFragment.show(getFragmentManager(), "overlay_fragment");
                    }
                    break;
                /*
                case R.id.trailerBlock:
                    Intent trailerIntent = new Intent(MovieDetailActivity.this, TrailerPlayerActivity.class);
                    if (!YouTubeData.isYouTubeUrl(mItem.trailer)) {
                        trailerIntent = new Intent(MovieDetailActivity.this, VideoPlayerActivity.class);
                    }
                    trailerIntent.putExtra(TrailerPlayerActivity.DATA, mItem);
                    trailerIntent.putExtra(TrailerPlayerActivity.LOCATION, mItem.trailer);
                    startActivity(trailerIntent);
                    break;
                */
                case R.id.playButton:
                    if (getFragmentManager().findFragmentByTag("overlay_fragment") != null)
                        return;

                    List<String> availableSeasonsStringList = new ArrayList<>();
                    final List<Integer> availableSeasons = new ArrayList<>();
                    for (String key : mItem.episodes.keySet()) {
                        if (!availableSeasons.contains(mItem.episodes.get(key).season)) {
                            availableSeasons.add(mItem.episodes.get(key).season);
                            availableSeasonsStringList.add(getString(R.string.season) + " " + ((Integer) mItem.episodes.get(key).season).toString());
                        }
                    }
                    Collections.sort(availableSeasonsStringList);
                    Collections.sort(availableSeasons);

                    openDialog(getString(R.string.season), availableSeasonsStringList.toArray(new String[availableSeasonsStringList.size()]), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int position) {
                            final int selectedSeason = availableSeasons.get(position);
                            final List<String> availableChapters = new ArrayList<>();
                            List<String> availableChaptersStringList = new ArrayList<>();
                            for (String key : mItem.episodes.keySet()) {
                                if (mItem.episodes.get(key).season == selectedSeason) {
                                    availableChapters.add(key);
                                    availableChaptersStringList.add(((Integer) mItem.episodes.get(key).episode).toString());
                                }
                            }

                            // sorting hack
                            Collections.sort(availableChapters, new Comparator<String>() {
                                @Override
                                public int compare(String lhs, String rhs) {
                                    Show.Episode lEpisode = mItem.episodes.get(lhs);
                                    Show.Episode rEpisode = mItem.episodes.get(rhs);

                                    return lEpisode.episode > rEpisode.episode ? 1 : -1;
                                }
                            });
                            Collections.sort(availableChaptersStringList, new Comparator<String>() {
                                @Override
                                public int compare(String lhs, String rhs) {
                                    int a = Integer.parseInt(lhs);
                                    int b = Integer.parseInt(rhs);
                                    if (a > b) {
                                        return 1;
                                    } else if (a < b) {
                                        return -1;
                                    } else {
                                        return 0;
                                    }
                                }
                            });

                            for (final ListIterator<String> iter = availableChaptersStringList.listIterator(); iter.hasNext(); ) {
                                final String element = iter.next();
                                iter.set(getString(R.string.episode) + " " + element);
                            }

                            dialog.dismiss();

                            openDialog(getString(R.string.episode), availableChaptersStringList.toArray(new String[availableChaptersStringList.size()]), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int position) {
                                    String key = availableChapters.get(position);
                                    Show.Episode episode = mItem.episodes.get(key);
                                    Media.Torrent torrent = episode.torrents.get(episode.torrents.keySet().toArray(new String[1])[0]);

                                    if (PrefUtils.get(ShowDetailActivity.this, Prefs.WIFI_ONLY,
											true) && !NetworkUtils.isWifiConnected(ShowDetailActivity.this) && NetworkUtils
											.isNetworkConnected(ShowDetailActivity.this)) {
                                        MessageDialogFragment.show(getFragmentManager(), R.string.wifi_only, R.string.wifi_only_message);
                                    } else {
                                        Intent streamIntent = new Intent(ShowDetailActivity.this, StreamLoadingActivity.class);
                                        streamIntent.putExtra(StreamLoadingActivity.STREAM_URL, torrent.url);
                                        streamIntent.putExtra(StreamLoadingActivity.QUALITY, key);
                                        streamIntent.putExtra(StreamLoadingActivity.SHOW, mItem);
                                        streamIntent.putExtra(StreamLoadingActivity.DATA, episode);
                                        if (mSubLanguage != null)
                                            streamIntent.putExtra(StreamLoadingActivity.SUBTITLES, mSubLanguage);
                                        startActivity(streamIntent);
                                    }
                                }
                            });
                        }
                    });

                    break;
            }

        }
    };

    private ViewTreeObserver.OnScrollChangedListener mOnScrollListener = new ViewTreeObserver.OnScrollChangedListener() {
        @Override
        public void onScrollChanged() {
            if (mToolbarHeight == 0) {
                mToolbarHeight = toolbar.getHeight();
                mHeaderHeight = mParallaxHeight - mToolbarHeight;
            }

            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) toolbar.getLayoutParams();

            if (scrollView.getScrollY() > mHeaderHeight) {
                if (mLastScrollLocation > scrollView.getScrollY()) {
                    // scroll up
                    if ((mOpenBarPos == null || !mOpenBar) && layoutParams.topMargin <= -mToolbarHeight)
                        mOpenBarPos = scrollView.getScrollY() - mToolbarHeight;
                    mOpenBar = true;
                } else if (mLastScrollLocation < scrollView.getScrollY()) {
                    // scroll down
                    if (mOpenBarPos == null || mOpenBar)
                        mOpenBarPos = scrollView.getScrollY();
                    mOpenBar = false;
                }

                if (layoutParams.topMargin <= 0) {
                    layoutParams.topMargin = mOpenBarPos - scrollView.getScrollY();
                }

                if (layoutParams.topMargin > 0) {
                    layoutParams.topMargin = 0;
                }
            }

            if (layoutParams.topMargin < 0) {
                scrollView.setOverScrollMode(View.OVER_SCROLL_NEVER);
            } else {
                scrollView.setOverScrollMode(View.OVER_SCROLL_IF_CONTENT_SCROLLS);
            }

                /* Fade out when over header */
            if (mParallaxHeight - scrollView.getScrollY() < 0) {
                if (mTransparentBar) {
                    mTransparentBar = false;
                    ActionBarBackground.changeColor(ShowDetailActivity.this, mPaletteColor, false);
                }
            } else {
                if (!mTransparentBar) {
                    mTransparentBar = true;
                    ActionBarBackground.fadeOut(ShowDetailActivity.this);
                }
            }

            toolbar.setLayoutParams(layoutParams);

            mLastScrollLocation = scrollView.getScrollY();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
		getWindow().getDecorView().setSystemUiVisibility( View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
		super.onCreate(savedInstanceState, R.layout.activity_moviedetail);
        setSupportActionBar(toolbar);
        setShowCasting(true);

        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ActionBarBackground.fadeOut(this);

        Drawable playButtonDrawable = PixelUtils.changeDrawableColor(this, R.drawable.ic_av_play_button, getResources().getColor(R.color.primary));
        if (mPlayButtonDrawable == null) playButton.setImageDrawable(playButtonDrawable);

        playButton.setOnClickListener(mOnClickListener);
        synopsisBlock.setOnClickListener(mOnClickListener);
        trailerBlock.setOnClickListener(mOnClickListener);
        subtitlesBlock.setOnClickListener(mOnClickListener);
        //favouriteBlock.setOnClickListener(mOnClickListener);
        qualityBlock.setOnClickListener(mOnClickListener);

        mParallaxHeight = (PixelUtils.getScreenHeight(this) / 3) * 2;
        parallax.getLayoutParams().height = mParallaxHeight;
        mToolbarHeight = toolbar.getHeight();
        mHeaderHeight = mParallaxHeight - mToolbarHeight;
        scrollView.getViewTreeObserver().addOnScrollChangedListener(mOnScrollListener);

        Intent intent = getIntent();
        mItem = intent.getParcelableExtra("item");

        mPaletteColor = intent.getIntExtra("palette", getResources().getColor(R.color.primary));
        mainInfoBlock.setBackgroundColor(mPaletteColor);
        mPlayButtonDrawable = PixelUtils.changeDrawableColor(ShowDetailActivity.this, R.drawable.ic_av_play_button, mPaletteColor);
        playButton.setImageDrawable(mPlayButtonDrawable);

        LogUtils.d(mItem.toString());
        titleText.setText(mItem.title);
        yearText.setText(mItem.year);
        ratingText.setText(mItem.rating + "/10");

        if (mItem.runtime != null && Integer.parseInt(mItem.runtime) > 0) {
            runtimeText.setText(mItem.runtime + " " + getString(R.string.minutes));
        } else {
            runtimeText.setText("n/a " + getString(R.string.minutes));
        }

        if (mItem.synopsis != null) {
            synopsisText.setText(mItem.synopsis);
        } else {
            synopsisBlock.setClickable(false);
        }

        trailerBlock.setVisibility(View.GONE);
        qualityBlock.setVisibility(View.GONE);
        subtitlesBlock.setVisibility(View.GONE);

        PopcornApplication.getPicasso().load(mItem.image).into(coverImage, new Callback() {
            @Override
            public void onSuccess() {
                int oldColor = mPaletteColor;
                if (mPaletteColor == getResources().getColor(R.color.primary)) {
                    Palette palette = Palette.generate(((BitmapDrawable) coverImage.getDrawable()).getBitmap());

                    int vibrantColor = palette.getVibrantColor(-1);
                    if (vibrantColor == -1) {
                        mPaletteColor = palette.getMutedColor(getResources().getColor(R.color.primary));
                    } else {
                        mPaletteColor = vibrantColor;
                    }
                }

                final ObjectAnimator mainInfoBlockColorFade = ObjectAnimator.ofObject(mainInfoBlock, "backgroundColor", new ArgbEvaluator(), oldColor, mPaletteColor);
                mainInfoBlockColorFade.setDuration(500);
                Drawable oldDrawable = PixelUtils.changeDrawableColor(ShowDetailActivity.this, R.drawable.ic_av_play_button, oldColor);
                mPlayButtonDrawable = PixelUtils.changeDrawableColor(ShowDetailActivity.this, R.drawable.ic_av_play_button, mPaletteColor);
                final TransitionDrawable td = new TransitionDrawable(new Drawable[]{oldDrawable, mPlayButtonDrawable});

                // Delay to make sure transition is smooth
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        playButton.setImageDrawable(td);
                        mainInfoBlockColorFade.start();
                        td.startTransition(500);
                        AnimUtils.fadeIn(coverImage);
                    }
                }, 1000);
            }

            @Override
            public void onError() {
                headerProgress.setVisibility(View.GONE);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mQuality == null) {
            //String[] keys = mItem.torrents.keySet().toArray(new String[mItem.torrents.size()]);
            //onQualitySelected(keys[0]);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        scrollView.getViewTreeObserver().removeOnScrollChangedListener(mOnScrollListener);
    }

    public void onQualitySelected(String quality) {
        mQuality = quality;
        qualityText.setText(mQuality);
    }

    public void onSubtitleLanguageSelected(String language) {
        mSubLanguage = language;
        if (!language.equals("no-subs")) {
            Locale locale;
            if (language.contains("-")) {
                locale = new Locale(language.substring(0, 2), language.substring(3, 5));
            } else {
                locale = new Locale(language);
            }
            subtitlesText.setText(locale.getDisplayName());
        } else {
            subtitlesText.setText(R.string.no_subs);
        }
    }

    public void openDialog(String title, String[] items, DialogInterface.OnClickListener onClickListener) {
        StringArraySelectorDialogFragment.show(getFragmentManager(), title, items, -1, onClickListener);
    }
}