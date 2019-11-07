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

package butter.droid.ui.terms;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import butter.droid.R;
import butter.droid.ui.ButterBaseActivity;
import butter.droid.utils.ToolbarUtils;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class TermsActivity extends ButterBaseActivity implements TermsView {

    @Inject TermsPresenter presenter;

    @BindView(R.id.toolbar) Toolbar toolbar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terms);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        ToolbarUtils.updateToolbarHeight(this, toolbar);
    }

    @OnClick(R.id.btn_accept) public void acceptClick() {
        presenter.accept();
    }

    @OnClick(R.id.btn_leave) public void leaveClick() {
        presenter.leave();
    }

    @Override public void closeSuccess() {
        setResult(RESULT_OK);
        closeSelf();
    }

    @Override public void closeSelf() {
        finish();
    }

    public static Intent getIntent(@NonNull Context context) {
        return new Intent(context, TermsActivity.class);
    }
}
