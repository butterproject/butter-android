package pct.droid.tv.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v17.leanback.app.GuidedStepFragment;

import pct.droid.tv.R;
import pct.droid.tv.activities.base.PTVBaseActivity;
import pct.droid.tv.fragments.PTVSettingsFragment;

public class PTVSettingsActivity extends PTVBaseActivity {

    public static Intent startActivity(Activity activity) {
        Intent intent = new Intent(activity, PTVSettingsActivity.class);
        activity.startActivity(intent);
        return intent;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (null == savedInstanceState) {
            GuidedStepFragment.add(getFragmentManager(), new PTVSettingsFragment());
        }
    }

    @Override
    public void onBackPressed() {
        if(getFragmentManager().getBackStackEntryCount() > 0) {
            getFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }
}
