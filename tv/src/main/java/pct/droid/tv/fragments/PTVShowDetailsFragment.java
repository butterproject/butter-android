package pct.droid.tv.fragments;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v17.leanback.widget.AbstractDetailsDescriptionPresenter;
import android.support.v17.leanback.widget.Action;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.ClassPresenterSelector;
import android.support.v17.leanback.widget.OnActionClickedListener;
import android.support.v17.leanback.widget.OnItemViewClickedListener;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.PresenterSelector;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.RowPresenter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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
import pct.droid.tv.presenters.showdetail.EpisodeRow;
import pct.droid.tv.presenters.showdetail.EpisodeRowPresenter;
import pct.droid.tv.presenters.showdetail.SeasonHeaderRow;
import pct.droid.tv.presenters.showdetail.SeasonHeaderRowPresenter;

public class PTVShowDetailsFragment extends PTVBaseDetailsFragment implements MediaProvider.Callback, OnActionClickedListener, EpisodeRowPresenter.Listener {

    EZTVProvider mTvProvider = new EZTVProvider();

    private EpisodeAdapter episodeAdapter;

    public static Fragment newInstance(Media media, String hero) {
        PTVShowDetailsFragment fragment = new PTVShowDetailsFragment();

        Bundle bundle = new Bundle();
        bundle.putParcelable(EXTRA_ITEM, media);
        bundle.putString(EXTRA_HERO_URL, hero);

        fragment.setArguments(bundle);
        return fragment;
    }

    public void loadDetails() {
        ArrayList<Media> mediaList = new ArrayList<>();
        mediaList.add(getShowItem());

        mTvProvider.getDetail(mediaList, 0, this);
    }

    private Show getShowItem() {
        return (Show) getMediaItem();
    }

    @Override
    AbstractDetailsDescriptionPresenter getDetailPresenter() {
        return new ShowDetailsDescriptionPresenter();
    }

    @Override
    protected ArrayObjectAdapter createAdapter(PresenterSelector selector) {

        this.episodeAdapter = new EpisodeAdapter(getActivity(), selector);
        return new AlbumDetailsObjectAdapter(getActivity(), selector, episodeAdapter);
    }

    @Override
    void addActions(Media item) {
        //no actions yet
    }

    @Override
    public void onActionClicked(Action action) {
        //no actions yet
    }

    @Override
    ClassPresenterSelector createPresenters(ClassPresenterSelector selector) {
        selector.addClassPresenter(EpisodeRow.class, new EpisodeRowPresenter(this));
        selector.addClassPresenter(SeasonHeaderRow.class, new SeasonHeaderRowPresenter());
        return null;
    }

    private void addSeasonsRow() {
        // Add a Related items row
        final TreeMap<Integer, List<Episode>> seasons = new TreeMap<>(new Comparator<Integer>() {
            @Override
            public int compare(Integer lhs, Integer rhs) {
                return lhs - rhs;
            }
        });

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

        episodeAdapter.setSeasons(seasons.descendingMap());

        ((AlbumDetailsObjectAdapter) getObjectArrayAdapter()).notifyDataSetChanged();
    }

    @Override
    public void onEpisodeRowClicked(EpisodeRow row) {
        Episode episode = row.getEpisode();

        if (null == episode) {
            return;
        }
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


    private class EpisodeAdapter extends ArrayObjectAdapter {

        private final Context context;
        Map<Integer, List<Episode>> seasons;

        private int mSize = -1;

        public EpisodeAdapter(Context context, PresenterSelector presenter) {
            super(presenter);
            this.context = context;
        }

        public void setSeasons(Map<Integer, List<Episode>> seasons) {
            this.seasons = seasons;
        }

        @Override
        public int size() {
            if (mSize == -1) {
                mSize = calculateSize();
            }
            return mSize;
        }

        private int calculateSize() {
            if (seasons == null) {
                return -1;
            }

            int size = seasons.size();//the number of headers

            for (Map.Entry<Integer, List<Episode>> entry : seasons.entrySet()) {
                size += entry.getValue().size();
            }

            return size;
        }

        @Override
        public Object get(int position) {

            for (Map.Entry<Integer, List<Episode>> adapter : seasons.entrySet()) {

                //season header
                if (position == 0) {
                    return new SeasonHeaderRow(context, position, adapter.getKey());
                }
                position -= 1;

                //episode row
                int size = adapter.getValue().size();
                if (position < size) {
                    return new EpisodeRow(context, position, adapter.getValue().get(position));
                }
                position -= size;
            }
            return new EpisodeRow(context, -1, null);
        }

    }


    private class AlbumDetailsObjectAdapter extends ArrayObjectAdapter {
        private final Context mContext;
        private final EpisodeAdapter mEpisodeAdapter;

        AlbumDetailsObjectAdapter(Context context, PresenterSelector presenter, EpisodeAdapter episodeAdapter) {
            super(presenter);
            this.mContext = context;
            this.mEpisodeAdapter = episodeAdapter;
//            this.mEpisodeAdapter.registerObserver(new ObjectAdapter.DataObserver() {
//                public void onChanged() {
////                    AlbumDetailsObjectAdapter.this.recalculateSize();
//                    AlbumDetailsObjectAdapter.this.notifyChanged();
//                }
//
//                public void onItemRangeChanged(int var1, int var2) {
//                    if (var1 == 0) {
//                        AlbumDetailsObjectAdapter.this.notifyItemRangeChanged(var1, 1);
//                    }
//
//                }
//
//                public void onItemRangeInserted(int var1, int var2) {
//                    if (var1 == 0) {
////                        AlbumDetailsObjectAdapter.this.recalculateSize();
//                        AlbumDetailsObjectAdapter.this.notifyItemRangeInserted(var1, 1);
//                    }
//
//                }
//
//                public void onItemRangeRemoved(int var1, int var2) {
//                    if (var1 == 0) {
////                        AlbumDetailsObjectAdapter.this.recalculateSize();
//                        AlbumDetailsObjectAdapter.this.notifyItemRangeRemoved(var1, 1);
//                    }
//
//                }
//            });
        }
//            this.mDetailsObjectAdapter = detailsAdapter;
//            this.mSongTitleRow = titleRow;
//            this.mSongFooterRow = footerRow;

//            });
//            this.recalculateSize();
//        }

        public void notifyDataSetChanged() {
            this.notifyChanged();
        }

        public Object get(int position) {
            if (position < this.size()) {
                if (position == 0) return super.get(position);

                return mEpisodeAdapter.get(position - 1);
            }
            return null;
        }

        public int size() {
            return mEpisodeAdapter.size() + 1;
        }

    }


}
