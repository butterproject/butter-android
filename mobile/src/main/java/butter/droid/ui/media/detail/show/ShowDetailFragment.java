/*
 * This file is part of Butter.
 *
 * Butter is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Butter is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Butter. If not, see <http://www.gnu.org/licenses/>.
 */

package butter.droid.ui.media.detail.show;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import butter.droid.R;
import butter.droid.base.providers.model.MediaWrapper;
import butter.droid.base.utils.PixelUtils;
import butter.droid.ui.media.detail.MediaDetailActivity;
import butter.droid.ui.media.detail.show.about.ShowDetailAboutFragment;
import butter.droid.ui.media.detail.show.pager.ShowDetailPagerAdapter;
import butter.droid.ui.media.detail.show.pager.model.UiShowDetailItem;
import butterknife.BindView;
import butterknife.ButterKnife;
import java.util.List;
import javax.inject.Inject;
import org.parceler.Parcels;

public class ShowDetailFragment extends Fragment implements ShowDetailView {

    private static final String ARG_SHOW = "butter.droid.ui.media.detail.show.ShowDetailFragment.show";

    @Inject ShowDetailPresenter presenter;


    @BindView(R.id.pager) ViewPager viewPager;
    @BindView(R.id.tabs) TabLayout tabs;
    @Nullable @BindView(R.id.about_content) ViewGroup aboutContainer;
    @Nullable @BindView(R.id.background) View background;

    private ShowDetailComponent component;
    private ShowDetailPagerAdapter pagerAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        component = ((MediaDetailActivity) getActivity()).getComponent()
                .showDetailComponentBuilder()
                .showDetailModule(new ShowDetailModule(this))
                .build();
        component.inject(this);

        MediaWrapper show = Parcels.unwrap(getArguments().getParcelable(ARG_SHOW));

        presenter.onCreate(show);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_showdetail, container, false);
        ButterKnife.bind(this, view);

        if (container != null) {
            int minHeight = container.getMinimumHeight() + PixelUtils.getPixelsFromDp(getContext(), 48);
            view.setMinimumHeight(minHeight);
            viewPager.setMinimumHeight(minHeight);
        }

        tabs.setTabGravity(TabLayout.GRAVITY_CENTER);

        return view;

    }

    @Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        pagerAdapter = new ShowDetailPagerAdapter(getContext(), getChildFragmentManager());
        viewPager.setAdapter(pagerAdapter);
        tabs.setupWithViewPager(viewPager);
        tabs.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(viewPager));
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabs));

        boolean isTablet = aboutContainer != null;

        if (!isTablet) {
            background.post(() -> background.getLayoutParams().height = background.getLayoutParams().height - tabs.getHeight());
        }

        presenter.viewCreated(isTablet);
    }

    @Override public void displayData(@NonNull MediaWrapper show, List<UiShowDetailItem> items) {
        if (items.size() == 1) {
            tabs.setTabMode(TabLayout.MODE_FIXED);
        } else {
            tabs.setTabMode(TabLayout.MODE_SCROLLABLE);
        }

        pagerAdapter.setData(show, items);

        if (show.hasColor()) {
            tabs.setSelectedTabIndicatorColor(show.getColor());
        }
    }

    @Override
    public void displayAboutData(@NonNull final MediaWrapper show) {
        getChildFragmentManager().beginTransaction()
                .replace(R.id.about_content, ShowDetailAboutFragment.newInstance(show))
                .commit();
    }

    public ShowDetailComponent getComponent() {
        return component;
    }

    public static ShowDetailFragment newInstance(MediaWrapper show) {
        Bundle args = new Bundle(1);
        args.putParcelable(ARG_SHOW, Parcels.wrap(show));

        ShowDetailFragment fragment = new ShowDetailFragment();
        fragment.setArguments(args);
        return fragment;
    }
}
