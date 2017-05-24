package butter.droid.utils;

import android.app.Activity;
import android.net.Uri;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.content.ContextCompat;
import butter.droid.R;
import butter.droid.utils.web.CustomTabActivityHelper;
import butter.droid.utils.web.WebviewFallback;

public class ButterCustomTabActivityHelper {

    public static void openCustomTab(Activity activity, Uri url) {
        final CustomTabsIntent customTabsIntent = new CustomTabsIntent.Builder()
                .setCloseButtonIcon(Bitmaps.getBitmap(activity, R.drawable.ic_arrow_back))
                .setToolbarColor(ContextCompat.getColor(activity, R.color.colorPrimary))
                .build();
        CustomTabActivityHelper.openCustomTab(activity, customTabsIntent, url, new WebviewFallback());
    }

}
