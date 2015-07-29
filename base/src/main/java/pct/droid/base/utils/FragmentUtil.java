package pct.droid.base.utils;

import android.support.v4.app.Fragment;

public class FragmentUtil {

    public static boolean isAdded(Fragment fragment) {
        return fragment.isAdded() && null != fragment.getActivity() && !fragment.getActivity().isFinishing();
    }
}
