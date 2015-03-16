package pct.droid.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.astuetz.PagerSlidingTabStrip;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import pct.droid.R;
import pct.droid.base.providers.media.MediaProvider;

/**
 * Fragment that contains a viewpager tabs for {@link pct.droid.fragments.MediaListFragment}
 */
public class MediaContainerFragment extends Fragment {

    public static final String PROVIDER_EXTRA = "provider";

    @InjectView(R.id.tabs)
    PagerSlidingTabStrip mTabs;
    @InjectView(R.id.pager)
    ViewPager mViewPager;
    private MediaPagerAdapter mAdapter;

    public static MediaContainerFragment newInstance(MediaProvider provider) {
        MediaContainerFragment frag = new MediaContainerFragment();
        Bundle args = new Bundle();
        args.putParcelable(PROVIDER_EXTRA, provider);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_media_container, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.inject(this, view);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        MediaProvider provider = getArguments().getParcelable(PROVIDER_EXTRA);

        mAdapter = new MediaPagerAdapter(provider, getChildFragmentManager(), provider.getNavigation());
        mViewPager.setAdapter(mAdapter);
        mTabs.setViewPager(mViewPager);
        mViewPager.setCurrentItem(1);
    }

    public static class MediaPagerAdapter extends FragmentPagerAdapter {

        private final List<MediaProvider.NavInfo> mTabs;
        private MediaProvider mProvider;

        public MediaPagerAdapter(MediaProvider provider, FragmentManager fm, List<MediaProvider.NavInfo> tabs) {
            super(fm);
            mTabs = tabs;
            mProvider = provider;
        }

        @Override
        public int getCount() {
            return mTabs.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return String.valueOf(mTabs.get(position).getLabel());
        }

        @Override
        public Fragment getItem(int position) {
            return MediaListFragment.newInstance(MediaListFragment.Mode.NORMAL, mProvider, mTabs.get(position).getFilter(), mTabs.get(position).getOrder()); //create new fragment instance
        }

    }
}
