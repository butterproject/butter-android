package pct.droid.fragments;

import android.content.Context;
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

import java.util.ArrayList;
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

        List<TabInfo> tabs = new ArrayList<>();
        tabs.add(new TabInfo(MediaProvider.Filters.Sort.DATE,getString(R.string.release_date)));
        tabs.add(new TabInfo(MediaProvider.Filters.Sort.POPULARITY,getString(R.string.popular_now)));
        tabs.add(new TabInfo(MediaProvider.Filters.Sort.RATING,getString(R.string.top_rated)));
        tabs.add(new TabInfo(MediaProvider.Filters.Sort.YEAR,getString(R.string.year)));
        tabs.add(new TabInfo(MediaProvider.Filters.Sort.ALPHABET,getString(R.string.a_to_z)));
        /*
        switch (provider){
            case 0:
                //movies
                tabs.add(new TabInfo(MediaProvider.Filters.Sort.DATE,getString(R.string.release_date)));
                tabs.add(new TabInfo(MediaProvider.Filters.Sort.POPULARITY,getString(R.string.popular_now)));
                tabs.add(new TabInfo(MediaProvider.Filters.Sort.RATING,getString(R.string.top_rated)));
                tabs.add(new TabInfo(MediaProvider.Filters.Sort.YEAR,getString(R.string.year)));
                tabs.add(new TabInfo(MediaProvider.Filters.Sort.ALPHABET,getString(R.string.a_to_z)));
                break;
            case 1:
                tabs.add(new TabInfo(MediaProvider.Filters.Sort.POPULARITY,getString(R.string.popular_now)));
                tabs.add(new TabInfo(MediaProvider.Filters.Sort.DATE,getString(R.string.release_date)));
                tabs.add(new TabInfo(MediaProvider.Filters.Sort.RATING,getString(R.string.top_rated)));
                tabs.add(new TabInfo(MediaProvider.Filters.Sort.YEAR,getString(R.string.year)));
                tabs.add(new TabInfo(MediaProvider.Filters.Sort.ALPHABET,getString(R.string.a_to_z)));
                //shows
                break;
        }
        */
        mAdapter = new MediaPagerAdapter(provider, getChildFragmentManager(), tabs);
        mViewPager.setAdapter(mAdapter);
        mTabs.setViewPager(mViewPager);
        mViewPager.setCurrentItem(1);
    }

    public static class MediaPagerAdapter extends FragmentPagerAdapter {

        private final List<TabInfo> mFilters;
        private MediaProvider mProvider;

        public MediaPagerAdapter(MediaProvider provider, FragmentManager fm, List<TabInfo> filters) {
            super(fm);
            mFilters = filters;
            mProvider = provider;
        }

        @Override
        public int getCount() {
            return mFilters.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return String.valueOf(mFilters.get(position).getLabel());
        }

        @Override
        public Fragment getItem(int position) {
            return MediaListFragment.newInstance(MediaListFragment.Mode.NORMAL, mProvider, mFilters.get(position).getFilter()); //create new fragment instance
        }

    }

    public static class TabInfo {
        private MediaProvider.Filters.Sort mSort;
        private String mLabel;

        public TabInfo(MediaProvider.Filters.Sort mSort, String mLabel) {
            this.mSort = mSort;
            this.mLabel = mLabel;
        }

        public MediaProvider.Filters.Sort getFilter() {
            return mSort;
        }

        public String getLabel() {
            return mLabel;
        }
    }
}
