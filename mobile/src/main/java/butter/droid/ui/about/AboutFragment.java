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

package butter.droid.ui.about;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import butter.droid.R;
import butter.droid.utils.ButterCustomTabActivityHelper;
import butterknife.ButterKnife;
import butterknife.OnClick;
import dagger.android.support.DaggerFragment;
import javax.inject.Inject;

public class AboutFragment extends DaggerFragment implements AboutView {

    @Inject AboutPresenter presenter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_about, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
    }


    @OnClick(value = {
            R.id.logo_imageview,
            R.id.facebook_button,
            R.id.git_button,
            R.id.butter_button,
            R.id.discuss_button,
            R.id.twitter_button})
    void onLogoClick(View view) {
        presenter.aboutButtonClicked(view.getId());
    }

    @Override public void displayIntent(@NonNull Intent intent) {
        final FragmentActivity activity = getActivity();
        final Uri url = intent.getData();
        ButterCustomTabActivityHelper.openCustomTab(activity, url);
    }

}
