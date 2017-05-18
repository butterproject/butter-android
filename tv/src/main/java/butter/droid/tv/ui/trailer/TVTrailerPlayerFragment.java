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

package butter.droid.tv.ui.trailer;

import android.os.Bundle;
import android.support.annotation.NonNull;
import butter.droid.base.providers.media.models.Media;
import butter.droid.base.torrent.StreamInfo;
import butter.droid.tv.TVButterApplication;
import butter.droid.tv.ui.player.abs.TVAbsPlayerFragment;
import javax.inject.Inject;

public class TVTrailerPlayerFragment extends TVAbsPlayerFragment implements TVTrailerPlayerView {

    private final static String ARG_URI = "butter.droid.tv.ui.trailer.TVTrailerPlayerFragment.uri";
    private final static String ARG_MEDIA = "butter.droid.tv.ui.trailer.TVTrailerPlayerFragment.media";

    @Inject TVTrailerPlayerPresenter presenter;

    @Override public void onCreate(final Bundle savedInstanceState) {
        TVTrailerPlayerActivity activity = (TVTrailerPlayerActivity) getActivity();
        TVButterApplication.getAppContext()
                .getComponent()
                .tvTrailerPlayerComponentBuilder()
                .trailerPlayerModule(new TVTrailerPlayerModule(this, activity))
                .build()
                .inject(this);

        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        String uri = args.getString(ARG_URI);
        Media media = args.getParcelable(ARG_MEDIA);

        presenter.onCreate(media, uri);
    }

    @Override public void displayStreamProgress(final int progress) {

    }

    @Override public void showVolumeMessage(final int volume) {

    }

    @Override public void startBeamPlayerActivity(@NonNull final StreamInfo streamInfo, final long currentTime) {

    }

//    DialogFactory.createErrorFetchingYoutubeVideoDialog(this, new ActionCallback() {
//        @Override
//        public void onButtonClick(final Dialog which, final @Action int action) {
//            finish();
//        }
//    }).show();

    public static TVTrailerPlayerFragment newInstance(final Media media, final String trailerUri) {
        Bundle args = new Bundle(2);
        args.putParcelable(ARG_MEDIA, media);
        args.putString(ARG_URI, trailerUri);

        TVTrailerPlayerFragment fragment = new TVTrailerPlayerFragment();
        fragment.setArguments(args);
        return fragment;
    }
}
