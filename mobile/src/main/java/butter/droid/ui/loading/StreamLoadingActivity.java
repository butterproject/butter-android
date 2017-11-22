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

package butter.droid.ui.loading;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.view.View;
import android.view.WindowManager;
import butter.droid.MobileButterApplication;
import butter.droid.R;
import butter.droid.base.providers.media.model.StreamInfo;
import butter.droid.base.torrent.TorrentService;
import butter.droid.ui.ButterBaseActivity;
import butter.droid.ui.loading.fragment.StreamLoadingFragment;
import javax.inject.Inject;

public class StreamLoadingActivity extends ButterBaseActivity implements StreamLoadingView {

    private static final String EXTRA_INFO = "butter.droid.ui.loading.StreamLoadingActivity.info";

    @Inject StreamLoadingPresenter presenter;

    private StreamLoadingComponent component;
    @Nullable private StreamLoadingFragment fragment;

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        getWindow().setBackgroundDrawableResource(R.color.bg);

        component = MobileButterApplication.getAppContext()
                .getComponent()
                .streamLoadingComponentBuilder()
                .streamLoadingModule(new StreamLoadingModule(this))
                .build();
        component.inject(this);

        super.onCreate(savedInstanceState, 0);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        if (!getIntent().hasExtra(EXTRA_INFO)) {
            finish();
        }

        StreamInfo info = getIntent().getParcelableExtra(EXTRA_INFO);

        presenter.onCreate(info, savedInstanceState != null);
    }

    @Override
    public void onTorrentServiceConnected(final TorrentService service) {
        super.onTorrentServiceConnected(service);
        if (null != fragment) {
            fragment.onTorrentServiceConnected(service);
        }
    }

    @Override
    public void onTorrentServiceDisconnected(final TorrentService service) {
        super.onTorrentServiceDisconnected(service);
        if (null != fragment) {
            fragment.onTorrentServiceDisconnected();
        }
    }

    @Override
    public void onBackPressed() {
        if (fragment != null) {
            fragment.cancelStream();
        }
        super.onBackPressed();
    }

    @Override public void displayStreamLoadingFragment(@NonNull StreamInfo info) {
        StreamLoadingFragment fragment = StreamLoadingFragment.newInstance(info);
        getSupportFragmentManager()
                .beginTransaction()
                .add(android.R.id.content, fragment)
                .commit();

        this.fragment = fragment;
    }

    public StreamLoadingComponent getComponent() {
        return component;
    }

    public static Intent startActivity(Activity activity, StreamInfo info) {
        Intent intent = new Intent(activity, StreamLoadingActivity.class);
        intent.putExtra(EXTRA_INFO, info);
        activity.startActivity(intent);
        return intent;
    }

    public static Intent startActivity(Activity activity, StreamInfo info, Pair<View, String>... elements) {
        Intent intent = new Intent(activity, StreamLoadingActivity.class);
        intent.putExtra(EXTRA_INFO, info);

        ActivityOptionsCompat options =
                ActivityOptionsCompat.makeSceneTransitionAnimation(activity, elements);
        ActivityCompat.startActivity(activity, intent, options.toBundle());
        return intent;
    }
}
