package pct.droid.utils;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.support.v7.widget.Toolbar;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.readystatesoftware.systembartint.SystemBarTintManager;

import pct.droid.R;
import pct.droid.base.utils.PixelUtils;
import pct.droid.base.utils.ResourceUtil;

public class ToolbarUtils {


		/**
		 * This is a bit of hackery to get KitKat to play nice with the toolbar. we use a combination of the SystemBarTint manager, and adding a
		 * top margin to the toolbar.
		 *
		 * SystemBarTintManager adds and colors a view to sit under the status bar.
		 *
		 * This works fine when using an activity ActionBar, but when using a Toolbar, the toolbar ends up appearing under the status bar.
		 *
		 * This is because SystemBarTint is designed for KitKat and the pre-toolbar ActionBar, which is forced to fit system windows.
		 *
		 * We then add a top margin to the toolbar which equals the height of the status bar, so that the toolbar will sit below the status
		 * bar.
		 *
		 * @param activity
		 * @param toolbar
		 */
		public static void setKitKatTranslucentStatus(Activity activity, Toolbar toolbar) {
			if (Build.VERSION.SDK_INT != Build.VERSION_CODES.KITKAT) return; //only for kitkat
			// create our manager instance after the content view is set
			addSystemBarTintManager(activity);

			//manually add padding to toolbar
			//increase the top margin
			int topMargin = PixelUtils
					.getStatusBarHeight(activity);
			addTopToolbarMargin(toolbar, topMargin);
		}

		public static void addSystemBarTintManager(Activity activity) {
			SystemBarTintManager tintManager = new SystemBarTintManager(activity);
			// enable status bar tint
			tintManager.setStatusBarTintEnabled(true);
			int primaryColor = ResourceUtil.resolveThemeAttribute(activity, pct.droid.base.R.attr.colorPrimary).resourceId;
			tintManager.setStatusBarTintColor(activity.getResources().getColor(primaryColor));
		}

		public static void addTopToolbarMargin(Toolbar toolbar, int margin) {
			ViewGroup.LayoutParams lp = toolbar.getLayoutParams();
			//for framelayout
			if (lp instanceof FrameLayout.LayoutParams)
				((FrameLayout.LayoutParams) lp).topMargin = ((FrameLayout.LayoutParams) lp).topMargin + margin;
				//for relativelayout
			else if (lp instanceof RelativeLayout.LayoutParams)
				((RelativeLayout.LayoutParams) lp).topMargin = ((RelativeLayout.LayoutParams) lp).topMargin + margin;
				//for linearlayout
			else if (lp instanceof LinearLayout.LayoutParams)
				((LinearLayout.LayoutParams) lp).topMargin = ((LinearLayout.LayoutParams) lp).topMargin + margin;
		}

	/**
	 * This method serves 2 purposes:
	 *
	 * 1. Fix a bug when rotating an activity that contains a toolbar, with the minHeight set to actionBarHeight; When the toolbar is rotate
	 * the height is updated, but the contents are not re layed out to match the new height which means they are aligned incorrectly. By
	 * resetting the minHeight on rotation we fix this.
	 *
	 * 2. This method adds extra padding to the toolbar on KitKat devices. This fixes the issue where the toolbar appears under the status
	 * bar.
	 *
	 * Note: A toolbars height MUST be set to wrap_content for the KitKat status bar to inherit it's background colour. On KitKat, when the
	 * statusbar is set to translucent, the system takes care of the color. It is meant to extend the windows colour to the status bar. I
	 * think what happens here is it extends the height of the toolbar, including its background, but only if the toolbar doesn't have an
	 * explicit height. Probably needs more investigation.
	 *
	 * @param context
	 * @param toolbar
	 */
	public static void updateToolbarHeight(Context context, Toolbar toolbar) {
		int extraPadding = 0;
		if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) extraPadding = PixelUtils.getStatusBarHeight(context);
		toolbar.getLayoutParams().height = context.getResources().getDimensionPixelSize(
				R.dimen.abc_action_bar_default_height_material) + extraPadding;
	}
}
