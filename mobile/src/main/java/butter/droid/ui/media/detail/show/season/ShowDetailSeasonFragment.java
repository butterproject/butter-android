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

package butter.droid.ui.media.detail.show.season;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import butter.droid.R;
import butter.droid.base.providers.media.model.MediaMeta;
import butter.droid.provider.base.model.Episode;
import butter.droid.provider.base.model.Season;
import butter.droid.ui.media.detail.dialog.EpisodeDialogFragment;
import butter.droid.ui.media.detail.show.season.list.EpisodeListAdapter;
import butter.droid.widget.LinearList;
import dagger.android.support.DaggerFragment;
import java.util.List;
import javax.inject.Inject;
import org.parceler.Parcels;

public class ShowDetailSeasonFragment extends DaggerFragment implements ShowDetailSeasonView, OnClickListener {

    private static final String ARG_MEDIA_META = "butter.droid.ui.media.detail.show.season.ShowDetailSeasonFragment.mediaMeta";
    private static final String ARG_SEASON = "butter.droid.ui.media.detail.show.season.ShowDetailSeasonFragment.season";

    @Inject ShowDetailSeasonPresenter presenter;

    private LinearList rootView;

    private EpisodeListAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        MediaMeta mediaMeta = args.getParcelable(ARG_MEDIA_META);
        Season season = Parcels.unwrap(args.getParcelable(ARG_SEASON));

        presenter.onCreate(mediaMeta, season);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_showdetail_season, container, false);
        if (container != null) {
            view.setMinimumHeight(container.getMinimumHeight());
        }

        return view;
    }

    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rootView = (LinearList) view;
        adapter = new EpisodeListAdapter(getContext());

        rootView.setOnItemClickListener(this);
        rootView.setAdapter(adapter);

        presenter.onViewCreated();
    }

    @Override public void onClick(View view) {
        int position = rootView.indexOfChild(view);

        presenter.episodeSelected(position);
    }


    @Override public void displayData(@ColorInt int color, List<Episode> episodes) {
        if (color == Color.TRANSPARENT) {
            color = ContextCompat.getColor(getContext(), R.color.primary);
        }

        adapter.setData(color, episodes);
    }

    @Override public void showEpisodeDialog(MediaMeta mediaMeta, Episode episode) {
        EpisodeDialogFragment fragment = EpisodeDialogFragment.newInstance(mediaMeta, episode);
        fragment.show(getFragmentManager(), "episode_dialog");
    }

    public static ShowDetailSeasonFragment newInstance(final MediaMeta mediaMeta, final Season season) {
        Bundle args = new Bundle(2);
        args.putParcelable(ARG_MEDIA_META, mediaMeta);
        args.putParcelable(ARG_SEASON, Parcels.wrap(season));

        ShowDetailSeasonFragment fragment = new ShowDetailSeasonFragment();
        fragment.setArguments(args);
        return fragment;
    }

}
