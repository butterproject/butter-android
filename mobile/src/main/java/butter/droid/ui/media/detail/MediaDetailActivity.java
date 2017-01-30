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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.util.Pair;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import javax.inject.Inject;

import butter.droid.MobileButterApplication;
import butter.droid.R;
import butter.droid.activities.StreamLoadingActivity;
import butter.droid.activities.TrailerPlayerActivity;
import butter.droid.activities.VideoPlayerActivity;
import butter.droid.activities.base.ButterBaseActivity;
import butter.droid.base.manager.beaming.BeamPlayerNotificationService;
import butter.droid.base.manager.beaming.server.BeamServerService;
import butter.droid.base.providers.media.models.Media;
import butter.droid.base.providers.media.models.Movie;
import butter.droid.base.providers.media.models.Show;
import butter.droid.base.torrent.StreamInfo;
import butter.droid.base.utils.PixelUtils;
import butter.droid.base.utils.VersionUtils;
import butter.droid.fragments.ShowDetailFragment;
import butter.droid.fragments.base.BaseDetailFragment;
import butter.droid.fragments.dialog.MessageDialogFragment;
import butter.droid.ui.media.detail.movie.MovieDetailFragment;
import butter.droid.widget.ObservableParallaxScrollView;
import butterknife.BindView;
import butterknife.OnClick;
import butterknife.Optional;

public class MediaDetailActivity extends ButterBaseActivity implements MediaDetailView,
        BaseDetailFragment.FragmentListener {

    private static final String EXTRA_MEDIA = "butter.droid.ui.media.detail.MediaDetailActivity.media";

    private boolean isTablet = false;

    @Inject MediaDetailPresenter presenter;

    private MediaDetailComponent component;

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.collapsing_toolbar) CollapsingToolbarLayout collapsingToolbar;
    @BindView(R.id.fab) @Nullable FloatingActionButton floatingActionButton;
    @BindView(R.id.scrollview) NestedScrollView scrollView;
    @BindView(R.id.content) FrameLayout content;
    @BindView(R.id.bg_image) ImageView bgImage;

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onCreate(Bundle savedInstanceState) {

        component = MobileButterApplication.getAppContext()
                .getComponent()
                .mediaDetailsComponentBuilder()
                .mediaDetailModule(new MediaDetailModule(this))
                .build();
        component.inject(this);

        super.onCreate(savedInstanceState, R.layout.activity_mediadetail);
        setSupportActionBar(toolbar);
        setShowCasting(true);

        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // parallaxLayout doesn't exist? Then this is a tablet or big screen device
        isTablet = floatingActionButton == null;

        Media media = getIntent().getExtras().getParcelable(EXTRA_MEDIA);
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

        if (null != torrentStream) {
            torrentStream.stopStreaming();
        }
        BeamServerService.getServer().stop();
        BeamPlayerNotificationService.cancelNotification();
    }

    @Optional @OnClick(R.id.fab) void play() {
        presenter.playMediaClicked();
    }

    public void setSubScrollListener(ObservableParallaxScrollView.Listener subScrollListener) {
        mSubOnScrollListener = subScrollListener;
    }

    /* The scroll listener makes the toolbar scroll off the screen when the user scroll all the way down. And it appears again on scrolling up. */
    private ObservableParallaxScrollView.Listener mSubOnScrollListener = null;

    @Override public void initMediaLayout(Media media) {
        getSupportActionBar().setTitle(media.title);

        collapsingToolbar.setContentScrimColor(media.color);
        collapsingToolbar.setStatusBarScrimColor(media.color);
        collapsingToolbar.setTitleEnabled(false);

        // Calculate toolbar scrolling variables
        int topHeight = PixelUtils.getScreenHeight(this) / 3 * 2;
        if (!isTablet) {
            //noinspection ConstantConditions
            floatingActionButton.setBackgroundTintList(ColorStateList.valueOf(media.color));
            bgImage.getLayoutParams().height = topHeight;
        } else {
            CoordinatorLayout.LayoutParams params =
                    (CoordinatorLayout.LayoutParams) scrollView.getLayoutParams();
            AppBarLayout.ScrollingViewBehavior behavior =
                    (AppBarLayout.ScrollingViewBehavior) params.getBehavior();
            behavior.setOverlayTop(topHeight);
        }

        loadBackgroundImage(media);
    }

    @Override public void displayMovie(Movie movie) {
        displayFragment(MovieDetailFragment.newInstance(movie));
    }

    @Override public void displayShow(Show show) {
        displayFragment(ShowDetailFragment.newInstance(show));
    }

    @Override public void displayDialog(@StringRes int title, @StringRes int message) {
        MessageDialogFragment.show(getFragmentManager(), title, message);
    }

    @Override public void playStream(StreamInfo streamInfo) {
        if (torrentStream != null) {
            torrentStream.startForeground();
        }

        if (VersionUtils.isLollipop()) {
            scrollView.smoothScrollTo(0, 0);
            StreamLoadingActivity.startActivity(this, streamInfo,
                    Pair.<View, String>create(bgImage, ViewCompat.getTransitionName(bgImage)));
        } else {
            StreamLoadingActivity.startActivity(this, streamInfo);
        }
    }

    @Override public void openVideoPlayer(StreamInfo streamInfo) {
        startActivity(VideoPlayerActivity.getIntent(this, streamInfo));
    }

    @Override public void openYouTube(Media media, String url) {
        startActivity(TrailerPlayerActivity.getIntent(this, media, url));
    }

    private void loadBackgroundImage(Media media) {
        String imageUrl = media.image;
        if (isTablet || !PixelUtils.screenIsPortrait(this)) {
            imageUrl = media.headerImage;
        }

        Picasso.with(this)
                .load(imageUrl)
                .error(R.drawable.butter_logo)
                .placeholder(R.drawable.butter_logo)
                .into(bgImage);
    }

    private void displayFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.content, fragment).commit();
    }

    public static Intent getIntent(@NonNull Context context, @NonNull Media media) {
        Intent intent = new Intent(context, MediaDetailActivity.class);
        intent.putExtra(EXTRA_MEDIA, media);
        return intent;
    }

    public MediaDetailComponent getComponent() {
        return component;
    }
}
