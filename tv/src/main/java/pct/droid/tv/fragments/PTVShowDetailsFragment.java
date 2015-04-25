package pct.droid.tv.fragments;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v17.leanback.widget.AbstractDetailsDescriptionPresenter;
import android.support.v17.leanback.widget.Action;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.DetailsOverviewRow;
import android.support.v17.leanback.widget.HeaderItem;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.OnActionClickedListener;
import android.support.v17.leanback.widget.OnItemViewClickedListener;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.RowPresenter;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import pct.droid.base.dialogfragments.FileSelectorDialogFragment;
import pct.droid.base.fragments.StringArraySelectorDialogFragment;
import pct.droid.base.providers.media.EZTVProvider;
import pct.droid.base.providers.media.MediaProvider;
import pct.droid.base.providers.media.models.Episode;
import pct.droid.base.providers.media.models.Media;
import pct.droid.base.providers.media.models.Show;
import pct.droid.base.torrent.StreamInfo;
import pct.droid.base.utils.NetworkUtils;
import pct.droid.tv.R;
import pct.droid.tv.activities.PTVStreamLoadingActivity;
import pct.droid.tv.presenters.EpisodeCardPresenter;
import pct.droid.tv.presenters.ShowDetailsDescriptionPresenter;

public class PTVShowDetailsFragment extends PTVBaseDetailsFragment implements MediaProvider.Callback, OnActionClickedListener {

    EZTVProvider mTvProvider = new EZTVProvider();

    public static Fragment newInstance(Media media, String hero) {
        PTVShowDetailsFragment fragment = new PTVShowDetailsFragment();

        Bundle bundle = new Bundle();
        bundle.putParcelable(EXTRA_ITEM, media);
        bundle.putString(EXTRA_HERO_URL, hero);

        fragment.setArguments(bundle);
        return fragment;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setOnItemViewClickedListener(new EpisodeItemClickedListener());
    }

    public void loadDetails() {
        mTvProvider.getDetail(getShowItem().videoId, this);
    }

    private Show getShowItem() {
        return (Show) getMediaItem();
    }

    @Override
    AbstractDetailsDescriptionPresenter getDetailPresenter() {
        return new ShowDetailsDescriptionPresenter();
    }

    @Override
    void addActions(Media item) {
        //no actions yet
    }

    @Override
    public void onActionClicked(Action action) {
        //no actions yet
    }

    private void addSeasonsRow() {
        // Add a Related items row
        EpisodeCardPresenter mediaCardPresenter = new EpisodeCardPresenter(getActivity());

        final Map<Integer, List<Episode>> seasons = new LinkedHashMap<>();
        for (Episode episode : getShowItem().episodes) {

            //create list if not there
            if (!seasons.containsKey(episode.season)) {
                seasons.put(episode.season, new ArrayList<Episode>());
            }

            //add episode to thel ist
            final List<Episode> seasonEpisodes = seasons.get(episode.season);
            seasonEpisodes.add(episode);
        }

        //sort the episodes into correct order
        for (List<Episode> seasonList : seasons.values()) {
            Collections.sort(seasonList, new Comparator<Episode>() {
                @Override
                public int compare(Episode lhs, Episode rhs) {
                    if (lhs.episode < rhs.episode) {
                        return -1;
                    } else if (lhs.episode > rhs.episode) {
                        return 1;
                    }
                    return 0;
                }
            });
        }

        for (int i = 1; i < seasons.size() + 1; i++) {

            //setup season adapter
            final ArrayObjectAdapter seasonAdapter = new ArrayObjectAdapter(mediaCardPresenter);
            if (seasons != null) {
                for (Episode episode : seasons.get(i)) {
                    seasonAdapter.add(episode);
                }

                HeaderItem header = new HeaderItem(i, String.format("Season %d", i));
                getObjectArrayAdapter().add(new ListRow(header, seasonAdapter));
            }
        }
    }


    private final class EpisodeItemClickedListener implements OnItemViewClickedListener {
        @Override
        public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item,
                                  RowPresenter.ViewHolder rowViewHolder, Row row) {

            Episode episode = (Episode) item;
            //check for network
            if (!NetworkUtils
                    .isNetworkConnected(getActivity())) {
                Toast.makeText(getActivity(), R.string.network_message, Toast.LENGTH_SHORT).show();
            } else {
                //start first torrent
                if (episode.torrents.size() == 1) {
                    final List<Map.Entry<String, Media.Torrent>> torrents = new ArrayList<>(episode
                            .torrents.entrySet());
                    onTorrentSelected(episode, torrents.get(0));
                }
                //ask user which torrent
                else {
                    showTorrentsDialog(episode, episode
                            .torrents);
                }

            }
        }
    }

    private void showTorrentsDialog(final Episode episode, final Map<String, Media.Torrent> torrents) {

        ArrayList<String> choices = new ArrayList<>(torrents.keySet());

        new AlertDialog.Builder(getActivity())
                .setTitle(getString(R.string.choose_quality))
                .setSingleChoiceItems(choices.toArray(new CharSequence[choices.size()]), 0, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        onTorrentSelected(episode, (Map.Entry<String, Media.Torrent>) new ArrayList(torrents.entrySet()).get(which));
                        dialog.dismiss();
                    }
                }).show();
    }

    private void onTorrentSelected(Episode episode, Map.Entry<String, Media.Torrent> torrent) {
        StreamInfo info =
                new StreamInfo(episode, getShowItem(), torrent.getValue().url, "no-subs",
                        torrent.getKey());

        PTVStreamLoadingActivity.startActivity(getActivity(), info);
    }


    @Override
    void onDetailLoaded() {
        addSeasonsRow();
    }

}
