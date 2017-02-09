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
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import javax.inject.Inject;

import butter.droid.MobileButterApplication;
import butter.droid.R;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AboutFragment extends Fragment implements AboutView {

    @Inject AboutPresenter presenter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MobileButterApplication.getAppContext()
                .getComponent()
                .aboutComponentBuilder()
                .aboutModule(new AboutModule(this))
                .build()
                .inject(this);
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
            R.id.blog_button,
            R.id.butter_button,
            R.id.discuss_button,
            R.id.twitter_button
    })
    void onLogoClick(View view) {
        presenter.aboutButtonClicked(view.getId());
    }

    @Override public void displayIntent(@NonNull Intent intent) {
        startActivity(intent);
    }

}
