package pct.droid.fragments;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import pct.droid.R;
import pct.droid.adapters.EpisodeListAdapter;
import pct.droid.base.providers.media.models.Episode;
import pct.droid.base.providers.media.models.Show;
import pct.droid.base.utils.VersionUtils;
import pct.droid.dialogfragments.EpisodeDialogFragment;
import pct.droid.fragments.base.BaseDetailFragment;
import pct.droid.widget.LinearList;

public class ShowDetailSeasonFragment extends BaseDetailFragment {

    protected static final String SEASON = "season";

    private static Show sShow;
    private List<Episode> mEpisodes = new ArrayList<>();

    public static ShowDetailSeasonFragment newInstance(Show show, int season) {
        sShow = show;
        Bundle b = new Bundle();
        b.putInt(SEASON, season);
        ShowDetailSeasonFragment showDetailFragment = new ShowDetailSeasonFragment();
        showDetailFragment.setArguments(b);
        return showDetailFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int season = getArguments().getInt(SEASON);

        if(sShow == null) return;

        for (Episode episode : sShow.episodes) {
            if (episode.season == season) {
                mEpisodes.add(episode);
            }
        }

        Collections.sort(mEpisodes, new Comparator<Episode>() {
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

    public int getSeasonNumber() {
        return getArguments().getInt(SEASON);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mRoot = inflater.inflate(R.layout.fragment_showdetail_season, container, false);
        if (VersionUtils.isJellyBean() && container != null) {
            mRoot.setMinimumHeight(container.getMinimumHeight());
        }

        EpisodeListAdapter adapter = new EpisodeListAdapter(inflater, mEpisodes, sShow.color);

        LinearList linearList = (LinearList) mRoot;
        linearList.setOnItemClickListener(mOnClickListener);
        linearList.setAdapter(adapter);

        return mRoot;
    }

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int position = ((LinearList) mRoot).indexOfChild(v);
            Episode episode = mEpisodes.get(position);

            EpisodeDialogFragment fragment = EpisodeDialogFragment.newInstance(sShow, episode);
            fragment.show(getFragmentManager(), "episode_dialog");
        }
    };

}
