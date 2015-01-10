package pct.droid.utils;

import android.content.Context;
import android.support.v7.widget.Toolbar;

import pct.droid.R;

public class ToolbarUtils {
	/**
	 * This method fixes a bug when rotating an activity that contains a toolbar, with the minHeight set to actionBarHeight; When the
	 * toolbar is rotate the height is updated, but the contents are not re layed out to match the new height which means they are aligned
	 * incorrectly. By resetting the minHeight on rotation we fix this.
	 *
	 * @param context
	 * @param toolbar
	 */
	public static void updateToolbarHeight(Context context, Toolbar toolbar) {
		toolbar.getLayoutParams().height = context.getResources().getDimensionPixelSize(
				R.dimen.abc_action_bar_default_height_material);
	}
}
