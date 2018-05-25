package butter.droid.base.utils;

import androidx.fragment.app.Fragment;

public class FragmentUtil {

    public static boolean isAdded(Fragment fragment) {
        return fragment.isAdded() && !fragment.isDetached() && null != fragment.getActivity() && !fragment.getActivity().isFinishing();
    }
}
