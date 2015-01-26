package pct.droid.fragments;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;
import android.text.Layout;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import pct.droid.R;
import pct.droid.activities.MediaDetailActivity;
import pct.droid.activities.StreamLoadingActivity;
import pct.droid.adapters.EpisodeListAdapter;
import pct.droid.base.preferences.Prefs;
import pct.droid.base.providers.media.models.Media;
import pct.droid.base.providers.media.models.Show;
import pct.droid.base.utils.NetworkUtils;
import pct.droid.base.utils.PrefUtils;
import pct.droid.base.utils.VersionUtils;
import pct.droid.dialogfragments.MessageDialogFragment;
import pct.droid.dialogfragments.SynopsisDialogFragment;
import pct.droid.widget.LinearList;

public class ShowDetailSeasonFragment extends BaseDetailFragment {

    protected static final String SEASON = "season";

    private Show mShow;
    private List<Show.Episode> mEpisodes = new ArrayList<>();

    public static ShowDetailSeasonFragment newInstance(Show show, int season, int color) {
        Bundle b = new Bundle();
        b.putParcelable(DATA, show);
        b.putInt(SEASON, season);
        b.putInt(COLOR, color);
        ShowDetailSeasonFragment showDetailFragment = new ShowDetailSeasonFragment();
        showDetailFragment.setArguments(b);
        return showDetailFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mShow = getArguments().getParcelable(DATA);
        mPaletteColor = getArguments().getInt(COLOR);
        int season = getArguments().getInt(SEASON);

        for(Show.Episode episode : mShow.episodes) {
            if(episode.season == season) {
                mEpisodes.add(episode);
            }
        }

        Collections.sort(mEpisodes, new Comparator<Show.Episode>() {
            @Override
            public int compare(Show.Episode lhs, Show.Episode rhs) {
                if(lhs.episode < rhs.episode) {
                    return -1;
                } else if(lhs.episode > rhs.episode) {
                    return 1;
                }
                return 0;
            }
        });
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mRoot = inflater.inflate(R.layout.fragment_showdetail_season, container, false);
        if(VersionUtils.isJellyBean() && container != null) {
            mRoot.setMinimumHeight(container.getMinimumHeight());
        }

        EpisodeListAdapter adapter = new EpisodeListAdapter(inflater, mEpisodes, mPaletteColor);

        LinearList linearList = (LinearList) mRoot;
        linearList.setOnItemClickListener(mOnClickListener);
        linearList.setAdapter(adapter);

        return mRoot;
    }

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int position = ((LinearList) mRoot).indexOfChild(v);
            Show.Episode episode = mEpisodes.get(position);
            String quality = episode.torrents.keySet().toArray(new String[1])[0];
            Media.Torrent torrent = episode.torrents.get(quality);

            StreamLoadingFragment.StreamInfo streamInfo = new StreamLoadingFragment.StreamInfo(episode, mShow, torrent.url, null, quality);
            mActivity.playStream(streamInfo);
        }
    };

    @OnClick(R.id.read_more)
    public void openReadMore(View v) {
        if (getFragmentManager().findFragmentByTag("overlay_fragment") != null)
            return;
        SynopsisDialogFragment synopsisDialogFragment = new SynopsisDialogFragment();
        Bundle b = new Bundle();
        b.putString("text", mShow.synopsis);
        synopsisDialogFragment.setArguments(b);
        synopsisDialogFragment.show(mActivity.getFragmentManager(), "overlay_fragment");
    }

}
