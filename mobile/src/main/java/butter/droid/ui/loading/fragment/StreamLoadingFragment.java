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

package butter.droid.ui.loading.fragment;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;

import javax.inject.Inject;

import butter.droid.R;
import butter.droid.base.fragments.dialog.StringArraySelectorDialogFragment;
import butter.droid.base.manager.internal.glide.GlideApp;
import butter.droid.base.providers.media.model.StreamInfo;
import butter.droid.base.ui.loading.fragment.BaseStreamLoadingFragment;
import butter.droid.base.utils.VersionUtils;
import butter.droid.ui.beam.BeamPlayerActivity;
import butter.droid.ui.player.VideoPlayerActivity;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class StreamLoadingFragment extends BaseStreamLoadingFragment implements StreamLoadingFragmentView {

    @Inject StreamLoadingFragmentPresenter presenter;

    @BindView(R.id.background_imageview) ImageView backgroundImageView;
    @Nullable @BindView(R.id.startexternal_button) TextView startExternalButton;

    @Override public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        StreamInfo streamInfo = getArguments().getParcelable(ARGS_STREAM_INFO);
        presenter.onCreate(streamInfo);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_streamloading, container, false);
    }

    @TargetApi(VERSION_CODES.LOLLIPOP)
    @Override public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        if (VersionUtils.isLollipop()) {
            //postpone the transitions until after the view is layed out.
            final FragmentActivity activity = requireActivity();
            activity.postponeEnterTransition();

            view.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                public boolean onPreDraw() {
                    view.getViewTreeObserver().removeOnPreDrawListener(this);
                    activity.startPostponedEnterTransition();
                    return true;
                }
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.onResume();
    }

    @Override public void loadBackgroundImage(String url) {
        GlideApp.with(this)
                .asDrawable()
                .load(url)
                .error(R.color.bg)
                .into(backgroundImageView);
    }

    @Override public void pickTorrentFile(String[] fileNames) {
        StringArraySelectorDialogFragment.show(getChildFragmentManager(), R.string.select_file, fileNames, -1,
                (dialog, position) -> presenter.selectTorrentFile(position));
    }

    @Override public void startBeamActivity(StreamInfo streamInfo, int resumePosition) {
        requireActivity().startActivity(BeamPlayerActivity.getIntent(getActivity(), streamInfo, resumePosition));
    }

    @Override public void closeSelf() {
        requireActivity().finish();
    }

    @Override public void startExternalPlayer(@NonNull Intent intent) {
        startActivity(intent);
    }

    @Override public void startPlayerActivity(StreamInfo streamInfo, int resumePosition) {
        startActivity(VideoPlayerActivity.getIntent(getContext(), streamInfo, resumePosition));
    }

    @Override public void showExternalPlayerButton() {
        startExternalButton.setVisibility(View.VISIBLE);
    }

    @OnClick(R.id.startexternal_button) public void externalClick() {
        presenter.startExternalPlayer();
    }

    public static StreamLoadingFragment newInstance(@NonNull StreamInfo streamInfo) {
        Bundle args = new Bundle();
        args.putParcelable(ARGS_STREAM_INFO, streamInfo);

        StreamLoadingFragment fragment = new StreamLoadingFragment();
        fragment.setArguments(args);

        return fragment;
    }
}
