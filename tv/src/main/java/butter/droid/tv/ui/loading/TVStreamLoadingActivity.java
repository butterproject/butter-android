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
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.util.Pair;
import android.view.View;
import butter.droid.base.providers.media.model.StreamInfo;
import butter.droid.base.torrent.TorrentService;
import butter.droid.base.ui.loading.fragment.BaseStreamLoadingFragment;
import butter.droid.tv.ui.TVBaseActivity;
import butter.droid.tv.ui.loading.fragment.TVStreamLoadingFragment;
import javax.inject.Inject;

public class TVStreamLoadingActivity extends TVBaseActivity implements TVStreamLoadingView {

    private static final String EXTRA_STREAM_INFO = "butter.droid.ui.loading.StreamLoadingActivity.info";

    @Inject TVStreamLoadingPresenter presenter;

    @Nullable private BaseStreamLoadingFragment fragment;

    @SuppressLint("MissingSuperCall")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, 0);

        StreamInfo streamInfo = getIntent().getParcelableExtra(EXTRA_STREAM_INFO);
        presenter.onCreate(streamInfo, savedInstanceState != null);
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

    @Override public void displayStreamLoadingFragment(@NonNull StreamInfo info) {
        TVStreamLoadingFragment fragment = TVStreamLoadingFragment.newInstance(info);
        getSupportFragmentManager()
                .beginTransaction()
                .add(android.R.id.content, fragment)
                .commit();

        this.fragment = fragment;
    }

    public static Intent startActivity(Activity activity, StreamInfo info) {
        Intent intent = new Intent(activity, TVStreamLoadingActivity.class);
        intent.putExtra(EXTRA_STREAM_INFO, info);
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
