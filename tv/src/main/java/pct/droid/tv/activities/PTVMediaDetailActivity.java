package pct.droid.tv.activities;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.SharedElementCallback;
import android.content.Context;
import android.content.Intent;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.ActivityCompat;
import android.view.View;

import java.util.List;
import java.util.Map;

import pct.droid.base.providers.media.models.Media;
import pct.droid.base.providers.media.models.Movie;
import pct.droid.base.utils.VersionUtils;
import pct.droid.tv.R;
import pct.droid.tv.activities.base.PTVBaseActivity;
import pct.droid.tv.fragments.PTVBaseDetailsFragment;
import pct.droid.tv.fragments.PTVMovieDetailsFragment;
import pct.droid.tv.fragments.PTVShowDetailsFragment;
import pct.droid.tv.utils.BackgroundUpdater;

public class PTVMediaDetailActivity extends PTVBaseActivity implements PTVMovieDetailsFragment.Callback {

    public static final String EXTRA_ITEM = "item";
    public static final String EXTRA_BACKGROUND_URL = "background_url";
    public static final String EXTRA_HERO_URL = "hero_url";

    public static final String SHARED_ELEMENT_NAME = "hero";

    private Media mItem;

    private BackgroundUpdater mBackgroundUpdater = new BackgroundUpdater();


    public static Intent startActivity(Activity activity, Media item,String background,String hero) {
        return startActivity(activity, null, item,background,hero);
    }

    public static Intent startActivity(Activity activity, Bundle options, Media item,String background,String hero) {
        Intent intent = buildIntent(activity, item, background, hero);
        activity.startActivity(intent, options);
        return intent;
    }

    public static Intent buildIntent(Context context, Media item,String background,String hero){
        Intent intent = new Intent(context, PTVMediaDetailActivity.class);
        intent.putExtra(EXTRA_ITEM, item);
        intent.putExtra(EXTRA_BACKGROUND_URL, background);
        intent.putExtra(EXTRA_HERO_URL, hero);
        return intent;
    }

    /**
     * Called when the activity is first created.
     */

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, R.layout.activity_media_details);

        mBackgroundUpdater.initialise(this, R.color.black);
        mItem = getIntent().getParcelableExtra(EXTRA_ITEM);
        String backgroundImage = getIntent().getStringExtra(EXTRA_BACKGROUND_URL);
        String heroImage = getIntent().getStringExtra(EXTRA_HERO_URL);

        updateBackground(backgroundImage);

        if (VersionUtils.isLollipop()) {
            postponeEnterTransition();
        }
        if (mItem instanceof Movie) {
//            .addSharedElement(View sharedElement, String name)
            getFragmentManager().beginTransaction().replace(R.id.fragment, PTVMovieDetailsFragment.newInstance(mItem, heroImage)).commit();
        } else {
            getFragmentManager().beginTransaction().replace(R.id.fragment, PTVShowDetailsFragment.newInstance(mItem, heroImage)).commit();
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
