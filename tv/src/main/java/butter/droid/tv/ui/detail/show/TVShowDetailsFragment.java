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

package butter.droid.tv.ui.detail.show;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import androidx.leanback.widget.ArrayObjectAdapter;
import androidx.leanback.widget.ClassPresenterSelector;
import androidx.leanback.widget.DetailsOverviewRow;
import androidx.leanback.widget.HeaderItem;
import androidx.leanback.widget.ListRow;
import androidx.fragment.app.Fragment;
import butter.droid.base.manager.internal.media.MediaDisplayManager;
import butter.droid.base.providers.media.model.MediaWrapper;
import butter.droid.base.providers.media.model.StreamInfo;
import butter.droid.provider.base.model.Episode;
import butter.droid.provider.base.model.Season;
import butter.droid.provider.base.model.Torrent;
import butter.droid.tv.R;
import butter.droid.tv.ui.detail.base.TVBaseDetailsFragment;
import butter.droid.tv.ui.detail.show.presenter.EpisodeCardPresenter;
import butter.droid.tv.ui.loading.TVStreamLoadingActivity;
import dagger.android.support.AndroidSupportInjection;
import java.util.ArrayList;
import javax.inject.Inject;

public class TVShowDetailsFragment extends TVBaseDetailsFragment implements TVShowDetailsView, EpisodeCardPresenter.Listener {

    @Inject TVShowDetailsPresenter presenter;
    @Inject MediaDisplayManager mediaDisplayManager;

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

    @Override protected void populatePresenterSelector(ClassPresenterSelector selector) {
        EpisodeCardPresenter presenter = new EpisodeCardPresenter(getActivity());
        selector.addClassPresenter(DetailsOverviewRow.class, presenter);
    }

    @Override public void onEpisodeClicked(final Episode row) {
        presenter.episodeClicked(row);
    }

    @Override public void showSeasons(final Season[] seasons) {
        ArrayObjectAdapter objectAdapter = getObjectArrayAdapter();

        for (int i = 0; i < seasons.length; i++) {
            Season season = seasons[i];

            HeaderItem header = new HeaderItem(i, season.getTitle());

            EpisodeCardPresenter presenter = new EpisodeCardPresenter(getActivity());
            presenter.setOnClickListener(this);
            ArrayObjectAdapter episodes = new ArrayObjectAdapter(presenter);

            for (final Episode episode : season.getEpisodes()) {
                episodes.add(episode);
            }
            objectAdapter.add(new ListRow(header, episodes));
        }

        objectAdapter.notifyArrayItemRangeChanged(1, objectAdapter.size());
    }

    @Override public void torrentSelected(final StreamInfo streamInfo) {
        TVStreamLoadingActivity.startActivity(getActivity(), streamInfo);
    }

    @Override public void pickTorrent(final Episode episode, final Torrent[] torrents) {
        ArrayList<String> choices = new ArrayList<>(torrents.length);
        for (final Torrent torrent : torrents) {
            choices.add(mediaDisplayManager.getFormatDisplayName(torrent.getFormat()));
        }

        new AlertDialog.Builder(getActivity())
                .setTitle(getString(R.string.choose_quality))
                .setSingleChoiceItems(choices.toArray(new CharSequence[choices.size()]), 0, (dialog, which) -> {
                    presenter.torrentSelected(episode, torrents[which]);
                    dialog.dismiss();
                }).show();
    }

    public static Fragment newInstance(final MediaWrapper media) {
        TVShowDetailsFragment fragment = new TVShowDetailsFragment();

        Bundle bundle = new Bundle();
        bundle.putParcelable(EXTRA_ITEM, media);

        fragment.setArguments(bundle);
        return fragment;
    }

}
