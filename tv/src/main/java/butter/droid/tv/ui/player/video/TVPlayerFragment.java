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

package butter.droid.tv.ui.player.video;

import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.v17.leanback.widget.BaseOnItemViewSelectedListener;
import android.support.v17.leanback.widget.Presenter.ViewHolder;
import android.support.v17.leanback.widget.RowPresenter;
import butter.droid.base.subs.Caption;
import butter.droid.base.torrent.StreamInfo;
import butter.droid.tv.R;
import butter.droid.tv.ui.player.TVVideoPlayerActivity;
import butter.droid.tv.ui.player.abs.TVAbsPlayerFragment;
import javax.inject.Inject;

public class TVPlayerFragment extends TVAbsPlayerFragment implements TVPlayerView, BaseOnItemViewSelectedListener {

    private static final String ARG_STREAM_INFO = "butter.droid.tv.ui.player.video.TVPlayerFragment.streamInfo";
    private static final String ARG_RESUME_POSITION = "butter.droid.tv.ui.player.video.TVPlayerFragment.resumePosition";

    @Inject TVPlayerPresenter presenter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        ((TVVideoPlayerActivity) getActivity())
                .getComponent()
                .tvPlayerComponentBuilder()
                .tvPlayerModule(new TVPlayerModule(this))
                .build()
                .inject(this);

        super.onCreate(savedInstanceState);

        StreamInfo streamInfo = getArguments().getParcelable(ARG_STREAM_INFO);
        long resumePosition = getArguments().getLong(ARG_RESUME_POSITION);

        stateBuilder.addCustomAction(PlayerMediaControllerGlue.ACTION_CLOSE_CAPTION, getString(R.string.subtitles), R.drawable.ic_av_subs);

        presenter.onCreate(streamInfo, resumePosition);
    }

    @Override public void showTimedCaptionText(final Caption caption) {
        // TODO: 5/7/17
    }

    @Override
    public void setupSubtitles(@ColorInt final int color, final int size, @ColorInt final int strokeColor, final int strokeWidth) {
        // TODO: 5/17/17
    }

    @Override public void updateSubtitleSize(final int size) {
        // TODO: 5/17/17
    }

    @Override public void showSubsSelectorDialog() {
        // TODO: 5/7/17  
    }

    @Override public void showPickSubsDialog(final String[] readableNames, final String[] adapterSubtitles, final String currentSubsLang) {
        // TODO: 5/7/17  
    }

    @Override public void showSubsFilePicker() {
        // TODO: 5/7/17
    }

    @Override public void displaySubsSizeDialog() {
        // TODO: 5/7/17  
    }

    @Override public void displaySubsTimingDialog(final int subtitleOffset) {
        // TODO: 5/7/17
    }

    @Override public void onItemSelected(final ViewHolder itemViewHolder, final Object item, final RowPresenter.ViewHolder rowViewHolder,
            final Object row) {

    }

    @Override protected boolean onCustomAction(final String action, final Bundle extras) {
        switch (action) {
            case PlayerMediaControllerGlue.ACTION_CLOSE_CAPTION:
                presenter.onSubsClicked();
                return true;
            default:
                return super.onCustomAction(action, extras);
        }
    }

    public static TVPlayerFragment newInstance(@NonNull StreamInfo streamInfo, long resumePosition) {
        Bundle args = new Bundle(2);
        args.putParcelable(ARG_STREAM_INFO, streamInfo);
        args.putLong(ARG_RESUME_POSITION, resumePosition);

        TVPlayerFragment fragment = new TVPlayerFragment();
        fragment.setArguments(args);
        return fragment;
    }


}
