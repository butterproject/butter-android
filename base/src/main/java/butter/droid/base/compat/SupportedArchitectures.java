package butter.droid.base.compat;

import android.annotation.TargetApi;
import android.os.Build;

import java.util.Locale;

public class SupportedArchitectures extends Compatibility {

    /**
     * The most preferred ABI is the first element in the list.
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @SuppressWarnings("deprecation")
    public static String getAbi() {
        if (hasApi(Build.VERSION_CODES.LOLLIPOP)) {
            return Build.SUPPORTED_ABIS[0].toLowerCase(Locale.US);
        }
        return Build.CPU_ABI.toLowerCase(Locale.US);
    }

}
