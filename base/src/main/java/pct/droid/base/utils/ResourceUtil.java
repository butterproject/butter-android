package pct.droid.base.utils;

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.util.TypedValue;

public class ResourceUtil {

	public static TypedValue resolveThemeAttribute(Context context, int attrId) {
		TypedValue value = new TypedValue();
		context.getTheme().resolveAttribute(attrId, value, true);
		return value;
	}

	public static float getAttributeDimension(final Context context, final Resources.Theme theme, final int resId) {
		final TypedValue typedValue = new TypedValue(); // create a new typed value to received the resolved attribute
		// value
		final DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
		if (!theme.resolveAttribute(resId, typedValue, true)) // if we can't resolve the value
		{
			throw new Resources.NotFoundException("Resource ID #0x" + Integer.toHexString(resId));
		}
		if (typedValue.type != TypedValue.TYPE_DIMENSION) // if the value isn't of the correct type
		{
			throw new Resources.NotFoundException("Resource ID #0x" + Integer.toHexString(resId) + " type #0x"
					+ Integer.toHexString(typedValue.type) + " is not valid");
		}
		return typedValue.getDimension(displayMetrics); // return the value of the attribute in terms of the display
	}
}
