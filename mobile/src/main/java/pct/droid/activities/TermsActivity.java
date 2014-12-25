package pct.droid.activities;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.RelativeLayout;

import butterknife.InjectView;
import pct.droid.R;
import pct.droid.base.utils.PixelUtils;
import pct.droid.base.utils.PrefUtils;

public class TermsActivity extends BaseActivity {

    public static String TERMS_ACCEPTED = "terms_accepted";

    @InjectView(R.id.toolbar)
    Toolbar toolbar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, R.layout.activity_terms);
        setSupportActionBar(toolbar);

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2) {
            toolbar.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, getResources().getDimensionPixelSize(R.dimen.abc_action_bar_default_height_material) + PixelUtils.getStatusBarHeight(this)));
        } else {
            toolbar.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, getResources().getDimensionPixelSize(R.dimen.abc_action_bar_default_height_material)));
        }
    }

    public void acceptClick(View v) {
        PrefUtils.save(this, TERMS_ACCEPTED, true);
        Intent overviewIntent = new Intent(this, OverviewActivity.class);
        startActivity(overviewIntent);
        finish();
    }

    public void leaveClick(View v) {
        finish();
    }

}
