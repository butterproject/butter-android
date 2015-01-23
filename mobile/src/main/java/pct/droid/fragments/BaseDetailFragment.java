package pct.droid.fragments;

import android.support.v4.app.Fragment;

import pct.droid.base.fragments.BaseStreamLoadingFragment;

public class BaseDetailFragment extends Fragment {


    public interface FragmentListener {
        public void playStream(BaseStreamLoadingFragment.StreamInfo streamInfo);
    }

}
