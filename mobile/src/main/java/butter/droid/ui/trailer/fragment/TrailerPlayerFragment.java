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

package butter.droid.ui.trailer.fragment;

import android.os.Bundle;
import butter.droid.base.torrent.StreamInfo;
import butter.droid.ui.player.abs.AbsPlayerFragment;
import butter.droid.ui.trailer.TrailerPlayerActivity;

public class TrailerPlayerFragment extends AbsPlayerFragment {

    @Override public void onCreate(final Bundle savedInstanceState) {
        TrailerPlayerActivity activity = (TrailerPlayerActivity) getActivity();
        activity.getComponent()
                .trailerPlayerComponentBuilder()
                .trailerPlayerModule(new TrailerPlayerModule(this, activity))
                .build()
                .inject(this);

        super.onCreate(savedInstanceState);
    }

    public static TrailerPlayerFragment newInstance(final StreamInfo streamInfo, final long resumePosition) {
        Bundle args = new Bundle(2);
//        args.putParcelable(ARG_STREAM_INFO, streamInfo);
//        args.putLong(ARG_RESUME_POSITION, resumePosition);

        TrailerPlayerFragment fragment = new TrailerPlayerFragment();
        fragment.setArguments(args);
        return fragment;
    }

}
