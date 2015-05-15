package pct.droid.tv.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v17.leanback.app.GuidedStepFragment;
import android.support.v17.leanback.widget.GuidanceStylist;
import android.support.v17.leanback.widget.GuidedAction;
import android.view.View;

import java.util.List;

import pct.droid.base.preferences.Prefs;
import pct.droid.base.utils.PrefUtils;
import pct.droid.tv.R;
import pct.droid.tv.activities.PTVMainActivity;

public class PTVWelcomeFragment extends GuidedStepFragment {

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
        GuidedAction acceptAction = new GuidedAction.Builder().id(R.id.action_accept).hasNext(true).title(getString(R.string.accept)).build();
        GuidedAction declineAction = new GuidedAction.Builder().id(R.id.action_decline).hasNext(true).title(getString(R.string.leave)).build();
        actions.add(acceptAction);
        actions.add(declineAction);
        super.onCreateActions(actions, savedInstanceState);
    }

    @Override
    public void onGuidedActionClicked(GuidedAction action) {
        switch ((int) action.getId()) {
            case R.id.action_accept:
                //set first run flag to false, don't show welcome again
                PrefUtils.save(getActivity(), Prefs.FIRST_RUN, false);
                //start main activity

                PTVMainActivity.startActivity(getActivity());
                getActivity().finish();
                return;
            case R.id.action_decline:
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
