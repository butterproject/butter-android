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

package butter.droid.tv.ui.detail.streamable;

import android.content.Context;
import android.os.Bundle;
import androidx.leanback.widget.AbstractDetailsDescriptionPresenter;
import androidx.fragment.app.Fragment;
import butter.droid.base.providers.media.model.MediaWrapper;
import butter.droid.base.providers.media.model.StreamInfo;
import butter.droid.base.providers.subs.model.SubtitleWrapper;
import butter.droid.provider.base.model.Torrent;
import butter.droid.tv.presenters.MediaDetailsDescriptionPresenter;
import butter.droid.tv.ui.detail.base.TVBaseDetailsFragment;
import butter.droid.tv.ui.loading.TVStreamLoadingActivity;
import butter.droid.tv.ui.trailer.TVTrailerPlayerActivity;
import dagger.android.support.AndroidSupportInjection;
import javax.inject.Inject;

public class TVStreamableDetailsFragment extends TVBaseDetailsFragment implements TVStreamableDetailsView {

    @Inject TVStreamableDetailsPresenter presenter;

    @Override public void onAttach(final Context context) {
        AndroidSupportInjection.inject(this);
        super.onAttach(context);
    }

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle arguments = getArguments();
        final MediaWrapper item = arguments.getParcelable(EXTRA_ITEM);

        presenter.onCreate(item);
    }

    @Override protected AbstractDetailsDescriptionPresenter getDetailPresenter() {
        return new MediaDetailsDescriptionPresenter();
    }

    @Override public void startTrailer(final MediaWrapper movie, final String trailer) {
        startActivity(TVTrailerPlayerActivity.getIntent(getActivity(), movie, trailer));
    }

    @Override public void startMovie(final MediaWrapper item, final Torrent torrent, final String quality) {
        // Default subtitles will be loaded
        StreamInfo info = new StreamInfo(torrent, item, null, new SubtitleWrapper());

        TVStreamLoadingActivity.startActivity(getActivity(), info);
    }

    public static Fragment newInstance(final MediaWrapper media) {
        TVStreamableDetailsFragment fragment = new TVStreamableDetailsFragment();

        Bundle bundle = new Bundle();
        bundle.putParcelable(EXTRA_ITEM, media);

        fragment.setArguments(bundle);
        return fragment;
    }

}
