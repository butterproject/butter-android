package butter.droid.fragments.base;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.view.View;

import butter.droid.ui.media.detail.MediaDetailActivity;
import butter.droid.base.torrent.StreamInfo;

public abstract class BaseDetailFragment extends Fragment {

    protected MediaDetailActivity mActivity;
    protected View mRoot;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof MediaDetailActivity)
            mActivity = (MediaDetailActivity) activity;
    }

    public interface FragmentListener {
        void playStream(StreamInfo streamInfo);
    }

}
