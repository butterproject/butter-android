/*
 * This file is part of Butter.
 *
 * Butter is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Butter is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Butter. If not, see <http://www.gnu.org/licenses/>.
 */

package butter.droid.tv.ui.terms;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import java.util.List;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.leanback.app.GuidedStepSupportFragment;
import androidx.leanback.widget.GuidanceStylist;
import androidx.leanback.widget.GuidedAction;
import butter.droid.base.manager.prefs.PrefManager;
import butter.droid.tv.R;
import dagger.android.support.AndroidSupportInjection;

public class TVTermsFragment extends GuidedStepSupportFragment implements TVTermsView {

    @Inject TVTermsPresenter presenter;
    @Inject PrefManager prefManager;

    @Override public void onAttach(final Context context) {
        AndroidSupportInjection.inject(this);
        super.onAttach(context);
    }

    @NonNull
    @Override
    public GuidanceStylist.Guidance onCreateGuidance(Bundle savedInstanceState) {
        return new GuidanceStylist.Guidance(getString(R.string.terms_and_conditions), getString(R.string.terms),
                getString(R.string.app_name), null);
    }

    @Override
    public GuidanceStylist onCreateGuidanceStylist() {
        return new TermsGuidanceStylist();
    }

    @Override
    public void onCreateActions(@NonNull List<GuidedAction> actions, Bundle savedInstanceState) {
        actions.add(new GuidedAction.Builder(getActivity())
                .id(R.id.action_accept)
                .hasNext(true)
                .title(getString(R.string.accept))
                .build());
        actions.add(new GuidedAction.Builder(getActivity())
                .id(R.id.action_decline)
                .hasNext(false)
                .title(getString(R.string.leave))
                .build());
    }

    @Override
    public void onGuidedActionClicked(GuidedAction action) {
        switch ((int) action.getId()) {
            case R.id.action_accept:
                presenter.accept();
                break;
            case R.id.action_decline:
                presenter.leave();
                break;
            default:
                super.onGuidedActionClicked(action);
        }
    }

    @Override public void closeSuccess() {
        getActivity().setResult(Activity.RESULT_OK);
        closeSelf();
    }

    @Override public void closeSelf() {
        getActivity().finish();
    }

    public static class TermsGuidanceStylist extends GuidanceStylist {

        @Override
        public int onProvideLayoutId() {
            return R.layout.guidance_type1;
        }
    }


}
