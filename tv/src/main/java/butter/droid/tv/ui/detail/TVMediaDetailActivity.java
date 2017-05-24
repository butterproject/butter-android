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

package butter.droid.tv.ui.detail;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import butter.droid.base.providers.media.models.Media;
import butter.droid.base.utils.VersionUtils;
import butter.droid.tv.R;
import butter.droid.tv.TVButterApplication;
import butter.droid.tv.activities.base.TVBaseActivity;
import butter.droid.tv.manager.internal.background.BackgroundUpdater;
import butter.droid.tv.manager.internal.background.BackgroundUpdaterModule;
import javax.inject.Inject;

public class TVMediaDetailActivity extends TVBaseActivity implements TVMediaDetailView {

    public static final String SHARED_ELEMENT_NAME = "hero";

    private static final String EXTRA_ITEM = "butter.droid.tv.ui.detail.TVMediaDetailActivity.item";

    @Inject TVMediaDetailPresenter presenter;
    @Inject BackgroundUpdater backgroundUpdater;

    private TVMediaDetailComponent component;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        component = TVButterApplication.getAppContext()
                .getComponent()
                .tvMediaDetailComponentBuilder()
                .mediDetailModule(new TvMediDetailModule(this))
                .backgroundUpdaterModule(new BackgroundUpdaterModule(this))
                .build();
        component.inject(this);

        super.onCreate(savedInstanceState, R.layout.activity_media_details);

        backgroundUpdater.initialise(this, R.color.black);
        Media media = getIntent().getParcelableExtra(EXTRA_ITEM);

        if (VersionUtils.isLollipop()) {
            postponeEnterTransition();
        }

        presenter.onCreate(media);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (backgroundUpdater != null) {
            backgroundUpdater.destroy();
        }
    }

    @Override public void displayFragment(final Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment, fragment)
                .commit();
        getFragmentManager().executePendingTransactions();

        if (VersionUtils.isLollipop()) {
            startPostponedEnterTransition();
        }
    }

    @Override public void updateBackground(String url) {
        backgroundUpdater.updateBackground(url);
    }

    public TVMediaDetailComponent getComponent() {
        return component;
    }

    public static Intent getIntent(Context context, Media item){
        Intent intent = new Intent(context, TVMediaDetailActivity.class);
        intent.putExtra(EXTRA_ITEM, item);
        return intent;
    }

}
