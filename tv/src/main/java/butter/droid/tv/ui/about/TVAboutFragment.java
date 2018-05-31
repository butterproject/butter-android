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

package butter.droid.tv.ui.about;

import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.leanback.app.GuidedStepFragment;
import androidx.leanback.widget.GuidanceStylist;
import androidx.leanback.widget.GuidanceStylist.Guidance;
import androidx.leanback.widget.GuidedAction;
import androidx.core.content.ContextCompat;
import butter.droid.tv.R;
import butter.droid.tv.ui.preferences.fragment.TVWebViewFragment;
import dagger.android.AndroidInjection;
import java.util.List;
import javax.inject.Inject;

public class TVAboutFragment extends GuidedStepFragment implements TVAboutView {

    @Inject TVAboutPresenter presenter;

    @Override public void onAttach(final Context context) {
        AndroidInjection.inject(this);
        super.onAttach(context);
    }

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @NonNull @Override public Guidance onCreateGuidance(Bundle savedInstanceState) {
        return new GuidanceStylist.Guidance(getString(R.string.about_app), getString(R.string.about_message),
                getString(R.string.preferences), ContextCompat.getDrawable(getActivity(), R.drawable.butter_logo));
    }

    @Override
    public void onCreateActions(@NonNull List<GuidedAction> actions, Bundle savedInstanceState) {
        presenter.createActions(getActivity(), actions);
    }

    @Override public void onGuidedActionClicked(GuidedAction action) {
        //noinspection WrongConstant
        presenter.aboutButtonClicked((int) action.getId());
    }

    @Override public void displayIntent(@NonNull Intent intent) {
        final Uri uri = intent.getData();
        final TVWebViewFragment fragment = TVWebViewFragment.newInstance(uri);
        final FragmentManager fm = getFragmentManager();
        fm.beginTransaction().replace(android.R.id.content, fragment, TVWebViewFragment.TAG).addToBackStack(TVWebViewFragment.TAG).commit();
    }
}
