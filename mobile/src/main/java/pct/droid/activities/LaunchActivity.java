package pct.droid.activities;

import android.content.Intent;
import android.os.Bundle;

import pct.droid.base.torrent.TorrentService;
import pct.droid.base.utils.PrefUtils;

public class LaunchActivity extends BaseActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TorrentService.start(this);

        if (PrefUtils.contains(this, TermsActivity.TERMS_ACCEPTED)) {
            startActivity(new Intent(this, OverviewActivity.class));
        } else {
            startActivity(new Intent(this, TermsActivity.class));
        }
        finish();
    }

}
