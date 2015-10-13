package pct.droid.tv.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import pct.droid.base.updater.PopcornUpdater;
import pct.droid.tv.R;
import pct.droid.tv.activities.base.PTVBaseActivity;

public class PTVMainActivity extends PTVBaseActivity {

    public static Intent startActivity(Activity activity) {
        Intent intent = new Intent(activity, PTVMainActivity.class);
        activity.startActivity(intent);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, R.layout.activity_main);
    }

    @Override
    protected void onResume() {
        super.onResume();
        PopcornUpdater.getInstance(this, new PopcornUpdater.Listener() {
            @Override
            public void updateAvailable(String filePath) {
                PTVUpdateActivity.startActivity(PTVMainActivity.this);
            }
        }).checkUpdates(false);
    }
}
