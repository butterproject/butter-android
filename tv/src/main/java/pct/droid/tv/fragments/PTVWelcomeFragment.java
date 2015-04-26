package pct.droid.tv.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v17.leanback.app.GuidedStepFragment;
import android.support.v17.leanback.widget.GuidanceStylist;
import android.support.v17.leanback.widget.GuidedAction;

import java.util.List;

import pct.droid.base.preferences.Prefs;
import pct.droid.base.utils.PrefUtils;
import pct.droid.tv.R;
import pct.droid.tv.activities.PTVMainActivity;

public class PTVWelcomeFragment extends GuidedStepFragment {
    public static final int ACTION_ACCEPT = 0;
    public static final int ACTION_DECLINE = 1;

    @NonNull
    @Override
    public GuidanceStylist.Guidance onCreateGuidance(Bundle savedInstanceState) {
        return new GuidanceStylist.Guidance(getString(R.string.terms_and_conditions), getString(R.string.terms), getString(R.string.app_name), null);
    }

    @Override
    public GuidanceStylist onCreateGuidanceStylist() {
        return new TermsGuidanceStylist();
    }

    @Override
    public void onCreateActions(@NonNull List<GuidedAction> actions, Bundle savedInstanceState) {
        GuidedAction acceptAction = new GuidedAction.Builder().id(ACTION_ACCEPT).hasNext(true).title(getString(R.string.accept)).build();
        GuidedAction declineAction = new GuidedAction.Builder().id(ACTION_DECLINE).hasNext(true).title(getString(R.string.leave)).build();
        actions.add(acceptAction);
        actions.add(declineAction);
        super.onCreateActions(actions, savedInstanceState);
    }

    @Override
    public void onGuidedActionClicked(GuidedAction action) {
        if (action.getId() == ACTION_ACCEPT) {
            //set first run flag to false, don't show welcome again
            PrefUtils.save(getActivity(), Prefs.FIRST_RUN, false);
            //start main activity

            PTVMainActivity.startActivity(getActivity());
            getActivity().finish();
            return;
        } else if (action.getId() == ACTION_DECLINE) {
            getActivity().finish();
            return;
        }
        super.onGuidedActionClicked(action);
    }

    public static class TermsGuidanceStylist extends GuidanceStylist {

        @Override
        public int onProvideLayoutId() {
            return R.layout.terms_guidance;
        }
    }


}
