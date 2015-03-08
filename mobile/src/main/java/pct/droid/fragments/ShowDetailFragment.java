package pct.droid.fragments;

import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Layout;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.astuetz.PagerSlidingTabStrip;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.Optional;
import pct.droid.R;
import pct.droid.adapters.ShowDetailPagerAdapter;
import pct.droid.base.providers.media.models.Episode;
import pct.droid.base.providers.media.models.Show;
import pct.droid.base.utils.PixelUtils;
import pct.droid.base.utils.VersionUtils;
import pct.droid.dialogfragments.StringArraySelectorDialogFragment;
import pct.droid.dialogfragments.SynopsisDialogFragment;
import pct.droid.widget.ObservableParallaxScrollView;
import pct.droid.widget.WrappingViewPager;

public class ShowDetailFragment extends BaseDetailFragment {

    private Show mShow;
    private Boolean mIsTablet = false;

    @InjectView(R.id.pager)
    WrappingViewPager mViewPager;
    @InjectView(R.id.tabs)
    PagerSlidingTabStrip mTabs;
    @Optional
    @InjectView(R.id.background)
    View mBackground;
    @Optional
    @InjectView(R.id.top)
    View mShadow;
    @Optional
    @InjectView(R.id.title)
    TextView mTitle;
    @Optional
    @InjectView(R.id.aired)
    TextView mMeta;
    @Optional
    @InjectView(R.id.synopsis)
    TextView mSynopsis;
    @Optional
    @InjectView(R.id.read_more)
    TextView mReadMore;
    @Optional
    @InjectView(R.id.rating)
    RatingBar mRating;
    @Optional
    @InjectView(R.id.cover_image)
    ImageView mCoverImage;

    public static ShowDetailFragment newInstance(Show show, int color) {
        Bundle b = new Bundle();
        b.putParcelable(DATA, show);
        b.putInt(COLOR, color);
        ShowDetailFragment showDetailFragment = new ShowDetailFragment();
        showDetailFragment.setArguments(b);
        return showDetailFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mShow = getArguments().getParcelable(DATA);
        mPaletteColor = getArguments().getInt(COLOR, getResources().getColor(R.color.primary));
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mRoot = inflater.inflate(R.layout.fragment_showdetail, container, false);
        ButterKnife.inject(this, mRoot);
        if(VersionUtils.isJellyBean() && container != null) {
            int minHeight = container.getMinimumHeight() + PixelUtils.getPixelsFromDp(mActivity, 48);
            mRoot.setMinimumHeight(minHeight);
            mViewPager.setMinimumHeight(minHeight);
        }

        mIsTablet = mCoverImage != null;

        List<Fragment> fragments = new ArrayList<>();
        if(mIsTablet) {
            Double rating = Double.parseDouble(mShow.rating);
            mTitle.setText(mShow.title);
            mRating.setProgress(rating.intValue());

            String metaDataStr = mShow.year;

            if (mShow.status != null) {
                metaDataStr += " • ";
                if (mShow.status == Show.Status.CONTINUING) {
                    metaDataStr += getString(R.string.continuing);
                } else {
                    metaDataStr += getString(R.string.ended);
                }
            }

            if (!TextUtils.isEmpty(mShow.genre)) {
                metaDataStr += " • ";
                metaDataStr += mShow.genre;
            }

            mMeta.setText(metaDataStr);

            if (!TextUtils.isEmpty(mShow.synopsis)) {
                mSynopsis.setText(mShow.synopsis);
                mSynopsis.post(new Runnable() {
                    @Override
                    public void run() {
                        boolean ellipsized = false;
                        Layout layout = mSynopsis.getLayout();
                        if (layout == null) return;
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

            Picasso.with(mCoverImage.getContext()).load(mShow.image).into(mCoverImage);
            mTabs.setIndicatorColor(mPaletteColor);
        } else {
            mBackground.post(new Runnable() {
                @Override
                public void run() {
                    mBackground.getLayoutParams().height = mBackground.getLayoutParams().height - mTabs.getHeight();
                }
            });
            fragments.add(ShowDetailAboutFragment.newInstance(mShow));
        }

        final List<Integer> availableSeasons = new ArrayList<>();
        for (Episode episode : mShow.episodes) {
            if (!availableSeasons.contains(episode.season)) {
                availableSeasons.add(episode.season);
            }
        }
        Collections.sort(availableSeasons);

        for(int seasonInt : availableSeasons) {
            fragments.add(ShowDetailSeasonFragment.newInstance(mShow, seasonInt, mPaletteColor));
        }
        ShowDetailPagerAdapter fragmentPagerAdapter = new ShowDetailPagerAdapter(mActivity, getChildFragmentManager(), fragments);

        mViewPager.setAdapter(fragmentPagerAdapter);
        mTabs.setViewPager(mViewPager);

        mActivity.setSubScrollListener(mOnScrollListener);

        return mRoot;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mActivity.setSubScrollListener(null);
    }

    @Optional
    @OnClick(R.id.read_more)
    public void openReadMore(View v) {
        if (getFragmentManager().findFragmentByTag("overlay_fragment") != null)
            return;
        SynopsisDialogFragment synopsisDialogFragment = new SynopsisDialogFragment();
        Bundle b = new Bundle();
        b.putString("text", mShow.synopsis);
        synopsisDialogFragment.setArguments(b);
        synopsisDialogFragment.show(getFragmentManager(), "overlay_fragment");
    }

    public void openDialog(String title, String[] items, DialogInterface.OnClickListener onClickListener) {
        StringArraySelectorDialogFragment.show(mActivity.getSupportFragmentManager(), title, items, -1, onClickListener);
    }

    private ObservableParallaxScrollView.Listener mOnScrollListener = new ObservableParallaxScrollView.Listener() {
        @Override
        public void onScroll(int scrollY, ObservableParallaxScrollView.Direction direction) {
            if(!mIsTablet) {
                if(scrollY > 0) {
                    int headerHeight = mActivity.getHeaderHeight();
                    if (scrollY < headerHeight) {
                        float alpha = 1.0f - ((float) scrollY / (float) headerHeight);
                        mShadow.setAlpha(alpha);
                        mTabs.setBackgroundColor(mActivity.getResources().getColor(android.R.color.transparent));
                        mTabs.setTranslationY(0);
                    } else {
                        mShadow.setAlpha(0);
                        mTabs.setBackgroundColor(mPaletteColor);
                        mTabs.setTranslationY(scrollY - headerHeight);
                    }
                }
            }
        }
    };

}
