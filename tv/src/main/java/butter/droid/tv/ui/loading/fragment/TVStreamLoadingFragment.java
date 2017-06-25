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

package butter.droid.tv.ui.loading.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import butter.droid.base.torrent.StreamInfo;
import butter.droid.base.ui.loading.fragment.BaseStreamLoadingFragment;
import butter.droid.provider.base.module.Show;
import butter.droid.tv.R;
import butter.droid.tv.manager.internal.background.BackgroundUpdater;
import butter.droid.tv.manager.internal.background.BackgroundUpdaterModule;
import butter.droid.tv.ui.loading.TVStreamLoadingActivity;
import butter.droid.tv.ui.player.TVVideoPlayerActivity;
import javax.inject.Inject;
import org.parceler.Parcels;

public class TVStreamLoadingFragment extends BaseStreamLoadingFragment implements TVStreamLoadingFragmentView {

    protected static final String ARGS_SHOW_INFO = "butter.droid.tv.ui.loading.fragment.TVStreamLoadingFragment.show";

    @Inject TVStreamLoadingFragmentPresenter presenter;
    @Inject BackgroundUpdater backgroundUpdater;

    @Override public void onAttach(Context context) {
        super.onAttach(context);

        ((TVStreamLoadingActivity) context).getComponent()
                .streamLoadingFragmentComponentBuilder()
                .tvStreamLoadingFragmentModule(new TVStreamLoadingFragmentModule(this))
                .backgroundUpdaterModule(new BackgroundUpdaterModule(getActivity()))
                .build()
                .inject(this);
    }

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle arguments = getArguments();
        StreamInfo streamInfo = arguments.getParcelable(ARGS_STREAM_INFO);
        Show show = Parcels.unwrap(arguments.getParcelable(ARGS_SHOW_INFO));
        presenter.onCreate(streamInfo, show);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_streamloading, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        backgroundUpdater.initialise(getActivity(), R.color.black);
    }

    @Override public void updateBackground(String imageUrl) {
        backgroundUpdater.updateBackgroundAsync(imageUrl);
    }

    @Override public void startPlayerActivity(StreamInfo streamInfo, int resumePosition) {
        TVVideoPlayerActivity.startActivity(getActivity(), streamInfo, resumePosition);
    }

    @Override public void startPlayerActivity(StreamInfo streamInfo, @NonNull Show show) {
        TVVideoPlayerActivity.startActivity(getActivity(), streamInfo, show);
    }

    public static TVStreamLoadingFragment newInstance(@NonNull StreamInfo streamInfo, @Nullable Show show) {
        Bundle args = new Bundle();
        args.putParcelable(ARGS_STREAM_INFO, streamInfo);
        args.putParcelable(ARGS_SHOW_INFO, Parcels.wrap(show));

        TVStreamLoadingFragment fragment = new TVStreamLoadingFragment();
        fragment.setArguments(args);

        return fragment;
    }
}
