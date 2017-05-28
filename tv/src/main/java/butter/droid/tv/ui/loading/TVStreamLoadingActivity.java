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

package butter.droid.tv.ui.loading;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.view.View;

import butter.droid.base.torrent.TorrentService;
import javax.inject.Inject;

import butter.droid.base.providers.media.models.Show;
import butter.droid.base.torrent.StreamInfo;
import butter.droid.base.ui.loading.fragment.BaseStreamLoadingFragment;
import butter.droid.tv.TVButterApplication;
import butter.droid.tv.activities.base.TVBaseActivity;
import butter.droid.tv.ui.loading.fragment.TVStreamLoadingFragment;

public class TVStreamLoadingActivity extends TVBaseActivity implements TVStreamLoadingView {

    private static final String EXTRA_STREAM_INFO = "butter.droid.ui.loading.StreamLoadingActivity.info";
    public static final String EXTRA_SHOW_INFO = "butter.droid.ui.loading.StreamLoadingActivity.show_info";

    @Inject TVStreamLoadingPresenter presenter;

    private TVStreamLoadingComponent component;

    @Nullable private BaseStreamLoadingFragment fragment;

    @SuppressLint("MissingSuperCall")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        component = TVButterApplication.getAppContext()
                .getComponent()
                .streamLoadingComponentBuilder()
                .streamLoadingModule(new TVStreamLoadingModule(this))
                .build();
        component.inject(this);

        super.onCreate(savedInstanceState, 0);

        StreamInfo streamInfo = getIntent().getParcelableExtra(EXTRA_STREAM_INFO);
        Show show = getIntent().getParcelableExtra(EXTRA_SHOW_INFO);
        presenter.onCreate(streamInfo, show, savedInstanceState != null);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        fragment.cancelStream();
    }

    @Override
    public void onTorrentServiceDisconnected(final TorrentService service) {
        if (null != fragment) {
            fragment.onTorrentServiceDisconnected();
        }
    }

    @Override
    public void onTorrentServiceConnected(final TorrentService service) {
        if (null != fragment) {
            fragment.onTorrentServiceConnected(getTorrentService());
        }
    }

    @Override public void displayStreamLoadingFragment(@NonNull StreamInfo info, Show show) {
        TVStreamLoadingFragment fragment = TVStreamLoadingFragment.newInstance(info, show);
        getSupportFragmentManager()
                .beginTransaction()
                .add(android.R.id.content, fragment)
                .commit();

        this.fragment = fragment;
    }

    public TVStreamLoadingComponent getComponent() {
        return component;
    }

    public static Intent startActivity(Activity activity, StreamInfo info) {
        Intent intent = new Intent(activity, TVStreamLoadingActivity.class);
        intent.putExtra(EXTRA_STREAM_INFO, info);
        activity.startActivity(intent);
        return intent;
    }

    public static Intent startActivity(Activity activity, StreamInfo info, Show show) {
        Intent intent = new Intent(activity, TVStreamLoadingActivity.class);
        intent.putExtra(EXTRA_STREAM_INFO, info);
        intent.putExtra(EXTRA_SHOW_INFO, show);
        activity.startActivity(intent);
        return intent;
    }

    public static Intent startActivity(Activity activity, StreamInfo info, Pair<View, String>... elements) {
        Intent intent = new Intent(activity, TVStreamLoadingActivity.class);
        intent.putExtra(EXTRA_STREAM_INFO, info);

        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                activity, elements);
        ActivityCompat.startActivity(activity, intent, options.toBundle());
        return intent;
    }

}
