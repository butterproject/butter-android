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
import pct.droid.utils.ToolbarUtils;

public class TermsActivity extends BaseActivity {

    public static String TERMS_ACCEPTED = "terms_accepted";

    @InjectView(R.id.toolbar)
    Toolbar toolbar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, R.layout.activity_terms);
        setSupportActionBar(toolbar);

		ToolbarUtils.updateToolbarHeight(this,toolbar);
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
