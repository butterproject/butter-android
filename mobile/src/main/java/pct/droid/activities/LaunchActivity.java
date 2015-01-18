package pct.droid.activities;

import android.content.Intent;
import android.os.Bundle;

import com.crashlytics.android.Crashlytics;
import io.fabric.sdk.android.Fabric;

import pct.droid.base.torrent.TorrentService;
import pct.droid.base.utils.PrefUtils;

public class LaunchActivity extends BaseActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        TorrentService.start(this);
        if (PrefUtils.contains(this, TermsActivity.TERMS_ACCEPTED)) {
            startActivity(new Intent(this, OverviewActivity.class));
        } else {
            startActivity(new Intent(this, TermsActivity.class));
        }
        finish();
    }

}
