/*
 * This file is part of Popcorn Time.
 *
 * Popcorn Time is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Popcorn Time is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Popcorn Time. If not, see <http://www.gnu.org/licenses/>.
 */

package pct.droid.tv.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import pct.droid.base.content.preferences.Prefs;
import pct.droid.base.fragments.BaseVideoPlayerFragment;
import pct.droid.base.providers.media.models.Episode;
import pct.droid.base.providers.media.models.Media;
import pct.droid.base.providers.media.models.Show;
import pct.droid.base.providers.subs.SubsProvider;
import pct.droid.base.torrent.StreamInfo;
import pct.droid.base.torrent.TorrentService;
import pct.droid.base.utils.PrefUtils;
import pct.droid.tv.R;
import pct.droid.tv.activities.base.PTVBaseActivity;
import pct.droid.tv.fragments.PTVPlaybackOverlayFragment;
import pct.droid.tv.fragments.PTVVideoPlayerFragment;

public class PTVVideoPlayerActivity extends PTVBaseActivity implements PTVVideoPlayerFragment.Callback {

    @Bind(R.id.next_episode)
    RelativeLayout mNextEpisode;
    @Bind(R.id.next_episode_thumbnail)
    ImageView mNextEpisodeThumbnail;
    @Bind(R.id.next_episode_title)
    TextView mNextEpisodeTitle;
    @Bind(R.id.next_episode_cancel)
    Button mNextEpisodeCancel;

    private PTVVideoPlayerFragment mPlayerFragment;
    private PTVPlaybackOverlayFragment mPlaybackOverlayFragment;

    public final static String EXTRA_STREAM_INFO = "stream_info";
    public final static String EXTRA_SHOW_INFO = "episode_info";

    private StreamInfo mStreamInfo;
    private boolean mIsBackPressed = false;
    private Episode mEpisodeInfo;
    private Show mShow;

    public static Intent startActivity(Context context, StreamInfo info) {
        return startActivity(context, info, 0);
    }

    public static Intent startActivity(
        Context context,
        StreamInfo info,
        @SuppressWarnings("UnusedParameters") long resumePosition) {
        Intent i = new Intent(context, PTVVideoPlayerActivity.class);
        i.putExtra(EXTRA_STREAM_INFO, info);
        // todo: resume position
        context.startActivity(i);
        return i;
    }

    public static Intent startActivity(Context context, StreamInfo info, Show show) {
        Intent i = new Intent(context, PTVVideoPlayerActivity.class);
        i.putExtra(EXTRA_STREAM_INFO, info);
        i.putExtra(EXTRA_SHOW_INFO, show);
        // todo: resume position
        context.startActivity(i);
        return i;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        mIsBackPressed = true;
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, R.layout.activity_videoplayer);

        createStreamInfo();

        mPlayerFragment = (PTVVideoPlayerFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);
        mPlaybackOverlayFragment = (PTVPlaybackOverlayFragment) getSupportFragmentManager().findFragmentById(R.id.playback_overlay_fragment);
        ButterKnife.bind(this);

        setupNextEpisodeCard();
    }

    private void setupNextEpisodeCard() {
        createEpisodeInfo();
        // if not a TV show
        if (mShow == null) {
            mNextEpisode.setVisibility(View.GONE);
            return;
        }

        // if already on end of TV show episodes
        if (mEpisodeInfo.episode == mShow.episodes.size() - 1) {
            return;
        }

        mNextEpisode.setVisibility(View.VISIBLE);

        final Episode episode = mShow.episodes.get(mEpisodeInfo.episode + 1);

        String imageUrl = episode.image;
        if (!imageUrl.equals("")) {
            Picasso.with(this)
                .load(imageUrl)
                .resize(
                    (int) getResources().getDimension(R.dimen.card_thumbnail_width),
                    (int) getResources().getDimension(R.dimen.card_thumbnail_height)
                ).centerCrop().error(ActivityCompat.getDrawable(this, R.drawable.banner))
                .into(mNextEpisodeThumbnail);
        }

        mNextEpisodeTitle.setText(episode.title);
        mNextEpisodeCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String subtitleLanguage = PrefUtils.get(
                    PTVVideoPlayerActivity.this,
                    Prefs.SUBTITLE_DEFAULT,
                    SubsProvider.SUBTITLE_LANGUAGE_NONE);

                List<Map.Entry<String, Media.Torrent>> torrents = new ArrayList<>(
                    episode.torrents.entrySet());

                @SuppressWarnings("SuspiciousMethodCalls")
                StreamInfo info = new StreamInfo(
                    episode,
                    mShow,
                    mEpisodeInfo.torrents.get(0).url,
                    subtitleLanguage,
                    torrents.get(0).getKey());

                PTVStreamLoadingActivity.startActivity(
                    PTVVideoPlayerActivity.this,
                    info,
                    mShow);
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mIsBackPressed) {
            mPlayerFragment.deactivateMediaSession();
        }

        if (mPlayerFragment.isMediaSessionActive()) {
            mPlaybackOverlayFragment.setKeepEventBusRegistration(true);
            return;
        }
        else {
            mPlaybackOverlayFragment.setKeepEventBusRegistration(false);
            PrefUtils.save(this, BaseVideoPlayerFragment.RESUME_POSITION, 0);
        }

        if (mService != null)
            mService.removeListener(mPlayerFragment);
    }

    @Override
    public void onVisibleBehindCanceled() {
        mPlayerFragment.pause();
        super.onVisibleBehindCanceled();
    }

    @Override
    protected void onDestroy() {
        if (mService != null)
            mService.stopStreaming();
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
            mNextEpisodeCancel.requestFocus();
            mNextEpisodeCancel.setSelected(true);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public StreamInfo getInfo() {
        if(mStreamInfo == null)
            createStreamInfo();
        return mStreamInfo;
    }

    @Override
    public TorrentService getService() {
        return mService;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mIsBackPressed = false;
    }

    @Override
    public void onTorrentServiceConnected() {
        mService.addListener(mPlayerFragment);
    }

    @Override
    public Long getResumePosition() {
        //todo: Implement ResumePosition on Android TV
        return 0L;
    }

    private void createEpisodeInfo() {
        if (mStreamInfo == null) {
            createStreamInfo();
        }

        if (!mStreamInfo.isShow()) {
            return;
        }

        mEpisodeInfo = (Episode) mStreamInfo.getMedia();
        mShow = getIntent().getParcelableExtra(EXTRA_SHOW_INFO);
    }

    private void createStreamInfo() {
        mStreamInfo = getIntent().getParcelableExtra(EXTRA_STREAM_INFO);
        String location = mStreamInfo.getVideoLocation();

        if (!location.startsWith("file://") && !location.startsWith("http://") && !location.startsWith("https://")) {
            location = "file://" + location;
        }

        mStreamInfo.setVideoLocation(location);
    }
}
