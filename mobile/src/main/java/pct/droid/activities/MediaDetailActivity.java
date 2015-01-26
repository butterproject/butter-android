package pct.droid.activities;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.util.Pair;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import butterknife.InjectView;
import butterknife.Optional;
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
import pct.droid.base.utils.VersionUtils;
import pct.droid.dialogfragments.MessageDialogFragment;
import pct.droid.fragments.BaseDetailFragment;
import pct.droid.fragments.MovieDetailFragment;
import pct.droid.fragments.ShowDetailFragment;
import pct.droid.utils.ActionBarBackground;

public class MediaDetailActivity extends BaseActivity implements BaseDetailFragment.FragmentListener {

    public static final String DATA = "item";
    public static final String COLOR = "palette";

    private Integer mLastScrollLocation = 0, mVisibleBarPos, mHeaderHeight = 0, mToolbarHeight = 0, mTopHeight, mPaletteColor;
    private Boolean mTransparentBar = true, mVisibleBar = true, mIsTablet = false;
    private Fragment mFragment;

    @InjectView(R.id.toolbar)
    Toolbar mToolbar;
    @InjectView(R.id.scrollview)
    ScrollView mScrollView;
    @Optional
    @InjectView(R.id.parallax)
    RelativeLayout mParallaxLayout;
    @InjectView(R.id.content)
    FrameLayout mContent;
    @InjectView(R.id.logo)
    ImageView mLogo;
    @InjectView(R.id.header_progress)
    ProgressBar mProgress;
    @InjectView(R.id.bg_image)
    ImageView mBgImage;

    public static void startActivity(Activity activity, Media media, int paletteColor) {
        Intent intent = new Intent(activity, MediaDetailActivity.class);
        if (paletteColor != -1) intent.putExtra("palette", paletteColor);
        intent.putExtra("item", media);
        activity.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        super.onCreate(savedInstanceState, R.layout.activity_mediadetail);
        setSupportActionBar(mToolbar);

        // Set transparent toolbar
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ActionBarBackground.fadeOut(this);

        // mParallaxLayout doesn't exist? Then this is a tablet or big screen device
        mIsTablet = mParallaxLayout == null;

        // Calculate toolbar scrolling variables
        if(!mIsTablet) {
            mParallaxLayout.getLayoutParams().height = mTopHeight = PixelUtils.getScreenHeight(this);
            ((LinearLayout.LayoutParams) mContent.getLayoutParams()).topMargin = -(mTopHeight / 3);
            mContent.setMinimumHeight(mTopHeight / 3);
        } else {
            mTopHeight = (PixelUtils.getScreenHeight(this) / 2);
            ((LinearLayout.LayoutParams) mContent.getLayoutParams()).topMargin = mTopHeight;
            mContent.setMinimumHeight(mTopHeight);
        }

        if(VersionUtils.isLollipop()) {
            int navigationBarHeight = PixelUtils.getNavigationBarHeight(this);
            mContent.setPadding(mContent.getPaddingLeft(), mContent.getPaddingTop(), mContent.getPaddingRight(), mContent.getPaddingBottom() + navigationBarHeight);
        }

        mScrollView.getViewTreeObserver().addOnScrollChangedListener(mOnScrollListener);

        Intent intent = getIntent();
        mPaletteColor = intent.getIntExtra(COLOR, getResources().getColor(R.color.primary));
        Media media = intent.getParcelableExtra(DATA);

        mFragment = null;
        if(media instanceof Movie) {
            mFragment = MovieDetailFragment.newInstance((Movie)media, mPaletteColor);
        } else if(media instanceof Show) {
            mFragment = ShowDetailFragment.newInstance((Show) media, mPaletteColor);
        }

        if(mFragment != null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.content, mFragment).commit();
        }

        String imageUrl = media.image;
        if(mIsTablet || !PixelUtils.screenIsPortrait(this)) {
            imageUrl = media.headerImage;
        }
        Picasso.with(this).load(imageUrl).into(mBgImage, new Callback() {
            @Override
            public void onSuccess() {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        AnimUtils.fadeIn(mBgImage);
                        mLogo.setVisibility(View.GONE);
                        mProgress.setVisibility(View.GONE);
                    }
                });
            }

            @Override
            public void onError() {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mProgress.setVisibility(View.GONE);
                    }
                });
            }
        });
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
            if (VersionUtils.isLollipop()) {
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
                if(!mIsTablet) {
                    mHeaderHeight = mTopHeight - mToolbarHeight;
                } else {
                    mHeaderHeight = mTopHeight + mToolbarHeight;
                }
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
            if (mTopHeight - mScrollView.getScrollY() < 0) {
                if (mTransparentBar) {
                    mTransparentBar = false;
                    ActionBarBackground.changeColor(MediaDetailActivity.this, mPaletteColor, true);
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