package pct.droid.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.design.widget.TabLayout;

import butterknife.ButterKnife;
import butterknife.InjectView;
import pct.droid.R;
import pct.droid.activities.MainActivity;
import pct.droid.adapters.MediaPagerAdapter;
import pct.droid.base.providers.media.MediaProvider;

/**
 * Fragment that contains a viewpager tabs for {@link pct.droid.fragments.MediaListFragment}
 */
public class MediaContainerFragment extends Fragment {

    public static final String EXTRA_PROVIDER = "provider";

    private MediaPagerAdapter mAdapter;

    @InjectView(R.id.pager)
    ViewPager mViewPager;

    public static MediaContainerFragment newInstance(MediaProvider provider) {
        MediaContainerFragment frag = new MediaContainerFragment();
        Bundle args = new Bundle();
        args.putParcelable(EXTRA_PROVIDER, provider);
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

        MediaProvider provider = getArguments().getParcelable(EXTRA_PROVIDER);

        mAdapter = new MediaPagerAdapter(provider, getChildFragmentManager(), provider.getNavigation());

        mViewPager.setAdapter(mAdapter);

        ((MainActivity) getActivity()).updateTabs(provider.getDefaultNavigationIndex());
    }

    public ViewPager getViewPager() {
        return mViewPager;
    }

}
