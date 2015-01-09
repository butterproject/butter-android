package pct.droid.base.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;

public class IntentUtils {

	/**
	 *
	 * Creates an intent for viewing a url in a browser
	 *
	 * @param context
	 * @return
	 */
	public static Intent getBrowserIntent(Context context, String url) {
		if (TextUtils.isEmpty(url)) throw new IllegalArgumentException("url cannot be empty or null");
		return new Intent(Intent.ACTION_VIEW).setData(Uri.parse(url));
	}
}
