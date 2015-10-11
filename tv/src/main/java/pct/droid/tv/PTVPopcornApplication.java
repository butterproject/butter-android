package pct.droid.tv;

import pct.droid.base.PopcornApplication;
import pct.droid.base.utils.VersionUtils;
import pct.droid.tv.activities.PTVUpdateActivity;

public class PTVPopcornApplication extends PopcornApplication {

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void updateAvailable(String filePath) {
        if(!VersionUtils.isAndroidTV()) {
            super.updateAvailable(filePath);
        }
    }
}
