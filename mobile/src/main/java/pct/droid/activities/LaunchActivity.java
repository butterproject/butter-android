package pct.droid.activities;

import android.content.Intent;
import android.os.Bundle;

/**
 * Created by Sebastiaan on 01-12-14.
 */
public class LaunchActivity extends BaseActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startActivity(new Intent(this, OverviewActivity.class));
    }
}
