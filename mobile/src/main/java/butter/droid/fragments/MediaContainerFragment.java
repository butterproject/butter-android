package butter.droid.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import javax.inject.Inject;

import butter.droid.MobileButterApplication;
import butter.droid.R;
import butter.droid.ui.main.MainActivity;
import butter.droid.ui.medialist.list.MediaPagerAdapter;
import butter.droid.base.manager.provider.ProviderManager;
import butter.droid.base.providers.media.MediaProvider;
import butter.droid.ui.medialist.base.BaseMediaListFragment;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Fragment that contains a viewpager tabs for {@link BaseMediaListFragment}
 */
public class MediaContainerFragment extends Fragment {

    @Inject ProviderManager providerManager;
    private MediaPagerAdapter mAdapter;
    private Integer mSelection = 0;

    @BindView(R.id.pager) ViewPager mViewPager;

    public static MediaContainerFragment newInstance() {
        return new MediaContainerFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_media_container, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        MobileButterApplication.getAppContext()
                .getComponent()
                .inject(this);

        MediaProvider mediaProvider = providerManager.getCurrentMediaProvider();
        mAdapter = new MediaPagerAdapter(mediaProvider, getChildFragmentManager(), mediaProvider.getNavigation());
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
        mSelection = mediaProvider.getDefaultNavigationIndex();
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
