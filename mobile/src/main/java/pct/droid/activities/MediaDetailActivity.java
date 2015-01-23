package pct.droid.activities;

import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.util.Pair;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.nirhart.parallaxscroll.views.ParallaxScrollView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import butterknife.InjectView;
import butterknife.OnClick;
import pct.droid.R;
import pct.droid.base.fragments.BaseStreamLoadingFragment;
import pct.droid.base.preferences.Prefs;
import pct.droid.base.providers.media.models.Media;
import pct.droid.base.providers.media.models.Movie;
import pct.droid.base.providers.media.models.Show;
import pct.droid.base.utils.AnimUtils;
import pct.droid.base.utils.NetworkUtils;
import pct.droid.base.utils.PixelUtils;
import pct.droid.base.utils.PrefUtils;
import pct.droid.base.utils.VersionUtil;
import pct.droid.dialogfragments.MessageDialogFragment;
import pct.droid.fragments.BaseDetailFragment;
import pct.droid.fragments.MovieDetailFragment;
import pct.droid.utils.ActionBarBackground;

public class MediaDetailActivity extends BaseActivity implements BaseDetailFragment.FragmentListener {

    public static final String DATA = "item";
    public static final String COLOR = "palette";

    private Integer mLastScrollLocation = 0, mPaletteColor, mVisibleBarPos, mHeaderHeight, mToolbarHeight, mParallaxHeight;
    private Boolean mTransparentBar = true, mVisibleBar = true;
    private Drawable mPlayButtonDrawable;
    private Fragment mFragment;

    @InjectView(R.id.toolbar)
    Toolbar mToolbar;
    @InjectView(R.id.scrollview)
    ParallaxScrollView mScrollView;
    @InjectView(R.id.parallax)
    RelativeLayout mParallaxLayout;
    @InjectView(R.id.logo)
    ImageView mLogo;
    @InjectView(R.id.header_progress)
    ProgressBar mProgress;
    @InjectView(R.id.bg_image)
    ImageView mBgImage;
    @InjectView(R.id.play_button)
    ImageButton mPlayFab;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        super.onCreate(savedInstanceState, R.layout.activity_mediadetail);
        setSupportActionBar(mToolbar);

        // Set transparent toolbar
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ActionBarBackground.fadeOut(this);

        // Calculate toolbar scrolling variables
        mParallaxHeight = (PixelUtils.getScreenHeight(this) / 3) * 2;
        mParallaxLayout.getLayoutParams().height = mParallaxHeight;

        mToolbarHeight = mToolbar.getHeight();
        mHeaderHeight = mParallaxHeight - mToolbarHeight;
        mScrollView.getViewTreeObserver().addOnScrollChangedListener(mOnScrollListener);

        Intent intent = getIntent();

        mPaletteColor = intent.getIntExtra(COLOR, getResources().getColor(R.color.primary));
        mPlayButtonDrawable = PixelUtils.changeDrawableColor(MediaDetailActivity.this, R.drawable.play_button_circle, mPaletteColor);
        mPlayFab.setBackgroundDrawable(mPlayButtonDrawable);

        Media media = intent.getParcelableExtra(DATA);

        mFragment = null;
        if(media instanceof Movie) {
            mFragment = MovieDetailFragment.newInstance((Movie)media);
        } else if(media instanceof Show) {
            mPlayFab.setVisibility(View.GONE);
            //mFragment = ShowDetailFragment.newInstance((Show)media);
        }

        if(mFragment != null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.frame_layout, mFragment).commit();
        }

        Picasso.with(this).load(media.image).into(mBgImage, new Callback() {
            @Override
            public void onSuccess() {
                final TransitionDrawable td;
                if (mPaletteColor == getResources().getColor(R.color.primary)) {
                    Palette palette = Palette.generate(((BitmapDrawable) mBgImage.getDrawable()).getBitmap());

                    int vibrantColor = palette.getVibrantColor(-1);
                    if (vibrantColor == -1) {
                        mPaletteColor = palette.getMutedColor(getResources().getColor(R.color.primary));
                    } else {
                        mPaletteColor = vibrantColor;
                    }

                    Drawable oldDrawable = mPlayButtonDrawable;
                    mPlayButtonDrawable = PixelUtils.changeDrawableColor(MediaDetailActivity.this, R.drawable.ic_av_play_button, mPaletteColor);
                    td = new TransitionDrawable(new Drawable[]{oldDrawable, mPlayButtonDrawable});
                } else {
                    td = null;
                }

                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (td != null) {
                            mPlayFab.setImageDrawable(td);
                            td.startTransition(500);
                        }
                        AnimUtils.fadeIn(mBgImage);
                        mLogo.setVisibility(View.GONE);
                        mProgress.setVisibility(View.GONE);
                    }
                }, 1000);
            }

            @Override
            public void onError() {
                mProgress.setVisibility(View.GONE);
            }
        });
    }

    @OnClick(R.id.play_button)
    public void play(View v) {
        if(mFragment instanceof MovieDetailFragment) {
            ((MovieDetailFragment) mFragment).play();
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void playStream(BaseStreamLoadingFragment.StreamInfo streamInfo) {
        if (PrefUtils.get(this, Prefs.WIFI_ONLY, true) &&
                !NetworkUtils.isWifiConnected(this) &&
                NetworkUtils.isNetworkConnected(this)) {
            MessageDialogFragment.show(getFragmentManager(), R.string.wifi_only, R.string.wifi_only_message);
        } else {
            Intent i = new Intent(this, StreamLoadingActivity.class);
            i.putExtra(StreamLoadingActivity.EXTRA_INFO, streamInfo);
            if (VersionUtil.isLollipop()) {
                StreamLoadingActivity.startActivity(this, streamInfo, Pair.create((View) mBgImage, mBgImage.getTransitionName()));
            } else {
                StreamLoadingActivity.startActivity(this, streamInfo);
            }
        }
    }

    /* The scroll listener makes the toolbar scroll off the screen when the user scroll all the way down. And it appears again on scrolling up. */
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
                    if ((mVisibleBarPos == null || !mVisibleBar) && layoutParams.topMargin <= -mToolbarHeight)
                        mVisibleBarPos = mScrollView.getScrollY() - mToolbarHeight;
                    mVisibleBar = true;
                } else if (mLastScrollLocation < mScrollView.getScrollY()) {
                    // scroll down
                    if (mVisibleBarPos == null || mVisibleBar)
                        mVisibleBarPos = mScrollView.getScrollY();
                    mVisibleBar = false;
                }

                if (layoutParams.topMargin <= 0) {
                    layoutParams.topMargin = mVisibleBarPos - mScrollView.getScrollY();
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
                    ActionBarBackground.changeColor(MediaDetailActivity.this, mPaletteColor, false);
                }
            } else {
                if (!mTransparentBar) {
                    mTransparentBar = true;
                    ActionBarBackground.fadeOut(MediaDetailActivity.this);
                }
            }

            mToolbar.setLayoutParams(layoutParams);

            mLastScrollLocation = mScrollView.getScrollY();
        }
    };
}