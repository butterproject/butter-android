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
import android.os.Bundle;
import android.support.v17.leanback.widget.AbstractDetailsDescriptionPresenter;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.ClassPresenterSelector;
import android.support.v17.leanback.widget.DetailsOverviewRow;
import android.support.v17.leanback.widget.HeaderItem;
import android.support.v17.leanback.widget.ListRow;
import android.support.v4.app.Fragment;
import butter.droid.base.torrent.StreamInfo;
import butter.droid.provider.base.module.Episode;
import butter.droid.provider.base.module.Media;
import butter.droid.provider.base.module.Show;
import butter.droid.provider.base.module.Torrent;
import butter.droid.tv.R;
import butter.droid.tv.presenters.ShowDetailsDescriptionPresenter;
import butter.droid.tv.ui.detail.TVMediaDetailActivity;
import butter.droid.tv.ui.detail.base.TVBaseDetailsFragment;
import butter.droid.tv.ui.detail.show.presenter.EpisodeCardPresenter;
import butter.droid.tv.ui.loading.TVStreamLoadingActivity;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import javax.inject.Inject;
import org.parceler.Parcels;


public class TVShowDetailsFragment extends TVBaseDetailsFragment implements TVShowDetailsView, EpisodeCardPresenter.Listener {

    @Inject TVShowDetailsPresenter presenter;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ((TVMediaDetailActivity) getActivity())
                .getComponent()
                .showDetailComponentBuilder()
                .snowDetailModule(new TVShowDetailModule(this))
                .build()
                .inject(this);

        Bundle arguments = getArguments();
        final int providerId = arguments.getInt(EXTRA_PROVIDER);
        final Show item = Parcels.unwrap(arguments.getParcelable(EXTRA_ITEM));

        presenter.onCreate(providerId, item);
    }

    @Override protected AbstractDetailsDescriptionPresenter getDetailPresenter() {
        return new ShowDetailsDescriptionPresenter();
    }

    @Override protected void populatePresenterSelector(ClassPresenterSelector selector) {
        EpisodeCardPresenter presenter = new EpisodeCardPresenter(getActivity());
        selector.addClassPresenter(DetailsOverviewRow.class, presenter);
    }

    // TODO
    /*
    @Override
    public void onEpisodeClicked(Episode episode) {
        presenter.episodeClicked(episode);
    }
    */

    @Override public void onEpisodeClicked(final Episode row) {
        // TODO
    }

    @Override public void showSeasons(final TreeMap<Integer, List<Episode>> seasons) {
        ArrayObjectAdapter objectAdapter = getObjectArrayAdapter();

        for (Integer seasonKey : seasons.descendingKeySet()) {
            EpisodeCardPresenter presenter = new EpisodeCardPresenter(getActivity());
            presenter.setOnClickListener(this);
            ArrayObjectAdapter episodes = new ArrayObjectAdapter(presenter);

            for (Episode episode : seasons.get(seasonKey)) {
                episodes.add(episode);
            }
            HeaderItem header = new HeaderItem(seasonKey, String.format(Locale.getDefault(), "Season %d", seasonKey));
            objectAdapter.add(new ListRow(header, episodes));
        }

        objectAdapter.notifyArrayItemRangeChanged(1, objectAdapter.size());
    }

    @Override public void torrentSelected(final Show show, final StreamInfo streamInfo) {
        TVStreamLoadingActivity.startActivity(getActivity(), streamInfo, show);
    }

    @Override public void pickTorrent(final Episode episode, final Map<String, Torrent> torrents) {
        ArrayList<String> choices = new ArrayList<>(torrents.keySet());
        final ArrayList<Map.Entry<String, Torrent>> torrent = new ArrayList<>(torrents.entrySet());
        new AlertDialog.Builder(getActivity())
                .setTitle(getString(R.string.choose_quality))
                .setSingleChoiceItems(choices.toArray(new CharSequence[choices.size()]), 0, (dialog, which) -> {
                    presenter.torrentSelected(episode, torrent.get(which));
                    dialog.dismiss();
                }).show();
    }

    public static Fragment newInstance(final int providerId, final Media media) {
        TVShowDetailsFragment fragment = new TVShowDetailsFragment();

        Bundle bundle = new Bundle();
        bundle.getInt(EXTRA_PROVIDER, providerId);
        bundle.putParcelable(EXTRA_ITEM, Parcels.wrap(media));

        fragment.setArguments(bundle);
        return fragment;
    }

}
