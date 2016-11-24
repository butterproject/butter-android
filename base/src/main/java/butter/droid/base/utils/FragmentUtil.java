package butter.droid.base.utils;

import android.support.v4.app.Fragment;

public class FragmentUtil {

    public static boolean isNotAdded(Fragment fragment) {
        return !fragment.isAdded() || fragment.isDetached() || null == fragment.getActivity() || fragment.getActivity().isFinishing();
    }
}
