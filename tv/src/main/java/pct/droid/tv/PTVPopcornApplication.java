package pct.droid.tv;

import pct.droid.base.PopcornApplication;
import pct.droid.base.utils.VersionUtils;

public class PTVPopcornApplication extends PopcornApplication {

    @Override
    public void updateAvailable() {
        if(!VersionUtils.isAndroidTV()) {
            super.updateAvailable();
            return;
        }

        // Update dialog
    }
}
