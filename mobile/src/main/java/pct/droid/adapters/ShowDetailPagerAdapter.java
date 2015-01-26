package pct.droid.adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

import pct.droid.R;
import pct.droid.fragments.ShowDetailAboutFragment;

public class ShowDetailPagerAdapter extends FragmentPagerAdapter {

    private List<Fragment> mFragments = new ArrayList<>();
    private Context mContext;
    private Boolean mHasAbout = false;

    public ShowDetailPagerAdapter(Context context, FragmentManager fm, List<Fragment> fragments) {
        super(fm);
        mFragments = fragments;
        mContext = context;

        if(mFragments.size() > 0)
            mHasAbout = mFragments.get(0) instanceof ShowDetailAboutFragment;
    }

    public void setFragments(List<Fragment> fragments) {
        mFragments = fragments;

        if(mFragments.size() > 0)
            mHasAbout = mFragments.get(0) instanceof ShowDetailAboutFragment;
    }

    @Override
    public Fragment getItem(int position) {
        return mFragments.get(position);
    }

    @Override
    public int getCount() {
        return mFragments.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        int season = position;
        if(mHasAbout) {
            if(season == 0)
                return mContext.getString(R.string.about);
        } else {
            season = season + 1;
        }
        return mContext.getString(R.string.season) + " " + season;
    }

}
