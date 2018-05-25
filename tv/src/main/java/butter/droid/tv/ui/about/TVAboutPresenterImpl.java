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

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.leanback.widget.GuidedAction;

import java.util.List;

import butter.droid.base.ui.about.BaseAboutPresenterImpl;
import butter.droid.tv.R;

public class TVAboutPresenterImpl extends BaseAboutPresenterImpl implements TVAboutPresenter {

    public TVAboutPresenterImpl(TVAboutView view) {
        super(view);
    }

    @Override public void aboutButtonClicked(@AboutLinks int id) {
        displayPage(id);
    }

    @Override public void createActions(@NonNull Context context, @NonNull List<GuidedAction> actions) {

        actions.add(new GuidedAction.Builder(context)
                .id(ABOUT_BUTTER)
                .title(R.string.website)
                .icon(R.drawable.icon_butter)
                .build());

        actions.add(new GuidedAction.Builder(context)
                .id(ABOUT_DISCUSS)
                .title(R.string.forum)
                .icon(R.drawable.icon_discourse)
                .build());

        actions.add(new GuidedAction.Builder(context)
                .id(ABOUT_TWITTER)
                .title(R.string.twitter)
                .icon(R.drawable.icon_twitter)
                .build());

        actions.add(new GuidedAction.Builder(context)
                .id(ABOUT_FB)
                .title(R.string.facebook)
                .icon(R.drawable.icon_facebook)
                .build());

        actions.add(new GuidedAction.Builder(context)
                .id(ABOUT_GIT)
                .title(R.string.git)
                .icon(R.drawable.icon_git)
                .build());

    }
}
