package pct.droid.tv.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v17.leanback.app.GuidedStepFragment;
import android.support.v17.leanback.widget.GuidanceStylist;
import android.support.v17.leanback.widget.GuidedAction;

import java.util.List;

import pct.droid.base.preferences.Prefs;
import pct.droid.base.updater.PopcornUpdater;
import pct.droid.base.utils.PrefUtils;
import pct.droid.tv.R;
import pct.droid.tv.activities.PTVMainActivity;

public class PTVUpdateFragment extends GuidedStepFragment {

    @NonNull
    @Override
    public GuidanceStylist.Guidance onCreateGuidance(Bundle savedInstanceState) {
        return new GuidanceStylist.Guidance(getString(R.string.update_available), getString(R.string.new_update), getString(R.string.app_name), null);
    }

    @Override
    public GuidanceStylist onCreateGuidanceStylist() {
        return new TermsGuidanceStylist();
    }

    @Override
    public void onCreateActions(@NonNull List<GuidedAction> actions, Bundle savedInstanceState) {
        GuidedAction acceptAction = new GuidedAction.Builder().id(R.id.action_update_now).hasNext(true).title(getString(R.string.now)).build();
        GuidedAction declineAction = new GuidedAction.Builder().id(R.id.action_update_later).hasNext(true).title(getString(R.string.later)).build();
        actions.add(acceptAction);
        actions.add(declineAction);
        super.onCreateActions(actions, savedInstanceState);
    }

    @Override
    public void onGuidedActionClicked(GuidedAction action) {
        switch ((int) action.getId()) {
            case R.id.action_update_now:
                String updateFile = PrefUtils.get(getActivity(), PopcornUpdater.UPDATE_FILE, "");
                Intent updateIntent = new Intent(Intent.ACTION_VIEW);
                updateIntent.setDataAndType(Uri.parse("file://" + getActivity().getFilesDir().getAbsolutePath() + "/" + updateFile), PopcornUpdater.ANDROID_PACKAGE);

                getActivity().startActivity(updateIntent);
            case R.id.action_update_later:
                getActivity().finish();
                return;
        }
        super.onGuidedActionClicked(action);
    }

    public static class TermsGuidanceStylist extends GuidanceStylist {

        @Override
        public int onProvideLayoutId() {
            return R.layout.guidance_type1;
        }

    }


}
