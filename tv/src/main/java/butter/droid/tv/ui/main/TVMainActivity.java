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

package butter.droid.tv.ui.main;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import butter.droid.tv.R;
import butter.droid.tv.TVButterApplication;
import butter.droid.tv.ui.update.TVUpdateActivity;
import butter.droid.tv.activities.base.TVBaseActivity;
import javax.inject.Inject;

public class TVMainActivity extends TVBaseActivity implements TVMainView {

    @Inject TVMainPresenter presenter;

    private TVMainComponent component;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        component = TVButterApplication.getAppContext()
                .getComponent()
                .tvMainComponentBuilder()
                .mainModule(new TVMainModule(this))
                .build();
        component.inject(this);

        super.onCreate(savedInstanceState, R.layout.activity_main);
    }

    @Override
    protected void onResume() {
        super.onResume();

        presenter.onResume();
    }

    @Override public void showUpdateActivity() {
        startActivity(TVUpdateActivity.newIntent(this));
    }

    public TVMainComponent getComponent() {
        return component;
    }

    public static Intent startActivity(Activity activity) {
        Intent intent = new Intent(activity, TVMainActivity.class);
        activity.startActivity(intent);
        return intent;
    }
}
