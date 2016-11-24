package butter.droid.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butter.droid.R;
import butter.droid.activities.MainActivity;
import butter.droid.adapters.MediaPagerAdapter;
import butter.droid.base.providers.media.MediaProvider;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Fragment that contains a viewpager tabs for {@link butter.droid.fragments.MediaListFragment}
 */
public class MediaContainerFragment extends Fragment {

    public static final String EXTRA_PROVIDER = "provider";

    private Integer mSelection = 0;

    @BindView(R.id.pager)
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
        ButterKnife.bind(this, view);

        MediaProvider mProvider = getArguments().getParcelable(EXTRA_PROVIDER);
        MediaPagerAdapter mAdapter = new MediaPagerAdapter(mProvider, getChildFragmentManager(), mProvider.getNavigation());
        mViewPager.setAdapter(mAdapter);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                mSelection = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        mSelection = mProvider.getDefaultNavigationIndex();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ((MainActivity) getActivity()).updateTabs(this, mSelection);
    }

    public ViewPager getViewPager() {
        return mViewPager;
    }

    public Integer getCurrentSelection() {
        return mSelection;
    }

}
