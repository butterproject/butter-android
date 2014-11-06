package pct.droid.activities;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.internal.LinkedTreeMap;
import com.nineoldandroids.animation.ArgbEvaluator;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nirhart.parallaxscroll.views.ParallaxScrollView;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import butterknife.InjectView;
import pct.droid.R;
import pct.droid.providers.media.YTSProvider;
import pct.droid.utils.ActionBarBackground;
import pct.droid.utils.LogUtils;
import pct.droid.utils.PixelUtils;

public class MovieDetailActivity extends BaseActivity {

    private Drawable mPlayButtonDrawable;
    private Integer mLastScrollLocation = 0, mPaletteColor = R.color.primary, mOpenBarPos, mHeaderHeight, mToolbarHeight, mParallaxHeight;
    private Boolean mTransparentBar = true, mOpenBar = true, mIsFavourited = false;

    @InjectView(R.id.toolbar)
    Toolbar toolbar;
    @InjectView(R.id.scrollView)
    ParallaxScrollView scrollView;
    @InjectView(R.id.coverImage)
    ImageView coverImage;
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
    @InjectView(R.id.favouriteText)
    TextView favouriteText;
    @InjectView(R.id.synopsisBlock)
    LinearLayout synopsisBlock;
    @InjectView(R.id.qualityBlock)
    LinearLayout qualityBlock;
    @InjectView(R.id.favouriteBlock)
    LinearLayout favouriteBlock;
    @InjectView(R.id.trailerBlock)
    LinearLayout trailerBlock;
    @InjectView(R.id.subtitlesBlock)
    LinearLayout subtitlesBlock;

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

        }
    };

    private ViewTreeObserver.OnScrollChangedListener mOnScrollListener = new ViewTreeObserver.OnScrollChangedListener() {
        @Override
        public void onScrollChanged() {
            if(mToolbarHeight == 0) {
                mToolbarHeight = toolbar.getHeight();
                mHeaderHeight = mParallaxHeight - mToolbarHeight;
            }
            
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) toolbar.getLayoutParams();

            if(scrollView.getScrollY() > mHeaderHeight) {
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

                if (layoutParams.topMargin <= 0)
                    layoutParams.topMargin = mOpenBarPos - scrollView.getScrollY();

                if (layoutParams.topMargin > 0) {
                    layoutParams.topMargin = 0;
                }
            }

                /* Fade out when over header */
            if(mParallaxHeight - scrollView.getScrollY() < 0) {
                if(mTransparentBar) {
                    mTransparentBar = false;
                    ActionBarBackground.changeColor(MovieDetailActivity.this, mPaletteColor, false);
                }
            } else {
                if(!mTransparentBar) {
                    mTransparentBar = true;
                    ActionBarBackground.fadeOut(MovieDetailActivity.this);
                }
            }

            toolbar.setLayoutParams(layoutParams);

            mLastScrollLocation = scrollView.getScrollY();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, R.layout.activity_moviedetail);
        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ActionBarBackground.fadeOut(this);

        Drawable playButtonDrawable = PixelUtils.changeDrawableColor(this, R.drawable.ic_av_play_button, getResources().getColor(R.color.primary));
        if(mPlayButtonDrawable == null) playButton.setImageDrawable(playButtonDrawable);

        playButton.setOnClickListener(mOnClickListener);
        synopsisBlock.setOnClickListener(mOnClickListener);
        trailerBlock.setOnClickListener(mOnClickListener);
        subtitlesBlock.setOnClickListener(mOnClickListener);
        favouriteBlock.setOnClickListener(mOnClickListener);
        qualityBlock.setOnClickListener(mOnClickListener);

        mParallaxHeight = PixelUtils.getPixelsFromDp(this, 228);
        mToolbarHeight = toolbar.getHeight();
        mHeaderHeight = mParallaxHeight - mToolbarHeight;
        scrollView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        scrollView.getViewTreeObserver().addOnScrollChangedListener(mOnScrollListener);

        final YTSProvider.Video item = getIntent().getParcelableExtra("item");
        LogUtils.d("MovieDetailActivity", getIntent().getExtras());
        titleText.setText(item.title);
        yearText.setText(item.year);
        ratingText.setText(item.rating + "/10");

        if(item.runtime != null) {
            runtimeText.setText(Integer.toString(item.runtime) + " " + getString(R.string.minutes));
        }

        if(item.synopsis != null) {
            synopsisText.setText(item.synopsis);
        }

        Picasso.with(this).load(item.image).into(new Target() {
            @Override
            public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
                Palette palette = Palette.generate(bitmap);

                    int vibrantColor = palette.getVibrantColor(R.color.primary);
                    if (vibrantColor == R.color.primary) {
                        mPaletteColor = palette.getMutedColor(R.color.primary);
                    } else {
                        mPaletteColor = vibrantColor;
                    }

                    final ObjectAnimator mainInfoBlockColorFade = ObjectAnimator.ofObject(mainInfoBlock, "backgroundColor", new ArgbEvaluator(), getResources().getColor(R.color.primary), mPaletteColor);
                    mainInfoBlockColorFade.setDuration(500);
                    Drawable oldDrawable = PixelUtils.changeDrawableColor(MovieDetailActivity.this, R.drawable.ic_av_play_button, getResources().getColor(R.color.primary));
                    mPlayButtonDrawable = PixelUtils.changeDrawableColor(MovieDetailActivity.this, R.drawable.ic_av_play_button, mPaletteColor);
                    final TransitionDrawable td = new TransitionDrawable(new Drawable[]{oldDrawable, mPlayButtonDrawable});

                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            playButton.setImageDrawable(td);
                            Picasso.with(MovieDetailActivity.this).load(item.headerImage).into(coverImage, new com.squareup.picasso.Callback() {
                                @Override
                                public void onSuccess() {
                                    Animation fadeInAnim = AnimationUtils.loadAnimation(MovieDetailActivity.this, R.anim.fade_in);

                                    mainInfoBlockColorFade.start();
                                    td.startTransition(500);
                                    coverImage.setVisibility(View.VISIBLE);
                                    coverImage.startAnimation(fadeInAnim);
                                }

                                @Override
                                public void onError() {

                                }
                            });
                        }
                    });
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {}

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {}
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        scrollView.getViewTreeObserver().removeOnScrollChangedListener(mOnScrollListener);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
