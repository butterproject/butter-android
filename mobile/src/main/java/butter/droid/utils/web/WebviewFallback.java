package butter.droid.utils.web;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import butter.droid.ui.base.WebViewActivity;

public class WebviewFallback implements CustomTabActivityHelper.CustomTabFallback {

    @Override
    public void openUri(Activity activity, Uri uri) {
        final Intent intent = new Intent(activity, WebViewActivity.class);
        intent.putExtra(WebViewActivity.EXTRA_URL, uri.toString());
        activity.startActivity(intent);
    }

}