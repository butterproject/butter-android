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
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import pct.droid.base.providers.media.models.Media;
import pct.droid.base.providers.media.models.Movie;
import pct.droid.base.utils.VersionUtils;
import pct.droid.tv.R;
import pct.droid.tv.activities.base.PTVBaseActivity;
import pct.droid.tv.fragments.PTVMovieDetailsFragment;
import pct.droid.tv.fragments.PTVShowDetailsFragment;
import pct.droid.tv.utils.BackgroundUpdater;

public class PTVMediaDetailActivity extends PTVBaseActivity implements PTVMovieDetailsFragment.Callback {

    public static final String EXTRA_ITEM = "item";
    public static final String SHARED_ELEMENT_NAME = "hero";

    private BackgroundUpdater mBackgroundUpdater = new BackgroundUpdater();

    public static Intent startActivity(Activity activity, Media item) {
        return startActivity(activity, null, item);
    }

    public static Intent startActivity(Activity activity, Bundle options, Media item) {
        Intent intent = buildIntent(activity, item);
        activity.startActivity(intent, options);
        return intent;
    }

    public static Intent buildIntent(Context context, Media item){
        Intent intent = new Intent(context, PTVMediaDetailActivity.class);
        intent.putExtra(EXTRA_ITEM, item);
        return intent;
    }

    /**
     * Called when the activity is first created.
     */

    @SuppressLint("MissingSuperCall")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, R.layout.activity_media_details);

        mBackgroundUpdater.initialise(this, R.color.black);
        Media media = getIntent().getParcelableExtra(EXTRA_ITEM);

        updateBackground(media.headerImage);

        if (VersionUtils.isLollipop()) {
            postponeEnterTransition();
        }

        if (media instanceof Movie) {
            getFragmentManager().beginTransaction().replace(R.id.fragment, PTVMovieDetailsFragment.newInstance(media)).commit();
        } else {
            getFragmentManager().beginTransaction().replace(R.id.fragment, PTVShowDetailsFragment.newInstance(media)).commit();
        }
        getFragmentManager().executePendingTransactions();

        if (VersionUtils.isLollipop()) {
            startPostponedEnterTransition();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != mBackgroundUpdater) mBackgroundUpdater.destroy();
    }

    protected void updateBackground(String backgroundImage) {
        mBackgroundUpdater.updateBackground(backgroundImage);
    }
}
