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

package butter.droid.ui.trailer;

import android.os.Bundle;
import android.view.View;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import butter.droid.base.providers.media.model.MediaWrapper;
import butter.droid.ui.player.abs.AbsPlayerFragment;
import butterknife.ButterKnife;

public class TrailerPlayerFragment extends AbsPlayerFragment implements TrailerPlayerView {

    private static final String ARG_URI = "butter.droid.ui.trailer.TrailerPlayerFragment.uri";
    private static final String ARG_MEDIA = "butter.droid.ui.trailer.TrailerPlayerFragment.media";

    @Inject TrailerPlayerPresenter presenter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        String uri = args.getString(ARG_URI);
        MediaWrapper media = args.getParcelable(ARG_MEDIA);

        presenter.onCreate(media, uri, getResumePosition(savedInstanceState));
    }

    @Override public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
    }

//    @Override
//    public void onDisplayErrorVideoDialog() {
//        DialogFactory.createErrorFetchingYoutubeVideoDialog(this, new ActionCallback() {
//            @Override
//            public void onButtonClick(final Dialog which, final @Action int action) {
//                finish();
//            }
//        }).show();
//    }

    public static TrailerPlayerFragment newInstance(final MediaWrapper media, final String trailerUri) {
        Bundle args = new Bundle(2);
        args.putParcelable(ARG_MEDIA, media);
        args.putString(ARG_URI, trailerUri);

        TrailerPlayerFragment fragment = new TrailerPlayerFragment();
        fragment.setArguments(args);
        return fragment;
    }

}
