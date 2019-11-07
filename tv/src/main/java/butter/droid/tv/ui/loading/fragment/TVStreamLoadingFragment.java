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

import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import butter.droid.base.providers.media.model.StreamInfo;
import butter.droid.base.ui.loading.fragment.BaseStreamLoadingFragment;
import butter.droid.tv.R;
import butter.droid.tv.manager.internal.background.BackgroundUpdater;
import butter.droid.tv.ui.player.TVVideoPlayerActivity;

public class TVStreamLoadingFragment extends BaseStreamLoadingFragment implements TVStreamLoadingFragmentView {

    @Inject TVStreamLoadingFragmentPresenter presenter;
    @Inject BackgroundUpdater backgroundUpdater;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle arguments = getArguments();
        StreamInfo streamInfo = arguments.getParcelable(ARGS_STREAM_INFO);
        presenter.onCreate(streamInfo);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_streamloading, container, false);
    }

    @Override public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.setBackground(new GradientDrawable(Orientation.TOP_BOTTOM, new int[] {0x66000000, 0x4C000000}));
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

    public static TVStreamLoadingFragment newInstance(@NonNull StreamInfo streamInfo) {
        Bundle args = new Bundle();
        args.putParcelable(ARGS_STREAM_INFO, streamInfo);

        TVStreamLoadingFragment fragment = new TVStreamLoadingFragment();
        fragment.setArguments(args);

        return fragment;
    }
}
