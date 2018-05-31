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

package butter.droid.ui.media.detail;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import com.google.android.material.appbar.AppBarLayout.ScrollingViewBehavior;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout.LayoutParams;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.core.util.Pair;
import androidx.core.view.ViewCompat;
import androidx.core.widget.NestedScrollView;
import androidx.appcompat.widget.Toolbar;
import android.widget.FrameLayout;
import android.widget.ImageView;

import javax.inject.Inject;

import butter.droid.R;
import butter.droid.base.manager.internal.beaming.BeamPlayerNotificationService;
import butter.droid.base.manager.internal.beaming.server.BeamServer;
import butter.droid.base.manager.internal.beaming.server.BeamServerService;
import butter.droid.base.manager.internal.glide.GlideApp;
import butter.droid.base.providers.media.model.MediaWrapper;
import butter.droid.base.providers.media.model.StreamInfo;
import butter.droid.base.torrent.TorrentHealth;
import butter.droid.base.torrent.TorrentService;
import butter.droid.base.utils.PixelUtils;
import butter.droid.base.utils.VersionUtils;
import butter.droid.provider.base.model.Media;
import butter.droid.ui.ButterBaseActivity;
import butter.droid.ui.loading.StreamLoadingActivity;
import butter.droid.ui.media.detail.dialog.EpisodeDialogFragment;
import butter.droid.ui.media.detail.dialog.MessageDialogFragment;
import butter.droid.ui.media.detail.show.ShowDetailFragment;
import butter.droid.ui.media.detail.streamable.StreamableDetailFragment;
import butter.droid.ui.player.VideoPlayerActivity;
import butter.droid.ui.trailer.TrailerPlayerActivity;
import butterknife.BindView;
import butterknife.OnClick;
import butterknife.Optional;

public class MediaDetailActivity extends ButterBaseActivity implements MediaDetailView, EpisodeDialogFragment.FragmentListener {

    private static final String EXTRA_MEDIA = "butter.droid.ui.media.detail.MediaDetailActivity.media";

    private boolean isTablet = false;

    @Inject MediaDetailPresenter presenter;

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.collapsing_toolbar) CollapsingToolbarLayout collapsingToolbar;
    @BindView(R.id.fab) @Nullable FloatingActionButton floatingActionButton;
    @BindView(R.id.scrollview) NestedScrollView scrollView;
    @BindView(R.id.content) FrameLayout content;
    @BindView(R.id.bg_image) ImageView bgImage;

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, R.layout.activity_mediadetail);
        setSupportActionBar(toolbar);
        setShowCasting(true);

        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // parallaxLayout doesn't exist? Then this is a tablet or big screen device
        isTablet = floatingActionButton == null;

        MediaWrapper media = getIntent().getExtras().getParcelable(EXTRA_MEDIA);
        if (media == null) {
            finish();
            return;
        }

        presenter.onCreate(media);

    }

    @Override
    protected void onResume() {
        super.onResume();
        supportInvalidateOptionsMenu();

        TorrentService torrentService = getTorrentService();
        if (torrentService != null) {
            torrentService.stopStreaming();
        }

        BeamServer server = BeamServerService.getServer();
        if (server != null) {
            server.stop();
        }

        BeamPlayerNotificationService.cancelNotification();
    }

    @Optional @OnClick(R.id.fab) void play() {
        presenter.playMediaClicked();
    }

    @Override public void initMediaLayout(MediaWrapper mediaWrapper) {
        Media media = mediaWrapper.getMedia();
        getSupportActionBar().setTitle(media.getTitle());

        if (mediaWrapper.hasColor()) {
            int color = mediaWrapper.getColor();
            collapsingToolbar.setContentScrimColor(color);
            collapsingToolbar.setStatusBarScrimColor(color);

            if (!isTablet) {
                //noinspection ConstantConditions
                floatingActionButton.setBackgroundTintList(ColorStateList.valueOf(color));
            }
        }

        collapsingToolbar.setTitleEnabled(false);

        // Calculate toolbar scrolling variables
        int topHeight = PixelUtils.getScreenHeight(this) / 3 * 2;
        if (isTablet) {
            LayoutParams params =
                    (LayoutParams) scrollView.getLayoutParams();
            ScrollingViewBehavior behavior =
                    (ScrollingViewBehavior) params.getBehavior();
            behavior.setOverlayTop(topHeight);
        }

        loadBackgroundImage(media);

    }

    @Override public void displayStreamable(MediaWrapper movie) {
        displayFragment(StreamableDetailFragment.newInstance(movie));
    }

    @Override public void displayShow(MediaWrapper show) {
        displayFragment(ShowDetailFragment.newInstance(show));
    }

    @Override public void displaySeason(MediaWrapper show) {
        displayFragment(ShowDetailFragment.newInstance(show));
    }

    @Override public void displayDialog(@StringRes int title, @StringRes int message) {
        MessageDialogFragment.show(getSupportFragmentManager(), title, message);
    }

    @Override public void playStream(StreamInfo streamInfo) {
        if (VersionUtils.isLollipop()) {
            scrollView.smoothScrollTo(0, 0);
            StreamLoadingActivity.startActivity(this, streamInfo,
                    Pair.create(bgImage, ViewCompat.getTransitionName(bgImage)));
        } else {
            StreamLoadingActivity.startActivity(this, streamInfo);
        }
    }

    @Override public void openVideoPlayer(StreamInfo streamInfo) {
        startActivity(VideoPlayerActivity.getIntent(this, streamInfo));
    }

    @Override public void openYouTube(MediaWrapper media, String url) {
        startActivity(TrailerPlayerActivity.getIntent(this, media, url));
    }

    @Override public void displayHealthInfo(TorrentHealth health, int seeds, int peers) {
        final Snackbar snackbar = Snackbar.make(content,
                getString(R.string.health_info, getString(health.getStringResource()), seeds, peers),
                Snackbar.LENGTH_LONG);

        snackbar.setAction(R.string.close, view -> snackbar.dismiss());
        snackbar.show();

    }

    private void loadBackgroundImage(Media media) {
        String imageUrl = media.getPoster();
        if (isTablet || !PixelUtils.screenIsPortrait(this)) {
            imageUrl = media.getBackdrop();
        }

        GlideApp.with(this)
                .asDrawable()
                .load(imageUrl)
                .error(R.drawable.butter_logo)
                .into(bgImage);
    }

    private void displayFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.content, fragment).commit();
    }

    public static Intent getIntent(@NonNull Context context, @NonNull MediaWrapper media) {
        Intent intent = new Intent(context, MediaDetailActivity.class);
        intent.putExtra(EXTRA_MEDIA, media);
        return intent;
    }

}
