package pct.droid.base.utils;

import android.os.Handler;
import android.os.Looper;

public class ThreadUtils {

	/**
	 * Execute the given {@link Runnable} on the ui thread.
	 *
	 * @param runnable The runnable to execute.
	 */
	public static void runOnUiThread(Runnable runnable) {
		Thread uiThread = Looper.getMainLooper().getThread();
		if (Thread.currentThread() != uiThread) new Handler(Looper.getMainLooper()).post(runnable);
		else runnable.run();
	}
}
