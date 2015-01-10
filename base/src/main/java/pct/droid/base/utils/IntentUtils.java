package pct.droid.base.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;

public class IntentUtils {

	/**
	 * Creates an intent for viewing a url in a browser
	 *
	 * @param context
	 *
	 * @return
	 */
	public static Intent getBrowserIntent(Context context, String url) {
		if (TextUtils.isEmpty(url)) throw new IllegalArgumentException("url cannot be empty or null");
		return new Intent(Intent.ACTION_VIEW).setData(Uri.parse(url));
	}


	public static Intent getSendIntent(Context context, String title, String text) {
		if (TextUtils.isEmpty(text)) throw new IllegalArgumentException("text cannot be empty or null");
		if (TextUtils.isEmpty(title)) throw new IllegalArgumentException("title cannot be empty or null");

		Intent sendIntent = new Intent();
		sendIntent.setAction(Intent.ACTION_SEND);
		sendIntent.putExtra(Intent.EXTRA_TEXT, text);
		sendIntent.setType("text/plain");
		return Intent.createChooser(sendIntent, title);
	}
}
